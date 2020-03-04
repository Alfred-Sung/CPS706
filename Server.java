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
    // <IP Address, ServerThread>
    static int totalUsers;
    static Directory directory;
    static Callback clientResponse = new Callback() {
        @Override
        public void invoke(Protocol.Status status, String data) {
            switch (status) {
                case ONLINE:
                    break;
                case OFFLINE:
                    break;
                case QUERY:
                    break;
                case JOIN:
                    break;
                case EXIT:
                    break;
            }
        }
    };

    public static void main(String args[]) {
        //VERBOSE = true;
        nickName = "Server";

        UDPConnection UDP = new UDPConnection() {
            @Override
            public void keyNotFound(InetAddress address, Protocol protocol) {
                receive(address, protocol,
                        clientResponse,
                        new Callback() {
                            @Override
                            public void invoke(Protocol.Status status, String data) {
                                send(address, Protocol.create(Protocol.Status.ERROR));
                            }
                        }
                );
            }
        };

        System.out.println("Server online!");
    }
}