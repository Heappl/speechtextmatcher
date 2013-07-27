package experiments;

import graphemesToPhonemesConverters.GraphemesToRussianPhonemesConverter;

import java.util.ArrayList;
import java.util.HashMap;



import common.AudioLabel;
import common.GenericListContainer;
import common.algorithms.DataByTimesExtractor;
import common.algorithms.ThresholdDataFilter;
import common.algorithms.gaussian.GaussianMixtureExpectedMaximalization;
import common.algorithms.gaussian.MultivariateNormalDistribution;
import common.exceptions.ImplementationError;
import common.statistics.OneDimensionalDataStatistics;

public class PerWordPhonemeDestructurer
{
    private HashMap<String, ArrayList<AudioLabel>> wordLabels = new HashMap<String, ArrayList<AudioLabel>>();
    private final double frameTime;
    private DataByTimesExtractor<double[]> extractor;
    private ThresholdDataFilter filter;
    
    public PerWordPhonemeDestructurer(
        AudioLabel[] words,
        ArrayList<double[]> allData,
        double totalTime)
    {
        this.frameTime = totalTime / allData.size();
        for (AudioLabel word : words) {
            String key = word.getLabel();
            ArrayList<AudioLabel> labelList =
                wordLabels.containsKey(key) ? wordLabels.get(key) : new ArrayList<AudioLabel>();
            labelList.add(word);
            wordLabels.put(key, labelList);
        }
        this.extractor = new DataByTimesExtractor<double[]>(
                new GenericListContainer<double[]>(allData), totalTime, 0);
        double[] powers = new double[allData.size()];
        for (int i = 0; i < allData.size(); ++i)
            powers[i] = allData.get(i)[0];
        OneDimensionalDataStatistics powerStats = new OneDimensionalDataStatistics(powers);

        int dimension = allData.get(0).length;
        double[] threshold = new double[dimension];
        threshold[0] = powerStats.getBackgroundMean();
        for (int i = 1; i < threshold.length; ++i) threshold[i] = Double.NEGATIVE_INFINITY;
        this.filter = new ThresholdDataFilter(threshold, 100.0);
    }
    
    public void process() throws ImplementationError
    {
        for (String word : wordLabels.keySet()) {
            if (word.length() < 3) continue;
            if (wordLabels.get(word).size() < 5) continue;
            System.err.println(word + " " + wordLabels.get(word).size());
            ArrayList<double[]> wordFrames = new ArrayList<double[]>();
            for (AudioLabel label : wordLabels.get(word))
                wordFrames.addAll(extractor.extract(label.getStart(), label.getEnd()));
//            wordFrames = filter.filter(wordFrames);
         
            GaussianMixtureExpectedMaximalization em = new GaussianMixtureExpectedMaximalization();
            GraphemesToRussianPhonemesConverter converter = new GraphemesToRussianPhonemesConverter();
            
            double[][] trainData = new double[wordFrames.size()][wordFrames.get(0).length - 1];
            for (int i = 0; i < trainData.length; ++i)
                for (int j = 0; j < trainData[i].length; ++j)
                    trainData[i][j] = wordFrames.get(i)[j + 1];
            String[] phonemes = converter.convert(word).get(0).split(" ");
            MultivariateNormalDistribution[] models = em.calculate(trainData, phonemes.length + 2);
            
            checkModels(wordLabels.get(word), models);
        }
    }

    private void checkModels(
        ArrayList<AudioLabel> wordLabels, MultivariateNormalDistribution[] models) throws ImplementationError
    {
        for (AudioLabel label : wordLabels) {
            System.err.println(label);
            ArrayList<double[]> data = extractor.extract(label.getStart(), label.getEnd());
//            data = filter.filter(data);
            
            for (double[] frame : data) {
                int bestModel = findBestModel(models, frame);
                System.err.print(bestModel + " ");
            }
            System.err.println();
        }
    }

    private int findBestModel(MultivariateNormalDistribution[] models, double[] frame) throws ImplementationError
    {
        double[] frameCopy = new double[frame.length - 1];
        for (int j = 0; j < frameCopy.length; ++j)
            frameCopy[j] = frame[j + 1];
        frameCopy[0] = 0;
        double bestScore = Double.NEGATIVE_INFINITY;
        int bestIndex = -1;
        for (int i = 0; i < models.length; ++i) {
            double score = models[i].logLikelihood(frameCopy);
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }
        return bestIndex;
    }
}
