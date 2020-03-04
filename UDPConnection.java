import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

/**
 * Handles sending, splitting and reconstructing protocol packets
 */
interface Callback { void invoke(Protocol.Status status, String data); }
public abstract class UDPConnection extends Thread {
    private static HashMap<InetAddress, ConnectionThread> threads = new HashMap<InetAddress, ConnectionThread>();

    public static void closeThread(InetAddress address) {
        System.out.println("Thread closed\n");
        threads.remove(address);
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
                    threads.get(address).pass(protocol);
                } else {
                    keyNotFound(address, protocol);
                }
            } catch (Exception e) {
                System.out.println(e + " at " + e.getStackTrace()[0]);
            }
        }
    }

    public void send(InetAddress toIP, Protocol[] fragments) { send(toIP, fragments, null, null); }
    public void send(InetAddress toIP, Protocol.Status status) { send(toIP, Protocol.create(status), null, null); }
    public void send(InetAddress toIP, Protocol.Status status, Callback threadResponse, Callback failResponse) { send(toIP, Protocol.create(status), threadResponse, failResponse); }
    public void send(InetAddress toIP, Protocol[] fragments, Callback threadResponse, Callback failResponse) {
        ConnectionThread thread = new SendThread(toIP, fragments, threadResponse, failResponse);
        threads.put(toIP, thread);
        thread.start();
    }

    public void receive(InetAddress fromIP) { receive(fromIP,null, null); }
    public void receive(InetAddress fromIP, Callback threadResponse, Callback failResponse) {
        ConnectionThread thread = new ReceiveThread(fromIP, threadResponse, failResponse);
        threads.put(fromIP, thread);
        thread.start();
    }
    public void receive(InetAddress fromIP, Protocol header) { receive(fromIP, header,null, null); }
    public void receive(InetAddress fromIP, Protocol header, Callback threadResponse, Callback failResponse) {
        ConnectionThread thread = new ReceiveThread(fromIP, threadResponse, failResponse);
        threads.put(fromIP, thread);
        thread.start();
        thread.pass(header);
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

                synchronized (monitor) { monitor.wait(); }

                switch (recent.status) {
                    case OK:
                        break;
                    case ERROR:
                        i--;
                        if (fail()) { return; };
                        break;
                }
            } catch (Exception e) {
                i--;
                if (fail()) { return; };
            }
        }

        if (threadResponse != null) { threadResponse.invoke(Protocol.Status.OK, ""); }
        UDPConnection.closeThread(address);
    }

    boolean fail() {
        failed++;
        if (failed > Connection.MAXREPEAT) {
            if (failedResponse != null) { failedResponse.invoke(Protocol.Status.ERROR, ""); }
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

        try {
            synchronized (monitor) { monitor.wait(); }
        } catch (Exception e) {

        }
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < fragments.length; i++) {
                synchronized (monitor) { monitor.wait(); }
                fragments[i] = recent;
                acknowledge();
            }
        } catch (Exception e) {
            if (failedResponse != null) { failedResponse.invoke(Protocol.Status.ERROR, ""); }
        }

        if (threadResponse != null) { threadResponse.invoke(Protocol.Status.OK, Protocol.constructData(fragments)); }
        UDPConnection.closeThread(address);
    }

    void acknowledge() {
        try {
            System.out.println("Acknowledged!");
            Protocol response = Protocol.create(Protocol.Status.OK)[0];
            DatagramPacket packet = new DatagramPacket(response.getBytes(), Protocol.LENGTH, address, Connection.PORT);
            Connection.socket.send(packet);
        } catch (Exception e) {
            System.out.println(e + " at " + e.getStackTrace()[0]);
        }
    }

    @Override
    public void pass(Protocol protocol){
        recent = protocol;
        System.out.println(address + "> " + protocol.status + " " + protocol.data);

        if (protocol.sequence == 0) { fragments = new Protocol[Integer.parseInt(protocol.data)]; }

        synchronized (monitor) {
            if (recent.sequence >= fragments.length) { monitor.notify(); }
        }
    }
}

abstract class ConnectionThread extends Thread {
    protected static final Object monitor = new Object();
    protected InetAddress address;
    protected Protocol recent;

    Callback threadResponse;
    Callback failedResponse;

    public ConnectionThread(InetAddress address, Callback threadResponse, Callback failedResponse) {
        this.address = address;
        this.threadResponse = threadResponse;
        this.failedResponse = failedResponse;
    }

    public void pass(Protocol protocol){
        synchronized (monitor) {
            recent = protocol;
            System.out.println(address + "> " + protocol.status + " " + protocol.data);

            monitor.notify();
        }
    }
}