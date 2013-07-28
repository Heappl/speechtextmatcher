package common.algorithms.hmm;

import java.util.ArrayList;

public class HMMState
{
    private IObservationScorer scorer;
    private ArrayList<HMMArc> outgoingArcs;
    
    public HMMState(IObservationScorer scorer)
    {
        this.scorer = scorer;
    }
    public HMMState(IObservationScorer scorer, ArrayList<HMMArc> arcs)
    {
        this.scorer = scorer;
        this.outgoingArcs = arcs;
    }
    
    public HMMState(String string)
    {
        // TODO Auto-generated constructor stub
    }
    public void addArc(HMMArc arc)
    {
        this.outgoingArcs.add(arc);
    }
    
    public HMMArc[] getOutgoingArcs()
    {
        return this.outgoingArcs.toArray(new HMMArc[0]);
    }
    public String getName()
    {
        return scorer.getName();
    }
}
