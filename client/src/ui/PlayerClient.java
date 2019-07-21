package ui;

import api.API;
import api.http.HTTPAsyncTask;
import api.http.ParameterMap;
import api.http.RequestMethod;
import api.http.SyncHTTP;
import com.google.gson.*;
import com.sun.javafx.collections.NonIterableChange;
import io.FileManager;
import model.*;
import respondx.Response;
import solvers.RandomSolver;
import solvers.Solver;
import ui.form.PlayerGameForm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

import static respondx.ResponseStatus.OK;

public class PlayerClient implements Runnable {

    private static Thread[] threads;
    private static PlayerClient[] clients;

    private static final boolean DEBUG = true;
    private static boolean gui = true;
    private PlayerGameForm gameForm;

    private String name;
    private String sessionID;
    private final ArrayList<StatelessGame> games = new ArrayList<>();
    private final PartialStatePreference partialStatePreference;
    private boolean stateInitialized = false;
    private final Solver solver;

    private int gameWidth;
    private int gameHeight;

    private GameState gameState;
    private PartialBoardState partialBoardState;

    private long commandSent = 0;
    private int movesMade = 0;
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd@HHmm-ss");
    private static final String filename = simpleDateFormat.format(new Date()) + ".csv";
    private final String dirName = "Simulations";

    public PlayerClient(String name, PartialStatePreference partialStatePreference, Solver solver) throws IOException {
        this.name = name;
        this.solver = solver;
        this.partialStatePreference = partialStatePreference;
//        FileManager.createDirectory(dirName, false);
    }

    public PartialStatePreference getPartialStatePreference() {
        return partialStatePreference;
    }

    public GameState getGameState() {
        return gameState;
    }

    public PartialBoardState getPartialBoardState() {
        return partialBoardState;
    }

    public String getSessionID() {
        return sessionID;
    }

    public int getGameWidth() {
        return gameWidth;
    }

    public int getGameHeight() {
        return gameHeight;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        class GetStateAsyncTask extends HTTPAsyncTask {

            public GetStateAsyncTask(ParameterMap params) {
                super(API.GET_STATE_ENDPOINT, params, RequestMethod.GET);
            }

            @Override
            protected void onResponseReceived(String response) {
                if (DEBUG) System.out.println(response);
                Gson gson = new Gson();
                try {
                    Response jsonResponse = gson.fromJson(response, Response.class);
                    if (jsonResponse.getStatus() != OK) {
                        MessageDialog.showInfo(jsonResponse.getTitle(), jsonResponse.getMessage());
                    }
                    else {
                        JsonObject data = jsonResponse.getData();
                        try {
                            partialBoardState = gson.fromJson(data.get("partialBoardState"), PartialBoardState.class);
                            String gameStateStr = data.get("gameState").getAsString();
                            gameState = GameState.valueOf(gameStateStr);

                            gameForm = new PlayerGameForm(PlayerClient.this, PlayerClient.this.name);
                            if (!stateInitialized) {
                                if (gui) gameForm.initialize();
                                stateInitialized = true;
                            }
                            if (gui) gameForm.update();

                            while (gameState != GameState.ENDED_WON && gameState != GameState.ENDED_LOST) {
                                Move move = solver.solve(partialBoardState, PlayerClient.this);

                                class PlayAsyncTask extends SyncHTTP {

                                    public PlayAsyncTask(ParameterMap params) {
                                        super(API.PLAY_ENDPOINT, params, RequestMethod.GET);
                                    }

                                    @Override
                                    protected void onResponseReceived(String response) {
                                        if (DEBUG) System.out.println(response);
                                        Gson gson = new Gson();
                                        try {
                                            Response jsonResponse = gson.fromJson(response, Response.class);
                                            if (jsonResponse.getStatus() != OK) {
                                                MessageDialog.showInfo(jsonResponse.getTitle(), jsonResponse.getMessage());
                                            }
                                            else {
                                                System.out.println(jsonResponse.getMessage());
                                                JsonObject data = jsonResponse.getData();
                                                PlayerClient.this.gameState = GameState.valueOf(data.get("gameState").getAsString());
                                            }
                                        }
                                        catch (JsonSyntaxException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }

                                ParameterMap params = new ParameterMap();
                                params.add("sessionID", sessionID);
                                params.add("move", move.getMoveType().toString());
                                params.add("row", String.valueOf(move.getRow()));
                                params.add("col", String.valueOf(move.getCol()));
                                new PlayAsyncTask(params).execute();

                            }

                        } catch (JsonSyntaxException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                catch (JsonSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        class JoinGameAsyncTask extends HTTPAsyncTask {

            public JoinGameAsyncTask(ParameterMap params) {
                super(API.JOIN_GAME_ENDPOINT, params, RequestMethod.GET);
            }

            @Override
            protected void onResponseReceived(String response) {
                if (DEBUG) System.out.println(response);
                Gson gson = new Gson();
                try {
                    Response jsonResponse = gson.fromJson(response, Response.class);
                    if (jsonResponse.getStatus() != OK) {
                        MessageDialog.showInfo(jsonResponse.getTitle(), jsonResponse.getMessage());
                    }
                    else {
                        System.out.println(jsonResponse.getMessage());
                        gameWidth = games.get(0).getGameSpecification().getWidth();
                        gameHeight = games.get(0).getGameSpecification().getHeight();
                        JsonObject data = jsonResponse.getData();
                        PlayerClient.this.sessionID = data.get("sessionID").getAsString();
                        System.out.println("SessionID -> " + sessionID);

                        ParameterMap params = new ParameterMap();
                        params.add("sessionID", sessionID);
                        new GetStateAsyncTask(params).execute();

                    }
                }
                catch (JsonSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        class ListGamesAsyncTask extends HTTPAsyncTask {

            public ListGamesAsyncTask() {
                super(API.LIST_GAMES_ENDPOINT, new ParameterMap(), RequestMethod.GET);
            }

            @Override
            protected void onResponseReceived(String response) {
                if (DEBUG) System.out.println(response);
                Gson gson = new Gson();
                try {
                    Response jsonResponse = gson.fromJson(response, Response.class);
                    if (jsonResponse.getStatus() != OK) {
                        MessageDialog.showInfo(jsonResponse.getTitle(), jsonResponse.getMessage());
                    }
                    else {
                        JsonObject data = jsonResponse.getData();
                        JsonArray jsonGames = data.getAsJsonArray("games");
                        for (JsonElement e : jsonGames) {
                            StatelessGame game = gson.fromJson(e, StatelessGame.class);
                            games.add(game);
                        }
                    }

                    ParameterMap parameterMap = new ParameterMap();
                    parameterMap.add("gameToken", games.get(0).getToken());
                    parameterMap.add("playerName", name);
                    parameterMap.add("partialStateWidth", "5");
                    parameterMap.add("partialStateHeight", "5");
                    new JoinGameAsyncTask(parameterMap).execute();

                }
                catch (JsonSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        new ListGamesAsyncTask().execute();
    }

    public static void main(String[] args) {
        final int numOfClients = 2;

        clients = new PlayerClient[numOfClients];

        for (int i = 0; i < numOfClients; i++) {
            try {
                clients[i] = new PlayerClient("Player" + (i+1), new PartialStatePreference(5, 5), new RandomSolver());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        threads = new Thread[numOfClients];
        for (int i = 0; i < numOfClients; i++) {
            threads[i] = new Thread(clients[i]);
            threads[i].start();
            System.out.println(clients[i].getName() + " started!");
        }

    }
}

