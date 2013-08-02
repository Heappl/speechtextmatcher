package common.algorithms.hmm2;

import java.util.ArrayList;
import java.util.Iterator;

public class Node implements Iterable<Arc>
{
    private final State state;
    private final ArrayList<Arc> arcs;

    public Node(State state)
    {
        this.state = state;
        this.arcs = new ArrayList<Arc>();
    }
    public Node(State state, ArrayList<Arc> arcs)
    {
        this.state = state;
        this.arcs = arcs;
    }
    public void addArc(Arc arc)
    {
        this.arcs.add(arc);
    }

    @Override
    public Iterator<Arc> iterator()
    {
        return this.arcs.iterator();
    }

    public State getState()
    {
        return this.state;
    }
}
