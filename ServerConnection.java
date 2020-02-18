import java.net.*;
import java.util.HashMap;

/**
 * Client-side script hat handles client-server communication
 * All communication uses UDP
 */

// TODO: Everything
public class ServerConnection extends Connection {
    static InetAddress serverAddress;

    public boolean connect(String serverIP, String nickName) {
        try {
            serverAddress = InetAddress.getByName(serverIP);
            Protocol protocol = new Protocol(Protocol.Status.ONLINE);

            DatagramPacket packet = new DatagramPacket(protocol.getBytes(), Protocol.LENGTH, serverAddress, PORT);
            socket.send(packet);

            packet = new DatagramPacket(new byte[Protocol.LENGTH], Protocol.LENGTH);
            socket.receive(packet);
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }

        return true;
    }

    public void joinChat() {}
}
