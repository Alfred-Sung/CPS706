public class Protocol {
    public final int LENGTH = 80;
    Status status;
    boolean last;

    String hostName;
    String nickName;

    public Protocol(Status status) {
        this.status = status;
    }
    public Protocol(byte[] data) {}

    public byte[] getBytes(){
        byte[] result = new byte[LENGTH];

        return result;
    }
}
