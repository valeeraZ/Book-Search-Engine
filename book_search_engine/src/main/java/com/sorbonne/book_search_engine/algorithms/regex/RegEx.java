package com.sorbonne.book_search_engine.algorithms.regex;

import java.util.ArrayList;

public class RegEx {

    //REGEX
    private static String regEx;

    //CONSTRUCTOR
    public RegEx() {
    }

    //FROM REGEX TO SYNTAX TREE
    public static RegExTree parse(String regEx){
        ArrayList<RegExTree> result = new ArrayList<>();
        for (int i = 0; i < regEx.length(); i++)
            result.add(new RegExTree(charToRoot(regEx.charAt(i)), new ArrayList<>()));

        try {
            return parse(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int charToRoot(char c) {
        if (c == '.') return NodeEnum.DOT;
        if (c == '*') return NodeEnum.ETOILE;
        if (c == '|') return NodeEnum.ALTERN;
        if (c == '(') return NodeEnum.PARENTHESEOUVRANT;
        if (c == ')') return NodeEnum.PARENTHESEFERMANT;
        return c;
    }

    private static RegExTree parse(ArrayList<RegExTree> result) throws Exception {
        while (containParenthese(result))
            result = processParenthese(result);
        while (containEtoile(result))
            result = processEtoile(result);
        while (containConcat(result))
            result = processConcat(result);
        while (containAltern(result))
            result = processAltern(result);

        if (result.size() > 1) throw new Exception();

        return removeProtection(result.get(0));
    }

    private static boolean containParenthese(ArrayList<RegExTree> trees) {
        for (RegExTree t : trees)
            if (t.root == NodeEnum.PARENTHESEFERMANT || t.root == NodeEnum.PARENTHESEOUVRANT)
                return true;
        return false;
    }

    private static ArrayList<RegExTree> processParenthese(ArrayList<RegExTree> trees) throws Exception {
        ArrayList<RegExTree> result = new ArrayList<>();
        boolean found = false;
        for (RegExTree t : trees) {
            if (!found && t.root == NodeEnum.PARENTHESEFERMANT) {
                boolean done = false;
                ArrayList<RegExTree> content = new ArrayList<RegExTree>();
                while (!done && !result.isEmpty())
                    if (result.get(result.size() - 1).root == NodeEnum.PARENTHESEOUVRANT) {
                        done = true;
                        result.remove(result.size() - 1);
                    } else content.add(0, result.remove(result.size() - 1));
                if (!done) throw new Exception();
                found = true;
                ArrayList<RegExTree> subTrees = new ArrayList<>();
                subTrees.add(parse(content));
                result.add(new RegExTree(NodeEnum.PROTECTION, subTrees));
            } else {
                result.add(t);
            }
        }
        if (!found) throw new Exception();
        return result;
    }

    private static boolean containEtoile(ArrayList<RegExTree> trees) {
        for (RegExTree t : trees)
            if (t.root == NodeEnum.ETOILE && t.subTrees.isEmpty())
                return true;
        return false;
    }

    private static ArrayList<RegExTree> processEtoile(ArrayList<RegExTree> trees) throws Exception {
        ArrayList<RegExTree> result = new ArrayList<>();
        boolean found = false;
        for (RegExTree t : trees) {
            if (!found && t.root == NodeEnum.ETOILE && t.subTrees.isEmpty()) {
                if (result.isEmpty()) throw new Exception();
                found = true;
                RegExTree last = result.remove(result.size() - 1);
                ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
                subTrees.add(last);
                result.add(new RegExTree(NodeEnum.ETOILE, subTrees));
            } else {
                result.add(t);
            }
        }
        return result;
    }

    private static boolean containConcat(ArrayList<RegExTree> trees) {
        boolean firstFound = false;
        for (RegExTree t : trees) {
            if (!firstFound && !(t.root == NodeEnum.ALTERN)) {
                firstFound = true;
                continue;
            }
            if (firstFound)
                if (!(t.root == NodeEnum.ALTERN))
                    return true;
            else firstFound = false;
        }
        return false;
    }

    private static ArrayList<RegExTree> processConcat(ArrayList<RegExTree> trees) throws Exception {
        ArrayList<RegExTree> result = new ArrayList<RegExTree>();
        boolean found = false;
        boolean firstFound = false;
        for (RegExTree t : trees) {
            if (!found && !firstFound && !(t.root == NodeEnum.ALTERN)) {
                firstFound = true;
                result.add(t);
                continue;
            }
            if (!found && firstFound && t.root == NodeEnum.ALTERN) {
                firstFound = false;
                result.add(t);
                continue;
            }
            if (!found && firstFound && (t.root != NodeEnum.ALTERN)) {
                found = true;
                RegExTree last = result.remove(result.size() - 1);
                ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
                subTrees.add(last);
                subTrees.add(t);
                result.add(new RegExTree(NodeEnum.CONCAT, subTrees));
            } else {
                result.add(t);
            }
        }
        return result;
    }

    private static boolean containAltern(ArrayList<RegExTree> trees) {
        for (RegExTree t : trees)
            if (t.root == NodeEnum.ALTERN && t.subTrees.isEmpty()) return true;
        return false;
    }

    private static ArrayList<RegExTree> processAltern(ArrayList<RegExTree> trees) throws Exception {
        ArrayList<RegExTree> result = new ArrayList<RegExTree>();
        boolean found = false;
        RegExTree gauche = null;
        boolean done = false;
        for (RegExTree t : trees) {
            if (!found && t.root == NodeEnum.ALTERN && t.subTrees.isEmpty()) {
                if (result.isEmpty()) throw new Exception();
                found = true;
                gauche = result.remove(result.size() - 1);
                continue;
            }
            if (found && !done) {
                if (gauche == null) throw new Exception();
                done = true;
                ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
                subTrees.add(gauche);
                subTrees.add(t);
                result.add(new RegExTree(NodeEnum.ALTERN, subTrees));
            } else {
                result.add(t);
            }
        }
        return result;
    }

    private static RegExTree removeProtection(RegExTree tree) throws Exception {
        if (tree.root == NodeEnum.PROTECTION && tree.subTrees.size() != 1)
            throw new Exception();
        if (tree.subTrees.isEmpty())
            return tree;
        if (tree.root == NodeEnum.PROTECTION)
            return removeProtection(tree.subTrees.get(0));

        ArrayList<RegExTree> subTrees = new ArrayList<>();
        for (RegExTree t : tree.subTrees) subTrees.add(removeProtection(t));
        return new RegExTree(tree.root, subTrees);
    }
}

