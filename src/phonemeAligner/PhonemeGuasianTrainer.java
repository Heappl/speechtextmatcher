package phonemeAligner;

import java.util.ArrayList;
import java.util.HashMap;

import common.AudioLabel;
import common.DoubleDataContainer;
import common.GenericDataContainer;
import commonExceptions.ImplementationError;
import edu.cmu.sphinx.frontend.FloatData;

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
        AudioLabel[] phonemeLabels,
        FloatData[] data,
        double[] powers,
        double totalTime) throws ImplementationError
    {
        ArrayList<PhonemeData> phonemes = extractPhonemeData(phonemeLabels, data, powers, totalTime); 
        
        ArrayList<GaussianMixturePhonemeScorer> trainedScorers = new ArrayList<GaussianMixturePhonemeScorer>();
        
        for (PhonemeData phoneme : phonemes) {
            trainedScorers.add(phoneme.train(modelsPerPhoneme));
        }
        
        return trainedScorers.toArray(new GaussianMixturePhonemeScorer[0]);
    }

    private ArrayList<PhonemeData> extractPhonemeData(
            AudioLabel[] phonemeLabels, FloatData[] data, double[] powers, double totalTime) throws ImplementationError
    {
        if (data.length != powers.length)
            throw new ImplementationError("data and powers lengths do not match");
        
        HashMap<String, ArrayList<double[]>> dataPerPhoneme = new HashMap<String, ArrayList<double[]>>();
        
        DataByTimesExtractor<FloatData> dataExtractor =
                new DataByTimesExtractor<FloatData>(new GenericDataContainer<FloatData>(data), totalTime, 0);
        DataByTimesExtractor<Double> powersExtractor =
                new DataByTimesExtractor<Double>(new DoubleDataContainer(powers), totalTime, 0);
        
        for (AudioLabel label : phonemeLabels) {
            ArrayList<FloatData> phonemeData = dataExtractor.extract(label.getStart(), label.getEnd());
            ArrayList<Double> phonemePowers = powersExtractor.extract(label.getStart(), label.getEnd());
            
            if (phonemeData.size() != phonemePowers.size())
                throw new ImplementationError("phoneme data and powers sizes do not match");
            
            ArrayList<double[]> phonemePoints =
                    (dataPerPhoneme.containsKey(label.getLabel())) ?
                        dataPerPhoneme.get(label.getLabel()):
                        new ArrayList<double[]>();
            double[][] points = new double[phonemeData.size()][phonemeData.get(0).getValues().length + 1];
            for (int i = 0; i < points.length; ++i) {
                points[i][0] = phonemePowers.get(i);
                for (int j = 1; j < points[i].length; ++j) {
                    points[i][j] = phonemeData.get(i).getValues()[j - 1];
                }
                phonemePoints.add(points[i]);
            }
            dataPerPhoneme.put(label.getLabel(), phonemePoints);
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
