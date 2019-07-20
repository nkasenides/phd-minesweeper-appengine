package model;

import exception.InvalidCellReferenceException;

public abstract class BoardState {

    protected final int width;
    protected final int height;
    protected CellState[][] cells;

    public BoardState(int width, int height) throws InvalidCellReferenceException {

        if (width < 0 || height < 0) {
            throw new InvalidCellReferenceException("The game state with width: " + width + ", height: " + height + " is not valid.");
        }

        this.width = width;
        this.height = height;
        this.cells = new CellState[height][width];
    }

    public CellState[][] getCells() {
        return cells;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public boolean isValidCell(int row, int col) {
        return (row >= 0) && (row < height) && (col >= 0) && (col < width);
    }

    public int countAdjacentMines(int row, int col) {
        int count = 0;


        if (isValidCell(row - 1, col)) {
            if (cells[row - 1][col].isMined()) {
                count++;
            }
        }

        if (isValidCell(row + 1, col)) {
            if (cells[row + 1][col].isMined()) {
                count++;
            }
        }

        if (isValidCell(row, col + 1)) {
            if (cells[row][col + 1].isMined()) {
                count++;
            }
        }

        if (isValidCell(row, col - 1)) {
            if (cells[row][col - 1].isMined()) {
                count++;
            }
        }

        if (isValidCell(row - 1, col + 1)) {
            if (cells[row - 1][col + 1].isMined()) {
                count++;
            }
        }

        if (isValidCell(row - 1, col - 1)) {
            if (cells[row - 1][col - 1].isMined()) {
                count++;
            }
        }

        if (isValidCell(row + 1, col + 1)) {
            if (cells[row + 1][col + 1].isMined()) {
                count++;
            }
        }

        if (isValidCell(row + 1, col - 1)) {
            if (cells[row + 1][col - 1].isMined()) {
                count++;
            }
        }

        return (count);

    }

}
