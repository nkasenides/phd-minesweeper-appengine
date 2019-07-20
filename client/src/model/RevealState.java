package model;

public enum RevealState {

    COVERED, // unknown
    FLAGGED, // flagged to have a mine
    REVEALED_0, // gray, empty
    REVEALED_1,
    REVEALED_2,
    REVEALED_3,
    REVEALED_4,
    REVEALED_5,
    REVEALED_6,
    REVEALED_7,
    REVEALED_8,
    REVEALED_MINE

    ;

    public static RevealState getRevealStateFromNumberOfAdjacentMines(int numOfMines) {
        switch (numOfMines) {
            case 0: return REVEALED_0;
            case 1: return REVEALED_1;
            case 2: return REVEALED_2;
            case 3: return REVEALED_3;
            case 4: return REVEALED_4;
            case 5: return REVEALED_5;
            case 6: return REVEALED_6;
            case 7: return REVEALED_7;
            case 8: return REVEALED_8;
        }
        return null;
    }

}
