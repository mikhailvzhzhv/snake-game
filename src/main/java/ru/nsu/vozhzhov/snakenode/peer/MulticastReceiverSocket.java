package ru.nsu.vozhzhov.snakenode.peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;

public class MulticastReceiverSocket implements Runnable {
    private final static int port = 9192;
    private final static String ip = "239.192.0.4";
    private final MulticastSocket udpSocket;

    public MulticastReceiverSocket() throws IOException {
        udpSocket = new MulticastSocket(port);
        InetAddress mcastaddr = InetAddress.getByName(ip);
        udpSocket.joinGroup(new InetSocketAddress(mcastaddr, 0), null);
    }

    public void run() {
        int bufSize = 4096;
        int x = 0;

        while (x == 0) {
            x = 1;
            DatagramPacket packet = new DatagramPacket(new byte[bufSize], bufSize);
            try {
                udpSocket.receive(packet);
                System.out.println(packet.getAddress());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
