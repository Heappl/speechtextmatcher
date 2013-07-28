package phonemeScorers.trainers;

import java.util.ArrayList;
import java.util.HashMap;

import phonemeScorers.IPhonemeScorer;
import phonemeScorers.SingleGaussianPhonemeScorer;



import common.AudioLabel;
import common.GenericListContainer;
import common.algorithms.DataByTimesExtractor;
import common.algorithms.gaussian.MultivariateNormalDistribution;
import common.exceptions.ImplementationError;
import common.statistics.MultivariateDataStatistics;

public class PhonemeSingleGaussianTrainer
{
    public IPhonemeScorer[] trainPhonemes(
        ArrayList<AudioLabel> phonemeLabels,
        ArrayList<double[]> data,
        double totalTime) throws ImplementationError
    {
        HashMap<String, ArrayList<AudioLabel>> sortedLabels = sortLabels(phonemeLabels);
        
        ArrayList<IPhonemeScorer> trainedScorers = new ArrayList<IPhonemeScorer>();
        for (String key : sortedLabels.keySet()) {
            trainedScorers.add(trainScorer(key, sortedLabels.get(key), data, totalTime));
        }
        return trainedScorers.toArray(new IPhonemeScorer[0]);
    }

    private IPhonemeScorer trainScorer(
        String phoneme, ArrayList<AudioLabel> labelList, ArrayList<double[]> data, double totalTime)
    {
        System.err.println("training " + phoneme);
        DataByTimesExtractor<double[]> dataExtractor =
                new DataByTimesExtractor<double[]>(new GenericListContainer<double[]>(data), totalTime, 0);
            
        ArrayList<double[]> phonemeData = new ArrayList<double[]>();
        for (AudioLabel label : labelList) {
            phonemeData.addAll(dataExtractor.extract(label.getStart(), label.getEnd()));
        }
        double[][] phonemePoints = new double[phonemeData.size()][];
        for (int i = 0; i < phonemeData.size(); ++i)
            phonemePoints[i] = phonemeData.get(i);
        MultivariateNormalDistribution model =
                new MultivariateDataStatistics(phonemePoints).getDistribution();
        return new SingleGaussianPhonemeScorer(model, phoneme);
    }

    private HashMap<String, ArrayList<AudioLabel>> sortLabels(ArrayList<AudioLabel> labels)
    {
        HashMap<String, ArrayList<AudioLabel>> out = new HashMap<String, ArrayList<AudioLabel>>();
        for (AudioLabel label : labels) {
            String key = label.getLabel();
            ArrayList<AudioLabel> labelList =
                (out.containsKey(key)) ? out.get(key) : new ArrayList<AudioLabel>();
            labelList.add(label);
            out.put(key, labelList);
        }
        return out;
    }
}
