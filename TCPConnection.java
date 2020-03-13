import java.net.*;
import java.io.*;
import java.util.*;

// TODO: Everything
class P2PClient extends TCPConnection {
    TCPThread thread;

    public P2PClient(InetAddress serverIP) throws Exception {
        super();
        thread = new TCPThread(new Socket(serverIP, Connection.PORT));
        thread.start();
    }

    @Override
    public void send(String message) { thread.send(message); }

    @Override
    public void exit() {
        super.exit();
        thread.close();
    }
}

class P2PServer extends TCPConnection {
    ServerSocket serverSocket;
    List<TCPThread> clients = new LinkedList<>();

    public P2PServer() throws Exception {
        super();
        this.start();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(Connection.PORT);
            while (true) {
                TCPThread thread = new TCPThread(serverSocket.accept());
                clients.add(thread);
                thread.start();
                Connection.log("Thread created : total " + clients.size());
            }
        } catch (Exception e) {}
    }

    @Override
    public void send(String message) { if (clients.size() > 0) { handle(message); } }

    @Override
    public void exit() {
        super.exit();
        for (TCPThread thread : clients) { thread.close(); }
    }

    @Override
    public void handle(String message) {
        super.handle(message);
        propagate(message);
    }

    public void propagate(String message) { for (TCPThread thread : clients) { thread.send(message); } }
}

public abstract class TCPConnection extends Thread {
    public static TCPConnection instance;

    public TCPConnection() { instance = this; }
    public abstract void send(String message);
    public void exit() { send(Connection.nickName + " has left the chat"); }
    public void handle(String message) { System.out.println(message); }
}

class TCPThread extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public TCPThread(Socket socket) throws Exception {
        this.socket = socket;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void run() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                TCPConnection.instance.handle(inputLine);
            }
        } catch (Exception e) {}
        Connection.log("TCP Thread closed");
    }

    public void send(String message) { out.println(message); }

    public void close() {
        try {
            send(null);
            in.close();
            out.close();
            socket.close();

        } catch (Exception e) {}
    }
}