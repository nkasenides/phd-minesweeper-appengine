package ui;

import api.API;
import api.http.HTTPAsyncTask;
import api.http.ParameterMap;
import api.http.RequestMethod;
import com.google.gson.*;
import io.FileManager;
import model.*;
import respondx.Response;
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

    private static final boolean DEBUG = false;
    private static boolean gui = false;
    private ArrayList<GameSpecification> games = null;
    private GameSpecification gameSpecification = null;
    private PlayerGameForm gameForm;

    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private Socket socket;
    private String name;
    private final int turnInterval;
    private final PartialStatePreference partialStatePreference;
    private boolean stateInitialized = false;
    private final Solver solver;

    private String sessionID = null;
    private int gameWidth;
    private int gameHeight;

    private GameState gameState;
    private PartialBoardState partialBoardState;

    private long commandSent = 0;
    private int movesMade = 0;
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd@HHmm-ss");
    private static final String filename = simpleDateFormat.format(new Date()) + ".csv";
    private final String dirName = "Simulations";

    public PlayerClient(Socket socket, String name, int turnInterval, PartialStatePreference partialStatePreference, Solver solver) throws IOException {
        this.name = name;
        this.socket = socket;
        this.turnInterval = turnInterval;
        this.solver = solver;
        this.partialStatePreference = partialStatePreference;
        printWriter = new PrintWriter(socket.getOutputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        FileManager.createDirectory(dirName, false);
    }

    public Socket getSocket() {
        return socket;
    }

    public String getName() {
        return name;
    }

    public void initializeState() {
        //Create request object:
        JsonObject object = new JsonObject();
        object.addProperty("sessionID", sessionID);
        Command command = new Command(CommandType.USER_SERVICE, "getPartialState", object);
        //Convert to JSON and send:
        Gson gson = new Gson();
        String commandJSON = gson.toJson(command);
        printWriter.println(commandJSON);
        printWriter.flush();
    }

    @Override
    public void run() {
        try {

            listAllGames();
            joinFirstGame();
            initializeState();

            if (DEBUG) System.out.println(name + ": Starting to play!");

            if (FileManager.fileIsDirectory(dirName)) {
                //Write to file:
                try {
                    FileManager.writeFile(dirName + "/" + filename, "players,latency");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println("Failed to write results to file - directory 'Simulations' could not be created.");
            }

            //While the game is not over, keep making moves:
            Gson gson = new Gson();
            while (true) {

                if (DEBUG) System.out.println();
                if (DEBUG) System.out.println();

                String reply = bufferedReader.readLine();
                long replyReceived = System.currentTimeMillis();
                if (commandSent != 0) {
                    long timeElapsed = replyReceived - commandSent;
//                    latencyMeasurements.add(timeElapsed);


//                    //Write to file:
//                    if (FileManager.fileIsDirectory(dirName)) {
//                        //Write to file:
//                        try {
//                            FileManager.appendToFile(dirName + "/" + filename, playerSimulationManager.getNumberOfActivePlayers() + "," + timeElapsed + System.lineSeparator());
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    else {
//                        System.out.println("Failed to write results to file - directory 'Simulations' could not be created.");
//                    }

                    movesMade++;
                }
                if (DEBUG) System.out.println("[" + name + "] Got: " + reply);

                Command receivedCommand = gson.fromJson(reply, Command.class);

                if (receivedCommand.getCommandType() != null) {
                    if (receivedCommand.getCommandType() == CommandType.CLIENT_UPDATE_SERVICE) {
                        JsonObject payload = receivedCommand.getPayload();
                        JsonElement gameStateElement = payload.get("gameState");
                        GameState gameState = gson.fromJson(gameStateElement, GameState.class);
                        JsonElement partialBoardStateElement = payload.get("partialBoardState");
                        PartialBoardState partialBoardState = gson.fromJson(partialBoardStateElement, PartialBoardState.class);
                        this.gameState = gameState;
                        this.partialBoardState = partialBoardState;

                        //DEBUGGING:
                        if (DEBUG) {
                            System.out.println(gson.toJson(gameState));
                            System.out.println(gson.toJson(partialBoardState));
                        }

                        if (gameState.isEnded()) {
                            if (DEBUG) System.out.println(name + ": GAME ENDED (" + gameState + ")");
                            break;
                        }

                        if (gui) gameForm.update();
                    }
                    else throw new RuntimeException("Invalid command type found, expected CLIENT_UPDATE_SERVICE.");
                }
                else {
                    Response response = gson.fromJson(reply, Response.class);
                    if (response.getStatus() == OK) {
                        JsonObject data = response.getData();
                        JsonElement gameStateElement = data.get("gameState");
                        GameState gameState = gson.fromJson(gameStateElement, GameState.class);
                        JsonElement partialBoardStateElement = data.get("partialBoardState");
                        PartialBoardState partialBoardState = gson.fromJson(partialBoardStateElement, PartialBoardState.class);
                        this.gameState = gameState;
                        this.partialBoardState = partialBoardState;
                        if (!stateInitialized) {
                            if (gui) gameForm.initialize();
                            stateInitialized = true;
                        }
                        if (gui) gameForm.update();

                        if (gameState.isEnded()) {
                            if (DEBUG) System.out.println(name + ": GAME ENDED (" + gameState + ")");
                            break;
                        }

                        Command outgoingCommand = solver.solve(partialBoardState, this);
                        if (DEBUG) System.out.println("[" + name + "]: Decided to make move '" + outgoingCommand.getEndpointName() + "' at cell (" +
                                outgoingCommand.getPayload().get("row").getAsInt() + "," + outgoingCommand.getPayload().get("col").getAsInt() + ")");

                        //Convert to JSON and send:
                        String moveCommandJSON = gson.toJson(outgoingCommand);
                        printWriter.println(moveCommandJSON);
                        printWriter.flush();
                        commandSent = System.currentTimeMillis();

                        if (turnInterval > 0) Thread.sleep(turnInterval);
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void joinFirstGame() throws IOException {
        if (DEBUG) System.out.println(name + ": Joining game with token '" + games.get(0).getToken() + "'...");

        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("token", games.get(0).getToken());
        jsonObject.addProperty("playerName", name);
        jsonObject.addProperty("partialStateWidth", partialStatePreference.getWidth());
        jsonObject.addProperty("partialStateHeight", partialStatePreference.getHeight());
        Command joinGameCommand = new Command(CommandType.MASTER_SERVICE, "join", jsonObject);
        String joinCommandJSON = gson.toJson(joinGameCommand);
        printWriter.println(joinCommandJSON);
        printWriter.flush();
        String joinReply = bufferedReader.readLine();
        Response joinResponse = gson.fromJson(joinReply, Response.class);
        if (joinResponse.getStatus() == OK) {
            gameSpecification = games.get(0);
            sessionID = joinResponse.getData().get("sessionID").getAsString();
            gameWidth = joinResponse.getData().get("totalWidth").getAsInt();
            gameHeight = joinResponse.getData().get("totalHeight").getAsInt();
            JsonElement gameStateElement = joinResponse.getData().get("gameState");
            gameState = gson.fromJson(gameStateElement, GameState.class);
            JsonElement partialBoardStateElement = joinResponse.getData().get("partialBoardState");
            partialBoardState = gson.fromJson(partialBoardStateElement, PartialBoardState.class);
            if (gui) {
                gameForm = new PlayerGameForm(this, name);
            }
            if (DEBUG) System.out.println(name + ": Joined game with token '" + games.get(0).getToken() + " with session ID '" + sessionID + "'.");
        }
    }

    private void listAllGames() throws IOException {
        if (DEBUG) System.out.println(name + ": Acquiring a list of games...");

        Command listGamesCommand = new Command(CommandType.MASTER_SERVICE, "listGames", null);
        Gson gson = new Gson();
        String commandJSON = gson.toJson(listGamesCommand);
        printWriter.println(commandJSON);
        printWriter.flush();
        String listReply = bufferedReader.readLine();
        Response listResponse = gson.fromJson(listReply, Response.class);
        if (listResponse.getStatus() == OK) {
            JsonArray gamesArray = listResponse.getData().get("games").getAsJsonArray();
            games = new ArrayList<>();
            for (JsonElement e : gamesArray) {
                GameSpecification s = gson.fromJson(e, GameSpecification.class);
                games.add(s);
            }
        }

        if (DEBUG) System.out.println(name + ": Games fetched.");

        if (games.size() < 1) {
            throw new RuntimeException("Error - No games found");
        }
    }

    public int getGameHeight() {
        return gameHeight;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getGameWidth() {
        return gameWidth;
    }

    public PartialBoardState getPartialBoardState() {
        return partialBoardState;
    }

    public PartialStatePreference getPartialStatePreference() {
        return partialStatePreference;
    }

    public String getSessionID() {
        return sessionID;
    }

    public static void main(String[] args) {

        System.out.println("STARTED!");

        ArrayList<StatelessGame> games = new ArrayList<>();

        class JoinGameAsyncTask extends HTTPAsyncTask {

            public JoinGameAsyncTask(ParameterMap params) {
                super(API.JOIN_GAME_ENDPOINT, params, RequestMethod.GET);
            }

            @Override
            protected void onResponseReceived(String response) {
                Gson gson = new Gson();
                try {
                    Response jsonResponse = gson.fromJson(response, Response.class);
                    if (jsonResponse.getStatus() != OK) {
                        MessageDialog.showInfo(jsonResponse.getTitle(), jsonResponse.getMessage());
                    }
                    else {
                        System.out.println(jsonResponse.getMessage());
                        JsonObject data = jsonResponse.getData();
                        String sessionID = data.get("sessionID").getAsString();
                        System.out.println("SessionID -> " + sessionID);

                        //TODO

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

                    System.out.println("--- Games: ");
                    if (games.size() > 0) {
                        for (StatelessGame g : games) {
                            System.out.println("- " + g.getToken());
                        }
                    }

                    ParameterMap parameterMap = new ParameterMap();
                    parameterMap.add("gameToken", games.get(0).getToken());
                    parameterMap.add("playerName", "playerName");
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
}

