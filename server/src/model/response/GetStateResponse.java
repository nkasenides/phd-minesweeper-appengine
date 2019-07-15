package model.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.PartialBoardState;
import respondx.SuccessResponse;

public class GetStateResponse extends SuccessResponse {

    public GetStateResponse(PartialBoardState partialBoardState, String sessionID) {
        super("Partial state retrieved", "Partial game state for session '" + sessionID + "' retrieved.");
        JsonObject data = new JsonObject();
        data.add("partialBoardState", new Gson().toJsonTree(partialBoardState).getAsJsonObject());
        setData(data);
    }

}
