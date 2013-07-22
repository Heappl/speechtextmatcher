package phonemeAligner.singleGaussianBased;

import java.util.ArrayList;
import phonemeScorers.IPhonemeScorer;
import common.Alignment;
import common.AudioLabel;
import common.exceptions.ImplementationError;

public class GaussianPhonemeAligner
{
    private IPhonemeScorer[] phonemeScorers;
    
    public GaussianPhonemeAligner(IPhonemeScorer[] phonemeScorers)
    {
        this.phonemeScorers = phonemeScorers;
    }
    
    public Alignment align(
            String[] phonemes,
            ArrayList<double[]> audio,
            double startTime,
            double endTime) throws ImplementationError
    {
        double frameTime = (endTime - startTime) / audio.size();

        PhonemeSequenceScorer[] scorers = new PhonemeSequenceScorer[phonemes.length];
        for (int i = 0; i < scorers.length; ++i) {
            for (int j = 0; j < phonemeScorers.length; ++j) {
                if (!phonemeScorers[j].getPhoneme().equals(phonemes[i])) continue; 
                scorers[i] = new PhonemeSequenceScorer(
                        phonemes[i],
                        phonemeScorers[j],
                        (i == 0) ? 0 : Double.NEGATIVE_INFINITY,
                        startTime);
                break;
            }
            if (scorers[i] == null) {
                System.err.println("null: " + phonemes[i]);
            }
        }
        
        for (int i = 0; i < audio.size(); ++i) {
            PhonemeSequenceScorer[] newScorers = new PhonemeSequenceScorer[phonemes.length];
            for (int j = 0; j < newScorers.length; ++j) {
                newScorers[j] = new PhonemeSequenceScorer(scorers[j]);
            }
            for (int j = 0; j < scorers.length; ++j) {
                PhonemeSequenceScorer previous = (j > 0) ? scorers[j - 1] : null;
                double previousScore = (j > 0) ? previous.getScore() : Double.NEGATIVE_INFINITY;
                double currentTime = frameTime * i + startTime;
                newScorers[j].score(audio.get(i), currentTime, previous, previousScore);
            }
            scorers = newScorers;
        }
        
        return new Alignment(
                scorers[scorers.length - 1].getBestAlignment(endTime),
                scorers[scorers.length - 1].getScore() - phonemes.length * 100);
    }
    
    private class PhonemeSequenceScorer
    {
        private String phoneme;
        private IPhonemeScorer dataScorer;
        private PhonemeSequenceScorer previous = null;
        private double bestScore;
        private double bestStartTime;

        public PhonemeSequenceScorer(
            String phoneme,
            IPhonemeScorer gaussianMixturePhonemeScorer,
            double initialScore,
            double initialTime)
        {
            this.phoneme = phoneme;
            this.dataScorer = gaussianMixturePhonemeScorer;
            this.bestScore = initialScore;
            this.bestStartTime = initialTime;
        }

        public PhonemeSequenceScorer(PhonemeSequenceScorer previous)
        {
            this.phoneme = previous.phoneme;
            this.dataScorer = previous.dataScorer;
            this.previous = previous.previous;
            this.bestScore = previous.bestScore;
            this.bestStartTime = previous.bestStartTime;
        }

        public ArrayList<AudioLabel> getBestAlignment(double endTime)
        {
            ArrayList<AudioLabel> ret =
                (previous == null) ? new ArrayList<AudioLabel>() :
                    previous.getBestAlignment(bestStartTime);
            ret.add(new AudioLabel(phoneme, bestStartTime, endTime));
            return ret;
        }

        public void score(
            double[] audio, double currentFrameTime, PhonemeSequenceScorer previous, double previousScore) throws ImplementationError
        {
            double frameScore = this.dataScorer.score(audio);
            double noChangeScore = this.bestScore + frameScore;
            double changeScore = ((previous != null) ? previous.getScore() : Double.NEGATIVE_INFINITY) + frameScore;
            
            if (noChangeScore > changeScore) {
                this.bestScore = noChangeScore;
            } else {
                this.bestScore = changeScore;
                this.bestStartTime = currentFrameTime;
                this.previous = previous;
            }
        }
        public double getScore()
        {
            return bestScore;
        }
    }
}
