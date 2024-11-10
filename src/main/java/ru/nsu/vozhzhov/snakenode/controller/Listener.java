package ru.nsu.vozhzhov.snakenode.controller;

import ru.nsu.vozhzhov.snakenode.proto.SnakesProto;

import java.net.InetAddress;

public interface Listener {
    void onModelChange(SnakesProto.GameMessage gameMessage, InetAddress address, int port, Receive receive);
}
