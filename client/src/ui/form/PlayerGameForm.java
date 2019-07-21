package ui.form;

import model.*;
import ui.PlayerClient;
import ui.component.MinesweeperButton;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

public class PlayerGameForm extends JFrame {

    //UI
    private JPanel gamePanel;
    private MinesweeperButton[][] buttons;
    private static final int WINDOW_SIZE = 800;

    //Other
    private PlayerClient client;

    public PlayerGameForm(PlayerClient client, String playerName) {

        //Initialize UI:
        setTitle(playerName + " | Minesweeper");
        this.client = client;
        this.buttons = new MinesweeperButton[client.getPartialStatePreference().getHeight()][client.getPartialStatePreference().getWidth()];
        setSize(WINDOW_SIZE, WINDOW_SIZE);
        setLocationRelativeTo(null);
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Resizing:
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                MinesweeperButton.setIconSize(buttons[0][0].getWidth() - 8);
                MinesweeperButton.resizeIcons();
            }
        });

    }

    public void initialize() {
        gamePanel = new JPanel();
        gamePanel.setLayout(new GridLayout(client.getPartialStatePreference().getHeight(), client.getPartialStatePreference().getWidth()));
        for (int row = 0; row < client.getPartialStatePreference().getHeight(); row++) {
            for (int col = 0; col < client.getPartialStatePreference().getWidth(); col++) {
                MinesweeperButton button = new MinesweeperButton();
                buttons[row][col] = button;
                gamePanel.add(button);
            }
        }
        add(gamePanel);

        updateButtons();
        setVisible(true);
        MinesweeperButton.setIconSize(buttons[0][0].getWidth() - 8);
        MinesweeperButton.resizeIcons();
    }

    public void update() {
        switch (client.getGameState()) {
            case NOT_STARTED:
                JOptionPane.showMessageDialog(null, "Game not started", "Error", JOptionPane.WARNING_MESSAGE);
                dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                break;
            case STARTED:
            case ENDED_WON:
            case ENDED_LOST:
                updateButtons();
                break;
        }
    }

    private void updateButtons() {
        for (int row = 0; row < client.getPartialBoardState().getHeight(); row++) {
            for (int col = 0; col < client.getPartialBoardState().getWidth(); col++) {

                //Set background:
                switch (client.getGameState()) {
                    case NOT_STARTED:
                        break;
                    case STARTED:
                        buttons[row][col].setBackground(Color.LIGHT_GRAY);
                        //Set the background of blank revealed cells to a darker gray:
                        if (client.getPartialBoardState().getCells()[row][col].getRevealState() == RevealState.REVEALED_0) {
                            buttons[row][col].setBackground(Color.GRAY);
                        }
                        break;
                    case ENDED_WON:
                        buttons[row][col].setBackground(Color.GREEN);
                        break;
                    case ENDED_LOST:
                        buttons[row][col].setBackground(Color.RED);
                        break;
                }

                //Set the icon:
                switch (client.getPartialBoardState().getCells()[row][col].getRevealState()) {
                    case COVERED:
                    case REVEALED_0:
                        buttons[row][col].setIcon(null);
                        break;
                    case FLAGGED:
                        buttons[row][col].setIcon(MinesweeperButton.FLAG);
                        break;
                    case REVEALED_1:
                        buttons[row][col].setIcon(MinesweeperButton.ONE);
                        break;
                    case REVEALED_2:
                        buttons[row][col].setIcon(MinesweeperButton.TWO);
                        break;
                    case REVEALED_3:
                        buttons[row][col].setIcon(MinesweeperButton.THREE);
                        break;
                    case REVEALED_4:
                        buttons[row][col].setIcon(MinesweeperButton.FOUR);
                        break;
                    case REVEALED_5:
                        buttons[row][col].setIcon(MinesweeperButton.FIVE);
                        break;
                    case REVEALED_6:
                        buttons[row][col].setIcon(MinesweeperButton.SIX);
                        break;
                    case REVEALED_7:
                        buttons[row][col].setIcon(MinesweeperButton.SEVEN);
                        break;
                    case REVEALED_8:
                        buttons[row][col].setIcon(MinesweeperButton.EIGHT);
                        break;
                    case REVEALED_MINE:
                        buttons[row][col].setIcon(MinesweeperButton.MINE);
                        break;
                }

            }
        }
    }

}