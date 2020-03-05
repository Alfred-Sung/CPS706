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
public class Server extends Connection {
    public static UDPConnection UDP;
    public static Directory directory = new Directory();
    static int totalUsers;

    static Callback clientResponse = new Callback() {
        @Override
        public void invoke(InetAddress address, Protocol protocol, String data) {
            switch (protocol.status) {
                case ONLINE:
                    Connection.log("Added user to directory");
                    directory.add(address, protocol);
                    break;
                case OFFLINE:
                    break;
                case QUERY:
                    UDP.send(address, Protocol.create(Protocol.Status.OK, directory.print()));
                    break;
                case JOIN:
                    break;
                case EXIT:
                    Connection.log("Removed user from directory");
                    directory.remove(address, protocol);
                    break;
                default:
                    UDP.send(address, Protocol.create(Protocol.Status.ERROR));
                    break;
            }
        }
    };

    public static void main(String args[]) {
        VERBOSE = true;
        nickName = "Server";

        UDP = new UDPConnection() {
            @Override
            public void keyNotFound(InetAddress address, Protocol protocol) {
                receive(address, protocol,
                        clientResponse,
                        new Callback() {
                            @Override
                            public void invoke(InetAddress address, Protocol protocol, String data) {
                                send(address, Protocol.create(Protocol.Status.ERROR));
                            }
                        }
                );
            }
        };
        UDP.start();

        System.out.println("Server online!");

        while(true){}
    }
}