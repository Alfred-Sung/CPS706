import java.lang.annotation.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;

/**
 * Client-side script that only handles application-level processes
 * ie. IO handling, chat commands, etc.
 */
// TODO: Everything
// TODO: Client-server interactions
// TODO: Create ClientConnection once server connection is established
// TODO: Finish chat command methods
public class Client {
    static ClientConnection client;
    static ServerConnection server;

    public static void main(String[] args) throws Exception {
        server = new ServerConnection();
        Scanner input = new Scanner(System.in);

        System.out.println("Welcome to Chat");

        String serverIP = "";
        boolean validServer = false;
        do {
            do {
                System.out.print("Enter server IP: ");
                serverIP = input.nextLine();
            } while (serverIP.equals(""));

            do {
                System.out.print("Enter nickname: ");
                Connection.nickName = input.nextLine();
            } while (Connection.nickName.equals(""));

            validServer = server.connect(serverIP);
            System.out.println(validServer ? "Server connected!" : "Bad server IP");
        } while (!validServer);

        System.out.println(server.getDirectory());

        while (true) {
            String query;
            query = input.nextLine();

            if (query.charAt(0) == ChatCommands.COMMANDCHAR) {
                String[] command = query.substring(1, query.length()).split(" ");

                Method method = ChatCommands.queryChatCommand(command[0]);
                if (method != null) {
                    ChatCommands.execute(method, Arrays.copyOfRange(command, 1, command.length));
                } else {
                    System.out.println("Invalid command!");
                }
            } else {

            }
        }
    }
}