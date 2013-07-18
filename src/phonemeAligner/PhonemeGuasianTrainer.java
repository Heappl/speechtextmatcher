package phonemeAligner;

import java.util.ArrayList;
import java.util.HashMap;

import common.AudioLabel;
import common.GenericListContainer;
import commonExceptions.ImplementationError;

import algorithms.DataByTimesExtractor;
import algorithms.GaussianMixtureExpectedMaximalization;
import algorithms.MixtureGaussianModel;

public class PhonemeGuasianTrainer
{
    private class PhonemeData
    {
        private double[][] data;
        private String phoneme;
        
        public PhonemeData(String phoneme, double[][] data)
        {
            this.data = data;
            this.phoneme = phoneme;
        }

        public GaussianMixturePhonemeScorer train(int numOfModels) throws ImplementationError
        {
            GaussianMixtureExpectedMaximalization em = new GaussianMixtureExpectedMaximalization();
            MixtureGaussianModel mixtureModel = new MixtureGaussianModel(em.calculate(data, numOfModels));
            return new GaussianMixturePhonemeScorer(mixtureModel, phoneme);
        }
    }
    
    
    public GaussianMixturePhonemeScorer[] trainPhonemes(
        int modelsPerPhoneme,
        ArrayList<AudioLabel> phonemeLabels,
        ArrayList<double[]> data,
        double totalTime) throws ImplementationError
    {
        ArrayList<PhonemeData> phonemes = extractPhonemeData(phonemeLabels, data, totalTime); 
        
        ArrayList<GaussianMixturePhonemeScorer> trainedScorers = new ArrayList<GaussianMixturePhonemeScorer>();
        
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
        HashMap<String, ArrayList<double[]>> dataPerPhoneme = new HashMap<String, ArrayList<double[]>>();
        
        DataByTimesExtractor<double[]> dataExtractor =
                new DataByTimesExtractor<double[]>(new GenericListContainer<double[]>(data), totalTime, 0);
        
        for (AudioLabel label : phonemeLabels) {
            ArrayList<double[]> phonemeData = dataExtractor.extract(label.getStart(), label.getEnd());
            dataPerPhoneme.put(label.getLabel(), phonemeData);
        }
        
        ArrayList<PhonemeData> ret = new ArrayList<PhonemeData>();
        for (String key : dataPerPhoneme.keySet()) {
            
            ArrayList<double[]> phonemeData = dataPerPhoneme.get(key);
            double[][] phonemePoints = new double[phonemeData.size()][];
            for (int i = 0; i < phonemeData.size(); ++i) {
                phonemePoints[i] = phonemeData.get(i);
            }
            ret.add(new PhonemeData(key, phonemePoints));
        }
        return ret;
    }
}
