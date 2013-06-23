import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.AudioLabel;
import common.Data;
import common.DataSequence;
import common.PhonemeDiff;
import common.Speech;
import common.Text;
import diffCalculators.HungarianMatchDiffCalculator;
import diffCalculators.ISequenceDiffCalculator;
import diffCalculators.SpectrumDiffCalculator;
import diffCalculators.SpectrumWeights;


public class CommonWordPhonemesFinder {
	DataSequence allData;
	Text text;
	double frameTime = 0;
	AudioLabel[] matched = null;
	ISequenceDiffCalculator diffCalculator = new HungarianMatchDiffCalculator(new SpectrumDiffCalculator());
	
	public CommonWordPhonemesFinder(DataSequence allData, Text text, AudioLabel[] matched) {
		this.allData = allData;
		this.text = text;
		this.frameTime = this.allData.get(1).getStartTime() - this.allData.get(0).getStartTime();
		this.matched = matched;
		this.diffCalculator = new HungarianMatchDiffCalculator(
				new SpectrumDiffCalculator());
	}

	AudioLabel[] process()
	{
		String[] words = text.getWords();
		Map<String, Integer> wordCounts = new HashMap<String, Integer>();
		
		int biggestCommonSize = 0;
		String biggestCommon = "";
		for (String word : words) {
			word = word.toLowerCase();
//			if (word.equalsIgnoreCase("niespodziewanie")) continue;
//			if (word.equalsIgnoreCase("polichnowicza")) continue;
//			if (word.equalsIgnoreCase("bijakowskiego")) continue;
//			if (word.equalsIgnoreCase("polichnowicz")) continue;
//			if (word.equalsIgnoreCase("odpowiedział")) continue;
			if (wordCounts.containsKey(word)) {
				int count = wordCounts.get(word) + 1;
				wordCounts.put(word, count);
				if ((count > 3) && (word.length() > biggestCommonSize)) {
					biggestCommonSize = word.length();
					biggestCommon = word;
				}
			}
			else wordCounts.put(word, 1);
		}
		double phonemeTime = biggestCommonSize * text.getEstimatedTimePerCharacter();
		int neigh = (int)Math.ceil(Math.sqrt(Math.sqrt(Math.sqrt(matched.length))));
		System.err.println("biggest: " + biggestCommon + " " + phonemeTime + " neigh:" + neigh);
		
		ArrayList<ArrayList<Speech>> usedSpeeches = new ArrayList<ArrayList<Speech>>();
		for (int i = 0; i < matched.length; ++i) {
			AudioLabel matching = matched[i];
			if (matching.getLabel().toLowerCase().indexOf(biggestCommon) >= 0) {
				ArrayList<Speech> surroundingSpeeches = new ArrayList<Speech>();
				for (int j = Math.max(0, i - neigh); j < Math.min(matched.length, i + neigh); ++j) {
					matching = matched[j];
					int dataStartIndex = findIndex(matching.getStart(), 0, allData.size());
					int dataEndIndex = findIndex(matching.getEnd(), 0, allData.size());
					double startTime = allData.get(dataStartIndex).getStartTime();
					double endTime = allData.get(dataEndIndex).getEndTime();
					surroundingSpeeches.add(new Speech(startTime, endTime, dataStartIndex, dataEndIndex));
				}
				usedSpeeches.add(surroundingSpeeches);
			}
		}
		int frames = (int)Math.ceil(phonemeTime / frameTime);
		
		int[] bestMatchings = findBestMatchings(usedSpeeches.get(0), usedSpeeches.subList(1, usedSpeeches.size()), frames);
		
		AudioLabel[] res = new AudioLabel[bestMatchings.length];
		for (int i = 0; i < bestMatchings.length; ++i) {
			double start = allData.get(bestMatchings[i]).getStartTime();
			double end = allData.get(bestMatchings[i] + frames).getEndTime();
			res[i] = new AudioLabel(biggestCommon + "_" + i, start, end);
		}
		return res;
	}
	
	private int[] findBestMatchings(ArrayList<Speech> target, List<ArrayList<Speech>> witnesses, int frames)
	{
		double smallestDiff = Double.MAX_VALUE;
		int[] bestMatching = new int[witnesses.size() + 1];
		
//		for (Speech speech : target) {
//			for (int i = speech.getStartDataIndex(); i < speech.getEndDataIndex() - frames; ++i) {
//				int start = findIndex(allData.get(i).getStartTime(), 0, allData.size() - 1);
//				int end = findIndex(allData.get(i + frames).getStartTime(), 0, allData.size() - 1);
				int start = findIndex(517.261, 0, allData.size() - 1);
				int end = findIndex(518.193, 0, allData.size() - 1);
				int[] aux = new int[witnesses.size()];
				double diff = findBests(aux, witnesses, start, end - start);
				if (diff < smallestDiff) {
					smallestDiff = diff;
					bestMatching[0] = start;
					for (int j = 0; j < aux.length; ++j) bestMatching[j + 1] = aux[j];
				}
//			}
//		}
		return bestMatching;
	}

	private double findBests(int[] bestMatching, List<ArrayList<Speech>> witnesses, int targetStart, int frames)
	{
		int index = 0;
		double totalDiff = 1;
		for (ArrayList<Speech> speeches : witnesses) {
			PhonemeDiff diff = findBest(speeches, targetStart, frames);
			totalDiff += diff.getDiff();
			bestMatching[index] = diff.getSecondStart();
			++index;
		}
		return totalDiff;
	}

	private PhonemeDiff findBest(ArrayList<Speech> speeches, int targetStart, int frames)
	{
		double bestDiff = Double.MAX_VALUE;
		int bestIndex = 0;
		for (Speech speech : speeches) {
			if ((speech.getStartDataIndex() <= targetStart) && (targetStart <= speech.getEndDataIndex()))
				continue;
			for (int i = speech.getStartDataIndex(); i < speech.getEndDataIndex() - frames; ++i) {
				double diff = calculateDiff(targetStart, i, frames);
				if (diff < bestDiff) {
					bestDiff = diff;
					bestIndex = i;
				}
			}
		}
		return new PhonemeDiff(bestDiff, targetStart, bestIndex, frames);
	}

	private int findIndex(double time, int bottom, int top)
	{
		if (bottom == top) return bottom;
		int between = (top + bottom) / 2;
		double auxTime = (allData.get(between).getStartTime() + allData.get(between + 1).getEndTime()) / 2;
		if (time < auxTime) return findIndex(time, bottom, between);
		else return findIndex(time, between + 1, top);
	}

	private double calculateDiff(int firstStart, int secondStart, int frames)
	{
		if (firstStart == secondStart) return Double.MAX_VALUE;
		return diffCalculator.diff(
				allData.subList(firstStart, firstStart + frames).toArray(new Data[0]),
				allData.subList(secondStart, secondStart + frames).toArray(new Data[0]));
	}
}
