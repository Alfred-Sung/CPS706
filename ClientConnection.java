import java.io.*;
import java.net.*;

/**
 * Client-side script that handles client-client communication
 * ClientServer is created by ClientConnection when user accepts to chat with peers
 * Handles forwarding messages from peers to all users in a chat room
 */

public class ClientConnection extends Connection {
    private final TCPConnection TCP;

    public ClientConnection(InetAddress IP) throws Exception { TCP = new P2PClient(IP); }
    public ClientConnection() throws Exception { TCP = new P2PServer(); }

    public void send(String message) { TCP.send(Connection.nickName + "> " + message); }
    public void exit() { TCP.exit(); }
}