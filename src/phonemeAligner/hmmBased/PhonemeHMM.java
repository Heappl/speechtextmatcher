package phonemeAligner.hmmBased;

import java.util.ArrayList;

import common.algorithms.hmm.BestSequenceFinder;
import common.exceptions.ImplementationError;

public class PhonemeHMM
{
    private final HMMGraphFromPhonemeSequenceCreator hmmGraphCreator;
    
    public PhonemeHMM(HMMGraphFromPhonemeSequenceCreator hmmGraphCreator)
    {
        this.hmmGraphCreator = hmmGraphCreator;
    }
    public String[] calculateMostProbableSequence(ArrayList<double[]> audioData, String text) throws ImplementationError
    {
        return new BestSequenceFinder().findBestSequence(audioData, this.hmmGraphCreator.create(text));
    }
}
