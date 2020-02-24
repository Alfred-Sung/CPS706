import java.net.*;
import java.util.HashMap;

/**
 * Client-side script hat handles client-server communication
 * All communication uses UDP
 */

// TODO: Everything
public class ServerConnection extends UDPConnection {
    static InetAddress serverAddress;

    public boolean connect(String serverIP) {
        try {
            serverAddress = InetAddress.getByName(serverIP);
            return send(Protocol.Status.ONLINE, serverAddress);
        } catch (Exception e) {
            return false;
        }
    }

    public String getDirectory() {
        try {
            send(Protocol.Status.QUERY, serverAddress);
            return receive();
        } catch (Exception e) {
            System.out.println(e + " at " + e.getStackTrace()[0]);
            return "";
        }
    }

    public void joinChat() {}
}
