package com.sorbonne.book_search_engine.algorithms.regex;

import java.util.ArrayList;

/*
    UTILITY CLASS
 */
public class RegExTree {

    protected int root;
    protected ArrayList<RegExTree> subTrees;

    public RegExTree(Integer root) {
        this.root = root;
        this.subTrees = new ArrayList<>();
    }

    public RegExTree(int root, ArrayList<RegExTree> subTrees) {
        this.root = root;
        this.subTrees = subTrees;
    }


    //FROM TREE TO PARENTHESIS
    public String toString() {
        if (subTrees.isEmpty())
            return rootToString();
        String result = rootToString() + "(" + subTrees.get(0).toString();
        for (int i = 1; i < subTrees.size(); i++)
            result += "," + subTrees.get(i).toString();
        return result + ")";
    }

    private String rootToString() {
        if (root == NodeEnum.CONCAT) return ".";
        if (root == NodeEnum.ETOILE) return "*";
        if (root == NodeEnum.ALTERN) return "|";
        if (root == NodeEnum.DOT) return ".";
        return Character.toString((char) root);
    }

    public RegExTree getLeftNode() {
        if (subTrees.size() == 0) {
            return null;
        }
        return subTrees.get(0);
    }

    public RegExTree getRightNode() {
        if (subTrees.size() <= 1) {
            return null;
        }
        return subTrees.get(1);
    }

    public void setRoot(int root) {
        this.root = root;
    }

    public void setSubTrees(ArrayList<RegExTree> subTrees) {
        this.subTrees = subTrees;
    }

    public int getRoot() {
        return root;
    }

    public RegExTree getStart() {
        return this;
    }

    public ArrayList<RegExTree> getSubTrees() {
        return subTrees;
    }

}
