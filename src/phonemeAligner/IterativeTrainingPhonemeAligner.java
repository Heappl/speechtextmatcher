package phonemeAligner;

import java.util.ArrayList;

import algorithms.DataByTimesExtractor;

import graphemesToPhonemesConverters.IWordToPhonemesConverter;

import common.AudioLabel;
import common.DataSequence;
import common.GenericDataContainer;
import common.GenericListContainer;
import commonExceptions.ImplementationError;

public class IterativeTrainingPhonemeAligner
{
    private AudioLabel[] words;
    ArrayList<double[]> data;
    IWordToPhonemesConverter converter;
    double totalTime;
    double frameTime;
    DataByTimesExtractor<double[]> dataExtractor;

    public IterativeTrainingPhonemeAligner(
            AudioLabel[] words,
            ArrayList<double[]> allData,
            IWordToPhonemesConverter converter,
            double totalTime)
    {
        this.words = words;
        this.converter = converter;
        this.totalTime = totalTime;
        this.frameTime = totalTime / allData.size();
        
        this.data = allData;

        this.dataExtractor = new DataByTimesExtractor<double[]>(
                new GenericListContainer<double[]>(this.data), totalTime, 0);
    }
    
    public ArrayList<AudioLabel> align(int iterations, int modelsPerPhoneme) throws ImplementationError
    {
        ArrayList<AudioLabel> phonemeLabels = new ArrayList<AudioLabel>();
        for (AudioLabel label : words) {
            phonemeLabels.addAll(initialSplit(label));
        }
        
        for (int i = 0; i < iterations; ++i) {
            System.err.println("align iteration " + i);
            PhonemeGuasianTrainer trainer = new PhonemeGuasianTrainer();
            GaussianMixturePhonemeScorer[] phonemeScorers =
                trainer.trainPhonemes(modelsPerPhoneme, phonemeLabels, data, totalTime);
            
            phonemeLabels.clear();
            for (AudioLabel word : words) {
                phonemeLabels.addAll(findPhonemes(word, phonemeScorers));
            }
            System.err.println("~align iteration " + i);
        }
        
        return phonemeLabels;
    }
    
    private class PhonemeSequenceScorer
    {
        String phoneme;
        GaussianMixturePhonemeScorer dataScorer;
        PhonemeSequenceScorer previous = null;
        double bestScore;
        double bestStartTime;

        public PhonemeSequenceScorer(
            String phoneme,
            GaussianMixturePhonemeScorer gaussianMixturePhonemeScorer,
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
            if ((previous != null) && (currentFrameTime - previous.bestStartTime < 0.05))
                changeScore = Double.NEGATIVE_INFINITY;
            
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

    private ArrayList<AudioLabel> findPhonemes(
            AudioLabel word, GaussianMixturePhonemeScorer[] phonemeScorers) throws ImplementationError
    {
        if (word.getEnd() <= word.getStart()) return new ArrayList<AudioLabel>();
        
        String[] phonemes = this.converter.convert(word.getLabel()).get(0).split(" ");
        ArrayList<double[]> audio = this.dataExtractor.extract(word.getStart(), word.getEnd());
        
        PhonemeSequenceScorer[] scorers = new PhonemeSequenceScorer[phonemes.length];
        for (int i = 0; i < scorers.length; ++i) {
            scorers[i] = new PhonemeSequenceScorer(
                    phonemes[i],
                    phonemeScorers[i],
                    (i == 0) ? 0 : Double.NEGATIVE_INFINITY,
                    word.getStart());
        }
        
        for (int i = 0; i < audio.size(); ++i) {
            PhonemeSequenceScorer[] newScorers = new PhonemeSequenceScorer[phonemes.length];
            for (int j = 0; j < newScorers.length; ++j) {
                newScorers[j] = new PhonemeSequenceScorer(scorers[j]);
            }
            for (int j = 0; j < scorers.length; ++j) {
                PhonemeSequenceScorer previous = (j > 0) ? scorers[j - 1] : null;
                double previousScore = (j > 0) ? previous.getScore() : Double.MAX_VALUE;
                double currentTime = frameTime * i + word.getStart();
                newScorers[j].score(audio.get(i), currentTime, previous, previousScore);
            }
            scorers = newScorers;
        }
        
        return scorers[scorers.length - 1].getBestAlignment(word.getEnd());
    }

    private ArrayList<AudioLabel> initialSplit(AudioLabel word)
    {
        String[] phonemes = this.converter.convert(word.getLabel()).get(0).split(" ");
        
        double splitTime = (word.getEnd() - word.getStart()) / phonemes.length;
        ArrayList<AudioLabel> split = new ArrayList<AudioLabel>();
        for (int i = 0; i < phonemes.length; ++i) {
            split.add(new AudioLabel(phonemes[i], i * splitTime, (i + 1) * splitTime));
        }
        return split;
    }
}
