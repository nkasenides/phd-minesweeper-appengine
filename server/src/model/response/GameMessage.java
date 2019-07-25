package model.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.GameState;
import model.PartialBoardState;

public class GameMessage {

    private final GameState gameState;
    private final PartialBoardState partialBoardState;


    public GameMessage(GameState gameState, PartialBoardState partialBoardState) {
        this.gameState = gameState;
        this.partialBoardState = partialBoardState;
    }

    public PartialBoardState getPartialBoardState() {
        return partialBoardState;
    }

    public GameState getGameState() {
        return gameState;
    }

    public String toJson() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("gameState", gameState.toString());
        jsonObject.add("partialBoardState", gson.toJsonTree(partialBoardState));
        return gson.toJson(jsonObject);
    }

}
