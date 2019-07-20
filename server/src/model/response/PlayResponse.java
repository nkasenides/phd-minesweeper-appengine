package model.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.GameState;
import model.Move;
import respondx.Response;
import respondx.SuccessResponse;

public class PlayResponse extends SuccessResponse {

    public PlayResponse(Move move, int row, int col, GameState gameState, int points) {
        super("Play success", "Your move '" + move.getName() + "' on cell (" + row + "," + col + ") was successful.");
        JsonObject data = new JsonObject();
        data.addProperty("gameState", gameState.toString());
        data.addProperty("points", points);
        setData(data);
    }

}
