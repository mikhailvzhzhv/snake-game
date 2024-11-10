package ru.nsu.vozhzhov.snakenode.peer;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.nsu.vozhzhov.snakenode.controller.Listener;
import ru.nsu.vozhzhov.snakenode.controller.Receive;
import ru.nsu.vozhzhov.snakenode.proto.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class MulticastReceiverSocket implements Runnable {
    private final static int port = 9192;
    private final static String ip = "239.192.0.4";
    private final MulticastSocket udpSocket;
    private Listener listener;

    public MulticastReceiverSocket() throws IOException {
        udpSocket = new MulticastSocket(port);
        InetAddress mcastaddr = InetAddress.getByName(ip);
        udpSocket.joinGroup(new InetSocketAddress(mcastaddr, 0), null);
    }

    public void run() {
        int bufSize = 4096;

        while (!Thread.interrupted()) {
            DatagramPacket packet = new DatagramPacket(new byte[bufSize], bufSize);
            try {
                udpSocket.receive(packet);
                System.out.println(packet.getAddress());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (listener != null) {
                try {
                    byte[] receivedData = Arrays.copyOf(packet.getData(), packet.getLength());
                    SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.parseFrom(receivedData);
                    listener.onModelChange(gameMessage, packet.getAddress(), packet.getPort(), Receive.ALL);
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public synchronized void addListener(Listener listener) {
        this.listener = listener;
    }
}
