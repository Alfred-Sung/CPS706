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
                System.out.println(e + " at " + e.getStackTrace()[0]);
                return false;
            }
        }

        return true;
    }

    public static boolean send(Protocol.Status status, InetAddress address, String message) {
        int failed = 0;
        Protocol[] outbound = Protocol.create(status, message);
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

    public static String receive() throws Exception {
        InetAddress address;

        DatagramPacket packet = new DatagramPacket(new byte[Protocol.LENGTH], Protocol.LENGTH);
        socket.receive(packet);
        address = packet.getAddress();
        send(Protocol.Status.OK, address);

        int packets = Integer.parseInt(Protocol.create(packet.getData()).data);
        Protocol[] fragments = new Protocol[packets];

        for (int i = 0; i < packets; i++) {
            packet = new DatagramPacket(new byte[Protocol.LENGTH], Protocol.LENGTH);
            socket.receive(packet);
            fragments[i] = Protocol.create(packet.getData());

            System.out.println(fragments[i].toString());

            send(Protocol.Status.OK, address);
        }

        return Protocol.constructData(fragments);
    }
}