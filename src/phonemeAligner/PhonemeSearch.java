package phonemeAligner;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import common.AudioLabel;
import commonExceptions.ImplementationError;

import edu.cmu.sphinx.decoder.scorer.ScoreProvider;
import edu.cmu.sphinx.decoder.scorer.Scoreable;
import edu.cmu.sphinx.decoder.search.ActiveList;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;
import edu.cmu.sphinx.linguist.acoustic.AcousticModel;
import edu.cmu.sphinx.linguist.acoustic.Context;
import edu.cmu.sphinx.linguist.acoustic.HMM;
import edu.cmu.sphinx.linguist.acoustic.HMMPosition;
import edu.cmu.sphinx.linguist.acoustic.HMMState;
import edu.cmu.sphinx.linguist.acoustic.HMMStateArc;
import edu.cmu.sphinx.linguist.acoustic.Unit;
import edu.cmu.sphinx.linguist.acoustic.UnitManager;
import edu.cmu.sphinx.linguist.acoustic.tiedstate.SenoneHMMState;
import graphemesToPhonemesConverters.IWordToPhonemesConverter;


public class PhonemeSearch {
	IWordToPhonemesConverter converter;
	UnitManager unitManager;
	AcousticModel acousticModel;
	
	public PhonemeSearch(
			IWordToPhonemesConverter converter,
			UnitManager unitManager,
			AcousticModel acousticModel)
	{
		this.converter = converter;
		this.unitManager = unitManager;
		this.acousticModel = acousticModel;
	}
	
	private class PhonemeScore
	{
		HMM[] hmms;
		HMMState state;
		double score = 0;
		int[] indexes;
		int currentIndex = 0;

		public PhonemeScore(HMM[] hmms)
		{
			this.hmms = hmms;
			this.state = hmms[this.currentIndex].getInitialState();
			this.indexes = new int[hmms.length];
		}

		private PhonemeScore(PhonemeScore other, HMMStateArc arc, Data data)
		{
			this.hmms = other.hmms;
			this.currentIndex = other.currentIndex;
			this.state = arc.getHMMState();
			this.score = other.score + arc.getLogProbability() + ((SenoneHMMState)this.state).getScore(data);
			this.indexes = other.indexes.clone();
		}
		private PhonemeScore(PhonemeScore other, int currentIndex, Data data, int dataIndex)
		{
			this.hmms = other.hmms;
			this.currentIndex = Math.min(currentIndex, this.hmms.length);
			if (currentIndex < this.hmms.length)
				this.state = other.hmms[this.currentIndex].getInitialState();
			else this.state = other.state;
			this.score = other.score + ((SenoneHMMState)this.state).getScore(data);
			this.indexes = other.indexes.clone();
			if (currentIndex < this.hmms.length)
				this.indexes[currentIndex] = dataIndex;
		}

		public ArrayList<PhonemeScore> score(Data data, int dataIndex)
		{
			ArrayList<PhonemeScore> ret = new ArrayList<PhonemeScore>();
			ret.add(new PhonemeScore(this, currentIndex, data, dataIndex));
			for (HMMStateArc arc : state.getSuccessors()) {
				if (arc.getHMMState().isExitState())
				{
					ret.add(new PhonemeScore(this, currentIndex + 1, data, dataIndex));
				}
				else ret.add(new PhonemeScore(this, arc, data));
			}
			return ret;
		}
		
		public boolean isExit()
		{
			return (this.currentIndex >= this.hmms.length);
		}
		
		public double getScore()
		{
			return this.score;
		}

		public int[] getIndexes()
		{
			return this.indexes;
		}
		
		public int compare(PhonemeScore other)
		{
			if (this.score < other.score) return 1;
			if (this.score > other.score) return -1;
			return 0;
		}
		public boolean equals(PhonemeScore other)
		{
			if (this.currentIndex != other.currentIndex) return false;
			return this.state.equals(other.state);
		}
		public String toString()
		{
			String ret = this.currentIndex + " " + ((SenoneHMMState)this.state) + " [";
			for (int i = 0; i < this.indexes.length; ++i)
				ret += this.indexes[i] + ", ";
			return ret.substring(0, ret.length() - 2) + "] " + this.score;
		}
	}
	
	class ActiveList
	{
		ArrayList<PhonemeScore> scores = new ArrayList<PhonemeScore>();
		
		void add(PhonemeScore score)
		{
			boolean add = true;
			int count = 0;
			for (int i = 0; i < scores.size(); ++i) {
				if (!scores.get(i).equals(score)) continue;
				if (scores.get(i).getScore() < score.getScore()) {
					++count;
					if (count >= 100)
						scores.remove(i);
					add = true;
				}
				else if (count < 100) add = false;
			}
			if (add) scores.add(score);
		}

