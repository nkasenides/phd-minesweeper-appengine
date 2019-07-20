package model;

public enum GameState {

    NOT_STARTED("Not started", false),
    STARTED("Started", false),
    ENDED_WON("Ended (Won)", true),
    ENDED_LOST("Ended (Lost)", true)

    ;

    private final String name;
    private final boolean ended;

    GameState(String name, boolean ended) {
        this.name = name;
        this.ended = ended;
    }

    public String getName() {
        return name;
    }

    public boolean isEnded() {
        return ended;
    }

}
