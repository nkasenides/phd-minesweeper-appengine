package model.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.GameState;
import model.PartialBoardState;
import respondx.SuccessResponse;

public class GetStateResponse extends SuccessResponse {

    public GetStateResponse(PartialBoardState partialBoardState, GameState gameState, String sessionID) {
        super("Partial state retrieved", "Partial game state for session '" + sessionID + "' retrieved.");
        JsonObject data = new JsonObject();
        data.add("partialBoardState", new Gson().toJsonTree(partialBoardState).getAsJsonObject());
        data.addProperty("gameState", gameState.toString());
        setData(data);
    }

}
