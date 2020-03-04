import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

/**
 * Handles sending, splitting and reconstructing protocol packets
 */
interface Callback { void invoke(InetAddress address, Protocol protocol, String data); }
public abstract class UDPConnection extends Thread {
    public static final Object UDPMonitor = new Object();
    private static HashMap<InetAddress, Queue<ConnectionThread>> threads = new HashMap<>();

    public static void spawnThread(InetAddress address, ConnectionThread thread) {
        if (threads.containsKey(address)) {
            threads.get(address).add(thread);
        } else {
            Queue<ConnectionThread> queue = new LinkedList<>();
            queue.add(thread);
            threads.put(address, queue);
        }

        if (threads.get(address).size() == 1) { threads.get(address).peek().start(); }
    }

    public static void closeThread(InetAddress address) {
        Connection.log("Thread closed\n");

        threads.get(address).remove();
        if (threads.get(address).size() > 0) {
            threads.get(address).peek().start();
        } else {
            threads.remove(address);
        }

        synchronized (UDPMonitor) { UDPMonitor.notify(); }
    }

    @Override
    public void run() {
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[Protocol.LENGTH], Protocol.LENGTH);
                Connection.socket.receive(packet);

                InetAddress address = packet.getAddress();
                Protocol protocol = Protocol.create(packet.getData())[0];

                if (threads.containsKey(address)) {
                    threads.get(address).peek().pass(protocol);
                } else {
                    keyNotFound(address, protocol);
                }
            } catch (Exception e) {
                Connection.log(e + " at " + e.getStackTrace()[0]);
            }
        }
    }

    public void send(InetAddress toIP, Protocol[] fragments) { send(toIP, fragments, null, null); }
    public void send(InetAddress toIP, Protocol.Status status) { send(toIP, Protocol.create(status), null, null); }
    public void send(InetAddress toIP, Protocol.Status status, Callback threadResponse, Callback failResponse) { send(toIP, Protocol.create(status), threadResponse, failResponse); }
    public void send(InetAddress toIP, Protocol[] fragments, Callback threadResponse, Callback failResponse) {
        ConnectionThread thread = new SendThread(toIP, fragments, threadResponse, failResponse);
        UDPConnection.spawnThread(toIP, thread);
    }

    public void awaitSend(InetAddress toIP, Protocol[] fragments) { awaitSend(toIP, fragments, null, null); }
    public void awaitSend(InetAddress toIP, Protocol.Status status) { awaitSend(toIP, Protocol.create(status), null, null); }
    public void awaitSend(InetAddress toIP, Protocol.Status status, Callback threadResponse, Callback failResponse) { awaitSend(toIP, Protocol.create(status), threadResponse, failResponse); }
    public void awaitSend(InetAddress toIP, Protocol[] fragments, Callback threadResponse, Callback failResponse) {
        send(toIP, fragments, threadResponse, failResponse);

        try {
            synchronized (UDPMonitor) { UDPMonitor.wait(); }
        } catch (Exception e) {
            Connection.log(e + " at " + e.getStackTrace()[0]);
        }
    }

    public void receive(InetAddress fromIP) { receive(fromIP,null, null); }
    public void receive(InetAddress fromIP, Callback threadResponse, Callback failResponse) {
        ConnectionThread thread = new ReceiveThread(fromIP, threadResponse, failResponse);
        UDPConnection.spawnThread(fromIP, thread);
    }
    public void receive(InetAddress fromIP, Protocol header) { awaitReceive(fromIP, header,null, null); }
    public void receive(InetAddress fromIP, Protocol header, Callback threadResponse, Callback failResponse) {
        ConnectionThread thread = new ReceiveThread(fromIP, threadResponse, failResponse);
        UDPConnection.spawnThread(fromIP, thread);
        thread.pass(header);
    }

    public void awaitReceive(InetAddress fromIP) { awaitReceive(fromIP,null, null); }
    public void awaitReceive(InetAddress fromIP, Callback threadResponse, Callback failResponse) {
       receive(fromIP, threadResponse, failResponse);

        try {
            synchronized (UDPMonitor) { UDPMonitor.wait(); }
        } catch (Exception e) {
            Connection.log(e + " at " + e.getStackTrace()[0]);
        }
    }
    public void awaitReceive(InetAddress fromIP, Protocol header) { awaitReceive(fromIP, header,null, null); }
    public void awaitReceive(InetAddress fromIP, Protocol header, Callback threadResponse, Callback failResponse) {
        receive(fromIP, header, threadResponse, failResponse);

        try {
            synchronized (UDPMonitor) { UDPMonitor.wait(); }
        } catch (Exception e) {
            Connection.log(e + " at " + e.getStackTrace()[0]);
        }
    }

    public abstract void keyNotFound(InetAddress address, Protocol protocol);
}

