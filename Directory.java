import java.net.InetAddress;
import java.util.HashMap;

/**
 * Data class that holds all the information the server needs for a chatroom
 */

// TODO: Calculate popularity within users 1 hour log-in time
public class Directory {
    HashMap<InetAddress, Profile> list = new HashMap<>();
    HashMap<String, InetAddress> usernames = new HashMap<>();

    public InetAddress getAddress(String key) {
        try {
            InetAddress address = InetAddress.getByName(key);
            if (list.containsKey(address)) { return address; }
            if (usernames.containsKey(key)) { return usernames.get(key); }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public Profile getProfile(String key) {
        try {
            InetAddress address = InetAddress.getByName(key);
            if (list.containsKey(address)) { return list.get(address); }
            if (usernames.containsKey(key)) { return list.get(usernames.get(key)); }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    public void add(InetAddress address, Protocol protocol) {
        Profile profile = new Profile(address, protocol);
        list.put(address, profile);
        usernames.put(protocol.nickName, address);
    }

    public void remove(InetAddress address, Protocol protocol) {
        list.remove(address);
        usernames.remove(protocol.nickName);
    }

    public void parse(String input) {
        list = new HashMap<>();
        usernames = new HashMap<>();

        String[] profiles = input.split("\n");
        for (String s : profiles) {
            try {
                String[] param = s.split("\t");
                String username = param[0];
                InetAddress IP = InetAddress.getByName(param[1]);
                String chatName = param[2];
                int popularity = Integer.parseInt(param[3]);

                list.put(IP, new Profile(username, IP, chatName, popularity));
                usernames.put(username, IP);
            } catch (Exception e) {
                continue;
            }
        }
    }

    public String getDirectory() {
        StringBuilder result = new StringBuilder();
        for (Profile c : list.values()) {
            result.append(c.getRecord() + '\n');
        }

        return result.toString();
    }

    public String print() {
        StringBuilder result = new StringBuilder("User\t\t\tIP\t\t\t\t\tChatroom\t\t\tPopularity\n");
        result.append(getDirectory());
        return result.toString();
    }
}

class Profile {
    String nickname;
    InetAddress IP;
    String chatName;
    int activeUsers;

    //LocalDateTime login;

    public Profile(InetAddress address, Protocol protocol) { this(protocol.nickName, address); }
    public Profile(String nickname, InetAddress IP) { this(nickname, IP, "", 0); }
    public Profile(String nickname, InetAddress IP, String chatName, int activeUsers) {
        this.nickname = nickname;
        this.IP = IP;
        this.chatName = chatName;
        this.activeUsers = activeUsers;

        //this.login = LocalDateTime.now();
    }

    public String getRecord() {
        return nickname + "\t\t\t" +
        IP + "\t\t" +
        (chatName.equals("") ? "None" : chatName) + "\t\t\t\t" +
        (Server.totalUsers == 0 ? 0 : ((float)activeUsers / Server.totalUsers));
    }

    public static Profile parse (String input) {
        try {
            String[] param = input.split("\t");
            String username = param[0];
            InetAddress IP = InetAddress.getByName(param[1]);
            String chatName = param[2];
            int popularity = Integer.parseInt(param[3]);

            return new Profile(username, IP, chatName, popularity);
        } catch (Exception e) {

        }
        return null;
    }
}