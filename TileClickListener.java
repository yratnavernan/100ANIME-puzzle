package puzzlegame;

import java.awt.event.*;

public class TileClickListener implements ActionListener {
    private PuzzleGame game;
    private int row, col;

    public TileClickListener(PuzzleGame game, int row, int col) {
        this.game = game;
        this.row = row;
        this.col = col;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        game.moveTile(row, col);
    }
}
