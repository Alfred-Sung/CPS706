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
public class Client extends Connection {
    static String nickname;

    static ClientConnection client;
    static ServerConnection server;

    public static void main(String[] args) {
        server = new ServerConnection();
        Scanner input = new Scanner(System.in);

        System.out.println("Welcome to Chat");

        while (server.serverAddress == null) {
            String serverIP = "";
            do {
                System.out.print("Enter server IP: ");
                serverIP = input.nextLine();
            } while (serverIP.equals(""));

            System.out.println(server.connect(serverIP) ? "Server connected!" : "Invalid server IP");
        }

        System.out.print("Enter nickname: ");
        nickname = input.nextLine();

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

/**
 * Chat commands can be anything but the parameters need to be of type String for some reason
 * Must be in the form:
 *      @Command(parameters = {"arg0", "arg1", ..}, description = "Lorem ipsum dolor sit amet")
 *      static void MethodName(String arg0, String arg1, ..) {
 */
@Retention(RetentionPolicy.RUNTIME)
@interface Command { String[] parameters(); String description(); }
class ChatCommands {
    static final char COMMANDCHAR = '/';
    static Method[] methods;
    static { methods = getChatCommands(); }

    static Method[] getChatCommands(){
        Class chatCommands = ChatCommands.class;
        Method[] allCommands = chatCommands.getDeclaredMethods();

        return Arrays.stream(allCommands)
                .filter(m -> m.isAnnotationPresent(Command.class))
                .toArray(Method[]::new);
    }

    static Method queryChatCommand(String command){
        Method method = Arrays.stream(methods)
                .filter(m -> m.getName().equals(command))
                .findAny()
                .orElse(null);
        return method;
    }

    static void execute(Method command, String[] arguments) {
        try {
            command.invoke(ChatCommands.class, Arrays.copyOf(arguments, arguments.length, Object[].class));
        } catch (Exception e) {
            System.out.println("Bad parameters use " + getMethodDescription(command));
        }
    }

    static String getMethodDescription(Method m) {
        String result = "";

        Command c = m.getAnnotation(Command.class);
        result += COMMANDCHAR + m.getName() + " ";
        for (String s : c.parameters()) { result += "<" + s + "> "; }
        result += "- ";
        result += c.description();

        return result;
    }

    // TODO: Must accept nickname or IP address
    @Command(parameters = {"name"}, description = "")
    static void join(String name) { System.out.println("User joined " + name + "'s chat!"); }

    @Command(parameters = {}, description = "")
    static void query() { System.out.println("Thing"); }

    @Command(parameters = {}, description = "")
    static void accept() { System.out.println("Thing"); }

    @Command(parameters = {}, description = "")
    static void decline() { System.out.println("Thing"); }

    @Command(parameters = {}, description = "")
    static void exit() { System.out.println("User exited chat!"); }

    @Command(parameters = {}, description = "")
    static void logoff() {
        // IMPORTANT: Must send 4 EXIT and 2 OFFLINE
    }

    @Command(parameters = {}, description = "Lists all available chat commands")
    static void help() {
        for (Method m : methods) { System.out.println(getMethodDescription(m));}
    }
}