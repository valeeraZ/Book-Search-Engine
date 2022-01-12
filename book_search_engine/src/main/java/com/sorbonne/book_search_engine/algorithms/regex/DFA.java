package com.sorbonne.book_search_engine.algorithms.regex;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by Wenzhuo Zhao on 02/10/2021.
 * Deterministic finite automaton
 * Modified by Chengyu Yang
 */
public class DFA {
    // relatively, root is the start state of an automate,
    // end is the accepting state of an automate
    private final DFAState root;

    private final Set<DFAState> acceptings;

    static Set<DFAState> allDFAStates = new HashSet<>();

    public DFA(DFAState root, Set<DFAState> acceptings) {
        this.root = root;
        this.acceptings = acceptings;
    }

    public DFAState getRoot() {
        return root;
    }

    public Set<DFAState> getAcceptings() {
        return acceptings;
    }

    /**
     * from NFA to DFA
     *
     * @param nfa a NFA
     * @return a DFA, without epsilon transition
     */
    public static DFA fromNFAtoDFA(NFA nfa) {
        Set<Integer> inputSymbols = nfa.getInputSymbols();
        NFAState root = nfa.getRoot();
        NFAState accepting = nfa.getAccepting();

        DFAState start = new DFAState(root.epsilonClosure());
        Set<DFAState> accepts = new HashSet<>();

        // use HashSet to add only new DFAStates
        //Set<DFAState> allDFAStates = new HashSet<>();
        allDFAStates.add(start);

        Queue<DFAState> queue = new LinkedList<>();
        queue.offer(start);

        while (!queue.isEmpty()) {
            DFAState current = queue.poll();
            for (Integer input : inputSymbols) {
                Set<NFAState> set = getNextSubStates(current, input);
                if (!set.isEmpty()) {
                    DFAState next = new DFAState(set);
                    if (!allDFAStates.add(next)) {
                        for (DFAState same : allDFAStates) {
                            if (same.equals(next)) {
                                // cycle transition
                                current.addTransition(input, same);
                                break;
                            }
                        }
                        DFAState.counter--;
                    } else {
                        // check if the `next` DFAState is an accepting state
                        // which means it contains the accepting NFAState of the NFA
                        if (next.getSubset().contains(accepting)) {
                            accepts.add(next);
                        }
                        queue.offer(next);
                        current.addTransition(input, next);
                    }
                }
            }
        }
        // Minimize DFA
        // divide the state in 2 parts
        // one for all ends and one for the others
        Queue<Set<DFAState>> worklist = new LinkedList<>();
        Set<DFAState> union1 = new HashSet<>();
        for (DFAState state : allDFAStates) {
            if (!accepts.contains(state)) {
                union1.add(state);
            }
        }
        // add two parts in worklist
        worklist.add(union1);
        worklist.add(accepts);
        while (!worklist.isEmpty()) {
            Set<DFAState> current = worklist.poll();
            // find if their next step still in the union
            Set<DFAState> union2 = new HashSet<>();
            for (Integer input : inputSymbols) {

                Set<DFAState> in_union = new HashSet<>(current);
                // once found, divide the union
                Set<DFAState> tmp = new HashSet<>();
                for (DFAState st : current) {
                    if (!in_union.contains(st.getTransition(input))) {
                        union2.add(st);
                        tmp.add(st);
                    }
                }
                current.removeAll(tmp);
            }
            // add the union into worklist
            if (union2.size() > 1) {
                worklist.add(union2);
            }

            // now for all state in current
            // their next step is in the union
            // so we can consider they are the same state

            if (current.size() < 2) {
                break;
            }
            // change the transitions for each state
            for (DFAState state : allDFAStates) {
                for (Integer input : inputSymbols) {
                    DFAState next = state.getTransitions().get(input);
                    if (next == null)
                        continue;
                    if (current.contains(next)) {
                        state.getTransitions().remove(input);
                        Set<DFAState> copy = new HashSet<>(current);
                        copy.remove(next);
                        state.getTransitions().put(input, copy.iterator().next());
                    }
                }
            }

        }

        return new DFA(start, accepts);
    }

    private static Set<NFAState> getNextSubStates(DFAState state, int input) {
        Set<NFAState> subset = new HashSet<>();
        Set<NFAState> tmp = NFAState.move(input, state.getSubset());
        for (NFAState nfaState : tmp) {
            subset.addAll(nfaState.epsilonClosure());
        }

        return subset;
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
