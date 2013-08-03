package common.algorithms.hmm;

import java.util.ArrayList;
import java.util.Iterator;

public class Node implements Iterable<Arc>
{
    private final State state;
    private final ArrayList<Arc> arcs;
    private final String name;

    public Node(String name, State state)
    {
        this.state = state;
        this.arcs = new ArrayList<Arc>();
        this.name = name;
    }
    public Node(String name, State state, ArrayList<Arc> arcs)
    {
        this.state = state;
        this.arcs = arcs;
        this.name = name;
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
    public String getName()
    {
        return this.name;
    }
}
