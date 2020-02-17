import java.util.*;
import java.net.*;

public class Server extends Connection {
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
    }

    public void run() {

    }
}
