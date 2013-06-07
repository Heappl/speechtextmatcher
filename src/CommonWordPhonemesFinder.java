import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonWordPhonemesFinder {
	ArrayList<Data> allData;
	Text text;
	double frameTime = 0;
	double[] variances = null;
	double[] averages = null;
	AudioLabel[] matched = null;
	
	public CommonWordPhonemesFinder(ArrayList<Data> allData, Text text, AudioLabel[] matched) {
		this.allData = allData;//mergeData(allData, 2);
		this.text = text;
		this.frameTime = this.allData.get(1).getStartTime() - this.allData.get(0).getStartTime();
		this.averages = calcAverages();
		this.variances = calcVariances(averages);
		this.matched = matched;
	}
	
	private ArrayList<Data> mergeData(ArrayList<Data> allData, int neigh)
	{
		ArrayList<Data> res = new ArrayList<Data>();
		for (int i = neigh; i < allData.size() - neigh; i += neigh) {
			int spectrumSize = allData.get(i).getSpectrum().length;
			double[] newSpectrum = new double[spectrumSize];
			for (int j = i - neigh; j < i + neigh; ++j) {
				for (int k = 0; k < spectrumSize; ++k)
					newSpectrum[k] += allData.get(j).getSpectrum()[k];
			}
			for (int k = 0; k < spectrumSize; ++k) newSpectrum[k] /= neigh * 2;
			res.add(new Data(allData.get(i - neigh).getStartTime(),
							 allData.get(i + neigh).getEndTime(),
							 newSpectrum));
		}
		return res;
	}

	AudioLabel[] process()
	{
		String[] words = text.getWords();
		Map<String, Integer> wordCounts = new HashMap<String, Integer>();
		
		int biggestCommonSize = 0;
		String biggestCommon = "";
		for (String word : words) {
			word = word.toLowerCase();
			if (word.equalsIgnoreCase("niespodziewanie")) continue;
			if (word.equalsIgnoreCase("polichnowicza")) continue;
			if (word.equalsIgnoreCase("bijakowskiego")) continue;
			if (word.equalsIgnoreCase("polichnowicz")) continue;
			if (word.equalsIgnoreCase("odpowiedział")) continue;
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
		double phonemeTime = (3 * biggestCommonSize / 4) * text.getEstimatedTimePerCharacter();
		System.err.println("biggest: " + biggestCommon + " " + phonemeTime);
		
		ArrayList<ArrayList<Speech>> usedSpeeches = new ArrayList<ArrayList<Speech>>();
		int neigh = (int)Math.ceil(Math.sqrt(Math.sqrt(matched.length)));
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
		
		int totalStarts = 0;
		for (Speech speech : target) totalStarts += Math.max(0, speech.getEndDataIndex() - frames - speech.getStartDataIndex());
		
		int count = 0;
		int speechCount = 0;
		for (Speech speech : target) {
			for (int i = speech.getStartDataIndex(); i < speech.getEndDataIndex() - frames; ++i) {
				if (i % 100 == 0)
					System.err.println("searching for target " + (count += 100) + "/" + totalStarts + " (" + allData.get(i).getStartTime() + ")");
				int[] aux = new int[witnesses.size()];
				double diff = findBests(aux, witnesses, i, frames);
				double mult = 1;
//				for (int j = Math.abs(speechCount - target.size() / 2); j >= 0; --j) mult *= 1.1;
				diff = diff * mult;
				if (diff < smallestDiff) {
					smallestDiff = diff;
					bestMatching[0] = i;
					for (int j = 0; j < aux.length; ++j) bestMatching[j + 1] = aux[j];
				}
			}
			++speechCount;
		}
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
		int speechCount = 0;
		for (Speech speech : speeches) {
			if ((speech.getStartDataIndex() <= targetStart) && (targetStart <= speech.getEndDataIndex()))
				continue;
			for (int i = speech.getStartDataIndex(); i < speech.getEndDataIndex() - frames; ++i) {
				double diff = calculateDiff(targetStart, i, frames);
				double mult = 1;
//				for (int j = Math.abs(speechCount - speeches.size() / 2); j >= 0; --j) mult *= 1.1;
				diff *= mult;
				if (diff < bestDiff) {
					bestDiff = diff;
					bestIndex = i;
				}
			}
			++speechCount;
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
		double diff = 0;
		for (int i = 0; i < frames; ++i) {
			double[] spectrum1 = allData.get(firstStart + i).getSpectrum();
			double[] spectrum2 = allData.get(secondStart + i).getSpectrum();
			for (int k = 0; k < spectrum1.length; ++k) {
				double aux = (spectrum1[k] - spectrum2[k]) * (spectrum1[k] - spectrum2[k]);
//				if (aux < variances[k]) aux = variances[k];
				diff += (int)Math.round(aux / variances[k]);
			}
		}
		return diff;
	}

	private double[] calcVariances(double[] averages) {
		int spectrumSize = allData.get(0).getSpectrum().length;
		double[] out = new double[spectrumSize];
		
		for (Data data : allData) {
			for (int i = 0; i < spectrumSize; ++i)
				out[i] += (averages[i] - data.getSpectrum()[i]) * (averages[i] - data.getSpectrum()[i]);
		}
		for (int i = 0; i < spectrumSize; ++i) out[i] /= allData.size();
		for (int i = 0; i < spectrumSize; ++i) out[i] = Math.sqrt(out[i]);
		return out;
	}

	private double[] calcAverages() {
		int spectrumSize = allData.get(0).getSpectrum().length;
		double[] out = new double[spectrumSize];
		
		for (Data data : allData) {
			for (int i = 0; i < spectrumSize; ++i)
				out[i] += data.getSpectrum()[i];
		}
		for (int i = 0; i < spectrumSize; ++i) out[i] /= allData.size();
		return out;
	}
}
