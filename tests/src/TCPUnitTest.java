import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;

public class TCPUnitTest {
    public static void main(String[] args) throws Exception {
        Connection.VERBOSE = true;
        ClientConnection clientConnection;
        Scanner input = new Scanner(System.in);

        String serverIP = "";
        System.out.print("Enter server IP: ");
        serverIP = input.nextLine();

        do {
            System.out.print("Enter nickname: ");
            Connection.nickName = input.nextLine();
        } while (Connection.nickName.equals(""));

        if (serverIP.equals("")) {
            clientConnection = new ClientConnection();
            System.out.println("Initialized as server");
        } else {
            InetAddress address = InetAddress.getByName(serverIP);
            clientConnection = new ClientConnection(address);
            System.out.println("Initialized as client");
        }

        while (true) {
            String query;
            query = input.nextLine();

            if (query.equals(ChatCommands.COMMANDCHAR + "exit")) {
                System.out.println("/exit found");
                clientConnection.exit();
            } else {
                clientConnection.send(query);
            }
        }
    }
}
