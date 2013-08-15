package textAligners;

import java.util.ArrayList;
import java.util.List;

import graphemesToPhonemesConverters.IWordToPhonemesConverter;
import phonemeAligner.singleGaussianBased.GaussianPhonemeAligner;
import phonemeScorers.IPhonemeScorer;
import common.Alignment;
import common.AudioLabel;
import common.GenericListContainer;
import common.algorithms.DataByTimesExtractor;
import common.exceptions.ImplementationError;
import dataExporters.LinesExporter;

public class GaussianSearch
{
    private DataByTimesExtractor<double[]> dataExtractor;
    private GaussianPhonemeAligner aligner;
    IPhonemeScorer silScorer;
    
    public GaussianSearch(
        IPhonemeScorer[] scorers,
        ArrayList<double[]> allData,
        double totalTime)
    {
        for (IPhonemeScorer scorer : scorers)
            if (scorer.getPhoneme().equals("sil"))
                this.silScorer = scorer;
        this.aligner = new GaussianPhonemeAligner(scorers);
        this.dataExtractor = new DataByTimesExtractor<double[]>(
                new GenericListContainer<double[]>(allData), totalTime, 0);
    }

    AudioLabel searchSil(double startTime, double endTime)
    {
        ArrayList<double[]> partData =
                this.dataExtractor.extract(startTime, endTime);

        double frameTime = (endTime - startTime) / partData.size();
        int width = 5;
        for (int j = width; j < partData.size() - width; j += width * 2) {
            double[] averaged = sumPoint(partData.subList(j - width, j + width));
            if (this.silScorer.score(averaged) < 5.0)
                return new AudioLabel("sil", startTime, ((j - width) * frameTime + startTime));
        }
        return new AudioLabel("sil", startTime, endTime);
    }
    
    ArrayList<AudioLabel> search(String[] phonemes, double startTime, double endTime, double estimatedTime)
    {
        ArrayList<double[]> partData =
                this.dataExtractor.extract(startTime, endTime);
        
        Alignment bestAlignment = new Alignment(new ArrayList<AudioLabel>(), Double.NEGATIVE_INFINITY);
        double frameTime = (endTime - startTime) / partData.size();
        
        double start = startTime;
        for (int j = 20; j < partData.size(); j += 10) {
            double end = start + j * frameTime;
            ArrayList<double[]> data = this.dataExtractor.extract(start, end);
            Alignment alignment = this.aligner.align(phonemes, data, start, end);
            
//            System.err.println("score: "
//                    + calcScore(alignment, estimatedTime) + " end time: "
//                    + alignment.getLabels().get(alignment.getLabels().size() - 1).getEnd() + " "
//                    + end + " "
//                    + calcScore(bestAlignment, estimatedTime) + " "
//                    + alignment.getScore() + " "
//                    + bestAlignment.getScore());
            if (calcScore(alignment, estimatedTime) > calcScore(bestAlignment, estimatedTime)) {
                bestAlignment = alignment;
//                endTime = bestAlignment.getLabels().get(bestAlignment.getLabels().size() - 1).getEnd();
//                endIndex = (int)Math.round((endTime - startTime) / frameTime / 2.0);
            }
        }

        return bestAlignment.getLabels();
    }

    private double calcScore(Alignment alignment, double estimatedTime)
    {
        ArrayList<AudioLabel> alignmentLabels = alignment.getLabels();
        if (alignmentLabels.size() == 0) return Double.NEGATIVE_INFINITY;
        double time = alignmentLabels.get(alignmentLabels.size() - 1).getEnd() - alignmentLabels.get(0).getStart();
        double timeDiff = (time - estimatedTime) * 100;
        return alignment.getScore();// - timeDiff * timeDiff; 
    }

    private double[] sumPoint(List<double[]> subList)
    {
        double[] ret = new double[subList.get(0).length];
        
        for (double[] p : subList)
            for (int i = 0; i < ret.length; ++i)
                ret[i] += p[i];
        for (int i = 0; i < ret.length; ++i)
            ret[i] /= subList.size();
        return ret;
    }
}
