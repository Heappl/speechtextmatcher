package phonemeAligner.hmmBased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.algorithms.hmm.Arc;
import common.algorithms.hmm.Node;
import common.algorithms.hmm.State;
import common.algorithms.hmm.StateExit;

public class MapOfPhonemeStates
{
    private class StateElements
    {
        StateExit loopExit = new StateExit();
        StateExit nextExit = new StateExit();
        State state;
        public StateElements(MultipleGaussianTrainer trainer)
        {
            ArrayList<StateExit> exits = new ArrayList<StateExit>();
            exits.add(nextExit);
            exits.add(loopExit);
            state = new State(
                exits,
                new GaussianObservationScorer());
        }
    }
    private Map<String, StateElements> phonemStates = new HashMap<String, StateElements>();
    private MultipleGaussianTrainer trainer = new MultipleGaussianTrainer();
    
    public Node createNode(Node next, String phoneme)
    {
        StateElements phonemeStateElements = getOrCreate(phoneme);
        Node ret = new Node(phoneme, phonemeStateElements.state);
        ret.addArc(new Arc(phonemeStateElements.loopExit, ret, ret));
        ret.addArc(new Arc(phonemeStateElements.nextExit, next, ret));
        return ret;
    }

    private StateElements getOrCreate(String phoneme)
    {
        if (!this.phonemStates.containsKey(phoneme))
            this.phonemStates.put(phoneme, new StateElements(trainer));
        return this.phonemStates.get(phoneme);
    }
}
