package ru.nsu.vozhzhov.snakenode.controller;

import ru.nsu.vozhzhov.snakenode.GUI.GUIView;
import ru.nsu.vozhzhov.snakenode.peer.GameCommunicationSocket;
import ru.nsu.vozhzhov.snakenode.peer.MulticastReceiverSocket;
import ru.nsu.vozhzhov.snakenode.proto.SnakesProto;

import java.net.InetAddress;

public class GUIController implements Listener {
    private final GUIView view;
    private final GameCommunicationSocket communicationSocket;
    private final MulticastReceiverSocket multicastSocket;

    public GUIController(
            GUIView view,
            GameCommunicationSocket communicationSocket,
            MulticastReceiverSocket multicastSocket
    ) {
        this.view = view;
        this.communicationSocket = communicationSocket;
        this.communicationSocket.addListener(this);
        this.multicastSocket = multicastSocket;
        this.multicastSocket.addListener(this);
        setActionsControls();
    }

    private void setActionsControls() {
        view.getGameScene().setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT -> communicationSocket.sendDirection(SnakesProto.Direction.LEFT, view.getContext());
                case RIGHT -> communicationSocket.sendDirection(SnakesProto.Direction.RIGHT, view.getContext());
                case UP -> communicationSocket.sendDirection(SnakesProto.Direction.UP, view.getContext());
                case DOWN -> communicationSocket.sendDirection(SnakesProto.Direction.DOWN, view.getContext());
                default -> communicationSocket.sendDirection(null, view.getContext());
            }
        });

         view.getStartGameButton().setOnMouseClicked(mouseEvent -> {
             communicationSocket.createGame();
             view.setGameScene();
         });
    }
    
    public synchronized void onModelChange(
            SnakesProto.GameMessage gameMessage,
            InetAddress address,
            int port,
            Receive receive
    ) {
        switch (receive) {
            case ALL -> view.updMenuScreen(gameMessage, address, port);
            case ANY -> view.updGameScreen(gameMessage);
        }
    }

    public void startGame() {
        view.show();
    }
}