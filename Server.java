import java.util.*;
import java.net.*;

/**
 * Server-side script that handles client-server communication
 * Tracks online users and chatrooms
 * Spawns individual threads for each user request and tracks them in a hashmap
 */
// TODO: Respond to incoming requests
// TODO: Handle fragmented packets
public class Server extends Connection {
    // <IP Address, ServerThread>
    private static HashMap<byte[], ServerThread> threads = new HashMap<byte[], ServerThread>();
    static int totalUsers;
    private static HashMap<byte[], Chatroom> userList = new HashMap<byte[], Chatroom>();

    public static void main(String args[]) {
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[256], 256);
                socket.receive(packet);

                ServerThread thread = new ServerThread(packet);
                //threads.put(, thread);
                thread.start();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public static String getUserList() {
        StringBuilder result = new StringBuilder("User\tIP\tChatroom\tPopularity\n");

        for (Chatroom c : userList.values()) {
            result.append(c.getRecord() + '\n');
        }

        return result.toString();
    }

    public static void closeThread(byte[] thread) { threads.remove(thread); }
}

/**
 * Spawned when client sends request to server
 * Services request and sends back a response to client
 */
class ServerThread extends Thread {
    InetAddress address;
    Protocol protocol;

    public ServerThread(DatagramPacket packet) {
        address = packet.getAddress();
        protocol = new Protocol(packet.getData());
    }

    public void run() {
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

        //Server.closeThread();
    }
}

/**
 * Data class that holds all the information the server needs for a chatroom
 */
class Chatroom {
    String nickname;
    String hostname;
    String IP;
    int port;
    String chatName;
    int activeUsers;

    public Chatroom(String nickname, String hostname, String IP, int port, String chatName, int activeUsers) {
        this.nickname = nickname;
        this.hostname = hostname;
        this.IP = IP;
        this.port = port;
        this.chatName = chatName;
        this.activeUsers = activeUsers;
    }

    public String getRecord() {
        return nickname + '\t' +
                IP + '\t' +
                chatName + '\t' +
                ((float)activeUsers / Server.totalUsers);
    }
}
