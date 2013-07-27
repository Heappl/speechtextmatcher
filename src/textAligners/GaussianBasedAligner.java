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
    private GuassianSearch searcher;

    IPhonemeScorer[] scorers;
    
    public GaussianBasedAligner(
        IPhonemeScorer[] scorers,
        IWordToPhonemesConverter converter,
        ArrayList<double[]> allData,
        double totalTime)
    {
        this.scorers = scorers;
        this.aligner = new GaussianPhonemeAligner(scorers);
        this.searcher = new GuassianSearch(scorers, converter, allData, totalTime);
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
            String sentences = "";
            for (AudioLabel label : partial)
                sentences += label.getLabel() + " ";
            ret.add(new AudioLabel(sentences, speech.getStartTime(), speech.getEndTime()));
            soFar += partial.size();
//            if (count-- == 0) break;
//            break;
        }
        return ret;
    }

    private ArrayList<AudioLabel> alignSpeech(
            Speech speech, String[] words, int start, double timePerChar) throws ImplementationError
    {
        int estimatedNumOfWords = estimateNumOfWords(words, start, timePerChar, speech.getTime());
        int neigh = Math.max(1, (int)Math.ceil(Math.sqrt(estimatedNumOfWords)));
        
        ArrayList<double[]> speechData =
                this.dataExtractor.extract(speech.getStartTime(), speech.getEndTime());
//        
//        double time = speech.getEndTime() - speech.getStartTime();
//        double frameTime = time / speechData.size();
//        int it = 0;
//        for (double[] frame : speechData) {
//            double bestScore = Double.NEGATIVE_INFINITY;
//            int bestIndex = 0;
//            for (int i = 0; i < this.scorers.length; ++i) {
//                double score = this.scorers[i].score(frame);
//                if ((this.scorers[i].getPhoneme().equals("s"))
//                    || (this.scorers[i].getPhoneme().equals("t")) 
//                    || (this.scorers[i].getPhoneme().equals("e")) 
//                    || (this.scorers[i].getPhoneme().equals("sil")))
//                {
//                    if (score > bestScore) {
//                        bestScore = score;
//                        bestIndex = i;
//                    }
//                }
//            }
//            System.err.println((it++ * frameTime + speech.getStartTime()) + " " + this.scorers[bestIndex].getPhoneme());
//        }
//        
//        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
////        ret.add(this.searcher.search(words[0], speech.getStartTime(), speech.getEndTime()));
////        ret.add(this.searcher.search(words[1], speech.getStartTime(), speech.getEndTime()));
//        return ret;
        
        Alignment bestAlignment = new Alignment(new ArrayList<AudioLabel>(), Double.NEGATIVE_INFINITY);
        for (int i = Math.max(1, estimatedNumOfWords - neigh);
                 i <= Math.min(words.length - start, estimatedNumOfWords + neigh);
                ++i) {
            String[] phonemes = convertToPhonemes(words, start, start + i);
            Alignment alignment =
                    this.aligner.align(phonemes, speechData, speech.getStartTime(), speech.getEndTime());
            if (alignment.getScore() > bestAlignment.getScore())
                bestAlignment = alignment;
        }
        return convertToWordLabels(words, start, bestAlignment.getLabels());
    }

    private ArrayList<AudioLabel> convertToWordLabels(String[] words, int start, ArrayList<AudioLabel> phonemes) throws ImplementationError
    {
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        phonemes = filterAllSils(phonemes);
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
            String[] phonemes = ("sil " + this.converter.convert(word).get(0)).split(" ");
            for (String phoneme : phonemes)
                ret.add(phoneme);
        }
        ret.add("sil");
        return ret.subList(1, ret.size()).toArray(new String[0]);
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
