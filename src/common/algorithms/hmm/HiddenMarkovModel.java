package common.algorithms.hmm;


public class HiddenMarkovModel
{
    private HMMNode[] nodes;
    private int maxNodesNumOfArcs = Integer.MIN_VALUE;
    
    public HiddenMarkovModel(HMMNode[] nodes)
    {
        this.nodes = nodes;
        for (HMMNode node : nodes)
            if (node.getNumOfArcs() > this.maxNodesNumOfArcs)
                this.maxNodesNumOfArcs = node.getNumOfArcs();
    }
    public HMMNodesProbabilities[] calculateStatesProbabilites(double[][] sequence, HMMPathGraph sequenceGraph)
    {
        // TODO Auto-generated method stub
        double[][][] ret = new double[sequence.length][nodes.length][maxNodesNumOfArcs];
        return new HMMNodesProbabilities[0];
    }

    public HMMResultSequence calculateMostProbableSequence(double[][] audioData, HMMPathGraph sequenceGraph)
    {
        // TODO Auto-generated method stub
        return null;
    }
    public int getNumOfNodes()
    {
        return this.nodes.length;
    }
    public int getMaxNumOfExitArcs()
    {
        return this.maxNodesNumOfArcs;
    }
}
