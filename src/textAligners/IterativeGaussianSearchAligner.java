package textAligners;

import graphemesToPhonemesConverters.IWordToPhonemesConverter;

import java.util.ArrayList;

import common.AudioLabel;
import common.Text;

import phonemeScorers.IPhonemeScorer;

public class IterativeGaussianSearchAligner
{
    private final GaussianSearch searcher;
    private final IWordToPhonemesConverter converter;
    
    public IterativeGaussianSearchAligner(
        IPhonemeScorer[] scorers,
        IWordToPhonemesConverter converter,
        ArrayList<double[]> allData,
        double totalTime)
    {
        this.searcher = new GaussianSearch(scorers, allData, totalTime);
        this.converter = converter;
    }
    
    ArrayList<AudioLabel> align(Text text)
    {
        String[] words = text.getWords();
        double timePerChar = text.getEstimatedTimePerCharacter();
        
        double startTime = 0;
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        int count = 10;
        int noWords = 2;
        for (int i = 0; i < words.length - noWords + 1; ++i) {
            String[] phonemes = new String[noWords * 2 - 1];
            for (int j = i; j < i + noWords; ++j) {
                phonemes[(j - i) * 2] =  this.converter.convert(words[j]).get(0);
            }
            for (int j = 1; j < phonemes.length; j += 2) phonemes[j] = "sil";
            
            double estimatedTime = 0;
            for (int j = i; j < i + noWords; ++j) estimatedTime += words[j].length() * timePerChar;
            double estimatedMaximalTime = 5 * estimatedTime;
            
            ArrayList<String> phonemesArray = new ArrayList<String>();
            for (int j = 0; j < phonemes.length; ++j)
                for (String phoneme : phonemes[j].split(" "))
                    phonemesArray.add(phoneme);
            
            ArrayList<AudioLabel> found = this.searcher.search(
                    phonemesArray.toArray(new String[0]),
                    startTime, estimatedMaximalTime + startTime, estimatedTime);
            for (AudioLabel label : found) {
                if (label.getLabel().equals("sil")) {
                    ret.add(new AudioLabel(words[i], startTime, label.getStart()));
                    ret.add(label);
                    break;
                }
            }
            startTime = ret.get(ret.size() - 1).getEnd();
            if (count-- < 0) break;
        }
        return ret;
    }
}
