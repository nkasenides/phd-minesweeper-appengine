package solvers;

import com.google.gson.JsonObject;
import model.*;
import ui.PlayerClient;

import java.util.ArrayList;
import java.util.Random;

public class RandomSolver implements Solver {

    @Override
    public Command solve(PartialBoardState partialBoardState, PlayerClient playerClient) {
        //Scan all the cells from partial state and save those who are RevealState.COVERED:
        class UnrevealedCell {
            private int row;
            private int col;
            private UnrevealedCell(int row, int col) { this.row = row; this.col = col; }
        }
        ArrayList<UnrevealedCell> unrevealedCells = new ArrayList<>();
        for (int row = 0; row < partialBoardState.getCells().length; row++) {
            for (int col = 0; col < partialBoardState.getCells()[row].length; col++) {
                if (partialBoardState.getCells()[row][col].getRevealState() == RevealState.COVERED) {
                    unrevealedCells.add(new UnrevealedCell(row, col));
                }
            }
        }

        //Command attributes:
        JsonObject object = new JsonObject();
        object.addProperty("sessionID", playerClient.getSessionID());

        //Check if there are any unrevealed cells, if not then the player has to shift position:
        if (unrevealedCells.size() < 1) {

            final String moveEndpoint = "move";
            final CommandType commandType = CommandType.USER_SERVICE;

            //Rightward shifting:
            final int cellsRight = playerClient.getGameWidth() - (partialBoardState.getStartingCol() + partialBoardState.getWidth());

            if (cellsRight >= partialBoardState.getWidth()) {
                object.addProperty("row", partialBoardState.getStartingRow());
                object.addProperty("col", partialBoardState.getStartingCol() + partialBoardState.getWidth());
                return new Command(commandType, moveEndpoint, object);
            }
            else if (cellsRight > 0) {
                object.addProperty("row", partialBoardState.getStartingRow());
                object.addProperty("col", partialBoardState.getStartingCol() + cellsRight);
                return new Command(commandType, moveEndpoint, object);
            }
            else {

                //Downward shifting:
                final int cellsDown = playerClient.getGameHeight() - (partialBoardState.getStartingRow() + partialBoardState.getHeight());
                if (cellsDown >= partialBoardState.getHeight()) {
                    object.addProperty("row", partialBoardState.getStartingRow() + partialBoardState.getHeight());
                    object.addProperty("col", 0);
                    return new Command(commandType, moveEndpoint, object);
                }
                else if(cellsDown > 0){
                    object.addProperty("row", partialBoardState.getStartingRow() + cellsDown);
                    object.addProperty("col", 0);
                    return new Command(commandType, moveEndpoint, object);
                } else {
                    return new Command(CommandType.USER_SERVICE, moveEndpoint, object);
                }
            }
        }

        //Otherwise, select a random cell from unrevealedCells with a random move and play it:
        else {

            //If there are unrevealed cells, choose a random one out of the list:
            Random random = new Random();
            int randomCellIndex = random.nextInt(unrevealedCells.size());
            UnrevealedCell chosenUnrevealedCell = unrevealedCells.get(randomCellIndex);

            int globalRow = chosenUnrevealedCell.row + partialBoardState.getStartingRow();
            int globalCol = chosenUnrevealedCell.col + partialBoardState.getStartingCol();

            //Choose which move to make. Currently a 60% reveal vs 40% flag chance.
//            moveEndpoint = random.nextInt(10) > 6 ? "flag" : "reveal";
            final String moveEndpoint = "reveal"; //TODO CHANGE TO ABOVE

            //Package the move into a command, convert to JSON and send:
            object.addProperty("row", globalRow);
            object.addProperty("col", globalCol);
            object.addProperty("sessionID", playerClient.getSessionID());

            return new Command(CommandType.USER_SERVICE, moveEndpoint, object);
        }
    }

}
