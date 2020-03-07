import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * Base class that all things that need to communicate extends from
 * Gets information about the computer for communication uses later
 */
public abstract class Connection {
    public static InetAddress localMachine;
    public static String hostName;
    public static String nickName;

    public static boolean VERBOSE = false;

    protected static final int PORT = 1000;
    protected static final int TIMEOUT = 100;
    protected static final int MAXREPEAT = 10;
    protected static DatagramSocket UDPSocket;
    protected static ServerSocket TCPSocket;

    static {
        try {
            localMachine = InetAddress.getLocalHost();
            hostName = localMachine.getHostName();
            TCPSocket = new ServerSocket(PORT);
            //socket.setSoTimeout(TIMEOUT);

            System.out.println(localMachine.getHostAddress() + " : " + hostName);
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    /**
     * Debug methods that print out a formatted version
     * Also checks if VERBOSE is on
     */

    // TODO: Replace System.out.println() with something faster
    public static void log(String message) {
        if (!VERBOSE) { return; }
        System.out.println(message);
    }
    public static void log(Protocol protocol) { log(localMachine, protocol); }
    public static void log(InetAddress address, Protocol protocol) {
        if (!VERBOSE) { return; }
        if (address.equals(localMachine)) {
            System.out.println("@> " + protocol.sequence + " " + protocol.status + " \"" + protocol.data + "\"");
        } else {
            System.out.println(address + "> " + protocol.sequence + " " + protocol.status + " \"" + protocol.data + "\"");
        }
    }
}