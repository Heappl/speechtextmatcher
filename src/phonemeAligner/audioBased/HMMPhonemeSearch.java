package phonemeAligner.audioBased;

import java.util.ArrayList;

import common.AudioLabel;
import common.exceptions.ImplementationError;

import edu.cmu.sphinx.frontend.FloatData;
import edu.cmu.sphinx.linguist.acoustic.AcousticModel;
import edu.cmu.sphinx.linguist.acoustic.Context;
import edu.cmu.sphinx.linguist.acoustic.HMM;
import edu.cmu.sphinx.linguist.acoustic.HMMPosition;
import edu.cmu.sphinx.linguist.acoustic.HMMState;
import edu.cmu.sphinx.linguist.acoustic.HMMStateArc;
import edu.cmu.sphinx.linguist.acoustic.LeftRightContext;
import edu.cmu.sphinx.linguist.acoustic.Unit;
import edu.cmu.sphinx.linguist.acoustic.UnitManager;
import graphemesToPhonemesConverters.IWordToPhonemesConverter;

public class HMMPhonemeSearch
{
    IWordToPhonemesConverter converter;
    UnitManager unitManager;
    AcousticModel acousticModel;
    
    public HMMPhonemeSearch(
            IWordToPhonemesConverter converter,
            UnitManager unitManager,
            AcousticModel acousticModel)
    {
        this.converter = converter;
        this.unitManager = unitManager;
        this.acousticModel = acousticModel;
    }
    
    private class State
    {
        HMMState scorer;
        String phoneme;
        public State(String phoneme, HMMState scorer, double exitProb)
        {
            this.phoneme = phoneme;
            this.scorer = scorer;
        }
        
        public double score(double[] data)
        {
            double score = scorer.getScore(convert(data));
//            if (score >= 0) return -Double.MIN_VALUE;
            return score;
        }
        
        public String toString()
        {
            return phoneme + " " + scorer;
        }
    }
    
    public ArrayList<AudioLabel> findPhonemes(
            AudioLabel word, ArrayList<double[]> data, double averageBackgroundPower) throws ImplementationError
    {
        double dataTimeDiff = (word.getEnd() - word.getStart()) / (double)data.size();

        String[] phonemes = ("SIL " + converter.convert(word.getLabel()).get(0) + " SIL").split(" ");
        if ((phonemes.length < 2) || (data.size() < phonemes.length)) {
            return new ArrayList<AudioLabel>();
        }
        
        //prepare hmms
        HMM[] hmms = new HMM[phonemes.length];
        for (int i = 0; i < phonemes.length; ++i) {
            HMMPosition position = HMMPosition.INTERNAL;
            if (i == 0) position = HMMPosition.BEGIN;
            if (i == phonemes.length - 1) position = HMMPosition.END;
            
            Context context = Context.EMPTY_CONTEXT;
            boolean isFiller = phonemes[i].equals("SIL");
            if (!isFiller) {
                Unit leftContextUnit = unitManager.getUnit(phonemes[i - 1], false, Context.EMPTY_CONTEXT);
                Unit rightContextUnit = unitManager.getUnit(phonemes[i + 1], false, Context.EMPTY_CONTEXT);
                context = LeftRightContext.get(new Unit[]{leftContextUnit}, new Unit[]{rightContextUnit});
            }
            Unit unit = unitManager.getUnit(phonemes[i], false, context);
            hmms[i] = acousticModel.lookupNearestHMM(unit, position, false);
        }

        //prepare states
        State[] states = new State[hmms.length * 3];
        for (int i = 0; i < hmms.length; ++i) {
            double exitProb1 = 0;
            for (HMMStateArc arc : hmms[i].getState(0).getSuccessors())
                if (arc.getHMMState() == hmms[i].getState(1))
                    exitProb1 = arc.getLogProbability();
            double exitProb2 = 0;
            for (HMMStateArc arc : hmms[i].getState(1).getSuccessors())
                if (arc.getHMMState() == hmms[i].getState(2))
                    exitProb2 = arc.getLogProbability();
            double exitProb3 = 0;
            for (HMMStateArc arc : hmms[i].getState(2).getSuccessors())
                if (arc.getHMMState().isExitState())
                    exitProb3 = arc.getLogProbability();
            
            states[3 * i] = new State(phonemes[i], hmms[i].getState(0), exitProb1);
            states[3 * i + 1] = new State(phonemes[i], hmms[i].getState(1), exitProb2);
            states[3 * i + 2] = new State(phonemes[i], hmms[i].getState(2), exitProb3);
        }
        
        double[] scores = new double[states.length];
        int[][] paths = new int[data.size()][states.length];
        for (int i = 1; i < scores.length; ++i) scores[i] = Double.NEGATIVE_INFINITY;
        scores[0] = states[0].score(data.get(0));
        
        for (int i = 1; i < data.size(); ++i) {
            double[] newScores = new double[states.length];
            int[][] newPaths = new int[data.size()][];
            newPaths[0] = new int[states.length]; 
            newScores[0] = scores[0] + states[0].score(data.get(i));
            
            for (int j = 1; j < states.length; ++j) {
                double frameScore = states[j].score(data.get(i));
                if (data.get(i)[0] < averageBackgroundPower) frameScore = 0;
                double noChangeScore = scores[j] + frameScore;
                double changeScore = scores[j - 1] + frameScore;
                if (noChangeScore > changeScore) {
                    newScores[j] = noChangeScore;
                    newPaths[j] = paths[j].clone();
                } else {
                    newScores[j] = changeScore;
                    newPaths[j] = paths[j - 1].clone();
                    newPaths[j][j] = i;
                }
            }
            
            paths = newPaths;
            scores = newScores;
        }
        
        int endIndex = data.size();
        AudioLabel[] labels = new AudioLabel[states.length];
        for (int i = states.length - 1; i >= 0; --i) {
            int startIndex = paths[states.length - 1][i];
            double startTime = startIndex * dataTimeDiff + word.getStart();
            double endTime = endIndex * dataTimeDiff + word.getStart();
            labels[i] = new AudioLabel(states[i].phoneme, startTime, endTime);
            endIndex = startIndex;
        }
        
        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        for (int i = 0; i < labels.length; i += 3) {
//            if (labels[i].getLabel().equals("SIL")) continue;
            double startTime = labels[i].getStart();
            double endTime = labels[i + 2].getEnd();
            String label = labels[i].getLabel();
            ret.add(new AudioLabel(label, startTime, endTime));
        }
        return ret;
    }
    
    private FloatData convert(double[] data)
    {
        float[] values = new float[data.length - 1];
        for (int i = 0; i < values.length; ++i) {
            values[i] = (float)data[i + 1];
        }
        return new FloatData(values, 1, 0);
    }
}
