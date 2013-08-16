package textAligners;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import phonemeScorers.IPhonemeScorer;

import common.AudioLabel;
import common.exceptions.ImplementationError;

public class BreadthSearchBestCutPhonemeAligner
{
    private final static Comparator<CuttingScorer> scorerComparator =
        new Comparator<CuttingScorer>() {
            @Override
            public int compare(CuttingScorer o1, CuttingScorer o2)
            {
                if (o1.getScore() < o2.getScore()) return 1;
                if (o1.getScore() > o2.getScore()) return -1;
                return 0;
            }
        };
    
    private final Map<String, IPhonemeScorer> phonemeScorers;
    
    public BreadthSearchBestCutPhonemeAligner(IPhonemeScorer[] phonemeScorers)
    {
        this.phonemeScorers = new HashMap<String, IPhonemeScorer>();
        for (IPhonemeScorer scorer : phonemeScorers)
            this.phonemeScorers.put(scorer.getPhoneme(), scorer);
    }
    
    public ArrayList<AudioLabel> align(
            String[] phonemes,
            ArrayList<double[]> audio,
            double totalTime,
            int numOfScorers)
    {
        double frameTime = totalTime / audio.size();
        

        ScorerQueue queue = new ScorerQueue(numOfScorers);
        queue.add(new CuttingScorer(
                this.phonemeScorers.get(phonemes[0]), 0, null, 0, 0));
        
        System.err.println("audio size: " + audio.size() + " phonemes length: " + phonemes.length);
        for (int i = 0; i < audio.size(); ++i) {
            ScorerQueue nextQueue = new ScorerQueue(numOfScorers);
            
            for (CuttingScorer scorer : queue) {
                IPhonemeScorer nextScorer = (scorer.getIndex() + 1 >= phonemes.length) ? null :
                    this.phonemeScorers.get(phonemes[scorer.getIndex() + 1]);
                for (CuttingScorer nextCScorer : scorer.score(audio.get(i), i * frameTime, nextScorer))
                    nextQueue.add(nextCScorer);
            }
            
            queue = nextQueue;
            if ((i + 1) % 1000 == 0)
                System.err.println(i + " "  + queue.first().getScore() + " " + queue.last().getScore());
        }
        
        return queue.first().getBestAlignment(totalTime);
    }
    
    private class ScorerQueue implements Iterable<CuttingScorer>
    {
        private final int maxSize;
        private Map<Integer, CuttingScorer> scorersPerIndex = new HashMap<Integer, CuttingScorer>();
        
        private class ScoringIndex
        {
            final int index;
            final double score;
            public ScoringIndex(double score, int index)
            {
                this.index = index;
                this.score = score;
            }
        }
        private final Comparator<ScoringIndex> scorerComparator =
                new Comparator<ScoringIndex>() {
                    @Override
                    public int compare(ScoringIndex arg0, ScoringIndex arg1)
                    {
                        if (arg0.score < arg1.score) return 1;
                        if (arg0.score > arg1.score) return -1;
                        return 0;
                    }
                };
        
        private SortedSet<ScoringIndex> bestScoringIndexes = new TreeSet<ScoringIndex>(scorerComparator);
        
        public ScorerQueue(int maxSize)
        {
            this.maxSize = maxSize;
        }
        public CuttingScorer first()
        {
            return this.scorersPerIndex.get(this.bestScoringIndexes.first().index);
        }
        public CuttingScorer last()
        {
            return this.scorersPerIndex.get(this.bestScoringIndexes.last().index);
        }
        public void add(CuttingScorer scorer) {
            if (this.scorersPerIndex.containsKey(scorer.getIndex())) {
                double currentScore = this.scorersPerIndex.get(scorer.getIndex()).getScore();
                if (currentScore < scorer.getScore()) {
                    int previousSize = this.bestScoringIndexes.size();
                    this.bestScoringIndexes.remove(new ScoringIndex(currentScore, scorer.getIndex()));
                    if (this.bestScoringIndexes.size() + 1 != previousSize)
                        throw new ImplementationError("not removed from best scoring indexes");
                    this.bestScoringIndexes.add(new ScoringIndex(scorer.getScore(), scorer.getIndex()));
                    if (this.bestScoringIndexes.size() != previousSize)
                        throw new ImplementationError("not added to best scoring indexes");
                    this.scorersPerIndex.put(scorer.getIndex(), scorer);
                }
            } else if (this.bestScoringIndexes.size() < this.maxSize) {
                this.bestScoringIndexes.add(new ScoringIndex(scorer.getScore(), scorer.getIndex()));
                this.scorersPerIndex.put(scorer.getIndex(), scorer);
            } else if (this.bestScoringIndexes.last().score < scorer.getScore()) {
                this.bestScoringIndexes.add(new ScoringIndex(scorer.getScore(), scorer.getIndex()));
                this.scorersPerIndex.put(scorer.getIndex(), scorer);
                
                this.scorersPerIndex.remove(this.bestScoringIndexes.last().index);
                this.bestScoringIndexes.remove(this.bestScoringIndexes.last());
            }
        }
        @Override
        public Iterator<CuttingScorer> iterator()
        {
            return new Iterator<CuttingScorer>() {
                private Iterator<ScoringIndex> indexIterator = bestScoringIndexes.iterator();
                @Override
                public boolean hasNext()
                {
                    return indexIterator.hasNext();
                }
                @Override
                public CuttingScorer next()
                {
                    ScoringIndex nextIndex = indexIterator.next();
                    if (nextIndex == null) return null;
                    if (!scorersPerIndex.containsKey(nextIndex.index))
                        throw new ImplementationError("no such index in scorers per index");
                    return scorersPerIndex.get(nextIndex.index);
                }
                @Override
                public void remove()
                {
                    throw new ImplementationError("remove unimplemented");
                }
            };
        }
    }
    
    private class CuttingScorer
    {
        private final IPhonemeScorer dataScorer;
        private final CuttingScorer previous;
        private double scoreSoFar;
        private double startTime;
        private final int index;

        public CuttingScorer(
            IPhonemeScorer scorer,
            double scoreSoFar,
            CuttingScorer previous,
            double startTime,
            int index)
        {
            this.dataScorer = scorer;
            this.scoreSoFar = scoreSoFar;
            this.previous = previous;
            this.startTime = startTime;
            this.index = index;
        }

        public ArrayList<AudioLabel> getBestAlignment(double endTime)
        {
            ArrayList<AudioLabel> ret =
                (previous == null) ? new ArrayList<AudioLabel>() :
                    previous.getBestAlignment(this.startTime);
            ret.add(new AudioLabel(this.dataScorer.getPhoneme(), this.startTime, endTime));
            return ret;
        }

        public ArrayList<CuttingScorer> score(
            double[] audio,
            double currentFrameTime,
            IPhonemeScorer nextScorer) throws ImplementationError
        {
            this.scoreSoFar += this.dataScorer.score(audio);
            ArrayList<CuttingScorer> ret = new ArrayList<CuttingScorer>();
            ret.add(this);
            
            if (nextScorer != null) {
                double changeScore =
                        this.scoreSoFar + this.dataScorer.transitionScore() + nextScorer.score(audio);
                ret.add(new CuttingScorer(nextScorer, changeScore, this, currentFrameTime, this.index + 1));
            }
            
            return ret;
        }
        
        public double getScore()
        {
            return this.scoreSoFar;
        }
        public int getIndex()
        {
            return this.index;
        }
    }
}
