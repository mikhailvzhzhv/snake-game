package ru.nsu.vozhzhov.snakenode.peer;

import ru.nsu.vozhzhov.snakenode.GUI.Context;
import ru.nsu.vozhzhov.snakenode.controller.Listener;
import ru.nsu.vozhzhov.snakenode.controller.Receive;
import ru.nsu.vozhzhov.snakenode.model.ModelController;
import ru.nsu.vozhzhov.snakenode.proto.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameCommunicationSocket implements Runnable {
    private final static int mcport = 9192;
    private final static String mcip = "239.192.0.4";

    private final DatagramSocket socket;
    private ModelController model;
    private Listener listener;
    private final ScheduledExecutorService scheduledThreadPoolExecutor;
    private int msgSeq;

    public GameCommunicationSocket() throws SocketException {
        socket = new DatagramSocket();
        scheduledThreadPoolExecutor = Executors.newScheduledThreadPool(1);
        msgSeq = 1;
    }

    public void run() {
        int bufSize = 1024;
        SnakesProto.GameMessage gameMessage;

        while (!Thread.interrupted()) {
            DatagramPacket packet = new DatagramPacket(new byte[bufSize], bufSize);
            try {
                socket.receive(packet);
                byte[] receivedData = Arrays.copyOf(packet.getData(), packet.getLength());
                gameMessage = SnakesProto.GameMessage.parseFrom(receivedData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (listener != null) {
                listener.onModelChange(gameMessage, packet.getAddress(), packet.getPort(), Receive.ANY);
            }
        }

        scheduledThreadPoolExecutor.shutdownNow();
        System.out.println("GameCommunicationSocket finished");
    }

    public void sendMessage(SnakesProto.GameMessage gameMessage, String host, int port) {
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

    public void sendDirection(SnakesProto.Direction direction, Context ctx) {
        SnakesProto.GameMessage.SteerMsg steerMsg = SnakesProto.GameMessage.SteerMsg.newBuilder()
                .setDirection(direction)
                .build();

        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                .setSteer(steerMsg)
                .build();

        sendMessage(gameMessage, ctx.getIp(), ctx.getPort());
    }

    public void createGame() {
        model = new ModelController();
        Runnable sendAnnouncementTask = () -> {
            SnakesProto.GameState gameState = model.getGameState();
            SnakesProto.GameConfig gameConfig = model.getGameConfig();

            SnakesProto.GameAnnouncement gameAnnouncement = SnakesProto.GameAnnouncement.newBuilder()
                    .setPlayers(gameState.getPlayers())
                    .setConfig(gameConfig)
                    .setCanJoin(true)
                    .setGameName("my game")
                    .build();

            SnakesProto.GameMessage.AnnouncementMsg announcementMsg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
                    .addGames(gameAnnouncement)
                    .build();

            SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
                    .setMsgSeq(msgSeq++)
                    .setAnnouncement(announcementMsg)
                    .build();

            sendMessage(gameMessage, mcip, mcport);
        };
        scheduledThreadPoolExecutor.scheduleAtFixedRate(sendAnnouncementTask, 0, 1, TimeUnit.SECONDS);
    }

    public synchronized void addListener(Listener listener) {
        this.listener = listener;
    }

//    private void sendMasterGame() {
//        SnakesProto.GamePlayer player = SnakesProto.GamePlayer.newBuilder()
//                .setName("Player1")
//                .setId(1)
//                .setRole(SnakesProto.NodeRole.NORMAL)
//                .setScore(0)
//                .build();
//
//        SnakesProto.GamePlayers players = SnakesProto.GamePlayers.newBuilder()
//                .addPlayers(player)
//                .build();
//
//        SnakesProto.GameConfig config = SnakesProto.GameConfig.newBuilder()
//                .setWidth(40)
//                .setHeight(30)
//                .setFoodStatic(1)
//                .setStateDelayMs(1000)
//                .build();
//
//        SnakesProto.GameAnnouncement gameAnnouncement = SnakesProto.GameAnnouncement.newBuilder()
//                .setPlayers(players)
//                .setConfig(config)
//                .setCanJoin(true)
//                .setGameName("my game")
//                .build();
//
//        SnakesProto.GameMessage.AnnouncementMsg announcementMsg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
//                .addGames(gameAnnouncement)
//                .build();
//
//        SnakesProto.GameMessage gameMessage = SnakesProto.GameMessage.newBuilder()
//                .setMsgSeq(1)
//                .setAnnouncement(announcementMsg)
//                .build();
//
//        sendMessage(gameMessage, mcip, mcport);
//    }
}
