package common.algorithms.hmm2;

public class Arc
{
    private final StateExit stateExit;
    private final Node toNode;
    
    public Arc(StateExit stateExit, Node targetNode)
    {
        this.stateExit = stateExit;
        this.toNode = targetNode;
    }

    public Node getLeadingToNode()
    {
        return this.toNode;
    }

    public StateExit getExit()
    {
        return stateExit;
    }
}
