import java.util.*;
import java.net.*;

public class Server extends Connection {
    private static HashMap<byte[], ServerThread> threads = new HashMap<byte[], ServerThread>();
    public static void main(String args[]) {
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[256], 256);
                socket.receive(packet);
                (new ServerThread(packet)).start();

            } catch (Exception e) {

            }
        }
    }
}

class ServerThread extends Thread {
    public ServerThread(DatagramPacket packet) {
        InetAddress address = packet.getAddress();
        Protocol protocol = new Protocol(packet.getData());

        switch (protocol.status) {
            case ONLINE:
                break;
            case OFFLINE:
                break;
            case JOIN:
                break;
            case QUERY:
                break;
            default:
                // Respond with 400 ERROR
        }
    }

    public void run() {

    }
}
