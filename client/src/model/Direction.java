package model;

public enum Direction {

    UP("Up"),
    DOWN("Down"),
    LEFT("Left"),
    RIGHT("Right");

    private final String name;

    Direction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
