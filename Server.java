import java.util.*;
import java.net.*;
import java.time.LocalDateTime;

/**
 * Server-side script that handles client-server communication
 * Tracks online users and chatrooms
 * Spawns individual threads for each user request and tracks them in a hashmap
 */
// TODO: Respond to incoming requests
// TODO: Handle fragmented packets
public class Server extends UDPConnection {
    // <IP Address, ServerThread>
    private static HashMap<InetAddress, ServerThread> threads = new HashMap<InetAddress, ServerThread>();
    static int totalUsers;
    public static HashMap<InetAddress, Profile> userList = new HashMap<InetAddress, Profile>();

    public static void main(String args[]) {
        nickName = "Server";
        System.out.println("Server online!");

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[Protocol.LENGTH], Protocol.LENGTH);
                socket.receive(packet);
                InetAddress address = packet.getAddress();

                if (threads.containsKey(address)) {
                    System.out.println("Test");
                } else {
                    ServerThread thread = new ServerThread(packet);
                    threads.put(packet.getAddress(), thread);
                    thread.start();
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public static String printUserList() {
        StringBuilder result = new StringBuilder("User\tIP\tChatroom\tPopularity\n");

        for (Profile c : userList.values()) {
            result.append(c.getRecord() + '\n');
        }

        return result.toString();
    }

    public static void closeThread(InetAddress address) {
        System.out.println("Thread closed\n");
        threads.remove(address);
    }
}

/**
 * Spawned when client sends request to server
 * Services request and sends back a response to client
 */
class ServerThread extends Thread {
    InetAddress address;
    Protocol inbound;

    public ServerThread(DatagramPacket packet) {
        address = packet.getAddress();
        inbound = Protocol.create(packet.getData());

        System.out.println("Packet from: " + address);
    }

    public void run() {
        System.out.println(inbound.toString());

        try {
            switch (inbound.status) {
                case ONLINE:
                    Profile user = new Profile(address, inbound);
                    Server.userList.put(address, user);
                    System.out.println("Added new record: " + user.getRecord());

                    acknowledge();
                    break;
                case OFFLINE:
                    Server.userList.remove(address);

                    acknowledge();
                    break;
                case JOIN:
                    acknowledge();
                    break;
                case EXIT:
                    acknowledge();
                    break;
                case QUERY:
                    acknowledge();

                    Server.send(Protocol.Status.OK, address, Server.printUserList());
                    System.out.println("OK!");
                    break;
                default:
                    // Respond with 400 ERROR
            }
        } catch (Exception e) {
            System.out.println("!!! THREAD ERROR: " + e + " !!!");
        }
        Server.closeThread(address);
    }

    void acknowledge() {
        try {
            System.out.println("Acknowledged!");
            Protocol response = Protocol.create(Protocol.Status.OK);
            DatagramPacket packet = new DatagramPacket(response.getBytes(), Protocol.LENGTH, address, Connection.PORT);
            Server.socket.send(packet);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

/**
 * Data class that holds all the information the server needs for a chatroom
 */

// TODO: Calculate popularity within users 1 hour log-in time
class Profile {
    String nickname;
    String hostname;
    String IP;
    String chatName;
    int activeUsers;

    //LocalDateTime login;

    public Profile(InetAddress address, Protocol protocol) {
        this(protocol.nickName, protocol.hostName, address.toString());
    }

    public Profile(String nickname, String hostname, String IP) {
        this.nickname = nickname;
        this.hostname = hostname;
        this.IP = IP;
        this.chatName = "";
        this.activeUsers = 0;

        //this.login = LocalDateTime.now();
    }

    public String getRecord() {
        return nickname + '\t' +
                IP + '\t' +
                chatName + '\t' +
                ((float)activeUsers / Server.totalUsers);
    }
}
