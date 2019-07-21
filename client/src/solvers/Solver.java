package solvers;

import com.sun.istack.internal.NotNull;
import model.Command;
import model.Move;
import model.PartialBoardState;
import ui.PlayerClient;

public interface Solver {

    public abstract Move solve(@NotNull PartialBoardState partialBoardState, PlayerClient playerClient);

}
