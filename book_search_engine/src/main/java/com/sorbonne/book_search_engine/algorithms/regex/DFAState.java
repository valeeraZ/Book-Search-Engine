package com.sorbonne.book_search_engine.algorithms.regex;

import java.util.*;

/**
 * Created by Wenzhuo Zhao on 02/10/2021.
 */
public class DFAState {
    /**
     * to have difference with numeric id, we use A-Z to represent each DFAState
     * (wish we won't have more than 26 DFAStates in one automaton...)
     */
    public static int counter = 65;

    /**
     * id of this DFAState
     */
    private final int id;

    /**
     * a subset of NFAStates
     */
    private final Set<NFAState> subset;

    /**
     * input symbols and next DFAStates
     */
    private final Map<Integer, DFAState> transitions;

    public DFAState() {
        id = counter++;
        subset = new HashSet<>();
        transitions = new HashMap<>();
    }

    public DFAState(Set<NFAState> subset) {
        id = counter++;
        this.subset = subset;
        transitions = new HashMap<>();
    }

    public Set<NFAState> getSubset() {
        return subset;
    }

    public Map<Integer, DFAState> getTransitions() {
        return transitions;
    }

    public void addTransition(int input, DFAState next) {
        this.transitions.put(input, next);
    }

    // get next state(s) by the input symbol
    public DFAState getTransition(int input) {
        return this.transitions.get(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DFAState dfaState = (DFAState) o;
        return Objects.equals(subset, dfaState.subset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subset);
    }

    @Override
    public String toString() {
        return print(new HashSet<DFAState>());
    }

    public String printSubset() {
        StringBuilder sb = new StringBuilder();
        sb.append((char) this.id).append(": ").append("{");
        for (NFAState state : subset) {
            sb.append(state.getId()).append(", ");
        }
        // remove the last 2 characters (',' and ' ')
        sb.delete(sb.length() - 2, sb.length());
        sb.append("}");
        return sb.toString();
    }

    public String print(HashSet<DFAState> visited) {
        if (!visited.add(this))
            return null;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, DFAState> entry : transitions.entrySet()) {
            DFAState state = entry.getValue();
            // (char): convert input symbol & id (A-Z) to ascii char
            sb.append(this.printSubset()).append(" -- ").append((char) entry.getKey().intValue()).append(" --> ").append(state.printSubset());
            sb.append("\n");

            String seq = state.print(visited);
            if (seq != null) {
                sb.append(seq);
            }

        }

        return sb.toString();
    }


}
