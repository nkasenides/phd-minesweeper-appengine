package model;

import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import model.exception.InvalidCellReferenceException;

import java.beans.Transient;
import java.util.*;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Entity
public class Game {

    private static final Gson gson = new Gson();

    @Id private Long id;
    @Index private String token;
    private GameSpecification gameSpecification;
    private String boardState;
    private GameState gameState;

    private transient FullBoardState fullBoardState;

    private Game() { }

    public Game(GameSpecification gameSpecification, String token) {
        this.token = token;
        this.gameSpecification = gameSpecification;
        this.gameState = GameState.NOT_STARTED;
        FullBoardState fullBoardState;
        try {
            fullBoardState = new FullBoardState(gameSpecification.getWidth(), gameSpecification.getHeight());
            initializeMatrix(fullBoardState);
            generateMines(fullBoardState);
            boardState = gson.toJson(fullBoardState);
        } catch (InvalidCellReferenceException e) {
            e.printStackTrace();
        }
    }

    @Transient
    public Key<Game> getKey() {
        return Key.create(Game.class, getId());
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setGameSpecification(GameSpecification gameSpecification) {
        this.gameSpecification = gameSpecification;
    }

    public GameSpecification getGameSpecification() {
        return gameSpecification;
    }

    public FullBoardState getFullBoardState() {
        return gson.fromJson(boardState, FullBoardState.class);
    }

    private void initializeMatrix(final FullBoardState fullBoardState) {
        for (int row = 0; row < fullBoardState.getCells().length; row++) {
            for (int col = 0; col < fullBoardState.getCells()[row].length; col++) {
                fullBoardState.getCells()[row][col] = new CellState(false);
            }
        }
    }

    private void generateMines(FullBoardState fullBoardState) {
        Random random = new Random();
        final int numberOfMines = Math.round(gameSpecification.getWidth() * gameSpecification.getHeight() * gameSpecification.getDifficulty().getMineRatio());
        int generatedMines = 0;
        do {
            int randomRow = random.nextInt(gameSpecification.getHeight());
            int randomCol = random.nextInt(gameSpecification.getWidth());
            if (!fullBoardState.getCells()[randomRow][randomCol].isMined()) {
                fullBoardState.getCells()[randomRow][randomCol].setMined(true);
                generatedMines++;
            }
        } while (generatedMines < numberOfMines);
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public boolean start() {
        if (gameState != GameState.STARTED) {
            this.gameState = GameState.STARTED;
            return true;
        }
        return false;
    }

    private int countFlaggedMines() {
        Gson gson = new Gson();
        FullBoardState fullBoardState = gson.fromJson(boardState, FullBoardState.class);
        int count = 0;
        for (int row = 0; row < fullBoardState.getHeight(); row++) {
            for (int col = 0; col < fullBoardState.getWidth(); col++) {
                if (fullBoardState.getCells()[row][col].isMined() && fullBoardState.getCells()[row][col].getRevealState() == RevealState.FLAGGED) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countMines() {
        Gson gson = new Gson();
        FullBoardState fullBoardState = gson.fromJson(boardState, FullBoardState.class);
        int count = 0;
        for (int row = 0; row < fullBoardState.getHeight(); row++) {
            for (int col = 0; col < fullBoardState.getWidth(); col++) {
                if (fullBoardState.getCells()[row][col].isMined()) {
                    count++;
                }
            }
        }
        return count;
    }

    private void computeGameState(FullBoardState fullBoardState) {

        if (gameState == GameState.STARTED) {
            int covered = 0;
            final int flaggedMines = countFlaggedMines();
            final int totalMines = countMines();

            for (int row = 0; row < fullBoardState.getHeight(); row++) {
                for (int col = 0; col < fullBoardState.getWidth(); col++) {

                    //IMPORTANT NOTE: Commented out for simulation purposes (to run the simulation for a longer time). This should be uncommented in a "normal" minesweeper game.

//                    if (fullBoardState.getCells()[row][col].isMined() && fullBoardState.getCells()[row][col].getRevealState() == RevealState.REVEALED_MINE) {
//                        gameState = GameState.ENDED_LOST;
//                        return;
//                    }

                    if (fullBoardState.getCells()[row][col].getRevealState() == RevealState.COVERED) {
                        covered++;
                    }

                }
            }

            if (covered == 0 && flaggedMines == totalMines ) {
                gameState = GameState.ENDED_WON;
                return;
            }

            //IMPORTANT NOTE: For simulation purposes ONLY! This will end the game as a win once all cells have been revealed:
            if (covered < 1) {
                gameState = GameState.ENDED_WON;
                return;
            }

        }

    }

    public void revealAll() {
        FullBoardState state = getFullBoardState();
        for (int row = 0; row < state.getHeight(); row++) {
            for (int col = 0; col < state.getWidth(); col++) {
                if (state.getCells()[row][col].isMined()) {
                    state.getCells()[row][col].setRevealState(RevealState.REVEALED_MINE);
                }
                else {
                    int adjacentMines = getFullBoardState().countAdjacentMines(row, col);
                    state.getCells()[row][col].setRevealState(RevealState.getRevealStateFromNumberOfAdjacentMines(adjacentMines));
                }
            }
        }
        boardState = gson.toJson(state);
    }

    public RevealState reveal(int row, int col) {
        FullBoardState fullBoardState = gson.fromJson(boardState, FullBoardState.class);
        RevealState revealState = doReveal(fullBoardState, row, col);
        computeGameState(fullBoardState);
        boardState = gson.toJson(fullBoardState);
        return revealState;
    }

    private RevealState doReveal(FullBoardState fullBoardState, int row, int col) {

        CellState referencedCell = fullBoardState.getCells()[row][col];
        if (referencedCell.getRevealState() == RevealState.COVERED) {
            if (referencedCell.isMined()) {
                referencedCell.setRevealState(RevealState.REVEALED_MINE);
                return RevealState.REVEALED_MINE;
            }
            else {
                int adjacentMines = fullBoardState.countAdjacentMines(row, col);
                if (adjacentMines > 0) {
                    RevealState revealState = RevealState.getRevealStateFromNumberOfAdjacentMines(adjacentMines);
                    referencedCell.setRevealState(revealState);
                    return revealState;
                }
                else {

                    //Reveal current cell:
                    referencedCell.setRevealState(RevealState.REVEALED_0);

                    //Scan adjacent cells, recursively:
                    if (fullBoardState.isValidCell(row - 1, col)) {
                        if (!fullBoardState.getCells()[row - 1][col].isMined()) {
                            doReveal(fullBoardState, row - 1, col);
                        }
                    }

                    if (fullBoardState.isValidCell(row + 1, col)) {
                        if (!fullBoardState.getCells()[row + 1][col].isMined()) {
                            doReveal(fullBoardState, row + 1, col);
                        }
                    }

                    if (fullBoardState.isValidCell(row, col + 1)) {
                        if (!fullBoardState.getCells()[row][col + 1].isMined()) {
                            doReveal(fullBoardState, row, col + 1);
                        }
                    }

                    if (fullBoardState.isValidCell(row, col - 1)) {
                        if (!fullBoardState.getCells()[row][col - 1].isMined()) {
                            doReveal(fullBoardState, row, col - 1);
                        }
                    }

                    if (fullBoardState.isValidCell(row - 1, col + 1)) {
                        if (!fullBoardState.getCells()[row - 1][col + 1].isMined()) {
                            doReveal(fullBoardState, row - 1, col + 1);
                        }
                    }

                    if (fullBoardState.isValidCell(row - 1, col - 1)) {
                        if (!fullBoardState.getCells()[row - 1][col - 1].isMined()) {
                            doReveal(fullBoardState, row - 1, col - 1);
                        }
                    }

                    if (fullBoardState.isValidCell(row + 1, col + 1)) {
                        if (!fullBoardState.getCells()[row + 1][col + 1].isMined()) {
                            doReveal(fullBoardState, row + 1, col + 1);
                        }
                    }

                    if (fullBoardState.isValidCell(row + 1, col - 1)) {
                        if (!fullBoardState.getCells()[row + 1][col - 1].isMined()) {
                            doReveal(fullBoardState, row + 1, col - 1);
                        }
                    }
                    return RevealState.REVEALED_0;
                }
            }
        }
        return referencedCell.getRevealState();
    }

    public void flag(int row, int col) {
        FullBoardState fullBoardState = gson.fromJson(boardState, FullBoardState.class);
        CellState referencedCell = fullBoardState.getCells()[row][col];
        if (fullBoardState.getCells()[row][col].getRevealState() == RevealState.COVERED) {
            referencedCell.setRevealState(RevealState.FLAGGED);
        }
        else if (fullBoardState.getCells()[row][col].getRevealState() == RevealState.FLAGGED) {
            referencedCell.setRevealState(RevealState.COVERED);
        }
        computeGameState(fullBoardState);
        boardState = gson.toJson(fullBoardState);
    }

}
