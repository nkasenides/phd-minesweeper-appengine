package ui;

        import api.API;
        import api.RequestEndpoint;
        import api.http.HTTPAsyncTask;
        import api.http.ParameterMap;
        import api.http.RequestMethod;
        import api.http.SyncHTTP;
        import com.google.gson.*;
        import com.sun.javafx.collections.NonIterableChange;
        import io.FileManager;
        import io.ably.lib.realtime.AblyRealtime;
        import io.ably.lib.realtime.Channel;
        import io.ably.lib.types.AblyException;
        import io.ably.lib.types.Message;
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

    AblyRealtime ably;

    //Simulation:
    class LatencyMeasurement {
        public LatencyMeasurement(int activePlayers, RequestEndpoint requestEndpoint, Long latency) {
            this.activePlayers = activePlayers;
            this.latency = latency;
            this.requestEndpoint = requestEndpoint;
        }
        public int activePlayers;
        public Long latency;
        public RequestEndpoint requestEndpoint;
    }
    private ArrayList<LatencyMeasurement> latencyMeasurements = new ArrayList<>();
    private long lastResponseSentTimestamp = 0;
    private long lastResponseReceivedTimestamp = 0;
    private RequestEndpoint lastResponseEndpoint = null;
    private int movesMade = 0;
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd@HHmm-ss");
    private final String filename;
    private static final String dirName = "Simulations";

    public PlayerClient(String name, PartialStatePreference partialStatePreference, Solver solver) throws IOException {
        this.name = name;
        this.solver = solver;
        this.filename = simpleDateFormat.format(new Date()) + "_" + name + ".csv";
        this.partialStatePreference = partialStatePreference;
        FileManager.createDirectory(dirName, false);
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

                //STATE_GET ENDPOINT SIM RESPONSE
                lastResponseReceivedTimestamp = System.currentTimeMillis();
                if (lastResponseEndpoint == RequestEndpoint.STATE_GET) {
                    latencyMeasurements.add(new LatencyMeasurement(0, RequestEndpoint.STATE_GET, lastResponseReceivedTimestamp - lastResponseSentTimestamp));
                }

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

                                        //PLAY ENDPOINT SIM RESPONSE
                                        lastResponseReceivedTimestamp = System.currentTimeMillis();
                                        if (lastResponseEndpoint == RequestEndpoint.PLAY) {
                                            latencyMeasurements.add(new LatencyMeasurement(0, RequestEndpoint.PLAY, lastResponseReceivedTimestamp - lastResponseSentTimestamp));
                                        }

                                        if (DEBUG) System.out.println(response);
                                        Gson gson = new Gson();
                                        try {
                                            Response jsonResponse = gson.fromJson(response, Response.class);
                                            if (jsonResponse.getStatus() != OK) {
                                                MessageDialog.showInfo(jsonResponse.getTitle(), jsonResponse.getMessage());
                                            }
                                            else {
                                                if (!jsonResponse.getTitle().equals("Cell already revealed")) {
                                                    System.out.println(jsonResponse.getMessage());
                                                    JsonObject data = jsonResponse.getData();
                                                    gameState = GameState.valueOf(data.get("gameState").getAsString());
                                                }
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

                                //PLAY ENDPOINT SIM REQUEST
                                lastResponseEndpoint = RequestEndpoint.PLAY;
                                lastResponseSentTimestamp = System.currentTimeMillis();
                                new PlayAsyncTask(params).execute();

                            }

                            writeData();

                            System.exit(0);

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

                //JOIN ENDPOINT SIM RESPONSE
                lastResponseReceivedTimestamp = System.currentTimeMillis();
                if (lastResponseEndpoint == RequestEndpoint.JOIN) {
                    latencyMeasurements.add(new LatencyMeasurement(0, RequestEndpoint.JOIN, lastResponseReceivedTimestamp - lastResponseSentTimestamp));
                }

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

                        //Also register on Ably channel:
                        try {
                            ably = new AblyRealtime("U4YruA.ehwdkQ:COJkxxIrPJo3DeLX");
                            final String channelName = "gameState-" + sessionID;
                            System.out.println(channelName);
                            Channel channel = ably.channels.get(channelName);
                            channel.subscribe(new Channel.MessageListener() {
                                @Override
                                public void onMessage(Message message) {

                                    //Push message latency:
                                    latencyMeasurements.add(new LatencyMeasurement(0, RequestEndpoint.PUSH_MESSAGE, System.currentTimeMillis() - getLastPlayRequestTimestamp()));

                                    System.out.println("Ably - Received `" + message.name + "` message with data: " + message.data);
                                    GameMessage gameMessage = new Gson().fromJson((String) message.data, GameMessage.class);
                                    partialBoardState = gameMessage.getPartialBoardState();
                                    gameState = gameMessage.getGameState();
                                    gameForm.update();
                                }
                            });

                        } catch (AblyException e) {
                            e.printStackTrace();
                        }

                        ParameterMap params = new ParameterMap();
                        params.add("sessionID", sessionID);

                        //GET_STATE ENDPOINT SIM REQUEST
                        lastResponseEndpoint = RequestEndpoint.STATE_GET;
                        lastResponseSentTimestamp = System.currentTimeMillis();

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

                //LIST ENDPOINT SIM RESPONSE
                lastResponseReceivedTimestamp = System.currentTimeMillis();
                if (lastResponseEndpoint == RequestEndpoint.GAME_LIST) {
                    latencyMeasurements.add(new LatencyMeasurement(0, RequestEndpoint.GAME_LIST, lastResponseReceivedTimestamp - lastResponseSentTimestamp));
                }

                if (DEBUG) System.out.println(response);
                Gson gson = new Gson();
                try {
                    Response jsonResponse = gson.fromJson(response, Response.class);
                    if (jsonResponse.getStatus() != OK) {
                        MessageDialog.showInfo(jsonResponse.getTitle(), jsonResponse.getMessage());
                    }
                    else {
                        if (jsonResponse.getMessage().equals("No games found")) {
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
                    }

                    if (games.size() < 1) {
                        System.out.println("No games found!");
                        return;
                    }

                    ParameterMap parameterMap = new ParameterMap();
                    parameterMap.add("gameToken", games.get(0).getToken());
                    parameterMap.add("playerName", name);
                    parameterMap.add("partialStateWidth", "5");
                    parameterMap.add("partialStateHeight", "5");

                    //JOIN ENDPOINT SIM REQUEST
                    lastResponseEndpoint = RequestEndpoint.JOIN;
                    lastResponseSentTimestamp = System.currentTimeMillis();

                    new JoinGameAsyncTask(parameterMap).execute();

                }
                catch (JsonSyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        //LIST ENDPOINT SIM REQUEST
        lastResponseEndpoint = RequestEndpoint.GAME_LIST;
        lastResponseSentTimestamp = System.currentTimeMillis();

        new ListGamesAsyncTask().execute();

    }

    private long getLastPlayRequestTimestamp() {
        for (int i = latencyMeasurements.size() - 1; i > 0; i--) {
            if (latencyMeasurements.get(i).requestEndpoint == RequestEndpoint.PLAY) {
                return latencyMeasurements.get(i).latency;
            }
        }
        return -1;
    }

    private static void writeData() {
        for (PlayerClient p : clients) {
            //Write data to file:
            if (FileManager.fileIsDirectory(dirName)) {
                //Write to file:
                try {

                    StringBuilder builder = new StringBuilder();

                    builder.append("players,endpoint,latency").append(System.lineSeparator());
                    for (LatencyMeasurement m : p.latencyMeasurements) {
                        builder.append(m.activePlayers).append(",").append(m.requestEndpoint.toString()).append(",").append(m.latency).append(System.lineSeparator());
                    }

                    FileManager.writeFile(dirName + "/" + p.filename, builder.toString(), true);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Failed to write results to file - directory 'Simulations' could not be created.");
            }
        }
    }

    public static void main(String[] args) {


        final int numOfClients = 2;
        final int joinDelay = 500;

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

            try {
                Thread.sleep(joinDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(clients[i].getName() + " started!");
        }

    }
}

