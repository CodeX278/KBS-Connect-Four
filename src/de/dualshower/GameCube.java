package de.dualshower;

import de.dualshower.Main.PIECE;
import de.dualshower.IllegalMoveException;

public class GameCube {
    private PIECE[][][] pieces = new PIECE[4][4][4]; //column, row, height

    /**
     * Initializes the new GameCube to be completely empty
     */
    public GameCube() {
        for(int a = 0; a < 4; a++) {
            for(int b = 0; b < 4; b++) {
                for(int c = 0; c < 4; c++) {
                    pieces[a][b][c] = PIECE.EMPTY;
                }
            }
        }
    }

    /**
     * Adds a new piece at the specified position and simulates it falling down to the correct position
     * @param piece : The type of piece to add
     * @param column : The column that the piece should be added in
     * @param row : The row that the piece should be added in
     * @throws IllegalMoveException : If the vertical pillar is already full
     */
    public void placePiece(PIECE piece, int column, int row) throws IllegalMoveException {
        int height;
        //find the lowest possible positon in the pillar
        for(height = 0; height < 4; height++) {
            if(pieces[column][row][height] == PIECE.EMPTY) {
                pieces[column][row][height] = piece;
                return;
            }
        }

        //no position found, throw exception
        throw new IllegalMoveException("Piece could not be placed in pillar: <" + column +"," + row + ">");
    }

    /**
     * returns the piece located at <column, row, height>
     * @param column : The column of the piece to be returned
     * @param row : The row of the piece to be returned
     * @param height : The height of the piece to be returned
     * @return : The PIECE at the specified positon
     */
    public PIECE getPiece(int column, int row, int height)
    {
        return pieces[column][row][height];
    }

    /**
     * Creates a new GameCube that is identical to this one
     * @return A Copy of this GameCube
     */
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
