import java.net.*;

/**
 * Client-side script that handles client-client communication
 * ClientServer is created by ClientConnection when user accepts to chat with peers
 * Handles forwarding messages from peers to all users in a chat room
 */
// TODO: Everything
public class ClientConnection {

}

/**
 * Spawned when user accepts a peer's request to chat
 * Constantly checks if there is an incoming peer message
 * Spawns a new ClientServerThread thread if there is a new message
 */
class ClientServer extends Thread {
    // TODO: Add list of all peers in chatroom

    public void run() {
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[256], 256);
                Connection.socket.receive(packet);
                (new ClientServerThread(packet)).start();

            } catch (Exception e) {

            }
        }
    }
}

/**
 * Spawned by ClientServer to service a message
 * Forwards message to all peers in the chatroom
 */
class ClientServerThread extends Thread {
    public ClientServerThread(DatagramPacket packet) {
        InetAddress address = packet.getAddress();
        Protocol protocol = new Protocol(packet.getData());
    }
}