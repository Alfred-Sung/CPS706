import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

/**
 * Handles UDPConnection and multithreading
 * Both Client and Server uses this class as the procedure is the same only with different responses
 *
 * Important to note that each thread has a callback; custom code that will be executed after the thread is done running
 * Since it's hard to get threads to return anything and when they'll finish, I opted for the callback method
 * This also allows us to "chain" together threads quite easily
 */

// TODO: Implement packet timeout/resending
interface UDPCallback { void invoke(InetAddress address, Protocol protocol, String data); }
public abstract class UDPConnection extends Thread {
    public static UDPConnection instance;
    public static final Object UDPMonitor = new Object();
    public DatagramSocket socket;

    // Each UDP request is identified by it's sender IP and holds a queue of threads waiting to be executed
    // Each IP's thread can only be executed one at a time before it is dequeued
    private static HashMap<InetAddress, Queue<UDPThread>> threads = new HashMap<>();

    /**
     * Spawns a new thread and enqueues it to the correct IP mapping
     * @param address - IP address of the sender
     * @param thread - Thread to be executed
     */
    public static void spawnThread(InetAddress address, UDPThread thread) {
        if (threads.containsKey(address)) {
            threads.get(address).add(thread);
        } else {
            Queue<UDPThread> queue = new LinkedList<>();
            queue.add(thread);
            threads.put(address, queue);
        }

        if (threads.get(address).size() == 1) {
            thread.start();
            Connection.log("-- New Thread: " + thread.getClass().getSimpleName());
        }
    }

    /**
     * Called when a thread is done execution
     * @param address - IP address of the sender
     */
    public static void closeThread(InetAddress address) {
        Connection.log("-- Thread closed\n");

        threads.get(address).remove();
        if (threads.get(address).size() > 0) {
            UDPThread next = threads.get(address).peek();
            next.start();
            Connection.log("-- New Thread: " + next.getClass().getSimpleName());
        } else {
            threads.remove(address);
        }
    }

    public static void notifyThread() {
        // Unblock any awaitSend/awaitReceive
        synchronized (UDPMonitor) { UDPMonitor.notify(); }
    }

    public UDPConnection() {
        instance = this;
        this.start();
    }

    /**
     * UDPConnection runs as its own thread because we want it to constantly check for incoming UDP packets
     * Doing this in the main thread will lock up any other processes so we move this function into its own thread
     */
    @Override
    public void run() {
        try {
            socket = new DatagramSocket(Connection.PORT);

            while (true) {

                    DatagramPacket packet = new DatagramPacket(new byte[Protocol.LENGTH], Protocol.LENGTH);
                    socket.receive(packet);

                    InetAddress address = packet.getAddress();
                    Protocol protocol = Protocol.create(packet.getData())[0];

                    if (threads.containsKey(address)) {
                        threads.get(address).peek().pass(protocol);
                    } else {
                        keyNotFound(address, protocol);
                    }
            }
        } catch (Exception e) {
            Connection.log(e + " at " + e.getStackTrace()[0]);
        }
    }

    /**
     * Sends UDP packets to a specified IP
     * NOTE: NOT RECOMMENDED TO USE OTHER THAN FOR THREADING STUFF
     */
    public void send(InetAddress toIP, Protocol.Status status, String message) { send(toIP, Protocol.create(status, message), null, null); }
    public void send(InetAddress toIP, Protocol.Status status, String message, UDPCallback threadResponse, UDPCallback failResponse) { send(toIP, Protocol.create(status, message), threadResponse, failResponse); }
    public void send(InetAddress toIP, Protocol.Status status) { send(toIP, Protocol.create(status, 0), null, null); }
    public void send(InetAddress toIP, Protocol.Status status, UDPCallback threadResponse, UDPCallback failResponse) { send(toIP, Protocol.create(status, 0), threadResponse, failResponse); }

    private void send(InetAddress toIP, Protocol[] fragments, UDPCallback threadResponse, UDPCallback failResponse) {
        UDPThread thread = new SendThread(toIP, fragments, threadResponse, failResponse);
        UDPConnection.spawnThread(toIP, thread);
    }

