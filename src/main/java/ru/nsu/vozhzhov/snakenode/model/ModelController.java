package ru.nsu.vozhzhov.snakenode.model;

import ru.nsu.vozhzhov.snakenode.proto.SnakesProto;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class ModelController {
    Random r;
    
    private SnakesProto.NodeRole role;
    private SnakesProto.GameState gameState;

    private int width;
    private int height;
    private int foodStatic;
    private int stateDelayMs;

    private int stateOrder;

    public  ModelController() {
        r = new Random();
        role = SnakesProto.NodeRole.MASTER;
        stateOrder = 0;

    }

    private void loadConfig() {
        File propertyFile = new File("resources/ru/nsu/vozhzhov/snakenode/config.properties");

        Properties properties = new Properties();
        try {
            properties.load(new FileReader(propertyFile));
        } catch (IOException e) {
            width = 40;
            height = 30;
            foodStatic = 1;
            stateDelayMs = 1000;
        }

        width = Integer.parseInt(properties.getProperty("width"));
        height = Integer.parseInt(properties.getProperty("height"));
        foodStatic = Integer.parseInt(properties.getProperty("foodStatic"));
        stateDelayMs = Integer.parseInt(properties.getProperty("stateDelayMs"));
    }

    private void initState() {
        SnakesProto.GameState.Snake snake = createSnakeOnFreePlace();

        List<SnakesProto.GameState.Coord> listFoods = new ArrayList<>();
        List<SnakesProto.GameState.Snake> listSnakes = new ArrayList<>();
        listSnakes.add(snake);

        SnakesProto.GameState.Builder gameStateBuilder = SnakesProto.GameState.newBuilder();
        for (int foodi = 0; foodi < foodStatic + 1; ++foodi) {
            SnakesProto.GameState.Coord food = getCreateRandomFood(listSnakes, listFoods);
            gameStateBuilder.setFoods(foodi, food);
            listFoods.add(food);
        }
        gameStateBuilder.setStateOrder(stateOrder).setSnakes(0, snake);

//        SnakesProto.GamePlayer player =

//        gameState = gameStateBuilder.build();
    }

    private SnakesProto.GameState.Coord getCreateRandomFood(
            List<SnakesProto.GameState.Snake> listSnakes, 
            List<SnakesProto.GameState.Coord> listFoods) {
        int rx = 0;
        int ry = 0;
        boolean free = false;

        while (!free) {
            free = true;
            rx = r.nextInt(width);
            ry = r.nextInt(height);

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

    private SnakesProto.GameState.Snake createSnakeOnFreePlace() {
        int area = 5;
        for (int yi = 0; yi < height; ++yi) {
            for (int xi = 0; xi < width; ++xi) {
                if (!hasFreeSquare(xi, yi, area)) {
                    return getCreateSnake(xi, yi);
                }
            }
        }

        return null;
    }

    private SnakesProto.GameState.Snake getCreateSnake(int xLeft, int yTop) {
        int x = (xLeft + 2) % width;
        int y = (yTop + 2) % height;

        SnakesProto.Direction dir = getRandomDir();
        SnakesProto.GameState.Coord head = SnakesProto.GameState.Coord.newBuilder()
                .setX(x)
                .setY(y)
                .build();
        SnakesProto.GameState.Coord tail = SnakesProto.GameState.Coord.newBuilder(getCoordByDir(x, y, dir))
                .build();

        SnakesProto.GameState.Snake snake = SnakesProto.GameState.Snake.newBuilder()
                .setState(SnakesProto.GameState.Snake.SnakeState.ALIVE)
                .setHeadDirection(dir)
                .setPoints(0, head)
                .setPoints(1, tail)
//                .setPlayerId()
                .build();

        return snake;
    }

    private SnakesProto.GameState.Coord getCoordByDir(int x, int y, SnakesProto.Direction dir) {
        switch (dir) {
            case UP -> {
                return SnakesProto.GameState.Coord.newBuilder().setX(x).setY((y + 1) % height).build();
            }
            case LEFT -> {
                return SnakesProto.GameState.Coord.newBuilder().setX((x + 1) % width).setY(y).build();
            }
            case RIGHT -> {
                return SnakesProto.GameState.Coord.newBuilder().setX((x - 1) % width).setY(y).build();
            }
            default -> {
                return SnakesProto.GameState.Coord.newBuilder().setX(x).setY((y - 1) % height).build();
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
        List<SnakesProto.GameState.Coord> listFoods = gameState.getFoodsList();
        List<SnakesProto.GameState.Snake> listSnakes = gameState.getSnakesList();
        int x, y;

        for (int yOffset = 0; yOffset < area; ++yOffset) {
            for (int xOffset = 0; xOffset < area; ++xOffset) {
                x = (xi + xOffset) % width;
                y = (yi + yOffset) % height;

                if (hasCoordCollision(x, y, listFoods)) {
                    return true;
                }

                for (SnakesProto.GameState.Snake snake: listSnakes) {
                    if (hasCoordCollision(x, y, snake.getPointsList())) {
                        return true;
                    }
                }
            }
        }

        return false;
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
