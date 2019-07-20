package solvers;

import com.sun.istack.internal.NotNull;
import model.Command;
import model.PartialBoardState;
import ui.PlayerClient;

public interface Solver {

    public abstract Command solve(@NotNull PartialBoardState partialBoardState, PlayerClient playerClient);

}
