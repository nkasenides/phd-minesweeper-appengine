package api;

import api.http.HTTPAsyncTask;

import java.util.HashMap;

public class API {

    public static final String BASE_URL = "https://minesweeper-246410.appspot.com/";
    public static final String LIST_GAMES_ENDPOINT = BASE_URL + "game/list";
    public static final String GET_GAME_ENDPOINT = BASE_URL + "game/get";
    public static final String JOIN_GAME_ENDPOINT = BASE_URL + "join";
    public static final String GET_STATE_ENDPOINT = BASE_URL + "getState";
    public static final String PLAY_ENDPOINT = BASE_URL + "play";

}
