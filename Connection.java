import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Base class that all things that need to communicate extends from
 * Gets information about the computer for communication uses later
 */
public class Connection {
    protected static final int PORT = 1000;
    protected static InetAddress localMachine;
    protected static DatagramSocket socket;

    static {
        try {
            localMachine = InetAddress.getLocalHost();
            socket = new DatagramSocket(PORT);
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }
}
