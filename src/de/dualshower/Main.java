package de.dualshower;

public class Main {

    public static final int PLUS_INFINITY   = 100000;
    public static final int MINUS_INFINITY  = -100000;

    public static final int MAX_DEPTH = 3;

    public enum PIECE {EMPTY, PLAYER_1, PLAYER_2}

    public static void main(String[] args) {
        GameCube initialState = new GameCube();

        //parse arguments, initialize GameCube
        inititializeCube(initialState, args[0]);

        PIECE player;

        switch(args[1].toCharArray()[0]) {
            case '1' : player = PIECE.PLAYER_1; break;
            case '2' : player = PIECE.PLAYER_2; break;
            default  : throw new IllegalArgumentException("Second Argument must be '1' or '2'");
        }

        int initialScore = rateGameCube(initialState, player);
        System.out.println("initialScore: " + initialScore);
        if(initialScore == PLUS_INFINITY) {
            System.out.println("ALREADY WON");
            return;
        }
        if(initialScore == MINUS_INFINITY) {
            System.out.println("ALREADY LOST");
            return;
        }

        TreeNode root = new TreeNode(initialState);
        buildTree(root, player, 0, MAX_DEPTH);
        calculateScore(root, player, true);
        String bestMove = getBestMove(root);

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

            if(current_player == PIECE.PLAYER_1) {
                nextPlayer = PIECE.PLAYER_2;
            }
            else {
                nextPlayer = PIECE.PLAYER_1;
            }

            for(int i = 0; i < 16; i++) {
                GameCube temp = root.getGameCube().clone();
                try {
                    temp.placePiece(current_player,i % 4, i / 4);

                    newNode = new TreeNode(temp);
                    newNode.setMove("<" + i % 4 + "," + i / 4 + ">");

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
        String bestMove = "";
        int bestScore = MINUS_INFINITY;

        for(TreeNode child : root.getChildren()) {
            System.out.println("Move: " + child.getMove() + ", score: " + child.getScore());
            if(child.getScore() > bestScore) {
                bestScore = child.getScore();
                bestMove = child.getMove();
            }
        }

        return bestMove + ": " + bestScore;
    }

    //check possible win situations
    //if player has already won -> return PLUS_INFINITY
    //if player has already lost -> return MINUS_INFINITY
    //otherwise return score 0-68
    public static int rateGameCube(GameCube gc, PIECE player) {
        int score = 0, tmpScore = 0;
 
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
    

    
    public static int checkObstructionDiagonal(PIECE[][] slice, PIECE playerPiece, int diagonal)
    {
        int hit = 0, notObstructed = 0;
        
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
        
        if(hit == 4)    return PLUS_INFINITY;
        if(hit == -4)   return MINUS_INFINITY;
        if(notObstructed == 4) return hit + 1;
        return 0;
    }
    
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
    
    public static int checkObstruction(GameCube gc, int variableDimension, int column, int row, int height, PIECE player)
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
            if(hit == 4)
            {
                return PLUS_INFINITY;
            }
            if(hit == -4)   return MINUS_INFINITY;
            if(notObstructed == 4) return hit + 1;
            return 0;
    }
}
