package model;

import exception.InvalidGameSpecificationException;

public class GameSpecification {

    public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.MEDIUM;

    private final int maxPlayers; // max number of players
    private final int width;
    private final int height;
    private final String token;
    private final Difficulty difficulty;

    public GameSpecification(String token, int maxPlayers, int width, int height, Difficulty difficulty) {
        this.maxPlayers = maxPlayers;
        this.width = width;
        this.height = height;
        this.token = token;
        this.difficulty = difficulty;
    }

    public GameSpecification(String token, int maxPlayers, int width, int height) throws InvalidGameSpecificationException {
        this(token, maxPlayers, width, height, DEFAULT_DIFFICULTY);
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

    public String getToken() {
        return token;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

}
