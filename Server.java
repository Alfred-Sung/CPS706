import java.util.*;
import java.net.*;
import java.time.LocalDateTime;

/**
 * Server-side script that handles client-server communication
 * Tracks online users and chatrooms
 * Spawns individual threads for each user request and tracks them in a hashmap
 */
// TODO: Finish clientResponse switch cases
public class Server extends Connection {
    public static UDPConnection UDP;
    public static Directory directory = new Directory();
    static int totalUsers;

    static UDPCallback clientResponse = new UDPCallback() {
        @Override
        public void invoke(InetAddress address, Protocol protocol, String data) {
            switch (protocol.status) {
                case ONLINE:
                    Connection.log("Added user to directory");
                    directory.add(address, protocol);
                    break;
                case OFFLINE:
                    Connection.log("Removed user from directory");
                    directory.remove(address, protocol);
                    break;
                case QUERY:
                    UDP.send(address, Protocol.Status.OK, directory.print());
                    break;
                case JOIN:
                    Profile client = directory.getProfile(protocol.nickName);
                    Profile other = directory.getProfile(data);

                    if (other == null) {
                        UDP.send(address, Protocol.Status.ERROR, "No such user exists");
                    } else if (client.IP.equals(other.IP)) {
                        UDP.send(address, Protocol.Status.ERROR, "Cannot send invitation to self");
                    } else {
                        UDP.send(address, Protocol.Status.OK, other.getData());
                    }

                    UDP.send(other.IP, Protocol.Status.JOIN, client.getData());
                    break;
                case EXIT:

                    break;
                case ACCEPT:
                    Profile profile = directory.getProfile(data);
                    UDP.send(profile.IP, Protocol.Status.ACCEPT);
                    break;
                case DECLINE:
                    profile = directory.getProfile(data);
                    UDP.send(profile.IP, Protocol.Status.DECLINE);
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
                // Discard incoming new OK/ERROR packets
                if (protocol.status == Protocol.Status.OK || protocol.status == Protocol.Status.ERROR) { return; }

                receive(address, protocol,
                        clientResponse,
                        new UDPCallback() {
                            @Override
                            public void invoke(InetAddress address, Protocol protocol, String data) {
                                send(address, Protocol.Status.ERROR);
                            }
                        }
                );
            }
        };

        System.out.println("Server online!");

        while(true){}
    }
}