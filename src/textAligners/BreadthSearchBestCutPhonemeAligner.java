package textAligners;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
        

        SortedSet<CuttingScorer> scorers = new TreeSet<CuttingScorer>(scorerComparator);
        scorers.add(new CuttingScorer(
                this.phonemeScorers.get(phonemes[0]), 0, null, 0, 0));
        
        System.err.println("audio size: " + audio.size() + " phonemes length: " + phonemes.length);
        for (int i = 0; i < audio.size(); ++i) {
            SortedSet<CuttingScorer> nextScorers = new TreeSet<CuttingScorer>(scorerComparator);
            
            for (CuttingScorer scorer : scorers) {
                IPhonemeScorer nextScorer = (scorer.getIndex() + 1 >= phonemes.length) ? null :
                    this.phonemeScorers.get(phonemes[scorer.getIndex() + 1]);
                nextScorers.addAll(scorer.score(audio.get(i), i * frameTime, nextScorer));
            }
            
            while (nextScorers.size() > numOfScorers) {
                nextScorers.remove(nextScorers.last());
            }
            scorers = nextScorers;
            if ((i + 1) % 1000 == 0)
                System.err.println(i + " "  + scorers.first().getScore() + " " + scorers.last().getScore());
        }
        
        return scorers.first().getBestAlignment(totalTime);
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
