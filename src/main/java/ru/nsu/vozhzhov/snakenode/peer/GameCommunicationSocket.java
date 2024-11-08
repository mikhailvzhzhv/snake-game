package ru.nsu.vozhzhov.snakenode.peer;

import ru.nsu.vozhzhov.snakenode.proto.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class GameCommunicationSocket implements Runnable {
    private final static int mcport = 9192;
    private final static String mcip = "239.192.0.4";

    private final DatagramSocket socket;

    public GameCommunicationSocket() throws SocketException {
        socket = new DatagramSocket();
    }

    public void run() {


    }

    private void sendMessage(SnakesProto.GameMessage gameMessage, String host, int port) {
        byte[] messageBytes = gameMessage.toByteArray();

        DatagramPacket packet = null;
        try {
            packet = new DatagramPacket(messageBytes, messageBytes.length, InetAddress.getByName(host), port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMasterGame() {
        SnakesProto.GamePlayer player = SnakesProto.GamePlayer.newBuilder()
                .setName("Player1")
                .setId(1)
                .setRole(SnakesProto.NodeRole.NORMAL)
                .setScore(0)
                .build();

        SnakesProto.GamePlayers players = SnakesProto.GamePlayers.newBuilder()
                .addPlayers(player)
                .build();

        SnakesProto.GameConfig config = SnakesProto.GameConfig.newBuilder()
                .setWidth(40)
                .setHeight(30)
                .setFoodStatic(1)
                .setStateDelayMs(1000)
                .build();

        SnakesProto.GameAnnouncement gameAnnouncement = SnakesProto.GameAnnouncement.newBuilder()
                .setPlayers(players)
                .setConfig(config)
                .setCanJoin(true)
                .setGameName("my game")
                .build();

        SnakesProto.GameMessage.AnnouncementMsg announcementMsg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
                .addGames(gameAnnouncement)
                .build();

        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(1)
                .setAnnouncement(announcementMsg)
                .build();

        sendMessage(gameMessage, mcip, mcport);
    }
}