    /**
     * Same as send() except awaitSend() will wait until the thread is completed before letting the code continue
     */
    public void awaitSend(InetAddress toIP, Protocol.Status status, String message) { awaitSend(toIP, Protocol.create(status, message), null, null); }
    public void awaitSend(InetAddress toIP, Protocol.Status status, String message, UDPCallback threadResponse, UDPCallback failResponse) { awaitSend(toIP, Protocol.create(status, message), threadResponse, failResponse); }
    public void awaitSend(InetAddress toIP, Protocol.Status status) { awaitSend(toIP, Protocol.create(status, 0), null, null); }
    public void awaitSend(InetAddress toIP, Protocol.Status status, UDPCallback threadResponse, UDPCallback failResponse) { awaitSend(toIP, Protocol.create(status, 0), threadResponse, failResponse); }


    private void awaitSend(InetAddress toIP, Protocol[] fragments, UDPCallback threadResponse, UDPCallback failResponse) {
        send(toIP, fragments, threadResponse, failResponse);

        try {
            synchronized (UDPMonitor) { UDPMonitor.wait(); }
        } catch (Exception e) {
            Connection.log(e + " at " + e.getStackTrace()[0]);
        }
    }

    /**
     * Receives UDP packets from a specified IP source
     * Returns information in the callback
     * NOTE: NOT RECOMMENDED TO USE OTHER THAN FOR THREADING STUFF
     */
    public void receive(InetAddress fromIP) { receive(fromIP,null, null); }
    public void receive(InetAddress fromIP, UDPCallback threadResponse, UDPCallback failResponse) {
        UDPThread thread = new ReceiveThread(fromIP, threadResponse, failResponse);
        UDPConnection.spawnThread(fromIP, thread);
    }
    public void receive(InetAddress fromIP, Protocol header) { awaitReceive(fromIP, header,null, null); }
    public void receive(InetAddress fromIP, Protocol header, UDPCallback threadResponse, UDPCallback failResponse) {
        UDPThread thread = new ReceiveThread(fromIP, threadResponse, failResponse);
        UDPConnection.spawnThread(fromIP, thread);
        thread.pass(header);
    }

    /**
     * Same as receive() except awaitReceive() will wait until the thread is completed before letting the code continue
     */
    public void awaitReceive(InetAddress fromIP) { awaitReceive(fromIP,null, null); }
    public void awaitReceive(InetAddress fromIP, UDPCallback threadResponse, UDPCallback failResponse) {
       receive(fromIP, threadResponse, failResponse);

        try {
            synchronized (UDPMonitor) { UDPMonitor.wait(); }
        } catch (Exception e) {
            Connection.log(e + " at " + e.getStackTrace()[0]);
        }
    }
    public void awaitReceive(InetAddress fromIP, Protocol header) { awaitReceive(fromIP, header,null, null); }
    public void awaitReceive(InetAddress fromIP, Protocol header, UDPCallback threadResponse, UDPCallback failResponse) {
        receive(fromIP, header, threadResponse, failResponse);

        try {
            synchronized (UDPMonitor) { UDPMonitor.wait(); }
        } catch (Exception e) {
            Connection.log(e + " at " + e.getStackTrace()[0]);
        }
    }

    /**
     * This method will execute when UDPConnection doesn't find an existing IP in the threads
     * We need this since Client and Server handles new IPs differently; Client throws and ERROR and Server creates a new thread to respond
     * @param address - IP address of the new sender
     * @param protocol - Protocol data that was sent in the packet
     */
    public abstract void keyNotFound(InetAddress address, Protocol protocol);
}

/**
 * UDP thread that specializes in sending packets to a target IP
 */
class SendThread extends UDPThread {
    /**
     * SendThreads can be instantiated without callbacks
     */
    public SendThread(InetAddress packet, Protocol[] outbound) { this(packet, outbound, null, null); }
    public SendThread(InetAddress packet, Protocol[] outbound, UDPCallback threadResponse, UDPCallback failedResponse) {
        super(packet, threadResponse, failedResponse);
        this.fragments = outbound;
    }

