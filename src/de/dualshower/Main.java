package de.dualshower;

public class Main {

    public static final int PLUS_INFINITY   = 100000;
    public static final int MINUS_INFINITY  = -100000;

    public static final int MAX_DEPTH = 3;

    public enum PIECE {EMPTY, PLAYER_1, PLAYER_2}


    /**
     * Main-function - reads parameters and controls program flow
     * @param args : args[0]: String of 1 to 64 characters( allowed are 0, 1, 2) representing the initial state of the Game
     *                        trailing zeros can be omitted.
     *               args[1]: either '1' or '2' - indicates the player at turn
     */
    public static void main(String[] args) {
        GameCube initialState = new GameCube();
        PIECE player;

        //parse args[0] to initialize GameCube
        inititializeCube(initialState, args[0]);


        switch(args[1].toCharArray()[0]) {
            case '1' : player = PIECE.PLAYER_1; break;
            case '2' : player = PIECE.PLAYER_2; break;
            default  : throw new IllegalArgumentException("Second Argument must be '1' or '2'");
        }

        //check initial state for win or loose conditions, skip calculation if found
        int initialScore = rateGameCube(initialState, player);
        if(initialScore == PLUS_INFINITY) {
            System.out.println("ALREADY WON");
            return;
        }
        if(initialScore == MINUS_INFINITY) {
            System.out.println("ALREADY LOST");
            return;
        }

        //initial state will be root of the state-tree
        TreeNode root = new TreeNode(initialState);
        //Build up the tree to the desired DEPTH
        buildTree(root, player, 0, MAX_DEPTH);
        //populate the nodes of the tree with their corresponding scores
        calculateScore(root, player, true);
        //retrieve the best possible move from the tree
        String bestMove = getBestMove(root);
        //return the best found move to the player
        System.out.println("Best move: " + bestMove);
    }

