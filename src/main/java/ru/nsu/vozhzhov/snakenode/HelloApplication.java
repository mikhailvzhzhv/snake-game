package ru.nsu.vozhzhov.snakenode;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ru.nsu.vozhzhov.snakenode.peer.GameCommunicationSocket;
import ru.nsu.vozhzhov.snakenode.peer.MulticastReceiverSocket;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Thread t1 = new Thread(new MulticastReceiverSocket());
        Thread t2 = new Thread(new GameCommunicationSocket());

        t1.start();
        t2.start();

        Scene scene = new Scene(new Pane(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}