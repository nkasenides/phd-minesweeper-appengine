package model;

import exception.InvalidCellReferenceException;

public class PartialBoardState extends BoardState {

    public static final int DEFAULT_WIDTH = 10;
    public static final int DEFAULT_HEIGHT = 10;
    public static final int DEFAULT_STARTING_X = 0;
    public static final int DEFAULT_STARTING_Y = 0;

    private final int startingCol;
    private final int startingRow;

    public PartialBoardState(int width, int height, int startingRow, int startingCol, FullBoardState entireFullGameState) throws InvalidCellReferenceException {

        super(width, height);

        if (startingCol + width > entireFullGameState.getWidth() || startingRow + height > entireFullGameState.getHeight()
                || startingCol < 0 || startingRow < 0) {
            throw new InvalidCellReferenceException("The partial state with row: " + startingRow + ", col: " + startingCol+ ", width: " + width + ", height: " + height + " is not valid.");
        }

        this.cells = new CellState[height][width];
        this.startingCol = startingCol;
        this.startingRow = startingRow;

        //Copy the partial state from the full state:
        for (int row = startingRow; (row < startingRow + height); row++) {
            for (int col = startingCol; (col < startingCol + width); col++) {
                cells[row - startingRow][col - startingCol] = entireFullGameState.getCells()[row][col];
            }
        }
    }

    public PartialBoardState(int startingCol, int startingRow, FullBoardState entireFullGameState) throws InvalidCellReferenceException {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, startingCol, startingRow, entireFullGameState);
    }

    public PartialBoardState(FullBoardState entireFullGameState) throws InvalidCellReferenceException {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_STARTING_X, DEFAULT_STARTING_Y, entireFullGameState);
    }

    public int getStartingCol() {
        return startingCol;
    }

    public int getStartingRow() {
        return startingRow;
    }

}
