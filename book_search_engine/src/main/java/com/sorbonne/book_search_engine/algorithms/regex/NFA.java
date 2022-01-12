package com.sorbonne.book_search_engine.algorithms.regex;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Wenzhuo Zhao on 28/09/2021.
 * Nondeterministic finite automaton
 */
public class NFA {
    // 256 ASCII chars
    private final static int COL = 256;

    // relatively, root is the start state of an automate,
    // end is the accepting state of an automate
    private final NFAState root;
    private final NFAState accepting;
    private Set<Integer> inputSymbols;

    public NFA(NFAState root, NFAState accepting, Set<Integer> inputSymbols){
        this.root = root;
        this.accepting = accepting;
        this.inputSymbols = inputSymbols;
    }

    public NFAState getRoot() {
        return root;
    }

    public NFAState getAccepting() {
        return accepting;
    }

    public static NFA fromRegExTreeToNFA(RegExTree ret){
        if(ret.subTrees.isEmpty()){
            NFAState start_state = new NFAState();
            NFAState final_state = new NFAState();
            Set<Integer> inputSymbols = new HashSet<>();
            if (ret.root != NodeEnum.DOT){
                // only 1 transition
                start_state.addTransition(ret.root, final_state);
                inputSymbols.add(ret.root);
            }else {
                for (int i = 0; i < COL; i++) {
                    // all the 256 transitions
                    start_state.addTransition(i, final_state);
                    inputSymbols.add(i);
                }
            }
            return new NFA(start_state, final_state, inputSymbols);
        }

        if(ret.root == NodeEnum.CONCAT){
            // from left's end to right's start
            NFA left = fromRegExTreeToNFA(ret.subTrees.get(0));
            NFA right = fromRegExTreeToNFA(ret.subTrees.get(1));
            left.accepting.addTransition(right.root);
            Set<Integer> inputSymbols = new HashSet<>();
            inputSymbols.addAll(left.inputSymbols);
            inputSymbols.addAll(right.inputSymbols);
            return new NFA(left.root, right.accepting, inputSymbols);
        }

        if (ret.root == NodeEnum.ALTERN){
            // from a new state to left's start and right's start
            // and connect the ends of left and right to end state
            NFAState start_state = new NFAState();
            NFA left = fromRegExTreeToNFA(ret.subTrees.get(0));
            NFA right = fromRegExTreeToNFA(ret.subTrees.get(1));

            Set<Integer> inputSymbols = new HashSet<>();
            inputSymbols.addAll(left.inputSymbols);
            inputSymbols.addAll(right.inputSymbols);

            NFAState end_state = new NFAState();

            start_state.addTransition(left.root);
            start_state.addTransition(right.root);
            left.accepting.addTransition(end_state);
            right.accepting.addTransition(end_state);

            return new NFA(start_state, end_state, inputSymbols);
        }

        if (ret.root == NodeEnum.ETOILE){
            // see fig 10.31:
            NFAState start_state = new NFAState();
            NFA left = fromRegExTreeToNFA(ret.subTrees.get(0));
            NFAState end_state = new NFAState();

            start_state.addTransition(left.root);
            start_state.addTransition(end_state);
            left.accepting.addTransition(left.root);
            left.accepting.addTransition(end_state);

            Set<Integer> inputSymbols = new HashSet<>(left.inputSymbols);

            return new NFA(start_state, end_state, inputSymbols);
        }

        return new NFA(new NFAState(), new NFAState(), new HashSet<>());
    }

    public Set<Integer> getInputSymbols(){
        return this.inputSymbols;
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
