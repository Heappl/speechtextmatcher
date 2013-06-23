import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import common.AudioLabel;
import common.Data;
import common.DataSequence;

import algorithms.HungarianAlgorithm;

import diffCalculators.SpectrumDiffCalculator;
import diffCalculators.SpectrumWeights;



public class PhonemeLearner
{
	private AudioLabel[] prepared;
	DataSequence allData;
	SpectrumDiffCalculator diffCalculator = new SpectrumDiffCalculator();
	
	public PhonemeLearner(AudioLabel[] prepared, DataSequence allData)
	{
		this.prepared = prepared;
		this.allData = allData;
		this.diffCalculator = new SpectrumDiffCalculator(new SpectrumWeights(allData).getWeights());
	}
	
	public void process()
	{
        HashMap<String, ArrayList<AudioLabel>> starts = new HashMap<String, ArrayList<AudioLabel>>();
        for (AudioLabel label : prepared) {
        	for (int i = 3; i < label.getLabel().length(); ++i) {
        		String prefix = label.getLabel().substring(0, i).toLowerCase();
        		if (starts.containsKey(prefix)) starts.get(prefix).add(label);
        		else {
            		ArrayList<AudioLabel> newLabels = new ArrayList<AudioLabel>();
            		newLabels.add(label);
        			starts.put(prefix, newLabels);
        		}
        	}
        }
        for (String prefix : starts.keySet())
        {
        	if (prefix.length() < 3) continue;
        	if (starts.get(prefix).size() < 10) continue;
        	System.err.println(prefix + " " + starts.get(prefix).size());
        	learn(starts.get(prefix), prefix.length());
        	break;
        }
	}
	
	void learn(ArrayList<AudioLabel> entryset, int size)
	{
		int[][][] assignments = new int[entryset.size()][entryset.size()][];
		int ind = 0;
		for (AudioLabel label1 : entryset) {
			int start1 = findIndex(label1.getStart(), 0, allData.size() - 1);
			int end1 = findIndex(label1.getStart() + size * 0.1, 0, allData.size() - 1);
			if (end1 == start1) { ++ind; continue; }
			
			int j = 0;
			System.err.println(ind + " " + label1.getLabel());
			for (AudioLabel label2 : entryset) {
				if (label1 == label2) { ++j; continue; };
				System.err.println("	" + j + " " + label2.getLabel());
				int start2 = findIndex(label2.getStart(), 0, allData.size() - 1);
				int end2 = findIndex(label2.getStart() + size * 0.1, 0, allData.size() - 1);
				if (end2 == start2) { ++j; continue; }
				
				double[][] weights = calculateDiffs(
						allData.subList(start1, end1).toArray(new Data[0]),
						allData.subList(start2, end2).toArray(new Data[0]));
				
				HungarianAlgorithm ha = new HungarianAlgorithm(weights);
				assignments[ind][j] = ha.execute();
				++j;
			}
			++ind;
		}
		
		int s = assignments.length;
		int windowSize = 3;
		for (int i = 0; i < s * s * s; ++i) {
			int third = i % s;
			int second = (i / s) % s;
			int first = i / s / s;
			if (second <= first) continue;
			if (third <= second) continue;
			if (assignments[first] == null) continue;
			if (assignments[second] == null) continue;
			if (assignments[first][second] == null) continue;
			if (assignments[first][third] == null) continue;
			if (assignments[second][third] == null) continue;
			
			for (int j = 0; j < assignments[first][second].length - windowSize; ++j) {
				boolean continous = true;
				boolean triplet = true;
				boolean full = true;
				for (int k = j; k < j + windowSize; ++k) {
					int secondMatch = assignments[first][second][k];
					if (secondMatch < 0) {
						full = false;
						break;
					}
					int thirdThroughFirstMatch = assignments[first][third][k];
					int thirdThroughSecondMatch = assignments[second][third][secondMatch];
					if (thirdThroughFirstMatch != thirdThroughSecondMatch) {
						triplet = false;
						break;
					}
					if ((secondMatch != assignments[first][second][j] + k - j)
						 || (thirdThroughFirstMatch != assignments[first][third][j] + k - j)) {
						continous = false;
						break;
					}
				}
				if (full && continous && triplet) {
					System.err.println(first + " " + second + " " + third + " " + j);
				}
			}
		}
	}

	private double[][] calculateDiffs(Data[] first, Data[] second)
	{
		int window = 5;
		double divisor = 2.0;
		double[][] weights = new double[first.length - 2 * window][second.length - 2 * window];
		for (int i = window; i < first.length - window; ++i) {
			for (int j = window; j < second.length - window; ++j) {
				for (int k = -window; k <= window; ++k) {
					double currDivisor = (k == j) ? 1.0 : Math.pow(divisor, Math.abs(j - k));
					double[] firstSpectrum = first[i + k].getSpectrum();
					double[] secondSpectrum = second[j + k].getSpectrum();
					double diff = diffCalculator.diffNorm2(firstSpectrum, secondSpectrum);
					weights[i - window][j - window] += diff / currDivisor;
				}
			}
		}
		return weights;
	}

	private int findIndex(double time, int bottom, int top)
	{
		if (bottom == top) return bottom;
		int between = (top + bottom) / 2;
		double auxTime = (allData.get(between).getStartTime() + allData.get(between + 1).getEndTime()) / 2;
		if (time < auxTime) return findIndex(time, bottom, between);
		else return findIndex(time, between + 1, top);
	}
}
