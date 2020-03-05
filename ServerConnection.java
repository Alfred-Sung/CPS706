import java.net.*;
import java.util.HashMap;

/**
 * Client-side script that handles client-server communication
 */
public class ServerConnection extends Connection {
    static InetAddress serverAddress;
    UDPConnection UDP;

    public ServerConnection() {
        UDP = new UDPConnection() {
            @Override
            public void keyNotFound(InetAddress address, Protocol protocol) { send(address, Protocol.create(Protocol.Status.ERROR)); }
        };
        UDP.start();
    }

    public boolean connect(String serverIP) {
        try {
            InetAddress temp = InetAddress.getByName(serverIP);
            UDP.awaitSend(temp, Protocol.Status.ONLINE,
                    new Callback() {
                        @Override
                        public void invoke(InetAddress address, Protocol protocol, String data) {
                            System.out.println("Connected!");
                            ServerConnection.serverAddress = temp;
                        }
                    },
                    new Callback() {
                        @Override
                        public void invoke(InetAddress address, Protocol protocol, String data) { System.out.println("Invalid server IP"); }
                    }
            );

        } catch (Exception e) {
            return false;
        }

        return (serverAddress != null);
    }

    public void printDirectory() {
        try {
            UDP.awaitSend(serverAddress, Protocol.Status.QUERY);
            UDP.awaitReceive(serverAddress,
                    new Callback() {
                        @Override
                        public void invoke(InetAddress address, Protocol protocol, String data) { System.out.println(data); }
                    },
                    new Callback() {
                        @Override
                        public void invoke(InetAddress address, Protocol protocol, String data) { System.out.println("Error"); }
                    }
            );
        } catch (Exception e) {
            System.out.println(e + " at " + e.getStackTrace()[0]);
        }
        System.out.println("Type /join <username or ip> to join a chat");
    }

    public void joinChat(String peerIP) {
        try {
            UDP.awaitSend(serverAddress, Protocol.create(Protocol.Status.JOIN, peerIP));
            UDP.awaitReceive(serverAddress,
                    new Callback() {
                        @Override
                        public void invoke(InetAddress address, Protocol protocol, String data) { }
                    },
                    new Callback() {
                        @Override
                        public void invoke(InetAddress address, Protocol protocol, String data) { System.out.println(data); }
                    }
            );
        } catch (Exception e) {
            System.out.println(e + " at " + e.getStackTrace()[0]);
        }
    }
}
