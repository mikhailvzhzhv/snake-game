package ru.nsu.vozhzhov.snakenode.model;

import lombok.Getter;
import ru.nsu.vozhzhov.snakenode.proto.SnakesProto;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class ModelController {
    private final Random r;
    
    private SnakesProto.NodeRole role;
    private SnakesProto.GameState gameState;
    @Getter
    private SnakesProto.GameConfig gameConfig;
    private int stateOrder;

    public ModelController() {
        r = new Random();
        role = SnakesProto.NodeRole.MASTER;
        stateOrder = 0;

        loadConfig();
        initState();
    }

    public SnakesProto.GameState getGameState() {
        stateOrder++;
        return gameState;
    }

    private void loadConfig() {
        int width, height, foodStatic, stateDelayMs;

        File propertyFile = new File("resources/ru/nsu/vozhzhov/snakenode/config.properties");
        System.out.println(propertyFile.exists());
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(propertyFile));
            width = Integer.parseInt(properties.getProperty("width"));
            height = Integer.parseInt(properties.getProperty("height"));
            foodStatic = Integer.parseInt(properties.getProperty("foodStatic"));
            stateDelayMs = Integer.parseInt(properties.getProperty("stateDelayMs"));
        } catch (IOException e) {
            width = 40;
            height = 30;
            foodStatic = 1;
            stateDelayMs = 1000;
        }

        gameConfig = SnakesProto.GameConfig.newBuilder()
                .setHeight(height)
                .setWidth(width)
                .setFoodStatic(foodStatic)
                .setStateDelayMs(stateDelayMs)
                .build();
    }

    private void initState() {
        SnakesProto.GamePlayer player = getCreatePlayer();
        SnakesProto.GameState.Snake snake = getCreateSnakeOnFreePlace(player);

        List<SnakesProto.GameState.Coord> listFoods = new ArrayList<>();
        List<SnakesProto.GameState.Snake> listSnakes = new ArrayList<>();
        listSnakes.add(snake);

        SnakesProto.GameState.Builder gameStateBuilder = SnakesProto.GameState.newBuilder();
        for (int foodi = 0; foodi < gameConfig.getFoodStatic() + 1; ++foodi) {
            SnakesProto.GameState.Coord food = getCreateRandomFood(listSnakes, listFoods);
            gameStateBuilder.addFoods(food);
            listFoods.add(food);
        }
        gameStateBuilder
                .setStateOrder(stateOrder)
                .addSnakes(snake);
        gameStateBuilder.setPlayers(SnakesProto.GamePlayers.newBuilder().addPlayers(player).build());

        gameState = gameStateBuilder.build();
    }

    private SnakesProto.GamePlayer getCreatePlayer() {
        return SnakesProto.GamePlayer.newBuilder()
                .setId(1)
                .setName("MASTER")
                .setRole(SnakesProto.NodeRole.MASTER)
                .setScore(0)
                .build();
    }

    private SnakesProto.GameState.Coord getCreateRandomFood(
            List<SnakesProto.GameState.Snake> listSnakes, 
            List<SnakesProto.GameState.Coord> listFoods) {
        int rx = 0;
        int ry = 0;
        boolean free = false;

        while (!free) {
            free = true;
            rx = r.nextInt(gameConfig.getWidth());
            ry = r.nextInt(gameConfig.getHeight());

            for (SnakesProto.GameState.Snake snake : listSnakes) {
                if (hasCoordCollision(rx, ry, snake.getPointsList())) {
                    free = false;
                    break;
                }
            }

            if (!free) {
                continue;
            }
            
            if (hasCoordCollision(rx, ry, listFoods)) {
                free = false;
            }
        }

        return SnakesProto.GameState.Coord.newBuilder()
                .setX(rx)
                .setY(ry)
                .build();
    }

    private void handleMove(SnakesProto.GameMessage gameMessage) {
    }

    private SnakesProto.GameState.Snake getCreateSnakeOnFreePlace(SnakesProto.GamePlayer player) {
        int area = 5;
        for (int yi = 0; yi < gameConfig.getHeight(); ++yi) {
            for (int xi = 0; xi < gameConfig.getWidth(); ++xi) {
                if (hasFreeSquare(xi, yi, area)) {
                    return getCreateSnake(xi, yi, player);
                }
            }
        }

        return null;
    }

    private SnakesProto.GameState.Snake getCreateSnake(int xLeft, int yTop, SnakesProto.GamePlayer player) {
        int x = (xLeft + 2) % gameConfig.getWidth();
        int y = (yTop + 2) % gameConfig.getHeight();

        SnakesProto.Direction dir = getRandomDir();
        SnakesProto.GameState.Coord head = SnakesProto.GameState.Coord.newBuilder()
                .setX(x)
                .setY(y)
                .build();
        SnakesProto.GameState.Coord tail = SnakesProto.GameState.Coord.newBuilder(getCoordByDir(x, y, dir))
                .build();

        return SnakesProto.GameState.Snake.newBuilder()
                .setState(SnakesProto.GameState.Snake.SnakeState.ALIVE)
                .setHeadDirection(dir)
                .addPoints(head)
                .addPoints(tail)
                .setPlayerId(player.getId())
                .build();
    }

    private SnakesProto.GameState.Coord getCoordByDir(int x, int y, SnakesProto.Direction dir) {
        switch (dir) {
            case UP -> {
                return SnakesProto.GameState.Coord.newBuilder().setX(x).setY((y + 1) % gameConfig.getHeight()).build();
            }
            case LEFT -> {
                return SnakesProto.GameState.Coord.newBuilder().setX((x + 1) % gameConfig.getWidth()).setY(y).build();
            }
            case RIGHT -> {
                return SnakesProto.GameState.Coord.newBuilder().setX((x - 1) % gameConfig.getWidth()).setY(y).build();
            }
            default -> {
                return SnakesProto.GameState.Coord.newBuilder().setX(x).setY((y - 1) % gameConfig.getHeight()).build();
            }
        }
    }

    private SnakesProto.Direction getRandomDir() {
        int dir = r.nextInt(4);
        switch (dir) {
            case 1 -> {
                return SnakesProto.Direction.DOWN;
            }
            case 3 -> {
                return SnakesProto.Direction.RIGHT;
            }
            case 2 -> {
                return SnakesProto.Direction.LEFT;
            }
            default -> {
                return SnakesProto.Direction.UP;
            }
        }
    }

    // Maybe make n*m matrix and upd every turn
    private boolean hasFreeSquare(int xi, int yi, int area) {
        if (gameState == null) {
            return true;
        }
        List<SnakesProto.GameState.Coord> listFoods = gameState.getFoodsList();
        List<SnakesProto.GameState.Snake> listSnakes = gameState.getSnakesList();
        int x, y;

        for (int yOffset = 0; yOffset < area; ++yOffset) {
            for (int xOffset = 0; xOffset < area; ++xOffset) {
                x = (xi + xOffset) % gameConfig.getWidth();
                y = (yi + yOffset) % gameConfig.getHeight();

                if (hasCoordCollision(x, y, listFoods)) {
                    return false;
                }

                for (SnakesProto.GameState.Snake snake: listSnakes) {
                    if (hasCoordCollision(x, y, snake.getPointsList())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean hasCoordCollision(int x, int y, List<SnakesProto.GameState.Coord> listCoords) {
        for (SnakesProto.GameState.Coord coord : listCoords) {
            if (coord.getX() == x && coord.getY() == y) {
                return true;
            }
        }
        return false;
    }
}
