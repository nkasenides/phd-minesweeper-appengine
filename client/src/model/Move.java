package model;

public class Move {

    private final MoveType moveType;
    private final int row;
    private final int col;

    public Move(MoveType moveType, int row, int col) {
        this.moveType = moveType;
        this.row = row;
        this.col = col;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

}
