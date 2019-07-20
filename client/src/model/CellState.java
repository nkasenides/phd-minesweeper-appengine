package model;

public class CellState {

    private boolean isMined;
    private RevealState revealState;

    public CellState(boolean isMined) {
        this.isMined = isMined;
        this.revealState = RevealState.COVERED;
    }

    public boolean isMined() {
        return isMined;
    }

    public RevealState getRevealState() {
        return revealState;
    }

    public void setMined(boolean mined) {
        isMined = mined;
    }

    public void setRevealState(RevealState revealState) {
        this.revealState = revealState;
    }

}
