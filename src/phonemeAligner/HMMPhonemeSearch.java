package phonemeAligner;

import java.util.ArrayList;

import common.AudioLabel;
import commonExceptions.ImplementationError;

import edu.cmu.sphinx.frontend.FloatData;
import edu.cmu.sphinx.linguist.acoustic.AcousticModel;
import edu.cmu.sphinx.linguist.acoustic.Context;
import edu.cmu.sphinx.linguist.acoustic.HMM;
import edu.cmu.sphinx.linguist.acoustic.HMMPosition;
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
    
    public ArrayList<AudioLabel> findPhonemes(AudioLabel word, ArrayList<double[]> data) throws ImplementationError
    {
        double dataTimeDiff = (word.getEnd() - word.getStart()) / (double)data.size();

        double averagePower = 0;
        for (double[] frameData : data) {
            averagePower += frameData[0];
        }
        averagePower /= data.size();
        
        double averageBackgroundPower = 0;
        int auxCount = 0;
        for (double[] frameData : data) {
            if (frameData[0] >= averagePower) continue;
            averageBackgroundPower += frameData[0];
            ++auxCount;
        }
        averageBackgroundPower /= auxCount;
        
        String[] phonemes = converter.convert(word.getLabel()).get(0).split(" ");
        if ((phonemes.length < 2) || (data.size() < phonemes.length)) {
            return new ArrayList<AudioLabel>();
        }
        HMM[] hmms = new HMM[phonemes.length];
        for (int i = 0; i < phonemes.length; ++i) {
            HMMPosition position = HMMPosition.INTERNAL;
            if (i == 0) position = HMMPosition.BEGIN;
            if (i == phonemes.length - 1) position = HMMPosition.END;
            Unit unit = unitManager.getUnit(phonemes[i], false, Context.EMPTY_CONTEXT);
            hmms[i] = acousticModel.lookupNearestHMM(unit, position, false);
        }
        
        int minLength = 10;
        
        double[] scores = new double[phonemes.length];
        for (int i = 1; i < phonemes.length; ++i) scores[i] = -Math.pow(2, 1000);
        scores[0] = calculateScore(data.get(0), hmms[0], averageBackgroundPower);
        int[][] sequences = new int[phonemes.length][phonemes.length];
        for (int i = 1; i < data.size() - minLength; i++) {
            double[] nextScores = new double[phonemes.length];
            int[][] nextSequences = new int[phonemes.length][phonemes.length];
            for (int p = 0; p < phonemes.length; ++p) {
                double auxScore = calculateScore(data.get(i), hmms[p], averageBackgroundPower);
                double score1 = scores[p] + auxScore;
                double score2 = (p > 0) ? scores[p - 1] + auxScore : score1 - 0.1;
                if ((p > 0) && (i - sequences[p - 1][p - 1] < minLength)) score2 = score1 - 0.1;
                if (score1 >= score2) {
                    nextScores[p] = score1;
                    nextSequences[p] = sequences[p].clone();
                } else {
                    nextScores[p] = score2;
                    nextSequences[p] = sequences[p - 1].clone();
                    nextSequences[p][p] = i + minLength / 2;//Math.max(minLength + sequences[p - 1][p - 1], i);
                }
            }
            scores = nextScores;
            sequences = nextSequences;
        }

        ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
        for (int i = 0; i < phonemes.length; ++i) {
//          double start = wordSpectrumSequence[sequences[phonemes.length - 1][i]].getStartTime();
//          double end = (i + 1 < phonemes.length) ? 
//                  wordSpectrumSequence[sequences[phonemes.length - 1][i + 1]].getStartTime() :
//                  word.getEnd();
            double start = sequences[phonemes.length - 1][i] * dataTimeDiff + word.getStart();
            double end = (i + 1 < phonemes.length) ? 
                    (sequences[phonemes.length - 1][i + 1] * dataTimeDiff + word.getStart()) :
                    word.getEnd();
            ret.add(new AudioLabel(phonemes[i], start, end));
        }
        return ret;
    }
    
    private double calculateScore(double[] frameData, HMM hmm, double averageBackgroundPower)
    {
        if (frameData[0] < averageBackgroundPower) return 0;
        float[] values = new float[frameData.length - 1];
        for (int i = 0; i < values.length; ++i) {
            values[i] = (float)frameData[i + 1];
        }
        return hmm.getState(1).getScore(new FloatData(values, 1, 0));
    }
}
