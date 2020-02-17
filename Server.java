import java.util.*;
import java.net.*;

public class Server extends Thread {
    static SecureConnection connection;

    public static void main(String args[]) {
        (new Server()).start();
    }

    public Server() { System.out.println("Server thread online"); }

    public void run() {
        boolean running = true;

        while (running) {
            DatagramPacket packet = new DatagramPacket(new byte[256], 256);
            try {
                connection.socket.receive(packet);
            } catch (Exception e) {

            }

            InetAddress address = packet.getAddress();
            packet = new DatagramPacket(new byte[256], 256, address, connection.port);
            String received = new String(packet.getData(), 0, packet.getLength());

            System.out.println(received);
        }
        connection.socket.close();
    }
}