    /**
     * parses the args string and fills the cube accordingly
     * @param cube : The cube to be filled
     * @param args : String of 64 characters either 0,1 or 2. 0 -> empty, 1 -> player 1, 2 -> player 2
     */
    public static void inititializeCube(GameCube cube, String args) {
        PIECE piece;

        for(int index = 0; index < args.length(); index++) {
            switch(args.charAt(index)) {
                case '1' : piece = PIECE.PLAYER_1; break;
                case '2' : piece = PIECE.PLAYER_2; break;
                case '0' : continue;
                default: throw new IllegalArgumentException("Only '0', '1' and '2' are allowed in the input string.");
            }

            try {
                cube.placePiece(piece, index % 4, (index / 4) % 4);
            }
            catch (IllegalMoveException e) {
                throw new RuntimeException("Illegal move encountered while initializing cube");
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

            TreeNode newNode;

            PIECE nextPlayer;

            //invert current_player
            if(current_player == PIECE.PLAYER_1) {
                nextPlayer = PIECE.PLAYER_2;
            }
            else {
                nextPlayer = PIECE.PLAYER_1;
            }

            //iterate over all theoretically possible moves
            for(int i = 0; i < 16; i++) {
                //clone the current state of the game
                GameCube temp = root.getGameCube().clone();
                try {
                    //try to add a new piece
                    temp.placePiece(current_player,i % 4, i / 4);

                    newNode = new TreeNode(temp);
                    newNode.setMove("<" + i % 4 + "," + i / 4 + ">");

                    //recursively build the sub trees for new node
                    newNode = buildTree(newNode, nextPlayer, depth + 1, maxDepth);

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

        //end condition for recursion
        if(root.isLeaf()) {
            bestScore = rateGameCube(root.getGameCube(), player);
        }
        else {
            int score;

            //Max-state
            if(state) {
                bestScore = MINUS_INFINITY;
            }
            //Min-state
            else {
                bestScore = PLUS_INFINITY;
            }

            for(TreeNode node : root.getChildren()) {
                score = calculateScore(node, player, !state);

                //Max-state
                if(state) {
                    bestScore = Math.max(bestScore, score);
                }
                //Min-state
                else {
                    bestScore = Math.min(bestScore, score);
                }
            }
        }

        root.setScore(bestScore);
        return bestScore;
    }

    /**
     * Iterates over all children and finds the child with the best score
     * @param root : root-Node of the tree to search
     * @return : A String of the format <x,y> representing the best move found
     */
    public static String getBestMove(TreeNode root) {
        String bestMove = "";
        int bestScore = MINUS_INFINITY;

        //find best child
        for(TreeNode child : root.getChildren()) {
            if(child.getScore() > bestScore) {
                bestScore = child.getScore();
                bestMove = child.getMove();
            }
        }

        return bestMove;
    }

    /**
     * Calculates a rating for a given GameCube
     * @param gc : The GameCube to rate
     * @param player : The piece that the player is using
     * @return : MINUS_INFINITY if the player has lost, PLUS_INFINITY if the player has won, or score in between
     */
    public static int rateGameCube(GameCube gc, PIECE player) {
        int score = 0;
        int tmpScore;

        //check straight lines
        for(int a = 0; a < 4; a++) {
                for(int b = 0; b < 4; b++) {
                    
                    tmpScore = checkObstruction(gc, 0, 0, a, b, player);

                    if(tmpScore != MINUS_INFINITY && tmpScore != PLUS_INFINITY)
                    {   //No win/loss but possibly a score
                        score += tmpScore;
                    }
                    else {
                        return tmpScore;
                    }
                    
                    tmpScore = checkObstruction(gc, 1, a, 0, b, player);
                    if(tmpScore != MINUS_INFINITY && tmpScore != PLUS_INFINITY)
                    {   //No win/loss but possibly a score
                        score += tmpScore;
                    }
                    else {
                        return tmpScore;
                    }

                    tmpScore = checkObstruction(gc, 2, a, b, 0, player);
                    if(tmpScore != MINUS_INFINITY && tmpScore != PLUS_INFINITY)
                    {   //No win/loss but possibly a score
                        score += tmpScore;
                    }
                    else {
                        return tmpScore;
                    }

                }
        }

        //chekc diagonals
        for(int i = 0; i<4; i++)
        {
            PIECE[][] slice = sliceA(gc, i);
            for(int k = 0; k < 2; k++)
            {
                tmpScore = checkObstructionDiagonal(slice, player, k);
                if(tmpScore != MINUS_INFINITY && tmpScore != PLUS_INFINITY)
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
        for(int i = 0; i<4; i++)
        {
            PIECE[][] slice = sliceB(gc, i);
            for(int k = 0; k < 2; k++)
            {
                tmpScore = checkObstructionDiagonal(slice, player, k);
                if(tmpScore != MINUS_INFINITY && tmpScore != PLUS_INFINITY)
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
        for(int i = 0; i<4; i++)
        {
            PIECE[][] slice = sliceC(gc, i);
            for(int k = 0; k < 2; k++)
            {
                tmpScore = checkObstructionDiagonal(slice, player, k);
                if(tmpScore != MINUS_INFINITY && tmpScore != PLUS_INFINITY)
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
        for(int i = 0; i<2; i++)
        {
            PIECE[][] slice = sliceD(gc, i);
            for(int k = 0; k < 2; k++)
            {
                tmpScore = checkObstructionDiagonal(slice, player, k);
                if(tmpScore != MINUS_INFINITY && tmpScore != PLUS_INFINITY)
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

    /**
     * Checks one straight line in the cube for obstruction by the enemy player
     * @param gc : The GameCube to check in
     * @param variableDimension : The direction of the line
     * @param column : The column of the piece that marks the starting point of the line
     * @param row :    The row    of the piece that marks the starting point of the line
     * @param height : The height of the piece that marks the starting point of the line
     * @param player : The piece that the player is using
     * @return MINUS_INFINITY if the player has lost, PLUS_INFINITY if the player has won, or score in between
     */
    public static int checkObstruction(GameCube gc, int variableDimension, int column, int row, int height, PIECE player)
    {
        int hit = 0; //counts the non-empty pieces in the line
        int notObstructed = 0; //counts the empty or player pieces in the line
        PIECE   checkPiece = PIECE.EMPTY; //the piece that is currently checked

        //iterate through the line
        for(int i = 0; i < 4; i++)
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

        if(hit == 4) return PLUS_INFINITY; //Player has won in this line
        if(hit == -4) return MINUS_INFINITY; //Player has lost in this line
        if(notObstructed == 4) return hit + 1; //Line is not obstructed by enemy
        return 0; //Line is obstructed by the enemy
    }

    /**
     * Checks one diagonal in a slice of the cube for obstruction by the enemy player
     * @param slice : The 4x4 slice that should be searched
     * @param playerPiece : The piece that the player is using
     * @param diagonal : The direction of the diagonal
     * @return MINUS_INFINITY if the player has lost, PLUS_INFINITY if the player has won, or score in between
     */
    public static int checkObstructionDiagonal(PIECE[][] slice, PIECE playerPiece, int diagonal)
    {
        int hit = 0; //counts the non-empty pieces in the line
        int notObstructed = 0; //counts the empty and player pieces in the line
        
        //Left bottom to right top
        if(diagonal == 0)
        {
            for(int i = 0; i < 4; i++)
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
            if(diagonal == 1)
            {
                for(int i = 0, k = 3; i < 4 && k >= 0; i++, k--)
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
        
        if(hit == 4)    return PLUS_INFINITY; //Player has won in this line
        if(hit == -4)   return MINUS_INFINITY; //Player has lost in this line
        if(notObstructed == 4) return hit + 1; //Line is not obstructed by enemy
        return 0; //Line is obstructed by the enemy
    }

    /**
     * Cuts a slice from the given GameCube in orientation A
     * @param gc : The GameCube to slice
     * @param sliceDim : the number of the slice in that orientation
     * @return A 4x4 array of pieces, sliced from the cube
     */
    public static PIECE[][] sliceA(GameCube gc, int sliceDim)
    {
        PIECE[][] slice = new PIECE[4][4];
        for(int a = 0; a < 4; a++)
            for(int b = 0; b < 4; b++)
            {
                slice[a][b] = gc.getPiece(sliceDim, a, b);
            }
        return slice;
    }

    /**
     * Cuts a slice from the given GameCube in orientation B
     * @param gc : The GameCube to slice
     * @param sliceDim : the number of the slice in that orientation
     * @return A 4x4 array of pieces, sliced from the cube
     */
    public static PIECE[][] sliceB(GameCube gc, int sliceDim)
    {
        PIECE[][] slice = new PIECE[4][4];
        for(int a = 0; a < 4; a++)
            for(int b = 0; b < 4; b++)
            {
                slice[a][b] = gc.getPiece(a, sliceDim, b);
            }
        return slice;
    }

    /**
     * Cuts a slice from the given GameCube in orientation C
     * @param gc : The GameCube to slice
     * @param sliceDim : the number of the slice in that orientation
     * @return A 4x4 array of pieces, sliced from the cube
     */
    public static PIECE[][] sliceC(GameCube gc, int sliceDim)
    {
        PIECE[][] slice = new PIECE[4][4];
        for(int a = 0; a < 4; a++)
            for(int b = 0; b < 4; b++)
            {
                slice[b][a] = gc.getPiece(a, b, sliceDim);
            }
        return slice;
    }

    /**
     * Cuts a slice from the given GameCube in orientation D
     * @param gc : The GameCube to slice
     * @param sliceDim : the number of the slice in that orientation
     * @return A 4x4 array of pieces, sliced from the cube
     */
    public static PIECE[][] sliceD(GameCube gc, int sliceDim)
    {
        PIECE[][] slice = new PIECE[4][4];
     
        if(sliceDim == 1)
        {
            for(int a = 0, b = 0; a < 4 && b < 4; a++, b++)
            {
                for(int c = 0; c < 4; c++)
                {
                    slice[b][c] = gc.getPiece(a, b, c);
                }
            }
        }
        else
        {
            for(int a = 0, b = 3; a < 4 && b >= 0; a++, b--)
            {
                for(int c = 0; c < 4; c++)
                {
                    slice[a][c] = gc.getPiece(a, b, c);
                }
            }
        }
        return slice;
    }
}
