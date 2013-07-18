package phonemeAligner;
import graphemesToPhonemesConverters.IWordToPhonemesConverter;

import java.util.ArrayList;
import java.util.HashMap;

import common.AudioLabel;
import common.DataSequence;
import commonExceptions.ImplementationError;
import diffCalculators.ISpectrumDiffCalculator;


public class WordToPhonemeBasedOnDistanceFromAverageAligner {
	
	private AudioLabel[] words;
	private DataSequence data;
	private int spectrumSize;
	private IWordToPhonemesConverter converter;
	private ISpectrumDiffCalculator diffCalculator;
	private double frameTime;
	
	public WordToPhonemeBasedOnDistanceFromAverageAligner(
			AudioLabel[] words,
			DataSequence allData,
			IWordToPhonemesConverter converter,
			ISpectrumDiffCalculator diffCalculator)
	{
		this.words = words;
		this.data = allData;
		this.spectrumSize = allData.get(0).getSpectrum().length;
		this.converter = converter;
		this.diffCalculator = diffCalculator;
		this.frameTime = allData.get(1).getStartTime() - allData.get(0).getStartTime();
	}
	
	public ArrayList<AudioLabel> align(int iterations) throws ImplementationError
	{
		HashMap<String, PhonemeAverage> averages = new HashMap<String, PhonemeAverage>();
		ArrayList<WordSeparation> auxSeparations = new ArrayList<WordSeparation>();
		for (int i = 0; i < this.words.length; ++i) {
			String repr = this.converter.convert(this.words[i].getLabel()).get(0);
			if (repr.split(" ").length < 2) continue;
			String[] phonemes = splitToPhonemes(repr, 1);
			for (String phoneme : phonemes)
				if (!averages.containsKey(phoneme))
			 		averages.put(phoneme, new PhonemeAverage());
			auxSeparations.add(new WordSeparation(
				this.words[i].getStart(), phonemes, extractSeq(this.words[i]), averages));
		}
		
		WordSeparation[] separations = auxSeparations.toArray(new WordSeparation[0]);
		System.err.println("#words " + separations.length);
		for (int i = 0; i < iterations; ++i) {
			System.err.println("iteration " + i);
			for (WordSeparation separation : separations)
				separation.recalculate(averages);
		}
		
		ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
		for (WordSeparation separation : separations)
			ret.addAll(separation.createLabels());
		return ret;
	}
	
	private String[] splitToPhonemes(String repr, int s)
	{
		String[] aux = repr.split(" ");
		ArrayList<String> split = new ArrayList<String>();
		for (int i = 0; i < aux.length / s; ++i) {
			int end = (i + 1) * s;
			if ((i + 1 >= aux.length / s) && ((i + 1) * s < aux.length))
				end = aux.length;
			String part = "";
			for (int j = i * s; j < end; ++j) {
				part += aux[j];
			}
			split.add(part);
		}
		return split.toArray(new String[0]);
	}

	private DataSequence extractSeq(AudioLabel audioLabel)
	{
		int start = findIndex(audioLabel.getStart(), 0, data.size() - 1);
		int end = findIndex(audioLabel.getEnd(), 0, data.size() - 1);
		return new DataSequence(data.subList(start, end));
	}

	private int findIndex(double time, int bottom, int top)
	{
		if (bottom == top) return bottom;
		int between = (top + bottom) / 2;
		double auxTime = (data.get(between).getStartTime() + data.get(between + 1).getEndTime()) / 2;
		if (time < auxTime) return findIndex(time, bottom, between);
		else return findIndex(time, between + 1, top);
	}

	private class PhonemeAverage
	{
		private double[] average;
		private int numOfPoints;
		
		public PhonemeAverage()
		{
			this.average = new double[spectrumSize];
		}
		
		void addPoint(double[] p) throws ImplementationError
		{
			if (p.length != average.length) throw new ImplementationError();
			for (int i = 0; i < p.length; ++i)
				average[i] += p[i];
			++numOfPoints;
		}
		
		void removePoint(double[] p) throws ImplementationError
		{
			if (p.length != average.length) throw new ImplementationError();
			for (int i = 0; i < p.length; ++i)
				average[i] -= p[i];
			--numOfPoints;
		}
		
		double diff(double[] p) throws ImplementationError
		{
			if (p.length != average.length) throw new ImplementationError();
			double[] auxAve = average.clone();
			for (int i = 0; i < auxAve.length; ++i)
				auxAve[i] /= numOfPoints;
			return diffCalculator.diff(auxAve, p);
		}
	}

	class WordSeparation
	{
		private String[] phonemes;
		private DataSequence sequence;
		private int[] separation;
		private double startTime;
		
