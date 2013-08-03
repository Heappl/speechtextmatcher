package phonemeAligner.hmmBased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.algorithms.gaussian.MixtureGaussianModel;
import common.algorithms.hmm.Arc;
import common.algorithms.hmm.ITrainableObservationLogLikelihoodCalculator;
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
        public StateElements()
        {
            ArrayList<StateExit> exits = new ArrayList<StateExit>();
            exits.add(nextExit);
            exits.add(loopExit);
            state = new State(exits, new GaussianObservationScorer());
        }
    }
    private Map<String, StateElements> phonemStates = new HashMap<String, StateElements>();
    
    public Node createNode(Node next, String phoneme)
    {
        StateElements phonemeStateElements = getOrCreate(phoneme);
        Node ret = new Node(phonemeStateElements.state);
        ret.addArc(new Arc(phonemeStateElements.loopExit, ret));
        ret.addArc(new Arc(phonemeStateElements.nextExit, next));
        return ret;
    }

    private StateElements getOrCreate(String phoneme)
    {
        if (!this.phonemStates.containsKey(phoneme))
            this.phonemStates.put(phoneme, new StateElements());
        return this.phonemStates.get(phoneme);
    }
}
