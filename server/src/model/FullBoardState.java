package model;

import model.exception.InvalidCellReferenceException;

public class FullBoardState extends BoardState {

    public static final int DEFAULT_WIDTH = 100;
    public static final int DEFAULT_HEIGHT = 100;

    private FullBoardState() throws InvalidCellReferenceException {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public FullBoardState(int width, int height) throws InvalidCellReferenceException {
        super(width, height);
    }

}