    @Override
    public void run() {
        while (index < fragments.length) {
            try {
                DatagramPacket packet = new DatagramPacket(fragments[index].getBytes(), Protocol.LENGTH, toIP, Connection.PORT);
                UDPConnection.instance.socket.send(packet);
                Connection.log(fragments[index]);

                if (!lock()) {
                    if (failed > Connection.MAXREPEAT) {
                        close(failedResponse);
                        return;
                    }
                }
            } catch (Exception e) {
                Connection.log(e + " at " + e.getStackTrace()[0]);
            }
        }

        close(threadResponse);
    }

    @Override
    public void pass(Protocol protocol) {
        int sequence = Integer.parseInt(protocol.data);
        if (!(sequence == index || sequence == index + 1)) { return; }

        index++;

        super.pass(protocol);
    }
}

/**
 * UDP thread that specializes in receiving UDP packets
 */
class ReceiveThread extends UDPThread {
    public ReceiveThread(InetAddress fromIP) { this(fromIP, null, null); }
    public ReceiveThread(InetAddress fromIP, UDPCallback threadResponse, UDPCallback failedResponse) {
        super(fromIP, threadResponse, failedResponse);
    }

    @Override
    public void run() {
        // Wait for leading packet; contains data to initialize fragments
        do {
            while (!lock()) {
                if (failed > Connection.MAXREPEAT) {
                    close(failedResponse);
                    return;
                }
                acknowledge(0);
            }
        // Ensure that the leading packet has been received
        } while (recent.sequence != 0);

        while (index < fragments.length) {
            while (!lock()) {
                if (failed > Connection.MAXREPEAT) {
                    close(failedResponse);
                    return;
                }
                acknowledge(index);
            }

            fragments[index - 1] = recent;
            acknowledge(index + 1);
        }

        close(threadResponse);
    }

    void acknowledge(int i) {
        try {
            Protocol response = Protocol.create(Protocol.Status.OK, i)[0];
            DatagramPacket packet = new DatagramPacket(response.getBytes(), Protocol.LENGTH, toIP, Connection.PORT);
            UDPConnection.instance.socket.send(packet);
            Connection.log(response);
        } catch (Exception e) {
            System.out.println(e);
            //acknowledge();
        }
    }

    @Override
    public void pass(Protocol protocol) {
        // Get leading packet; initialize size of fragments
        if (protocol.sequence == 0) {
            int length = Integer.parseInt(protocol.data);
            fragments = new Protocol[length];
            acknowledge(Math.min(length, 1));

        // Discard packet if it is not the next
        } else if (protocol.sequence != index + 1) {
            return;
        }

        index = protocol.sequence;

        super.pass(protocol);
    }
}

/**
 * Base class that all UDP threads extend from
 * Has the basic functionalities
 */
abstract class UDPThread extends Thread {
    protected static final Object threadMonitor = new Object();

    protected InetAddress toIP;
    protected int index = 0;
    protected Protocol[] fragments = new Protocol[0];
    protected Protocol recent;

    //private boolean isLocked = true;
    private boolean isNotified;

    UDPCallback threadResponse;
    UDPCallback failedResponse;

    int failed = 0;

    public UDPThread(InetAddress address, UDPCallback threadResponse, UDPCallback failedResponse) {
        this.toIP = address;
        this.threadResponse = threadResponse;
        this.failedResponse = failedResponse;
    }

    public void pass(Protocol protocol){
        synchronized (threadMonitor) {
            recent = protocol;
            Connection.log(toIP, protocol);

            unlock();
        }
    }

    protected synchronized boolean lock() {
        if (!isNotified) {
            try {
                wait(Connection.TIMEOUT);
            } catch (Exception e) {
                System.out.println(e);
            }

            if (!isNotified) {
                Connection.log("Received timeout");
                failed++;
                if (failed > Connection.MAXREPEAT) { Connection.log("Failed maximum exceeded"); }
                return false;
            }
        }

        isNotified = false;
        return true;
    }

    protected synchronized void unlock() {
        isNotified = true;
        notify();
    }

    protected void close(UDPCallback response) {
        UDPConnection.closeThread(toIP);
        if (response != null) { response.invoke(toIP, recent, Protocol.constructData(fragments)); }
        UDPConnection.notifyThread();
    }
}