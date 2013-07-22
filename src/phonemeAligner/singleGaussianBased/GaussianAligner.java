package phonemeAligner.singleGaussianBased;

import graphemesToPhonemesConverters.IWordToPhonemesConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import phonemeScorers.IPhonemeScorer;

import common.AudioLabel;
import common.GenericListContainer;
import common.algorithms.DataByTimesExtractor;
import common.exceptions.ImplementationError;

public class GaussianAligner
{
    private AudioLabel[] chunks;
    ArrayList<double[]> data;
    IWordToPhonemesConverter converter;
    double totalTime;
    double frameTime;
    DataByTimesExtractor<double[]> dataExtractor;

    public GaussianAligner(
            double maxTimeOfChunk,
            AudioLabel[] chunks,
            ArrayList<double[]> allData,
            IWordToPhonemesConverter converter,
            double totalTime)
    {
        Arrays.sort(chunks, new Comparator<AudioLabel>() {
            @Override
            public int compare(AudioLabel o1, AudioLabel o2)
            {
                double t1 = o1.getEnd() - o1.getStart();
                double t2 = o2.getEnd() - o2.getStart();
                if (t1 < t2) return -1;
                if (t1 > t2) return 1;
                return 0;
            }
        });
        ArrayList<AudioLabel> auxChunks = new ArrayList<AudioLabel>();
        for (int i = 0; i < chunks.length; ++i) {
            if (chunks[i].getEnd() - chunks[i].getStart() > maxTimeOfChunk)
                break;
            auxChunks.add(chunks[i]);
        }
        System.err.println("took " + auxChunks.size() + " chunks out of " + chunks.length);
        
        this.chunks = auxChunks.toArray(new AudioLabel[0]);
        this.converter = converter;
        this.totalTime = totalTime;
        this.frameTime = totalTime / allData.size();
        this.data = allData;
        this.dataExtractor = new DataByTimesExtractor<double[]>(
                new GenericListContainer<double[]>(this.data), totalTime, 0);
    }
    
    public ArrayList<AudioLabel> align(IPhonemeScorer[] phonemeScorers) throws ImplementationError
    {
        ArrayList<AudioLabel> phonemeLabels = new ArrayList<AudioLabel>();
        for (AudioLabel word : chunks) {
            ArrayList<AudioLabel> wordPhonemes = findPhonemes(word, phonemeScorers);
            phonemeLabels.addAll(wordPhonemes);
        }
        
        return phonemeLabels;
    }
    
    private ArrayList<AudioLabel> findPhonemes(
            AudioLabel chunk, IPhonemeScorer[] phonemeScorers) throws ImplementationError
    {
        if (chunk.getEnd() <= chunk.getStart()) return new ArrayList<AudioLabel>();
        
        String[] phonemes = splitChunk(chunk.getLabel());
        ArrayList<double[]> audio = this.dataExtractor.extract(chunk.getStart(), chunk.getEnd());
        GaussianPhonemeAligner aligner = new GaussianPhonemeAligner(phonemeScorers);
        return aligner.align(phonemes, audio, chunk.getStart(), chunk.getEnd()).getLabels();
    }

    public String[] splitChunk(String chunk)
    {
        String[] words = chunk.split("[. ]+");
        ArrayList<String> phonemes = new ArrayList<String>();
        for (String word : words) {
            phonemes.add("sil");
            String[] wordPhonemes = splitWord(word);
            for (String phoneme : wordPhonemes)
                phonemes.add(phoneme);
        }
        phonemes.add("sil");
        return phonemes.toArray(new String[0]);
    }
    private String[] splitWord(String word)
    {
        return this.converter.convert(word).get(0).split(" ");
    }
}
