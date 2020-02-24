import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Chat commands can be anything but the parameters need to be of type String for some reason
 * Must be in the form:
 *      @Command(parameters = {"arg0", "arg1", ..}, description = "Lorem ipsum dolor sit amet")
 *      static void MethodName(String arg0, String arg1, ..) {
 */
@Retention(RetentionPolicy.RUNTIME)
@interface Command { String[] parameters(); String description(); }
public class ChatCommands {
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