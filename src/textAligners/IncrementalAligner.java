package textAligners;
import java.util.ArrayList;

import common.Data;
import common.DataSequence;

class Match
{
	public DataSequence sequence;
}

public class IncrementalAligner
{
	private ArrayList<DataSequence> sequences = new ArrayList<DataSequence>();
	private ArrayList<DataSequence> referenceSeqs = new ArrayList<DataSequence>();
	private DataSequence average = new DataSequence();
	int spectrumSize = 0;
	int numberOfFrames;
	
	public IncrementalAligner(int numberOfFrames)
	{
		this.numberOfFrames = numberOfFrames;
	}
	
	public void addSequence(DataSequence sequence)
	{
		if (sequence.size() < numberOfFrames) return;
		sequences.add(sequence);
		referenceSeqs.add(new DataSequence(sequence.subList(0, numberOfFrames)));
		spectrumSize = sequence.get(0).getSpectrum().length;
	}
	
	public void align()
	{
		calculateAverageSeq();
		double[] diffs = new double[sequences.size()];
		
		while (true) {
			
			for (int i = 0; i < sequences.size(); ++i) {
				Match better = findBetterMatch(average, sequences.get(i));
				updateAverage(better, referenceSeqs.get(i));
				referenceSeqs.remove(i);
				referenceSeqs.add(i, better.sequence);
			}
		}
	}

	private void updateAverage(Match better, DataSequence dataSequence)
	{
	}

	private Match findBetterMatch(DataSequence target, DataSequence inquiry)
	{
		
		return null;
	}

	private void calculateAverageSeq()
	{
		for (int i = 0; i < numberOfFrames; ++i) {
			double[] frameSpectrum = new double[spectrumSize];
			for (int j = 0; j < sequences.size(); ++j) {
				double[] spectrum = sequences.get(j).get(i).getSpectrum();
				for (int k = 0; k < spectrumSize; ++k) frameSpectrum[k] += spectrum[k];
			}
			for (int k = 0; k < spectrumSize; ++k) frameSpectrum[k] /= sequences.size();
			average.add(new Data(0, 0, frameSpectrum));
		}
	}
}
