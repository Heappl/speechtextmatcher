package phonemeAligner.simpleGaussianTraining;

import java.util.ArrayList;
import phonemeScorers.trainers.IterativePhonemeScorerTraining;
import graphemesToPhonemesConverters.IWordToPhonemesConverter;
import common.AudioLabel;
import common.exceptions.ImplementationError;

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
