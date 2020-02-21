import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

/**
 * Handles sending, splitting and reconstructing protocol packets
 */

// TODO: Handle resending packets
public class UDPConnection extends Connection {
    public static String send(Protocol.Status status, InetAddress address) { return null; }

    public static String send(Protocol.Status status, InetAddress address, String message) throws Exception {
        int failed = 0;
        Protocol[] outbound = Protocol.create(status, hostName, nickName, message);

        for (int i = 0; i < outbound.length; i++) {
            try {
                DatagramPacket packet = new DatagramPacket(outbound[i].getBytes(), Protocol.LENGTH, address, Server.PORT);
                socket.send(packet);

                socket.setSoTimeout(TIMEOUT);

                packet = new DatagramPacket(new byte[Protocol.LENGTH], Protocol.LENGTH);
                socket.receive(packet);

                Protocol response = Protocol.create(packet.getData());

                switch (response.status) {
                    case OK:
                        break;
                    case ERROR:
                        break;
                }
            } catch (Exception e) {
                i--;
                if ((failed++) > MAXREPEAT) { throw new TimeoutException(); }
            }
        }

        return null;
    }
}
