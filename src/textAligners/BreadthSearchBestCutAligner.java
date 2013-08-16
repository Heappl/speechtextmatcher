package textAligners;

import graphemesToPhonemesConverters.IWordToPhonemesConverter;

import java.util.ArrayList;

import common.AudioLabel;
import common.Text;
import common.exceptions.ImplementationError;

import phonemeScorers.IPhonemeScorer;

public class BreadthSearchBestCutAligner
{
    private final BreadthSearchBestCutPhonemeAligner aligner;
    private final IWordToPhonemesConverter converter;
    private final int numOfScorers;

    public BreadthSearchBestCutAligner(
        IPhonemeScorer[] scorers,
        IWordToPhonemesConverter converter,
        int numOfScorers)
    {
        this.aligner = new BreadthSearchBestCutPhonemeAligner(scorers);
        this.converter = converter;
        this.numOfScorers = numOfScorers;
    }

    public ArrayList<AudioLabel> align(Text text, ArrayList<double[]> audio, double totalTime)
    {
        ArrayList<String> phonemes = new ArrayList<String>();
        phonemes.add("sil");
        for (String word : text.getWords()) {
            for (String phoneme : this.converter.convert(word).get(0).split(" "))
                phonemes.add(phoneme);
            phonemes.add("sil");
        }
        
        ArrayList<AudioLabel> phonemesLabels =
            this.aligner.align(phonemes.toArray(new String[0]), audio, totalTime, this.numOfScorers);
        
        return convertToWords(text, phonemesLabels);
    }

    private ArrayList<AudioLabel> convertToWords(Text text, ArrayList<AudioLabel> phonemesLabels)
    {
        ArrayList<AudioLabel> wordLabels = new ArrayList<AudioLabel>();
        ArrayList<AudioLabel> filteredPhonemeLabels = filterOutSils(phonemesLabels);
        for (String word : text.getWords()) {
            String[] phonemes = this.converter.convert(word).get(0).split(" ");
            if (phonemes.length > filteredPhonemeLabels.size()) break;
            ArrayList<AudioLabel> currWordLabels = extractLabels(phonemes, filteredPhonemeLabels);
            wordLabels.add(new AudioLabel(
                    word,
                    currWordLabels.get(0).getStart(),
                    currWordLabels.get(currWordLabels.size() - 1).getEnd()));
        }
        return wordLabels;
    }

    private ArrayList<AudioLabel> extractLabels(String[] phonemes, ArrayList<AudioLabel> labels)
    {
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        for (String phoneme : phonemes) {
            if (!labels.get(0).getLabel().equals(phoneme))
                throw new ImplementationError("phoneme not found: " + phoneme + " " + labels.get(0).getLabel());
            ret.add(labels.get(0));
            labels.remove(0);
        }
        return ret;
    }

    private ArrayList<AudioLabel> filterOutSils(ArrayList<AudioLabel> labels)
    {
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        for (AudioLabel label : labels)
            if (!label.getLabel().equals("sil"))
                ret.add(label);
        return ret;
    }
}
