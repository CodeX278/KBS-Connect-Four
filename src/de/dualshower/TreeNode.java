package de.dualshower;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private int score;
    private GameCube gameCube;
    private List<TreeNode> children;
    private String move;

    public TreeNode(GameCube gc) {
        children = new ArrayList<TreeNode>();
        gameCube = gc;
    }

    /**
     * Adds a new child to the node
     * @param child : The child to add
     */
    public void addChild(TreeNode child) {
        children.add(child);
    }

    /**
     * Returns all children of the node
     * @return : The list of all children of the node
     */
    public List<TreeNode> getChildren() {
        return children;
    }

    /**
     * Checks if the node is a leaf of the tree
     * @return : true if the node is a leaf, false otherwise
     */
    public boolean isLeaf() {
        return children.size() == 0;
    }

    /**
     * Gets the GameCube associated with this node
     * @return : the GameCube of this node
     */
    public GameCube getGameCube() {
        return gameCube;
    }

    /**
     * Sets the last move that led to the GameCube in this node
     * @param move : A String indicating the last move that led to this state
     */
    public void setMove(String move) {
        this.move = move;
    }

    /**
     * Gets the last move that lead to the GameCube associated with this node
     * @return : The last move that lead to this state
     */
    public String getMove() {
        return move;
    }

    /**
     * Sets the score that is associated with this node
     * @param score : the score to set
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * gets the score of this node
     * @return : the score of this node
     */
    public int getScore() {
        return score;
    }
}
