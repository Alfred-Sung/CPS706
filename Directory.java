import java.net.InetAddress;
import java.util.HashMap;

/**
 * Data class that holds all the information the server needs for a chatroom
 */

// TODO: Calculate popularity within users 1 hour log-in time
public class Directory {
    //IP, profile
    HashMap<String, Profile> list = new HashMap<>();
    // Username, IP
    HashMap<String, String> usernames = new HashMap<>();

    public InetAddress getAddress(String key) {
        try {
            if (list.containsKey(key)) { return list.get(key).IP; }
            if (usernames.containsKey(key)) { return list.get(usernames.get(key)).IP; }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public Profile getProfile(String key) {
        try {
            //InetAddress address = InetAddress.getByName(key);
            //if (list.containsKey(address)) { return list.get(address); }
            if (usernames.containsKey(key)) { return list.get(usernames.get(key)); }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public void add(InetAddress address, Protocol protocol) {
        Profile profile = new Profile(address, protocol);
        list.put(address.toString(), profile);
        usernames.put(protocol.nickName, address.toString());
    }

    public void remove(InetAddress address, Protocol protocol) {
        list.remove(address);
        usernames.remove(protocol.nickName);
    }

    public String print() {
        StringBuilder result = new StringBuilder("User\t\t\t" +
                "Hostname\t\t\t\t\t" +
                "IP\t\t\t\t\t" +
                "Chatroom\t\t\t" +
                "Popularity" +
                "\n");
        for (Profile c : list.values()) { result.append(c.print() + '\n'); }

        return result.toString();
    }
}

class Profile {
    String nickname;
    String hostname;
    InetAddress IP;
    String chatName;
    int activeUsers;

    //LocalDateTime login;

    public Profile(InetAddress address, Protocol protocol) { this(protocol.nickName, protocol.hostName, address); }
    public Profile(String nickname, String hostname, InetAddress IP) { this(nickname, hostname, IP, "", 0); }
    public Profile(String nickname, String hostname, InetAddress IP, String chatName, int activeUsers) {
        try {
            this.nickname = nickname;
            this.hostname = hostname;
            this.IP = IP;
            this.chatName = chatName;
            this.activeUsers = activeUsers;

            //this.login = LocalDateTime.now();
        } catch (Exception e) {}
    }

    public String print() {
        return nickname + "\t\t\t" +
        IP + "\t\t" +
        (chatName.equals("") ? "None" : chatName) + "\t\t\t\t" +
        (Server.totalUsers == 0 ? 0 : ((float)activeUsers / Server.totalUsers));
    }

    public String getData() {
        return nickname + '\t' +
                hostname + '\t' +
                IP.getAddress() + '\t' +
                chatName+ '\t' +
                (Server.totalUsers == 0 ? 0 : ((float)activeUsers / Server.totalUsers));
    }

    public static Profile parse (String input) {
        try {
            String[] param = input.split("\t");

            String username = param[0];
            String hostname = param[1];
            String address = param[2];
            String chatName = param[3];
            int popularity = Integer.parseInt(param[4]);

            return new Profile(username, hostname, InetAddress.getByAddress(hostname, address.getBytes()), chatName, popularity);
        } catch (Exception e) {

        }
        return null;
    }

}