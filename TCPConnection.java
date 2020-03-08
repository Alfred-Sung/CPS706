import java.net.*;
import java.io.*;
import java.util.*;

class P2PClient extends TCPConnection {
    TCPThread thread;

    public P2PClient(InetAddress serverIP) {
        super();
        thread = new TCPThread(new Socket(serverIP, Connection.PORT));
    }

    @Override
    public void send(String message) {

    }

    @Override
    public void exit() {

    }
}

class P2PServer extends TCPConnection {
    ServerSocket serverSocket;
    List<TCPThread> clients = new LinkedList<>();

    public P2PServer() {
        super();

        serverSocket = new ServerSocket(Connection.PORT);
        while (true) {
            TCPThread thread = new TCPThread(serverSocket.accept());
            clients.add(thread);
            thread.start();
        }
    }

    @Override
    public void send(String message) {

    }

    @Override
    public void exit() {

    }

    @Override
    public void handle(String message) {
        super.handle(message);
        propogate(message);
    }

    public void propogate(String message) { for (TCPThread thread : clients) { thread.send(message); } }
}

public abstract class TCPConnection extends Thread {
    public static TCPConnection instance;

    public TCPConnection() { instance = this; }
    public abstract void send(String message);
    public abstract void exit();
    public void handle(String message) { System.out.println(message); }
}

class TCPThread extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public TCPThread(Socket socket) {
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void run() {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            TCPConnection.instance.handle(inputLine);
        }
    }

    public void send(String message) { out.println(message); }

    public void close() {
        in.close();
        out.close();
        socket.close();
    }
}