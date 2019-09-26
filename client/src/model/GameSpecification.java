package model;

import exception.InvalidGameSpecificationException;

public class GameSpecification {

    public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.MEDIUM;

    private final int maxPlayers; // max number of players
    private final int width;
    private final int height;
    private final String gameToken;
    private final Difficulty difficulty;

    public GameSpecification(String gameToken, int maxPlayers, int width, int height, Difficulty difficulty) {
        this.maxPlayers = maxPlayers;
        this.width = width;
        this.height = height;
        this.gameToken = gameToken;
        this.difficulty = difficulty;
    }

    public GameSpecification(String gameToken, int maxPlayers, int width, int height) throws InvalidGameSpecificationException {
        this(gameToken, maxPlayers, width, height, DEFAULT_DIFFICULTY);
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getGameToken() {
        return gameToken;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

}