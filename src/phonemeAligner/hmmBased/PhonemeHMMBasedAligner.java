package phonemeAligner.hmmBased;

import java.util.ArrayList;

import common.AudioLabel;
import common.algorithms.hmm.HMMPathGraph;
import common.algorithms.hmm.HMMResultSequence;
import common.algorithms.hmm.HMMState;
import common.algorithms.hmm.HiddenMarkovModel;

public class PhonemeHMMBasedAligner
{
    private HiddenMarkovModel model;
    private HMMGraphFromPhonemeSequenceCreator hmmGraphCreator = new HMMGraphFromPhonemeSequenceCreator();
    
    public PhonemeHMMBasedAligner(HiddenMarkovModel model)
    {
        this.model = model;
    }
    
    public ArrayList<AudioLabel> align(String[][] phonemeSequence, double[][] audioData, double totalTime)
    {
        HMMPathGraph sequenceGraph = this.hmmGraphCreator.create(phonemeSequence);
        HMMResultSequence result = this.model.calculateMostProbableSequence(audioData, sequenceGraph);
        
        double stepTime = (double)audioData.length / totalTime;
        
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        int start = 0;
        int count = 0;
        HMMState prev = null;
        for (HMMState state : result) {
            if (prev != state) {
                if (prev != null) {
                    double startTime = start * stepTime;
                    double endTime = count * stepTime;
                    ret.add(new AudioLabel(prev.getName(), startTime, endTime));
                }
                prev = state;
                start = count;
            }
            ++count;
        }
        
        return ret;
    }
}
