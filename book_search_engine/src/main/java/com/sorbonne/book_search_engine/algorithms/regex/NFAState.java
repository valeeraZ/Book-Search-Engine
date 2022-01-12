package com.sorbonne.book_search_engine.algorithms.regex;

import java.util.*;

/**
 * Created by Wenzhuo Zhao on 28/09/2021.
 */
public class NFAState {
    public static int counter = 0;

    private final int id;

    // input symbols and next states
    private final Map<Integer, NFAState> transitions;

    // non input symbol (epsilon), only next states
    private final Set<NFAState> epsilonTransitions;

    public NFAState(){
        id = counter++;
        transitions = new HashMap<>();
        epsilonTransitions = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public Map<Integer, NFAState> getTransitions() {
        return transitions;
    }

    /**
     * get next state(s) by epsilon found in transitions
     * @return a set NFAState
     */
    public Set<NFAState> getEpsilonTransitions() {
        return epsilonTransitions;
    }

    /**
     * add a transition(input symbol, next NFAState) to this
     * @param input an input symbol in all the 256 ascii char
     * @param next the next NFAState that `this` will go to via the input
     */
    public void addTransition(int input, NFAState next){
        transitions.put(input, next);
    }

    /**
     * add an epsilon transition - no input, only the next state
     * @param next the NFAState which `this` goes to via an epsilon
     */
    public void addTransition(NFAState next){
        this.epsilonTransitions.add(next);
    }

    /**
     * get next state by the input symbol found in transitions
     * @param input an input symbol in all the 256 ascii char
     * @return a set NFAState
     */
    public NFAState getTransition(int input){
        return this.transitions.get(input);
    }

    private Set<NFAState> search(int input){
        NFAState stateViaInput = this.getTransition(input);
        if (stateViaInput == null)
            return new HashSet<>();

        Set<NFAState> res = new HashSet<>();
        res.add(stateViaInput);
        res.addAll(stateViaInput.search(input));

        return res;
    }

    /**
     * from a set of NFAStates, move via the input, get a new set of NFAStates
     * the subset makes a new DFAState
     * @param input an input symbol in all the 256 ascii char
     * @return a subset of NFAStates
     */
    public static Set<NFAState> move(int input, Set<NFAState> dfa){
        Set<NFAState> subset = new HashSet<>();

        for(NFAState nfaState : dfa){
            subset.addAll(nfaState.search(input));
        }
        return subset;
    }

    /**
     * a set of NFAStates, which departing from `this` NFAState, does a Kleene Closure of epsilon, can get
     * @return a set of NFAStates
     */
    public Set<NFAState> epsilonClosure(){
        Set<NFAState> res = new HashSet<>(this.epsilonTransitions);
        // you can't operate res while iterating res -- ConcurrentModificationException
        // so use a tmp variable
        Set<NFAState> tmp = new HashSet<>();
        for (NFAState next: res) {
            tmp.addAll(next.epsilonClosure());
        }
        res.add(this);
        res.addAll(tmp);
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NFAState state = (NFAState) o;
        return id == state.id ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString(){
        return print(new HashSet<>());
    }

    public String print(HashSet<NFAState> visited) {
        if (!visited.add(this))
            return null;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, NFAState> entry: transitions.entrySet()) {
            NFAState state = entry.getValue();
            // (char): convert input symbol to ascii char
            sb.append(this.id).append(" -- ").append((char)entry.getKey().intValue()).append(" --> ").append(state.id);
            sb.append("\n");


            String seq = state.print(visited);
            if(seq != null){
                sb.append(seq);
            }

        }

        // and epsilon transitions
        for(NFAState state : epsilonTransitions){
            sb.append(this.id).append(" -- ").append("Ɛ").append(" --> ").append(state.id);
            sb.append("\n");
        }
        for (NFAState state: epsilonTransitions) {
            String seq = state.print(visited);
            if(seq != null){
                sb.append(seq);
            }
        }

        return sb.toString();
    }
}
