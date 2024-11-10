package ru.nsu.vozhzhov.snakenode;

import javafx.application.Application;
import javafx.css.converter.StopConverter;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ru.nsu.vozhzhov.snakenode.GUI.GUIView;
import ru.nsu.vozhzhov.snakenode.controller.GUIController;
import ru.nsu.vozhzhov.snakenode.model.ModelController;
import ru.nsu.vozhzhov.snakenode.peer.GameCommunicationSocket;
import ru.nsu.vozhzhov.snakenode.peer.MulticastReceiverSocket;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        GUIView view = new GUIView(stage);
        MulticastReceiverSocket multicastReceiverSocket = new MulticastReceiverSocket();
        GameCommunicationSocket communicationSocket = new GameCommunicationSocket();
        GUIController guiController = new GUIController(view, communicationSocket, multicastReceiverSocket);

        Thread t1 = new Thread(multicastReceiverSocket);
        Thread t2 = new Thread(communicationSocket);

        t1.start();
        t2.start();

        communicationSocket.createGame();

//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        guiController.startGame();
    }

    public static void main(String[] args) {
        launch();
    }
}