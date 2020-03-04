import java.nio.ByteBuffer;
import java.util.*;

/**
 * HTTP-like protocol that is attached to every packet sent between client-server
 */
// TODO: Implement fragmented packets
public class Protocol {
    public static final int LENGTH = 80;
    public static final int DATALENGTH = 40;
    Status status; //4 bytes
    Integer sequence; //4 bytes

    String hostName; //16 bytes
    String nickName; //16 bytes

    String data; //40 bytes

    public static Protocol[] create(Status status) { return new Protocol[] { new Protocol(status, 0, "0") }; }
    public static Protocol[] create(byte[] data) { return new Protocol[] { new Protocol(data) }; }
    public static Protocol[] create(Status status, String data) { return split(status, data); }

    private static Protocol[] split(Status status, String data) {
        // Split data string into chunks of length DATALENGTH
        List<String> dataFragments = new LinkedList<>();
        for (int start = 0; start < data.length(); start += DATALENGTH) {
            dataFragments.add(data.substring(start, Math.min(data.length(), start + DATALENGTH)));
        }


        List<Protocol> fragments = new LinkedList<>();
        fragments.add(new Protocol(status, 0, String.valueOf(dataFragments.size())));
        for (int i = 0; i < dataFragments.size(); i++) {
            System.out.println(dataFragments.get(i));
            fragments.add(new Protocol(status, i + 1, dataFragments.get(i)));
        }

        return fragments.toArray(new Protocol[0]);
    }

    public static String constructData(Protocol[] fragments) {
        StringBuilder result = new StringBuilder();
        for (Protocol frag : fragments) {
            if (frag.sequence == 0) { continue; }

            result.append(frag.data);
        }

        return result.toString();
    }

    private Protocol(Status status, int index, String data) {
        this.status = status;
        this.sequence = index;

        this.hostName = Connection.hostName.substring(0, Math.min(Connection.hostName.length(), 16));
        this.nickName = Connection.nickName.substring(0, Math.min(Connection.nickName.length(), 16));

        this.data = data.substring(0, Math.min(data.length(), 40));
    }

    private Protocol(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        this.status = Status.valueOf(buffer.getInt(0));
        this.sequence = buffer.getInt(4);

        byte[] hostNameBytes = new byte[16];
        buffer.get(8, hostNameBytes, 0, hostNameBytes.length);
        this.hostName = new String(hostNameBytes).trim();

        byte[] nickNameBytes = new byte[16];
        buffer.get(24, nickNameBytes, 0, nickNameBytes.length);
        this.nickName = new String(nickNameBytes).trim();

        byte[] dataBytes = new byte[40];
        buffer.get(40, dataBytes, 0, dataBytes.length);
        this.data = new String(dataBytes).trim();
    }

    public byte[] getBytes(){
        ByteBuffer result = ByteBuffer.allocate(LENGTH);
        result.putInt(0, status.getValue());
        result.putInt(4, sequence);

        result.put(8, hostName.getBytes());
        result.put(24, nickName.getBytes());

        result.put(40, data.getBytes());

        return result.array();
    }

    public String toString() {
        return status.value +  " " + status.toString() + " " +
                sequence + " " +
                hostName + " " +
                nickName + '\n' +
                data;
    }

    /**
     * Status codes that the packet protocols use
     * Each status contains a unique number
     */
    public enum Status {
        // General status codes
        OK(200), ERROR(400),
        // Client to Server status codes
        ONLINE(1), OFFLINE(2), JOIN(3), EXIT(4), QUERY(5);

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