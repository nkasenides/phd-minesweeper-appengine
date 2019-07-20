package model;

public class StatelessGame {

    private final String token;
    private final GameSpecification gameSpecification;
    private final GameState gameState;

    public StatelessGame(String token, GameSpecification gameSpecification, GameState gameState) {
        this.token = token;
        this.gameSpecification = gameSpecification;
        this.gameState = gameState;
    }

    public String getToken() {
        return token;
    }

    public GameSpecification getGameSpecification() {
        return gameSpecification;
    }

    public GameState getGameState() {
        return gameState;
    }

}