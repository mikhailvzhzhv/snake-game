package ru.nsu.vozhzhov.snakenode.GUI;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;
import ru.nsu.vozhzhov.snakenode.proto.SnakesProto;

import java.net.InetAddress;

public class GUIView {
    private Stage stage;
    @Getter
    private final Context context;
    @Getter
    private Scene gameScene;
    private Scene menuScene;
    @Getter
    private Button startGameButton;

    private VBox menuBox;

    private int height;
    private int width;
    
    public GUIView(Stage stage) {
        this.stage = stage;
        context = new Context();
        width = 600;
        height = 400;
        initMenuScene();
        initGameScene();
        startGameButton = new Button("Create game");
        stage.setTitle("snake game");
        stage.setScene(menuScene);
    }

    private void initMenuScene() {
        menuBox = new VBox();
        menuBox.setAlignment(Pos.TOP_CENTER);
        menuBox.setSpacing(5);
        menuScene = new Scene(menuBox, width, height);
    }

    private void initGameScene() {
        gameScene = new Scene(new Pane(), width, height);
    }

    public void setMenuScene() {
        Platform.runLater(() -> {
            stage.setScene(menuScene);
        });
    }

    public void setGameScene() {
        Platform.runLater(() -> {
            stage.setScene(gameScene);
        });
    }

    public void show() {
        stage.show();
    }

    public void updMenuScreen(SnakesProto.GameMessage gameMessage, InetAddress address, int port) {
        Platform.runLater(() -> {
            SnakesProto.GameAnnouncement g = gameMessage.getAnnouncement().getGames(0);
            HBox hBox = new HBox(
                    new Text("host : " + address.getHostAddress()),
                    new Text("port : " + port),
                    new Text(g.getGameName())
            );
            hBox.setSpacing(5);
            menuBox.getChildren().add(hBox);
        });
    }

    public void updGameScreen(SnakesProto.GameMessage gameMessage) {

    }
}
