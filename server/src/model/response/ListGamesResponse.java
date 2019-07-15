package model.response;

import com.google.gson.JsonObject;
import model.Game;
import model.StatelessGame;
import respondx.JsonConvert;
import respondx.SuccessResponse;

import java.util.List;

public class ListGamesResponse extends SuccessResponse {

    public ListGamesResponse(List<StatelessGame> games) {
        super("Games fetched", "A list of games has been fetched.");
        JsonObject data = new JsonObject();
        data.add("games", JsonConvert.listToJsonArray(games));
        setData(data);
    }

}
