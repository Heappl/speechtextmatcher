package common.algorithms.hmm;

public class HmmPathArc
{
    public HMMPathNode to;
    private int arcSet;

    public HmmPathArc(HMMPathNode to, int set)
    {
        this.to = to;
        this.arcSet = set;
    }
}
