package phonemeAligner.simpleGaussianTraining;

import java.util.ArrayList;

import phonemeAligner.singleGaussianBased.GaussianAligner;
import phonemeScorers.IPhonemeScorer;
import phonemeScorers.trainers.IterativePhonemeScorerTraining;
import phonemeScorers.trainers.PhonemeSingleGaussianTrainer;



import graphemesToPhonemesConverters.IWordToPhonemesConverter;

import common.AudioLabel;
import common.DataSequence;
import common.GenericDataContainer;
import common.GenericListContainer;
import common.algorithms.DataByTimesExtractor;
import common.exceptions.ImplementationError;
import dataProducers.AudacityLabelImporter;
import dataProducers.TextImporter;

public class IterativeTrainingPhonemeAligner
{
    private IterativePhonemeScorerTraining trainer;

    public IterativeTrainingPhonemeAligner(
            AudioLabel[] words,
            ArrayList<double[]> allData,
            IWordToPhonemesConverter converter,
            double totalTime)
    {
        this.trainer = new IterativePhonemeScorerTraining(
                Double.POSITIVE_INFINITY, words, allData, converter, totalTime);
    }
    
    public ArrayList<AudioLabel> align(int iterations) throws ImplementationError
    {
        this.trainer.train(iterations);
        return this.trainer.getLastResults();
    }
}
