package phonemeAligner.powerBased;

import graphemesToPhonemesConverters.IWordToPhonemesConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import common.AudioLabel;

public class PowerDescentPhonemeAligner
{
    private ArrayList<TimedData> allData = new ArrayList<TimedData>();
    private IWordToPhonemesConverter converter;
    private String[] usedPhonemes;

    public PowerDescentPhonemeAligner(
        AudioLabel[] words,
        ArrayList<double[]> allData,
        IWordToPhonemesConverter converter,
        double totalTime)
    {
        this.converter = converter;
        double frameTime = totalTime / allData.size();
        
        double time = 0;
        for (double[] data : allData) {
            this.allData.add(new TimedData(time, data));
            time += frameTime;
        }
        Collections.sort(this.allData, new Comparator<TimedData>() {
            @Override
            public int compare(TimedData o1, TimedData o2)
            {
                if (o1.data[0] < o2.data[0]) return 1;
                if (o1.data[0] > o2.data[0]) return -1;
                return 0;
            }
        });
        usedPhonemes = this.converter.getAllPhonemes();
    }
    
    public ArrayList<AudioLabel> align()
    {
        for (TimedData data : allData) {
            double[] probs = calculateProbabilities(data);
            retrainClassifiers(probs, data.data);
        }
        
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        return ret;
    }

    private void retrainClassifiers(double[] probs, double[] data)
    {
        // TODO Auto-generated method stub
    }

    private double[] calculateProbabilities(TimedData data)
    {
        AudioLabel word = findWord(data.time);
        String[] phonemes = calculatePhonemes(word.getLabel());
        
        double[] probs = new double[this.usedPhonemes.length];

        for (int i = 0; i < probs.length; ++i) {
            if (!isInWord(phonemes, this.usedPhonemes[i])) continue;
            probs[i] = calculatePositionProbability(
                    data.time, this.usedPhonemes[i], word, phonemes);
            probs[i] *= calculateModelProbability(i, data.data);
        }
        return probs;
    }

    private double calculateModelProbability(int i, double[] data)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    private double calculatePositionProbability(
        double time, String string, AudioLabel word, String[] wordPhonemes)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    private boolean isInWord(String[] wordPhonemes, String searchedPhoneme)
    {
        for (String phoneme : wordPhonemes)
            if (phoneme.equals(searchedPhoneme)) return true;
        return false;
    }

    private String[] calculatePhonemes(String word)
    {
        return this.converter.convert(word).get(0).split(" ");
    }

    private AudioLabel findWord(double time)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
