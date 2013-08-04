package common.algorithms.hmm;

public class Arc
{
    private final StateExit stateExit;
    private final Node toNode;
    private final Node fromNode;
    
    public Arc(StateExit stateExit, Node targetNode, Node sourceNode)
    {
        this.stateExit = stateExit;
        this.toNode = targetNode;
        this.fromNode = sourceNode;
    }

    public Node getLeadingToNode()
    {
        return this.toNode;
    }
    public Node getOutgoingFromNode()
    {
        return this.fromNode;
    }

    public StateExit getExit()
    {
        return stateExit;
    }
    
    public boolean isExitState()
    {
        return (this.toNode == null);
    }
}
