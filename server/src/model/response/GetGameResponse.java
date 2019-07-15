package model.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.StatelessGame;
import respondx.SuccessResponse;


public class GetGameResponse extends SuccessResponse {

    public GetGameResponse(StatelessGame game) {
        super("Games fetched", "Game with token '" + game.getToken() + "' has been fetched.");
        JsonObject data = new JsonObject();
        data.add("game", new Gson().toJsonTree(game).getAsJsonObject());
        setData(data);
    }

}