class SendThread extends ConnectionThread {
    int failed = 0;
    InetAddress toIP;
    Protocol[] outbound;

    public SendThread(InetAddress packet, Protocol[] outbound) { this(packet, outbound, null, null); }
    public SendThread(InetAddress packet, Protocol[] outbound, Callback threadResponse, Callback failedResponse) {
        super(packet, threadResponse, failedResponse);
        this.outbound = outbound;
    }

    @Override
    public void run() {
        for (int i = 0; i < outbound.length; i++) {
            try {
                DatagramPacket packet = new DatagramPacket(outbound[i].getBytes(), Protocol.LENGTH, address, Connection.PORT);
                Connection.socket.send(packet);
                Connection.log(outbound[i]);

                // TODO: Fix
                //Connection.socket.setSoTimeout(Connection.TIMEOUT);
                lock();

                switch (recent.status) {
                    case OK:
                        break;
                    case ERROR:
                        i--;
                        if (fail()) { return; };
                        break;
                }
            } catch (Exception e) {
                Connection.log(e + " at " + e.getStackTrace()[0]);

                i--;
                if (fail()) { return; };
            }
        }

        if (threadResponse != null) { threadResponse.invoke(address, recent, recent.data); }
        UDPConnection.closeThread(address);
    }

    boolean fail() {
        failed++;
        if (failed > Connection.MAXREPEAT) {
            if (failedResponse != null) { failedResponse.invoke(address, recent, recent.data); }
            UDPConnection.closeThread(address);
            return true;
        }

        return false;
    }
}

class ReceiveThread extends ConnectionThread {
    protected Protocol[] fragments;

    public ReceiveThread(InetAddress fromIP) { this(fromIP, null, null); }
    public ReceiveThread(InetAddress fromIP, Callback threadResponse, Callback failedResponse) {
        super(fromIP, threadResponse, failedResponse);
    }

    @Override
    public void run() {
        lock();

        for (int i = 0; i < fragments.length; i++) {
            lock();

            fragments[i] = recent;
            acknowledge();
        }

        //if (failedResponse != null) { failedResponse.invoke(address, recent, recent.data); }

        if (threadResponse != null) { threadResponse.invoke(address, recent, Protocol.constructData(fragments)); }
        UDPConnection.closeThread(address);
    }

    // TODO: Handle packet resending
    void acknowledge() {
        try {
            Connection.log("Acknowledged!");
            Protocol response = Protocol.create(Protocol.Status.OK)[0];
            DatagramPacket packet = new DatagramPacket(response.getBytes(), Protocol.LENGTH, address, Connection.PORT);
            Connection.socket.send(packet);
            Connection.socket.setSoTimeout(Connection.TIMEOUT);
        } catch (Exception e) {

        }
    }

    @Override
    public void pass(Protocol protocol){
        recent = protocol;
        Connection.log(address , protocol);

        if (protocol.sequence == 0) {
            fragments = new Protocol[Integer.parseInt(protocol.data)];
            acknowledge();
        }

        synchronized (threadMonitor) { threadMonitor.notify(); }
    }
}

abstract class ConnectionThread extends Thread {
    protected static final Object threadMonitor = new Object();
    protected InetAddress address;
    protected Protocol recent;

    protected boolean isLocked = false;

    Callback threadResponse;
    Callback failedResponse;

    public ConnectionThread(InetAddress address, Callback threadResponse, Callback failedResponse) {
        Connection.log("New Thread: " + this.getClass().getSimpleName());

        this.address = address;
        this.threadResponse = threadResponse;
        this.failedResponse = failedResponse;
    }

    public void pass(Protocol protocol){
        synchronized (threadMonitor) {
            recent = protocol;
            Connection.log(address, protocol);

            isLocked = false;
            threadMonitor.notify();
        }
    }

    protected void lock() {
        if (!isLocked) { return; }
        isLocked = true;
        try {
            synchronized (threadMonitor) { threadMonitor.wait(); }
        } catch (Exception e) {
            Connection.log(e + " at " + e.getStackTrace()[0]);
        }
        isLocked = false;
    }
}