		public void addAll(ArrayList<PhonemeScore> scores)
		{
			for (PhonemeScore score : scores) add(score);
		}
	}
	ArrayList<AudioLabel> findPhonemes2(AudioLabel word, FloatData[] wordSequence, common.Data[] wordSpectrumSequence)
	{
		String[] phonemes = converter.convert(word.getLabel()).get(0).split(" ");
		HMM[] hmms = new HMM[phonemes.length];
		for (int i = 0; i < phonemes.length; ++i) {
			HMMPosition position = HMMPosition.INTERNAL;
			if (i == 0) position = HMMPosition.BEGIN;
			if (i == phonemes.length - 1) position = HMMPosition.END;
			Unit unit = unitManager.getUnit(phonemes[i], false, Context.EMPTY_CONTEXT);
			hmms[i] = acousticModel.lookupNearestHMM(unit, position, false);
		}
		int minLength = 5;
		
		double avePower = 0;
		for (int i = 0; i < wordSequence.length - minLength; ++i) {
			avePower += wordSpectrumSequence[i].getSpectrum()[0];
		}
		avePower /= wordSpectrumSequence.length;
		double aveBackgroundPower = 0;
		int count = 0;
		for (int i = 0; i < wordSequence.length - minLength; ++i) {
			double power = wordSpectrumSequence[i].getSpectrum()[0];
			if (power >= avePower) continue;
			aveBackgroundPower += power;
			++count;
		}
		aveBackgroundPower /= count;
		double aveBackgroundPower2 = 0;
		count = 0;
		for (int i = 0; i < wordSequence.length - minLength; ++i) {
			double power = wordSpectrumSequence[i].getSpectrum()[0];
			if (power >= aveBackgroundPower) continue;
			aveBackgroundPower2 += power;
			++count;
		}
		aveBackgroundPower2 /= count;
		
		double[][] scores = new double[phonemes.length][wordSequence.length - minLength];
		for (int i = 0; i < wordSequence.length - minLength; ++i) {
			for (int p = 0; p < phonemes.length; ++p) {
				if (wordSpectrumSequence[i].getSpectrum()[0] < aveBackgroundPower)
					scores[p][i] = Double.MIN_VALUE;
				else
					scores[p][i] = calculateBestScore(wordSequence, i, minLength, hmms[p]);
//				scores[p][i] += wordSpectrumSequence[i].getSpectrum()[0];
			}
		}
		int neighSize = 5;// wordSequence.length / phonemes.length / 2;
		boolean[][] localExtremums = new boolean[wordSequence.length - minLength][phonemes.length];
		int maxSequenceIndex = wordSequence.length - 1 - minLength;
		for (int p = 0; p < phonemes.length; ++p) {
			for (int i = 0; i < wordSequence.length - minLength; ++i) {
				boolean isLocalEkstremum = (scores[p][i] != Double.MIN_VALUE);
				for (int j = Math.max(0, i - neighSize); j <= Math.min(maxSequenceIndex, i + neighSize); ++j) {
					if (i == j) continue;
					if (scores[p][i] < scores[p][j]) {
						isLocalEkstremum = false;
						break;
					}
				}
				localExtremums[i][p] = isLocalEkstremum;
				if (isLocalEkstremum) {
					System.err.println(phonemes[p] + " " + wordSpectrumSequence[i].getStartTime() + " " + scores[p][i]);
				}
			}
		}
		
		ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
		for (int i = 0; i < phonemes.length; ++i) {
		}
		return ret;
	}
	public ArrayList<AudioLabel> findPhonemes(AudioLabel word, FloatData[] wordSequence, common.Data[] wordSpectrumSequence) throws ImplementationError
	{
		double dataTimeDiff = (word.getEnd() - word.getStart()) / (double)wordSequence.length;
//		if (wordSequence.length != wordSpectrumSequence.length)
//			throw new ImplementationError("(wordSequence.length != wordSpectrumSequence.length) "
//							+ wordSequence.length + " != " + wordSpectrumSequence.length);

		String[] phonemes = converter.convert(word.getLabel()).get(0).split(" ");
		if (wordSequence.length < phonemes.length)
			System.err.println("ERROR: " + word.getLabel() + " " + word.getStart());
//			throw new ImplementationError(word.getLabel());
		if ((phonemes.length < 2) || (wordSequence.length < phonemes.length)) {
			ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
			ret.add(new AudioLabel(converter.convert(word.getLabel()).get(0), word.getStart(), word.getEnd()));
			return ret;
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
		scores[0] = calculateBestScore(wordSequence, 0, minLength, hmms[0]);
		int[][] sequences = new int[phonemes.length][phonemes.length];
		for (int i = 1; i < wordSequence.length - minLength; i++) {
			double[] nextScores = new double[phonemes.length];
			int[][] nextSequences = new int[phonemes.length][phonemes.length];
			for (int p = 0; p < phonemes.length; ++p) {
				double auxScore = calculateBestScore(wordSequence, i, minLength, hmms[p]);
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
//			double start = wordSpectrumSequence[sequences[phonemes.length - 1][i]].getStartTime();
//			double end = (i + 1 < phonemes.length) ? 
//					wordSpectrumSequence[sequences[phonemes.length - 1][i + 1]].getStartTime() :
//					word.getEnd();
			double start = sequences[phonemes.length - 1][i] * dataTimeDiff + word.getStart();
			double end = (i + 1 < phonemes.length) ? 
					(sequences[phonemes.length - 1][i + 1] * dataTimeDiff + word.getStart()) :
					word.getEnd();
			ret.add(new AudioLabel(phonemes[i], start, end));
		}
		return ret;
	}
	
	private class ScoreWithIndex
	{
		double score;
		int index;
		public ScoreWithIndex(double score, int index) {
			this.score = score;
			this.index = index;
		}
	}
	private double calculateBestScore(FloatData[] wordSequence, int i, int minLength, HMM hmm)
	{
		HMMState state = hmm.getState(1);
		double bestScore = Double.MIN_VALUE;
		for (int j = i; j < i + minLength; ++j) {
			double auxScore = state.getScore(wordSequence[i]);
			if ((bestScore == Double.MIN_VALUE) || (bestScore < auxScore))
				bestScore = auxScore;
		}
		return bestScore;
	}
	
	private double calculateBestScore2(FloatData[] wordSequence, int i, int minLength, HMM hmm)
	{
		HMMState initialState = hmm.getInitialState();
		double bestScore = Double.MIN_VALUE;
		HashMap<HMMState, Double> scores = new HashMap<HMMState, Double>();
		scores.put(initialState, 0.0);
		for (int j = i; j < wordSequence.length; ++j) {
			HashMap<HMMState, Double> nextScores = new HashMap<HMMState, Double>();
			for (HMMState state : scores.keySet()) {
				double currentScore = scores.get(state);
				double stateEventScore = ((SenoneHMMState)state).getScore(wordSequence[j]);
				double noChangeScore = currentScore + stateEventScore;
				nextScores.put(state, noChangeScore);
			}
			for (HMMState state : scores.keySet()) {
				double currentScore = nextScores.get(state);
				double prevScore = scores.get(state);
				for (HMMStateArc arc : state.getSuccessors()) {
					double newStateScore = prevScore + arc.getLogProbability();
					if (!arc.getHMMState().isExitState()) {
						newStateScore +=  ((SenoneHMMState)arc.getHMMState()).getScore(wordSequence[j]);
						nextScores.put(arc.getHMMState(), Math.max(currentScore, newStateScore));
					} else if ((j - i >= minLength) && ((bestScore == Double.MIN_VALUE)
							   		  || (bestScore < newStateScore)))
						bestScore = newStateScore;
				}
			}
			scores = nextScores;
		}
		
		return bestScore;
	}

	ArrayList<AudioLabel> findPhonemes3(AudioLabel word, FloatData[] wordSequence)
	{
		double dataTimeDiff = (word.getEnd() - word.getStart()) / (double)wordSequence.length;
		
		String[] phonemes = converter.convert(word.getLabel()).get(0).split(" ");
		HMM[] hmms = new HMM[phonemes.length];
		for (int i = 0; i < phonemes.length; ++i) {
			HMMPosition position = HMMPosition.INTERNAL;
			if (i == 0) position = HMMPosition.BEGIN;
			if (i == phonemes.length - 1) position = HMMPosition.END;
			Unit unit = unitManager.getUnit(phonemes[i], false, Context.EMPTY_CONTEXT);
			hmms[i] = acousticModel.lookupNearestHMM(unit, position, false);
		}
		
		ArrayList<PhonemeScore> scores = new ArrayList<PhonemeScore>();
		scores.add(new PhonemeScore(hmms));
		int dataIndex = 0;
		for (Data data : wordSequence) {
			ActiveList nextScores = new ActiveList();
			for (PhonemeScore score : scores)
				nextScores.addAll(score.score(data, dataIndex));
			scores = new ArrayList<PhonemeScore>();
			scores.addAll(nextScores.scores);
			for (PhonemeScore score : scores)
				System.err.println(score);
			dataIndex++;
		}
		
		PhonemeScore bestScore = scores.get(0);
		for (PhonemeScore score : scores) {
			if (!score.isExit()) continue;
			if (bestScore.getScore() < score.getScore())
				bestScore = score;
		}
		
		ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
		int prevIndex = 0;
		for (int i = 0; i < phonemes.length; ++i) {
			int currEndIndex = bestScore.getIndexes()[i];
			double start = word.getStart() + prevIndex * dataTimeDiff;
			double end = word.getStart() + currEndIndex * dataTimeDiff;
			ret.add(new AudioLabel(phonemes[i], start, end));
			prevIndex = currEndIndex;
		}
		return ret;
	}
}
