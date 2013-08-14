package phonemeScorers.trainers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import phonemeScorers.GaussianMixturePhonemeScorer;


import common.AudioLabel;
import common.GenericListContainer;
import common.algorithms.DataByTimesExtractor;
import common.algorithms.gaussian.GaussianMixtureExpectedMaximalization;
import common.algorithms.gaussian.MixtureGaussianModel;
import common.exceptions.ImplementationError;


public class PhonemeGuasianMixtureTrainer
{
    private class PhonemeData
    {
        private double[][] data;
        private String phoneme;
        private double transitionScore = 0;
        
        public PhonemeData(String phoneme, double[][] data, double transitionScore)
        {
            System.err.println(phoneme + " " + data.length);
            this.data = data;
            this.phoneme = phoneme;
            this.transitionScore = transitionScore;
        }

        public GaussianMixturePhonemeScorer train(int numOfModels)
        {
            GaussianMixtureExpectedMaximalization em = new GaussianMixtureExpectedMaximalization();
            MixtureGaussianModel mixtureModel = new MixtureGaussianModel(em.calculate(data, numOfModels));
            return new GaussianMixturePhonemeScorer(mixtureModel, this.transitionScore, phoneme);
        }
    }
    
    
    public GaussianMixturePhonemeScorer[] trainPhonemes(
        int modelsPerPhoneme,
        ArrayList<AudioLabel> phonemeLabels,
        ArrayList<double[]> data,
        double totalTime) throws ImplementationError
    {
        ArrayList<PhonemeData> phonemes = extractPhonemeData(phonemeLabels, data, totalTime); 
        
        ArrayList<GaussianMixturePhonemeScorer> trainedScorers =
                new ArrayList<GaussianMixturePhonemeScorer>();
        
        for (PhonemeData phoneme : phonemes) {
            trainedScorers.add(phoneme.train(modelsPerPhoneme));
        }
        
        return trainedScorers.toArray(new GaussianMixturePhonemeScorer[0]);
    }

    private ArrayList<PhonemeData> extractPhonemeData(
            ArrayList<AudioLabel> phonemeLabels,
            ArrayList<double[]> data,
            double totalTime)
    {
        Map<String, ArrayList<double[]>> dataPerPhoneme = new HashMap<String, ArrayList<double[]>>();
        Map<String, Double> transitionScoresPerPhoneme = new HashMap<String, Double>();
        Map<String, Integer> phonemeCounts = new HashMap<String, Integer>();
        
        DataByTimesExtractor<double[]> dataExtractor =
                new DataByTimesExtractor<double[]>(new GenericListContainer<double[]>(data), totalTime, 0);
        
        for (AudioLabel label : phonemeLabels) {
            ArrayList<double[]> phonemeData = dataExtractor.extract(label.getStart(), label.getEnd());
            double current = 1.0 / (double)phonemeData.size();
            if (dataPerPhoneme.containsKey(label.getLabel())) {
                double previous = transitionScoresPerPhoneme.get(label.getLabel());
                int previousCount = phonemeCounts.get(label.getLabel());
                transitionScoresPerPhoneme.put(label.getLabel(), previous + current);
                phonemeCounts.put(label.getLabel(), previousCount + 1);
                dataPerPhoneme.get(label.getLabel()).addAll(phonemeData);
            }
            else {
                dataPerPhoneme.put(label.getLabel(), phonemeData);
                transitionScoresPerPhoneme.put(label.getLabel(), current);
                phonemeCounts.put(label.getLabel(), 1);
            }
        }
        
        ArrayList<PhonemeData> ret = new ArrayList<PhonemeData>();
        for (String key : dataPerPhoneme.keySet()) {
            
            ArrayList<double[]> phonemeData = dataPerPhoneme.get(key);
            double[][] phonemePoints = new double[phonemeData.size()][];
            for (int i = 0; i < phonemeData.size(); ++i) {
                phonemePoints[i] = phonemeData.get(i);
            }
            double transitionScore = transitionScoresPerPhoneme.get(key);
            transitionScore /= phonemeCounts.get(key);
            ret.add(new PhonemeData(key, phonemePoints, Math.log(transitionScore)));
        }
        return ret;
    }
}
