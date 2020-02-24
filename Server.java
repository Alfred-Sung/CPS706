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
                    threads.get(address).pass(packet);
                } else {
                    ServerThread thread = new ServerThread(packet);
                    threads.put(address, thread);
                    thread.start();
                }
            } catch (Exception e) {
                System.out.println(e + " at " + e.getStackTrace()[0]);
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
    Protocol protocol;
    Protocol[] fragments;

    public ServerThread(DatagramPacket packet) {
        address = packet.getAddress();
        protocol = Protocol.create(packet.getData());
        fragments = new Protocol[Integer.parseInt(protocol.data)];

        System.out.println("Packet from: " + address);
        System.out.println(protocol.toString());

        if (protocol.sequence < fragments.length) {
            try {
                wait();
            } catch (Exception e) {

            }
        }
    }

    // jfc I have no idea
    public void run() {
        try {
            switch (protocol.status) {
                case ONLINE:
                    Profile user = new Profile(address, protocol);
                    Server.userList.put(address, user);
                    System.out.println("Added new record: " + user.getRecord());

                    acknowledge();
                    break;
                case OFFLINE:
                    Server.userList.remove(address);

                    acknowledge();
                    break;
                case JOIN:
                    //Server.send(Protocol.Status.OK, address, );

                    acknowledge();
                    break;
                case EXIT:
                    acknowledge();
                    break;
                case QUERY:
                    acknowledge();

                    Protocol[] out = Protocol.create(Protocol.Status.OK, Server.printUserList());
                    for (Protocol frag : out) {
                        DatagramPacket packet = new DatagramPacket(frag.getBytes(), Protocol.LENGTH, address, Connection.PORT);
                        Server.socket.send(packet);
                        wait();
                    }
                    System.out.println("OK!");
                    break;
                default:
                    Protocol response = Protocol.create(Protocol.Status.ERROR);
                    DatagramPacket packet = new DatagramPacket(response.getBytes(), Protocol.LENGTH, address, Connection.PORT);
                    Server.socket.send(packet);
            }
        } catch (Exception e) {
            System.out.println("!!! THREAD ERROR: " + e + " !!!");
        }
        Server.closeThread(address);
    }

    public void pass(DatagramPacket fragment) {
        Protocol inbound = Protocol.create(fragment.getData());
        System.out.println(inbound.toString());

        if (inbound.sequence >= fragments.length) { notify(); }
    }

    void acknowledge() {
        try {
            System.out.println("Acknowledged!");
            Protocol response = Protocol.create(Protocol.Status.OK);
            DatagramPacket packet = new DatagramPacket(response.getBytes(), Protocol.LENGTH, address, Connection.PORT);
            Server.socket.send(packet);
        } catch (Exception e) {
            System.out.println(e + " at " + e.getStackTrace()[0]);
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
