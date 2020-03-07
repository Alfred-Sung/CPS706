import java.io.*;
import java.net.*;

/**
 * Client-side script that handles client-client communication
 * ClientServer is created by ClientConnection when user accepts to chat with peers
 * Handles forwarding messages from peers to all users in a chat room
 */
// TODO: Everything
public class ClientConnection extends Connection {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void ClientConnection(String ip) {
        clientSocket = new Socket(ip, PORT);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public String sendMessage(String msg) {
        out.println(msg);
        String resp = in.readLine();
        return resp;
    }

    public void stopConnection() {
        in.close();
        out.close();
        clientSocket.close();
    }

    public void startServer(int port) {
        serverSocket = new ServerSocket(port);
        while (true)
            new TCPThread(serverSocket.accept()).start();
    }

    public void stopServer() {
        serverSocket.close();
    }
}

class TCPThread extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public TCPThread(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            if (".".equals(inputLine)) {
                out.println("bye");
                break;
            }
            out.println(inputLine);
        }

        in.close();
        out.close();
        clientSocket.close();
    }
}