import java.net.*;
import java.util.HashMap;

/**
 * Client-side script hat handles client-server communication
 * All communication uses UDP
 */

// TODO: Everything
public class ServerConnection extends UDPConnection {
    static InetAddress serverAddress;

    public boolean connect(String serverIP, String nickName) {
        try {
            serverAddress = InetAddress.getByName(serverIP);
            Protocol protocol = Protocol.create(Protocol.Status.ONLINE);

            DatagramPacket packet = new DatagramPacket(protocol.getBytes(), Protocol.LENGTH, serverAddress, PORT);
            socket.send(packet);

            packet = new DatagramPacket(new byte[Protocol.LENGTH], Protocol.LENGTH);
            socket.receive(packet);
            Protocol response = Protocol.create(packet.getData());

            if (response.status == Protocol.Status.OK) { return true; }
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }

        return true;
    }

    public String getDirectory() {
        try {
            Protocol protocol = Protocol.create(Protocol.Status.QUERY);

            DatagramPacket packet = new DatagramPacket(protocol.getBytes(), Protocol.LENGTH, serverAddress, PORT);
            socket.send(packet);

            packet = new DatagramPacket(new byte[Protocol.LENGTH], Protocol.LENGTH);
            socket.receive(packet);
            Protocol response = Protocol.create(packet.getData());

            if (response.status == Protocol.Status.OK) { return ""; }
        } catch (Exception e) {
            System.out.println(e);
            return "";
        }

        return "";
    }

    public void joinChat() {}
}
