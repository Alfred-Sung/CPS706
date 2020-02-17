import java.util.HashMap;

/**
 * HTTP-like protocol that is attached to every packet sent; client-client and client-server
 */
// TODO: Implement fragmented packets
public class Protocol {
    public final int LENGTH = 80;
    Status status;
    boolean last;

    String hostName;
    String nickName;

    String data;

    public Protocol(Status status) {
        this.status = status;
    }
    public Protocol(byte[] data) {}

    public byte[] getBytes(){
        byte[] result = new byte[LENGTH];

        return result;
    }

    /**
     * Status codes that the packet protocols use
     * Each status contains a unique number
     */
    public enum Status {
        // General status codes
        OK(200), ERROR(400), BADENCRYPT(600),
        // Client to Server status codes
        ONLINE(1), OFFLINE(2), JOIN(3), EXIT(4), QUERY(5),
        // Client to Client status codes
        ACCEPT(6), DECLINE(7), MESSAGE(8), POST(9);

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
}
