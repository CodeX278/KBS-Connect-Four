package de.dualshower;

public class Main {

    public static final int PLUS_INFINITY   = 100000;
    public static final int MINUS_INFINITY  = -100000;

    public static final int MAX_DEPTH = 3;

    public enum PIECE {EMPTY, PLAYER_1, PLAYER_2}

    public static void main(String[] args) {
        GameCube initialState = new GameCube();
        PIECE player = PIECE.PLAYER_1; //set according to arguments

        //parse arguments, initialize GameCube
        inititalizeCube(initialState,"");

        TreeNode root = new TreeNode(initialState);
        buildTree(root, player, 1, MAX_DEPTH);
        calculateScore(root, player, true);
        String bestMove = getBestMove(root);

        System.out.println("Best move: " + bestMove);
    }

    /**
     * parses the args string and fills the cube accordingly
     * @param cube : The cube to be filled
     * @param args : String of 64 characters either 0,1 or 2. 0 -> empty, 1 -> player 1, 2 -> player 2
     */
    public static void inititalizeCube(GameCube cube, String args) {
        PIECE piece;

        for(int a = 0; a < 3; a++) {
            for(int b = 0; b < 3; b++) {
                for(int c = 0; c < 3; c++) {
                    switch(args.charAt(a * 16 +  b * 4 + c)) {
                        case '1' : piece = PIECE.PLAYER_1; break;
                        case '2' : piece = PIECE.PLAYER_2; break;
                        case '0' : piece = PIECE.EMPTY; break;
                        default: throw new IllegalArgumentException("Only '0', '1' and '2' are allowed in the input string.");
                    }

                    try {
                        cube.placePiece(piece, b, c);
                    }
                    catch (IllegalMoveException e) {
                        throw new RuntimeException("Illegal move encountered while initializing cube");
                    }
                }
            }
        }
    }

    /**
     * generates a Tree originating in the root node by iterating all possible moves
     * @param root : the root node of the tree
     * @param current_player : the player who's at turn
     * @param depth : the current depth of the tree
     * @param maxDepth : the maximum depth of the tree
     * @return : root with tree added below
     */
    public static TreeNode buildTree(TreeNode root, PIECE current_player, int depth, int maxDepth) {
        if(depth < maxDepth) {
            GameCube temp;
            TreeNode newNode;

            for(int i = 0; i < 16; i++) {
                temp = root.getGameCube().clone();
                try {
                    temp.placePiece(current_player,i % 4, i / 4);

                    if(current_player == PIECE.PLAYER_1) {
                        current_player = PIECE.PLAYER_2;
                    }
                    else {
                        current_player = PIECE.PLAYER_1;
                    }

                    newNode = new TreeNode(temp);
                    newNode.setMove("<" + i % 4 + "," + i / 4 + ">");

                    newNode = buildTree(newNode, current_player, depth + 1, maxDepth);

                    root.addChild(newNode);
                }
                catch (IllegalMoveException e) {
                    //do nothing, don't add child
                }
            }
        }

        return root;
    }

    /**
     * calculates the scores of the root node by calculating all child scores and propagating up
     * @param root : the node to calculate the score for
     * @param player : the piece that the player is using
     * @param state : true -> MAX-state, false -> MIN-STATE
     * @return : the score of root
     */
    public static int calculateScore(TreeNode root, PIECE player, boolean state) {
        int bestScore;

        if(root.isLeaf()) {
            bestScore = rateGameCube(root.getGameCube(), player);
        }
        else {
            int score;

            if(state) {
                bestScore = MINUS_INFINITY;
            }
            else {
                bestScore = PLUS_INFINITY;
            }

            for(TreeNode node : root.getChildren()) {
                score = calculateScore(node, player, !state);

                if(state) {
                    bestScore = Math.max(bestScore, score);
                }
                else {
                    bestScore = Math.min(bestScore, score);
                }
            }
        }

        root.setScore(bestScore);
        return bestScore;
    }


    public static String getBestMove(TreeNode root) {
        String bestMove = "LOST";
        int bestScore = MINUS_INFINITY;

        for(TreeNode child : root.getChildren()) {
            if(child.getScore() > bestScore) {
                bestScore = child.getScore();
                bestMove = child.getMove();
            }
        }

        return bestMove;
    }

    //check possible win situations
    //if player has already won -> return PLUS_INFINITY
    //if player has already lost -> return MINUS_INFINITY
    //otherwise return score 0-68
    public static int rateGameCube(GameCube gc, PIECE player) {
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
            for(int i = 0; i<4; i++)
            {
                tmpScore = checkObstructionDiagonal(sliceA(gc, row), column, height);
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
        }
        
        return score;
    }
    
    public int checkObstructionDiagonal(PIECE[][] slice, int column, int row)
    {
        PIECE playerPiece = slice[column][row];
        int hit = 0, notObstructed = 0;
        
        //Left bottom to right top
        if(column == row)
        {
            for(int i = 0; i < 3; i++)
            {
                if(slice[i][i] == playerPiece) {
                    hit++;
                    notObstructed++;
                }
                else if(slice[i][i] == PIECE.EMPTY)
                {
                   notObstructed++;
                }
                else
                {
                    hit--;
                }
            }
        }
        else
        {
            if(column + row == 3)
            {
                for(int i = 0, k = 3; i < 3 && k>1; i++, k--)
                {
                    if(slice[i][k] == playerPiece) {
                        hit++;
                        notObstructed++;
                    }
                    else if(slice[i][k] == PIECE.EMPTY)
                    {
                       notObstructed++;
                    }
                    else
                    {
                        hit--;
                    }
                }


            }
        }
        
        if(hit == 4)    return PLUS_INFINITY;
        if(hit == -4)   return MINUS_INFINITY;
        if(notObstructed == 4) return 1;
        return 0;
    }
    
    public PIECE[][] sliceA(GameCube gc, int sliceDim)
    {
        PIECE[][] slice = new PIECE[4][4];
        for(int column = 0; column < 4; column++)
            for(int height = 0; height < 4; height++)
            {
                slice[column][height] = gc.getPiece(sliceDim, column, height);
            }
        return slice;
    }
    
    public PIECE[][] sliceB(GameCube gc, int sliceDim)
    {
        PIECE[][] slice = new PIECE[4][4];
        for(int row = 0; row < 4; row++)
            for(int height = 0; height < 4; height++)
            {
                slice[row][height] = gc.getPiece(row, sliceDim, height);
            }
        return slice;
    }
    
    public PIECE[][] sliceC(GameCube gc, int sliceDim)
    {
        PIECE[][] slice = new PIECE[4][4];
        for(int row = 0; row < 4; row++)
            for(int column = 0; column < 4; column++)
            {
                slice[column][row] = gc.getPiece(row, column, sliceDim);
            }
        return slice;
    }
    
    public int checkObstruction(GameCube gc, int variableDimension, int column, int row, int height, PIECE player)
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
                    notObstructed++;
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