		public WordSeparation(
				double startTime,
				String[] phonemes,
				DataSequence seq,
				HashMap<String, PhonemeAverage> averages) throws ImplementationError
		{
			this.startTime = startTime;
			this.phonemes = phonemes;
			this.sequence = seq;
			this.separation = new int[phonemes.length];
			
			int step = seq.size() / phonemes.length;
			int initial = seq.size() - step * phonemes.length;
			for (int i = 0; i < phonemes.length; ++i) {
				int start = i * step + ((i == 0) ? 0 : initial);
				int end = (i + 1) * step + initial;
				PhonemeAverage phonemeAverage = averages.get(phonemes[i]);
				this.separation[i] = end;
				for (int j = start; j < end; ++j) {
					phonemeAverage.addPoint(seq.get(j).getSpectrum());
				}
			}
		}
		
		public void recalculate(HashMap<String, PhonemeAverage> averages) throws ImplementationError
		{
			removePreviousSeparation(averages);
			double[] currScores = new double[this.phonemes.length];
			int[][] transitions = new int[this.phonemes.length][this.phonemes.length];
			int minSize = 5;
			for (int j = 1; j < this.phonemes.length; ++j)
				currScores[j] = Double.MAX_VALUE;
			for (int i = 0; i < this.sequence.size(); ++i) {
				double[] nextScores = new double[this.phonemes.length];
				int[][] nextTransitions = new int[this.phonemes.length][];
				for (int j = 0; j < this.phonemes.length; ++j) {
					double diff = averages.get(this.phonemes[j]).diff(sequence.get(i).getSpectrum());
					double nextScoreWithoutTransition = currScores[j] + diff;
					double nextScoreWithTransition = (j > 0) ? currScores[j - 1] + diff : Double.MAX_VALUE;
					if ((j < this.phonemes.length - 1) && (this.sequence.size() - i < minSize))
						nextScoreWithoutTransition = Double.MAX_VALUE;
					if ((j > 0) && (Math.abs(i - transitions[j - 1][j - 1]) < minSize))
						nextScoreWithTransition = Double.MAX_VALUE;
					if ((j == 0) && (i < minSize))
						nextScoreWithTransition = Double.MAX_VALUE;
					if (nextScoreWithoutTransition <= nextScoreWithTransition) {
						nextScores[j] = nextScoreWithoutTransition;
						nextTransitions[j] = transitions[j].clone();
					} else {
						nextScores[j] = nextScoreWithTransition;
						nextTransitions[j] = (j > 0) ? transitions[j - 1].clone() : new int[this.phonemes.length];
						nextTransitions[j][j] = i;
					}
				}
				transitions = nextTransitions;
				currScores = nextScores;
			}
			this.separation[this.separation.length - 1] = this.sequence.size();
			for (int i = this.phonemes.length - 1; i > 0; --i) {
				this.separation[i - 1] = transitions[this.phonemes.length - 1][i];
			}

			int start = 0;
			for (int i = 0; i < this.separation.length; ++i) {
				PhonemeAverage phonemeAverage = averages.get(phonemes[i]);
				for (int j = start; j < this.separation[i]; ++j)
					phonemeAverage.addPoint(this.sequence.get(j).getSpectrum());
				start = this.separation[i];
			}
		}
		
		public ArrayList<AudioLabel> createLabels()
		{
			ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
			int prevSeparationIndex = 0;
			for (int i = 0; i < this.separation.length; ++i) {
				double start = this.startTime + prevSeparationIndex * frameTime;
				double end = this.startTime + this.separation[i] * frameTime;
				ret.add(new AudioLabel(this.phonemes[i], start, end));
				prevSeparationIndex = this.separation[i];
			}
			return ret;
		}
		
		private void removePreviousSeparation(HashMap<String, PhonemeAverage> averages) throws ImplementationError
		{
			int start = 0;
			for (int i = 0; i < this.separation.length; ++i) {
				PhonemeAverage phonemeAverage = averages.get(phonemes[i]);
				for (int j = start; j < this.separation[i]; ++j)
					phonemeAverage.removePoint(this.sequence.get(j).getSpectrum());
				start = this.separation[i];
			}
		}
	}
	
	//A:
	
	//state:
	//	separation for each word with phoneme attached
	//  phoneme with set of separations?
	//initial:
	//  separation to equal parts
	
	//iteration:
	//  for each word:
	//    find best separation given all other data -> minimize diff from other phonemes (average?)
	//keep total value? for some other algorithms?
	
	//B:
	
	//state:
	//  classification as set of points (with neighborhood) for each phoneme
	//initial state:
	//  for each word phonemes points are equally spaced
	
	//iteration:
	//  learn each phoneme based on given classification
	//  classify each word with learned centroids? lr? nn?
	
	//C: A + B
	//D: A + B with diff based on classification (what matters)
}
