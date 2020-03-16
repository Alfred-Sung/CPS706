import java.nio.ByteBuffer;
import java.util.*;

/**
 * HTTP-like protocol
 * Handles splitting a message into 80 byte instances to be put in a UDP packet
 *
 * Leading packets' data field will always contain the number of subsequent packets
 */
public class Protocol {
    public static final int LENGTH = 80;
    public static final int DATALENGTH = 40;
    Status status; //4 bytes
    Integer sequence; //4 bytes

    String hostName; //16 bytes
    String nickName; //16 bytes

    String data; //40 bytes

    /**
     * Methods to create Protocol packets
     * Automatically handles splitting
     */
    public static Protocol[] create(Status status, int sequence) { return new Protocol[] { new Protocol(status, 0, Integer.toString(sequence)) }; }
    public static Protocol[] create(byte[] data) { return new Protocol[] { new Protocol(data) }; }
    public static Protocol[] create(Status status, String data) { return split(status, data); }

    /**
     * Splits and creates multiple protocol packets based on an input message
     */
    private static Protocol[] split(Status status, String data) {
        // Split data string into chunks of length DATALENGTH
        List<String> dataFragments = new LinkedList<>();
        for (int start = 0; start < data.length(); start += DATALENGTH) {
            dataFragments.add(data.substring(start, Math.min(data.length(), start + DATALENGTH)));
        }


        List<Protocol> fragments = new LinkedList<>();
        fragments.add(new Protocol(status, 0, String.valueOf(dataFragments.size())));
        for (int i = 0; i < dataFragments.size(); i++) {
            fragments.add(new Protocol(status, i + 1, dataFragments.get(i)));
        }

        return fragments.toArray(new Protocol[0]);
    }

    /**
     * Reconstructs a message from multiple Protocol packets
     */
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

        buffer.position(16);
        byte[] hostNameBytes = new byte[16];
        buffer.get(hostNameBytes);
        this.hostName = new String(hostNameBytes).trim();

        buffer.position(24);
        byte[] nickNameBytes = new byte[16];
        buffer.get(nickNameBytes);
        this.nickName = new String(nickNameBytes).trim();

        buffer.position(40);
        byte[] dataBytes = new byte[40];
        buffer.get(dataBytes);
        this.data = new String(dataBytes).trim();
    }

    public byte[] getBytes(){
        ByteBuffer result = ByteBuffer.allocate(LENGTH);
        result.position(0);
        result.putInt(status.getValue());

        result.position(4);
        result.putInt(sequence);

        result.position(8);
        result.put(hostName.getBytes());

        result.position(24);
        result.put(nickName.getBytes());

        result.position(40);
        result.put(data.getBytes());

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
        OK(200), ERROR(400), FINAL(7),
        // Client to Server status codes
        ONLINE(0), OFFLINE(1), JOIN(2), EXIT(3), QUERY(4), ACCEPT(5), DECLINE(6);

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