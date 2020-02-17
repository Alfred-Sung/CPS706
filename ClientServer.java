import java.net.DatagramPacket;
import java.net.InetAddress;

public class ClientServer extends Connection {
    public ClientServer() {
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[256], 256);
                socket.receive(packet);
                (new ClientServerThread(packet)).start();

            } catch (Exception e) {

            }
        }
    }
}

class ClientServerThread extends Thread {
    public ClientServerThread(DatagramPacket packet) {
        InetAddress address = packet.getAddress();
        Protocol protocol = new Protocol(packet.getData());

        switch (protocol.status) {
            case POST:
                break;
            default:
                // Respond with 400 ERROR
        }
    }
}