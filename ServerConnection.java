import java.net.*;
import java.util.HashMap;

/**
 * Client-side script that handles client-server communication
 */
public class ServerConnection extends Connection {
    static InetAddress serverAddress;
    private static UDPConnection UDP;
    private static Profile TCPServerProfile;

    public ServerConnection() {
        VERBOSE = true;

        UDP = new UDPConnection() {
            @Override
            public void keyNotFound(InetAddress address, Protocol protocol) {
                if (!address.equals(serverAddress)) {
                    return;
                }

                switch (protocol.status) {
                    case JOIN:
                        receive(address, protocol,
                                new UDPCallback() {
                                    @Override
                                    public void invoke(InetAddress address, Protocol protocol, String data) {
                                        Profile profile = Profile.parse(data);
                                        Client.requestedClient = profile;

                                        System.out.println(profile.nickname + " wants to chat");
                                        System.out.println("Type /accept or /decline");
                                    }
                                },
                                new UDPCallback() {
                                    @Override
                                    public void invoke(InetAddress address, Protocol protocol, String data) {
                                        send(address, Protocol.Status.ERROR);
                                    }
                                }
                        );
                        break;
                    case ACCEPT:
                        System.out.println(Client.requestedClient.nickname + " accepted your invite!");
                    case DECLINE:
                        System.out.println(Client.requestedClient.nickname + " declined your invite!");
                }
            }
        };
    }

    public void send(InetAddress address, Protocol.Status status) { UDP.awaitSend(address, status); }
    public void send(InetAddress address, Protocol.Status status, String message) { UDP.awaitSend(address, status, message); }

    public boolean connect(String serverIP) {
        try {
            InetAddress temp = InetAddress.getByName(serverIP);
            UDP.awaitSend(temp, Protocol.Status.ONLINE,
                    new UDPCallback() {
                        @Override
                        public void invoke(InetAddress address, Protocol protocol, String data) {
                            System.out.println("Connected!");
                            ServerConnection.serverAddress = temp;
                        }
                    },
                    new UDPCallback() {
                        @Override
                        public void invoke(InetAddress address, Protocol protocol, String data) { System.out.println("Invalid server IP"); }
                    }
            );

        } catch (Exception e) {
            System.out.println(e);
            return false;
        }

        return (serverAddress != null);
    }

    public void printDirectory() {
        try {
            UDP.awaitSend(serverAddress, Protocol.Status.QUERY,
                    new UDPCallback() {
                        @Override
                        public void invoke(InetAddress address, Protocol protocol, String data) {
                            UDP.awaitReceive(serverAddress,
                                    new UDPCallback() {
                                        @Override
                                        public void invoke(InetAddress address, Protocol protocol, String data) {
                                            System.out.println(data);
                                            System.out.println("Type /join <user> to join a chat");
                                        }
                                    },
                                    new UDPCallback() {
                                        @Override
                                        public void invoke(InetAddress address, Protocol protocol, String data) { System.out.println("Error"); }
                                    }
                            );
                        }
                    },
                    null
            );
        } catch (Exception e) {
            System.out.println(e + " at " + e.getStackTrace()[0]);
        }
    }

    public void joinChat(String peerIP) {
        try {
            UDP.awaitSend(serverAddress, Protocol.Status.JOIN, peerIP);
            UDP.awaitReceive(serverAddress,
                    new UDPCallback() {
                        @Override
                        public void invoke(InetAddress address, Protocol protocol, String data) {
                            try {
                                TCPServerProfile = Profile.parse(data);
                            } catch (Exception e) {
                                System.out.println(e);
                            }

                            System.out.println("Waiting for " + TCPServerProfile.nickname + " to accept");
                        }
                    },
                    null
            );
        } catch (Exception e) {
            System.out.println(e + " at " + e.getStackTrace()[0]);
        }
    }
}
