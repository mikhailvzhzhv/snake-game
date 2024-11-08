module ru.nsu.vozhzhov.snakenode {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.protobuf;
    requires static lombok;


    opens ru.nsu.vozhzhov.snakenode to javafx.fxml;
    exports ru.nsu.vozhzhov.snakenode;
}