package model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.beans.Transient;
import java.util.UUID;

@Entity
public class Session {

    @Id private Long id;
    @Index private String sessionID;
    private PartialStatePreference partialStatePreference;
    @Index private String playerName;
    @Index private String gameToken;
    @Index private Key<Game> gameKey;
    private int positionCol;
    private int positionRow;
    private boolean spectator;
    private int points;

    private Session() { }

    public Session(PartialStatePreference partialStatePreference, String playerName, String gameToken, boolean spectator) {
        this.sessionID = UUID.randomUUID().toString().replace("-", "");
        this.partialStatePreference = partialStatePreference;
        this.playerName = playerName;
        this.gameToken = gameToken;
        this.positionCol = 0;
        this.positionRow = 0;
        this.spectator = spectator;
        this.points = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Key<Game> getGameKey() {
        return gameKey;
    }

    @Transient
    public Key<Session> getKey() {
        return Key.create(Session.class, getId());
    }

    public void setGameKey(Key<Game> gameKey) {
        this.gameKey = gameKey;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public void setPartialStatePreference(PartialStatePreference partialStatePreference) {
        this.partialStatePreference = partialStatePreference;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setGameToken(String gameToken) {
        this.gameToken = gameToken;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }

    public void changePoints(int points) {
        this.points += points;
    }

    public String getSessionID() {
        return sessionID;
    }

    public PartialStatePreference getPartialStatePreference() {
        return partialStatePreference;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getGameToken() {
        return gameToken;
    }

    public int getPositionCol() {
        return positionCol;
    }

    public int getPositionRow() {
        return positionRow;
    }

    public void setPositionCol(int positionCol) {
        this.positionCol = positionCol;
    }

    public void setPositionRow(int positionRow) {
        this.positionRow = positionRow;
    }

    public boolean isSpectator() {
        return spectator;
    }

}
