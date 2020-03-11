import java.net.*;
import java.util.HashMap;

/**
 * Client-side script that handles client-server communication
 */
public class ServerConnection extends Connection {
    static InetAddress serverAddress;
    private static UDPConnection UDP;

    private static Profile TCPServerProfile;
    static UDPCallback response = new UDPCallback() {
        @Override
        public void invoke(InetAddress address, Protocol protocol, String data) {
            switch (protocol.status) {
                case JOIN:
                    Profile profile = Profile.parse(data);
                    Client.requestedClient = profile.IP;

                    System.out.println(profile.nickname + " wants to chat");
                    System.out.println("Type /accept or /decline");
                    break;
                default:
                    UDP.send(address, Protocol.Status.ERROR);
                    break;
            }
        }
    };

    public ServerConnection() {
        VERBOSE = true;

        UDP = new UDPConnection() {
            @Override
            public void keyNotFound(InetAddress address, Protocol protocol) {
                if (address.equals(serverAddress)) {
                    receive(address, protocol,
                            response,
                            new UDPCallback() {
                                @Override
                                public void invoke(InetAddress address, Protocol protocol, String data) {
                                    send(address, Protocol.Status.ERROR);
                                }
                            }
                    );
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
            UDP.awaitSend(serverAddress, Protocol.Status.QUERY);
            UDP.awaitReceive(serverAddress,
                    new UDPCallback() {
                        @Override
                        public void invoke(InetAddress address, Protocol protocol, String data) { System.out.println(data); }
                    },
                    new UDPCallback() {
                        @Override
                        public void invoke(InetAddress address, Protocol protocol, String data) { System.out.println("Error"); }
                    }
            );
        } catch (Exception e) {
            System.out.println(e + " at " + e.getStackTrace()[0]);
        }
        System.out.println("Type /join <user> to join a chat");
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

                            UDP.awaitReceive(serverAddress,
                                    new UDPCallback() {
                                        @Override
                                        public void invoke(InetAddress address, Protocol protocol, String data) {
                                            switch(protocol.status) {
                                                // TODO: Pass IP to TCPConnection
                                                case ACCEPT:
                                                    break;
                                                case DECLINE:
                                                    break;
                                            }
                                        }
                                    },
                                    null
                            );
                        }
                    },
                    null
            );
        } catch (Exception e) {
            System.out.println(e + " at " + e.getStackTrace()[0]);
        }
    }
}
