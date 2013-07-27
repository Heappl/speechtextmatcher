package textAligners;

import java.util.ArrayList;

import graphemesToPhonemesConverters.IWordToPhonemesConverter;
import phonemeAligner.singleGaussianBased.GaussianPhonemeAligner;
import phonemeScorers.IPhonemeScorer;
import common.Alignment;
import common.AudioLabel;
import common.GenericListContainer;
import common.algorithms.DataByTimesExtractor;
import common.exceptions.ImplementationError;

public class GuassianSearch
{
    private DataByTimesExtractor<double[]> dataExtractor;
    private GaussianPhonemeAligner aligner;
    private IWordToPhonemesConverter converter;
    
    public GuassianSearch(
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
    
    AudioLabel search(String word, double startTime, double endTime) throws ImplementationError
    {
        ArrayList<double[]> partData =
                this.dataExtractor.extract(startTime, endTime);
        String[] phonemes = this.converter.convert(word).get(0).split(" ");
        
        Alignment bestAlignment = new Alignment(new ArrayList<AudioLabel>(), Double.NEGATIVE_INFINITY);
        double frameTime = (endTime - startTime) / partData.size();
        System.err.println(partData.size());
        for (int i = 0; i < partData.size(); i += 5) {
            double start = startTime + i * frameTime;
            for (int j = i + 20; j < partData.size(); j += 5) {
                double end = startTime + j * frameTime;
                ArrayList<double[]> data = this.dataExtractor.extract(start, end);
                Alignment alignment = this.aligner.align(phonemes, data, start, end);
                if (alignment.getScore() > bestAlignment.getScore())
                    bestAlignment = alignment;
            }
            System.err.println(i + " score: " + bestAlignment.getScore() + " " +
                    bestAlignment.getLabels().get(0).getStart() + " " + 
                    bestAlignment.getLabels().get(bestAlignment.getLabels().size() - 1).getEnd() + " ");
        }

        return new AudioLabel(word,
                bestAlignment.getLabels().get(0).getStart(),
                bestAlignment.getLabels().get(0).getEnd());
    }
}
