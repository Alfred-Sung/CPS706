import java.net.*;
import java.util.HashMap;

/**
 * Client-side script that handles client-server communication
 * All communication uses UDP
 */

// TODO: Everything
public class ServerConnection extends Connection {
    static InetAddress serverAddress;
    static Directory directory;
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
            UDP.send(temp, Protocol.Status.ONLINE,
                    new Callback() {
                        @Override
                        public void invoke(Protocol.Status status, String data) {
                            System.out.println("Connected!");
                            ServerConnection.serverAddress = temp;
                        }
                    },
                    new Callback() {
                        @Override
                        public void invoke(Protocol.Status status, String data) { System.out.println("Error!"); }
                    }
            );

        } catch (Exception e) {
            return false;
        }

        return (serverAddress != null);
    }

    public String getDirectory() {
        try {
            UDP.send(serverAddress, Protocol.Status.QUERY);
            UDP.receive(serverAddress,
                    new Callback() {
                        @Override
                        public void invoke(Protocol.Status status, String data) {
                            System.out.println(data);
                        }
                    },
                    new Callback() {
                        @Override
                        public void invoke(Protocol.Status status, String data) { System.out.println("Error"); }
                    }
            );
        } catch (Exception e) {
            System.out.println(e + " at " + e.getStackTrace()[0]);
            return "";
        }

        return directory.getDirectory();
    }

    public void joinChat(String peerIP) {
        try {
            UDP.send(serverAddress, Protocol.create(Protocol.Status.JOIN, peerIP));
        } catch (Exception e) {
            System.out.println(e + " at " + e.getStackTrace()[0]);
        }
    }
}
