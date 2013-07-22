package textAligners;

import graphemesToPhonemesConverters.IWordToPhonemesConverter;

import java.util.ArrayList;
import java.util.Collection;

import common.Alignment;
import common.AudioLabel;
import common.GenericListContainer;
import common.Speech;
import common.Speeches;
import common.Text;
import common.algorithms.DataByTimesExtractor;
import common.exceptions.ImplementationError;

import phonemeAligner.singleGaussianBased.GaussianPhonemeAligner;
import phonemeScorers.IPhonemeScorer;

public class GaussianBasedAligner
{
    private GaussianPhonemeAligner aligner;
    private DataByTimesExtractor<double[]> dataExtractor;
    private IWordToPhonemesConverter converter;
    
    public GaussianBasedAligner(
        IPhonemeScorer[] scorers,
        IWordToPhonemesConverter converter,
        ArrayList<double[]> allData,
        double totalTime)
    {
        this.aligner = new GaussianPhonemeAligner(scorers);
        this.converter = converter;
        this.dataExtractor = new DataByTimesExtractor<double[]>(
                new GenericListContainer<double[]>(allData), totalTime, 0);
    }
    
    public ArrayList<AudioLabel> align(Text text, Speeches speeches) throws ImplementationError
    {
        String[] words = text.getWords();
        
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        int soFar = 0;
        int count = 3;
        for (Speech speech : speeches) {
            System.err.println("processing " + speech.getStartTime() + " " + speech.getTime());
            ArrayList<AudioLabel> partial =
                    alignSpeech(speech, words, soFar, text.getEstimatedTimePerCharacter());
            ret.addAll(partial);
            soFar += partial.size();
            if (count-- == 0) break;
        }
        
        return ret;
    }

    private ArrayList<AudioLabel> alignSpeech(
            Speech speech, String[] words, int start, double timePerChar) throws ImplementationError
    {
        int estimatedNumOfWords = estimateNumOfWords(words, start, timePerChar, speech.getTime());
        int neigh = estimatedNumOfWords;//Math.max(1, (int)Math.ceil(Math.sqrt(estimatedNumOfWords)));
        System.err.println("estimated number of words: " + estimatedNumOfWords);
        
        ArrayList<double[]> speechData =
                this.dataExtractor.extract(speech.getStartTime(), speech.getEndTime());
        
        Alignment bestAlignment = new Alignment(new ArrayList<AudioLabel>(), Double.NEGATIVE_INFINITY);
        for (int i = Math.max(1, estimatedNumOfWords - neigh);
                 i <= Math.min(words.length - start, estimatedNumOfWords + neigh);
                ++i) {
            String[] phonemes = convertToPhonemes(words, start, start + i);
            Alignment alignment =
                    this.aligner.align(phonemes, speechData, speech.getStartTime(), speech.getEndTime());
            System.err.println(i + " score: " + alignment.getScore() + " " + alignment.getLabels().size());
            if (alignment.getScore() > bestAlignment.getScore())
                bestAlignment = alignment;
        }
        return convertToWordLabels(words, start, bestAlignment.getLabels());
    }

    private ArrayList<AudioLabel> convertToWordLabels(String[] words, int start, ArrayList<AudioLabel> phonemes) throws ImplementationError
    {
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        phonemes = filterAllSils(phonemes);
        System.err.println("phonemes labels " + phonemes.size());
        for (int i = start; i < words.length; ++i) {
            if (phonemes.isEmpty()) break;
            ArrayList<AudioLabel> wordLabels = getWordPhonemesOnly(words[i], phonemes);
            ret.add(new AudioLabel(
                    words[i],
                    wordLabels.get(0).getStart(),
                    wordLabels.get(wordLabels.size() - 1).getEnd()
                    ));
            phonemes = new ArrayList<AudioLabel>(phonemes.subList(wordLabels.size(), phonemes.size()));
        }
        System.err.println("word labels " + ret.size());
        return ret;
    }

    private ArrayList<AudioLabel> filterAllSils(ArrayList<AudioLabel> phonemes)
    {
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        for (AudioLabel phoneme : phonemes) {
            if (phoneme.getLabel().equalsIgnoreCase("sil")) continue;
            ret.add(phoneme);
        }
        return ret;
    }

    private ArrayList<AudioLabel> getWordPhonemesOnly(String word, ArrayList<AudioLabel> phonemes) throws ImplementationError
    {
        word = word.replaceAll("'", "");
        String[] wordPhonemes = this.converter.convert(word).get(0).split(" ");
        
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        int it = 0;
        for (AudioLabel phoneme : phonemes) {
            if (!phoneme.getLabel().equalsIgnoreCase(wordPhonemes[it]))
                throw new ImplementationError("phoneme not found " + phoneme.getLabel() + " " + wordPhonemes[it]);
            ret.add(phoneme);
            ++it;
            if (it >= wordPhonemes.length) break;
        }
        return ret;
    }

    private String[] convertToPhonemes(String[] words, int start, int end)
    {
        ArrayList<String> ret = new ArrayList<String>();
        for (int i = start; i < end; ++i) {
            String word = words[i].replaceAll("'", "");
            String[] phonemes = ("sil " + this.converter.convert(word).get(0) + " sil").split(" ");
            for (String phoneme : phonemes)
                ret.add(phoneme);
        }
        return ret.toArray(new String[0]);
    }

    private int estimateNumOfWords(String[] words, int start, double timePerChar, double time)
    {
        double nearestTime = 0;
        int bestNumOfWords = 0;
        
        double timeSoFar = 0;
        for (int i = start; i < words.length; ++i) {
            double auxTime = words[i].length() * timePerChar;
            double nextTime = auxTime + timeSoFar;
            
            if (Math.abs(nextTime - time) < Math.abs(nearestTime - time)) {
                nearestTime = nextTime;
                bestNumOfWords = i;
            } else if (nextTime > time) break;
            timeSoFar += auxTime;
        }
        return Math.max(1, bestNumOfWords - start);
    }
}
