package de.dualshower;

import de.dualshower.Main.PIECE;
import de.dualshower.IllegalMoveException;

public class GameCube {
    private PIECE[][][] pieces = new PIECE[4][4][4]; //column, row, height

    public GameCube() {
        for(int a = 0; a < 4; a++) {
            for(int b = 0; b < 4; b++) {
                for(int c = 0; c < 4; c++) {
                    pieces[a][b][c] = PIECE.EMPTY;
                }
            }
        }
    }

    public void placePiece(PIECE piece, int column, int row) throws IllegalMoveException {
        int height;
        for(height = 0; height < 4; height++) {
            if(pieces[column][row][height] == PIECE.EMPTY) {
                pieces[column][row][height] = piece;
                return;
            }
        }

        if(height == 4) {
            throw new IllegalMoveException("Piece could not be placed in pillar: <" + column +"," + row + ">");
        }
    }
    
    public PIECE getPiece(int column, int row, int height)
    {
        return pieces[column][row][height];
    }

    public GameCube clone() {
        GameCube clone = new GameCube();

        for(int a = 0; a < 4; a++) {
            for(int b = 0; b < 4; b++) {
                for(int c = 0; c < 4; c++) {
                    try {
                        clone.placePiece(pieces[a][b][c], a, b);
                    }
                    catch(IllegalMoveException e) {
                        System.out.println("Error while cloning Cube: " + e.getMessage());
                    }
                }
            }
        }

        return clone;
    }
}
