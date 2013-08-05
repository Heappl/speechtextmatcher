package phonemeAligner.hmmBased;

import java.util.ArrayList;

import common.AudioLabel;
import common.GenericListContainer;
import common.algorithms.DataByTimesExtractor;

public class PhonemeHMMBasedAligner
{
    private PhonemeHMM model;
    private DataByTimesExtractor<double[]> dataExtractor;
    
    public PhonemeHMMBasedAligner(PhonemeHMM model, ArrayList<double[]> data, double totalTime)
    {
        this.model = model;
        this.dataExtractor = new DataByTimesExtractor<double[]>(
                new GenericListContainer<double[]>(data), totalTime, 0);
    }

    public ArrayList<AudioLabel> align(AudioLabel[] chunks)
    {
        System.err.println("aligning");
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        for (AudioLabel chunk : chunks)
            ret.addAll(align(chunk, this.dataExtractor.extract(chunk.getStart(), chunk.getEnd())));
        return ret;
    }
    
    public ArrayList<AudioLabel> align(AudioLabel chunk, ArrayList<double[]> audioData)
    {
        String text = chunk.getLabel();
        double totalTime = chunk.getEnd() - chunk.getStart();
        String[] result = this.model.calculateMostProbableSequence(audioData, text);
        
        double stepTime = totalTime / (double)audioData.size();
        
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        int start = 0;
        int count = 0;
        String prev = null;
        for (String phoneme : result) {
            if (prev != phoneme) {
                if (prev != null) {
                    double startTime = start * stepTime + chunk.getStart();
                    double endTime = count * stepTime + chunk.getStart();
                    ret.add(new AudioLabel(prev, startTime, endTime));
                }
                prev = phoneme;
                start = count;
            }
            ++count;
        }
        
        return ret;
    }
}
