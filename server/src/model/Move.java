package model;

public enum Move {

    REVEAL("Reveal"),
    FLAG_UNFLAG("Flag/Unflag"),
    SHIFT("Shift")

    ;

    private final String name;

    Move(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
