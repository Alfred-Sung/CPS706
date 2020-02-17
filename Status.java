import java.util.HashMap;

public enum Status {
    // General status codes
    OK(200), ERROR(400), BADENCRYPT(600),
    // Client to Server status codes
    ONLINE(1), OFFLINE(2), JOIN(3), EXIT(4), QUERY(5),
    // Client to Client status codes
    ACCEPT(6), MESSAGE(7), POST(8);

    private static HashMap<Integer, Status> map = new HashMap<Integer, Status>();
    static {
        for (Status status : Status.values()) {
            map.put(status.value, status);
        }
    }
    public static Status valueOf(int value) { return (Status) map.get(value);  }

    private final int value;
    Status(int value) { this.value = value; }
    public int getValue() { return value; }
}
