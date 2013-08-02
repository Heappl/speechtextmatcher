package phonemeAligner.hmmBased;

import java.util.ArrayList;

import common.AudioLabel;

public class PhonemeHMMBasedAligner
{
    private PhonemeHMM model;
    
    public PhonemeHMMBasedAligner(PhonemeHMM model)
    {
        this.model = model;
    }
    
    public ArrayList<AudioLabel> align(String text, double[][] audioData, double totalTime)
    {
        String[] result = this.model.calculateMostProbableSequence(audioData, text);
        
        double stepTime = (double)audioData.length / totalTime;
        
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        int start = 0;
        int count = 0;
        String prev = null;
        for (String phoneme : result) {
            if (prev != phoneme) {
                if (prev != null) {
                    double startTime = start * stepTime;
                    double endTime = count * stepTime;
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
