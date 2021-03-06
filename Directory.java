import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Data class that holds all the information the server needs for a chatroom
 */

// TODO: Calculate connectedUsers within users 1 hour log-in time
public class Directory {
    public static int totalUsers;

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
        totalUsers++;
    }

    public void remove(InetAddress address, Protocol protocol) {
        list.remove(address);
        usernames.remove(protocol.nickName);
        totalUsers--;
    }

    public String print() {
        StringBuilder result = new StringBuilder("User\t\t\t" +
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
    int connectedUsers;

    LocalDateTime joinTime;

    public Profile(InetAddress address, Protocol protocol) { this(protocol.nickName, protocol.hostName, address); }
    public Profile(String nickname, String hostname, InetAddress IP) { this(nickname, hostname, IP, "", 0); }
    public Profile(String nickname, String hostname, InetAddress IP, String chatName, int connectedUsers) {
        try {
            this.nickname = nickname;
            this.hostname = hostname;
            this.IP = IP;
            this.chatName = chatName;
            this.connectedUsers = connectedUsers;
        } catch (Exception e) {}
    }

    public String print() {
        return nickname + "\t\t\t" +
                IP.getHostAddress() + "\t\t" +
                (chatName.equals("") ? "None" : chatName) + "\t\t\t\t" +
                ((float)connectedUsers / Directory.totalUsers);
    }

    public String getData() {
        return nickname + '\t' +
                hostname + '\t' +
                IP.getHostAddress() + '\t' +
                chatName + '\t' +
                connectedUsers;
    }

    public void setJoinTime() { this.joinTime = LocalDateTime.now(); }
    public void clearJoinTime() { this.joinTime = LocalDateTime.now(); }

    public static Profile parse (String input) {
        try {
            String[] param = input.split("\t");

            String username = param[0];
            String hostname = param[1];
            // Maybe slow
            InetAddress IP = InetAddress.getByName(param[2]);
            String chatName = param[3];
            int connectedUsers = Integer.parseInt(param[4]);

            return new Profile(username, hostname, IP, chatName, connectedUsers);
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

}