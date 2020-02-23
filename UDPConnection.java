import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

/**
 * Handles sending, splitting and reconstructing protocol packets
 */

// TODO: Handle resending packets
public abstract class UDPConnection extends Connection {
    public static boolean send(Protocol.Status status, InetAddress address) {
        for (int i = 0; i < MAXREPEAT; i++) {
            try {
                Protocol protocol = Protocol.create(status);

                DatagramPacket packet = new DatagramPacket(protocol.getBytes(), Protocol.LENGTH, address, PORT);
                socket.send(packet);

                socket.setSoTimeout(TIMEOUT);

                packet = new DatagramPacket(new byte[Protocol.LENGTH], Protocol.LENGTH);
                socket.receive(packet);
                Protocol response = Protocol.create(packet.getData());

                if (response.status == Protocol.Status.OK) { return true; }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    public static boolean send(Protocol.Status status, InetAddress address, String message) {
        int failed = 0;
        Protocol[] outbound = Protocol.create(status, message);
        System.out.println(outbound.length);
        for (int i = 0; i < outbound.length; i++) {
            try {
                DatagramPacket packet = new DatagramPacket(outbound[i].getBytes(), Protocol.LENGTH, address, Server.PORT);
                System.out.println("Sent packet");
                socket.send(packet);

                socket.setSoTimeout(TIMEOUT);

                packet = new DatagramPacket(new byte[Protocol.LENGTH], Protocol.LENGTH);
                socket.receive(packet);

                Protocol response = Protocol.create(packet.getData());

                switch (response.status) {
                    case OK:
                        break;
                    case ERROR:
                        return false;
                }
            } catch (Exception e) {
                i--;
                failed++;
                if (failed > MAXREPEAT) { return false; }
            }
        }

        return true;
    }

    public static String receive() throws Exception{
        DatagramPacket packet = new DatagramPacket(new byte[Protocol.LENGTH], Protocol.LENGTH);
        socket.receive(packet);
        send(Protocol.Status.OK, packet.getAddress());
        Protocol inbound = Protocol.create(packet.getData());

        int packets = Integer.parseInt(inbound.data);
        Protocol[] fragments = new Protocol[packets];
        for (int i = 0; i < packets - 1; i++) {
            socket.receive(packet);
            send(Protocol.Status.OK, packet.getAddress());
        }

        return Protocol.constructData(fragments);
    }
}