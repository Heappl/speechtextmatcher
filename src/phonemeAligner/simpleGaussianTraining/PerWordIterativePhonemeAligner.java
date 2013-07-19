package phonemeAligner.simpleGaussianTraining;

import graphemesToPhonemesConverters.IWordToPhonemesConverter;

import java.util.ArrayList;
import java.util.HashMap;

import algorithms.DataByTimesExtractor;

import common.AudioLabel;
import common.GenericListContainer;
import commonExceptions.ImplementationError;

public class PerWordIterativePhonemeAligner
{
    private HashMap<String, ArrayList<AudioLabel>> wordLabels = new HashMap<String, ArrayList<AudioLabel>>();
    
    ArrayList<double[]> data;
    IWordToPhonemesConverter converter;
    double totalTime;
    double frameTime;
    DataByTimesExtractor<double[]> dataExtractor;

    public PerWordIterativePhonemeAligner(
            AudioLabel[] words,
            ArrayList<double[]> allData,
            IWordToPhonemesConverter converter,
            double totalTime)
    {
        this.converter = converter;
        this.totalTime = totalTime;
        this.frameTime = totalTime / allData.size();
        
        this.data = allData;

        this.dataExtractor = new DataByTimesExtractor<double[]>(
                new GenericListContainer<double[]>(this.data), totalTime, 0);
        
        for (AudioLabel label : words) {
            String word = label.getLabel();
            ArrayList<AudioLabel> labels =
                    (wordLabels.containsKey(word)) ? wordLabels.get(word) : new ArrayList<AudioLabel>();
            labels.add(label);
            wordLabels.put(word, labels);
        }
    }

    public ArrayList<AudioLabel> align(int iterations) throws ImplementationError
    {
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        
        for (String word : wordLabels.keySet()) {
            if (word.length() < 2) continue;
            ArrayList<AudioLabel> labels = wordLabels.get(word);
            if (labels.size() < 5) continue;
            
            IterativeTrainingPhonemeAligner aligner =
                    new IterativeTrainingPhonemeAligner(labels.toArray(new AudioLabel[0]), data, converter, totalTime);
            ret.addAll(aligner.align(iterations));
        }
        return ret;
    }
}
