package de.dualshower;

public class Main {

    public static final int PLUS_INFINITY   = 100000;
    public static final int MINUS_INFINITY  = -100000;

    public enum PIECE {EMPTY, CUBE, BALL};

    public static void main(String[] args) {

    }

    //check possible win situations
    //if player has already won -> return PLUS_INFINITY
    //if player has already lost -> return MINUS_INFINITY
    //otherwise return score 0-68
    public int rateGameCube(GameCube gc, PIECE player) {
        int score = 0, tmpScore = 0;
        
        for(int height = 0; height < 4; height++)
        {
            for(int column = 0; column < 4; column++)
            {
                for(int row = 0; row < 4; row++)
                {
                    tmpScore = rateWinPiece(gc, player, column, row, height);
                    
                    //Check for win/loss
                    if(tmpScore < 0 || tmpScore > 4)
                    {
                        return tmpScore;
                    }
                    else
                    {
                        //No win/loss but possibly a score
                        score += tmpScore;
                    }
                }
            }
        }
        
        return score;
    }
    
    public int rateWinPiece(GameCube gc, PIECE player, int column, int row, int height)
    {
        PIECE piece = gc.getPiece(column, row, height);
        int score = 0, tmpScore = 0;
        
        //Piece empty or enemy, no addition to score
        if(piece != player){
            return 0;
        }
        else
        {
            //Check all three dimensions for obstructions, wins or losses
            for(int i = 0; i < 3; i++)
            {
                tmpScore = checkObstruction(gc, i, column, row, height, player);
                
                if(tmpScore == 0 || tmpScore == 1)
                {
                    score += tmpScore;
                }
                else
                {
                    //Win or loss
                    return tmpScore;
                }
            }
            
            //Also check diagonals
            if(column == row && row == height)
            {
               //TODO math
            }
        }
        
        return score;
    }
    
    public int checkObstruction(GameCube gc, int variableDimension,int column, int row, int height, PIECE player)
    {
        int     i = 0, hit = 0, notObstructed = 0;
        PIECE   checkPiece = PIECE.EMPTY;
        
            //Check 
            for(i = 0; i < 4; i++)
            {
                switch(variableDimension)
                {
                    case 0:
                        checkPiece = gc.getPiece(i, row, height);
                    break;
                    case 1:
                        checkPiece = gc.getPiece(column, i, height);
                    break;
                    case 2:
                        checkPiece = gc.getPiece(column, row, i);
                    break;
                }
                
                if(checkPiece == player)
                { 
                    hit++;          
                }
                else if (checkPiece == PIECE.EMPTY)
                {
                    notObstructed++;
                }
                else
                {
                    hit--;
                }
            }
            if(hit == 4)    return PLUS_INFINITY;
            if(hit == -4)   return MINUS_INFINITY;
            if(notObstructed == 4) return 1;
            return 0;
    }
}
