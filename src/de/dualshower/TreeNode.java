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

    public void addChild(TreeNode child) {
        children.add(child);
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public GameCube getGameCube() {
        return gameCube;
    }

    public void setMove(String move) {
        this.move = move;
    }

    public String getMove() {
        return move;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
