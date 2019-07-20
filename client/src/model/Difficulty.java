package model;

public enum Difficulty {

    EASY("Easy", 0.1f),
    MEDIUM("Medium", 0.15f),
    HARD("Hard", 0.2f),

    ;

    private final float mineRatio; // percentage of mines - must be 0.5 ... 0.25 (0.1 is easy, 0.15 is medium, 0.2 is hard)
    private final String name;

    Difficulty(String name, float mineRatio) {
        this.name = name;
        this.mineRatio = mineRatio;
    }

    public String getName() {
        return name;
    }

    public float getMineRatio() {
        return mineRatio;
    }

}
