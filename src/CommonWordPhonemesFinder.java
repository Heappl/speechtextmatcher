import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.cmu.sphinx.tools.audio.AudioData;


public class CommonWordPhonemesFinder {
	Speeches speeches;
	ArrayList<Data> allData;
	Text text;
	double frameTime = 0;
	double[] variances = null;
	double[] averages = null;
	AudioLabel[] matched = null;
	
	public CommonWordPhonemesFinder(Speeches speeches, ArrayList<Data> allData, Text text, AudioLabel[] matched) {
		this.speeches = speeches;
		this.allData = mergeData(allData, 10);
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
		System.err.println("biggest: " + biggestCommon);
		
		ArrayList<ArrayList<Speech>> usedSpeeches = new ArrayList<ArrayList<Speech>>();
		int neigh = (int)Math.ceil(Math.sqrt(Math.sqrt(speeches.size())));
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
				System.err.println(surroundingSpeeches.size());
			}
		}
		double phonemeTime = biggestCommon.length() * text.getEstimatedTimePerCharacter();
		int frames = (int)Math.ceil(phonemeTime / frameTime);
		
		double[][] diffs = new double[usedSpeeches.size() * 2 * neigh][usedSpeeches.size() * 2 * neigh];
		System.err.println("neigh: " + neigh + " diffs " + diffs.length);
		for (int i = 0; i < diffs.length; ++i)
			for (int j = 0; j < diffs.length; ++j)
				diffs[i][j] = Double.MAX_VALUE;
		
		for (int i = 0; i < usedSpeeches.size(); ++i) {
			System.err.println("A: " + i + "/" + usedSpeeches.size());
			for (int j = i + 1; j < usedSpeeches.size(); ++j) {
				System.err.println("B: " + j + "/" + usedSpeeches.size());
				for (int k = 0; k < usedSpeeches.get(i).size(); ++k) {
					System.err.println("C: " + k + "/" + usedSpeeches.get(i).size());
					for (int l = 0; l < usedSpeeches.get(j).size(); ++l) {
						System.err.println("D: " + l + "/" + usedSpeeches.get(j).size());
						double bestDiff = findBest(usedSpeeches.get(i).get(k), usedSpeeches.get(j).get(l), frames);
						int index1 = i * 2 * neigh + k;
						int index2 = j * 2 * neigh + l; 
						diffs[index1][index2] = bestDiff;
						diffs[index2][index1] = bestDiff;
					}
				}
			}
		}
		
		int[] indexes = new int[usedSpeeches.size()];
		int[] bestIndexes = new int[usedSpeeches.size()];
		double bestScore = Double.MAX_VALUE;
		while (true) {
			double score = 0;
			for (int i = 0; i < indexes.length; ++i) {
				for (int j = i + 1; j < indexes.length; ++j) {
					score += diffs[i * 2 * neigh + indexes[i]][j * 2 * neigh + indexes[j]];
				}
			}
			if (score < bestScore) {
				bestScore = score;
				for (int i = 0; i < indexes.length; ++i) bestIndexes[i] = indexes[i];
			}
			for (int i = 0; i < indexes.length; ++i) System.err.print(indexes[i] + " ");
			System.err.println();
			if (next(indexes, 2 * neigh)) break;
		}
		
		AudioLabel[] res = new AudioLabel[usedSpeeches.size()];
		for (int i = 0; i < indexes.length; ++i) {
			ArrayList<Speech> auxSpeeches = usedSpeeches.get(i);
			Speech speech = auxSpeeches.get(bestIndexes[i]);
			res[i] = new AudioLabel(biggestCommon, speech.getStartTime(), speech.getEndTime());
		}
		return res;
	}
	
	private boolean next(int[] indexes, int top)
	{
		for (int j = indexes.length - 1; j >= 0; --j) {
			if (indexes[j] == top - 1) continue;
			indexes[j]++;
			for (int k = j + 1; k < indexes.length; ++k) indexes[k] = 0;
			return false;
		}
		return true;
	}

	private int findIndex(double time, int bottom, int top)
	{
		if (bottom == top) return bottom;
		int between = (top + bottom) / 2;
		double auxTime = (allData.get(between).getStartTime() + allData.get(between + 1).getEndTime()) / 2;
		if (time < auxTime) return findIndex(time, bottom, between);
		else return findIndex(time, between + 1, top);
	}

//	private PhonemeDiff findBest(Speech speech1, Speech speech2, int frames)
	private double findBest(Speech speech1, Speech speech2, int frames)
	{
		System.err.println("find best in speeches: " +
			(speech1.getEndDataIndex() - speech1.getStartDataIndex() - frames) + " " +
			(speech2.getEndDataIndex() - speech2.getStartDataIndex() - frames) + " " +
			frames);
		double smallestDiff = Double.MAX_VALUE;
		int bestFirstIndex = 0;
		int bestSecondIndex = 0;
		for (int i = speech1.getStartDataIndex(); i < speech1.getEndDataIndex() - frames; i += 2) {
			if ((i - speech1.getStartDataIndex()) % 100 == 0)
				System.err.println((i - speech1.getStartDataIndex()) + "/" + (speech1.getEndDataIndex() - frames - speech1.getStartDataIndex()));
			for (int j = speech2.getStartDataIndex(); j < speech2.getEndDataIndex() - frames; j += 2) {
				double diff = calculateDiff(i, j, frames);
				if (diff < smallestDiff) {
					smallestDiff = diff;
					bestFirstIndex = i;
					bestSecondIndex = j;
				}
			}
		}
		return smallestDiff;
//		return new PhonemeDiff(smallestDiff, bestFirstIndex, bestSecondIndex, frames);
	}

	private double calculateDiff(int firstStart, int secondStart, int frames)
	{
		double diff = 0;
		for (int i = firstStart; i < firstStart + frames; ++i) {
			for (int j = secondStart; j < secondStart + frames; ++j) {
				double[] spectrum1 = allData.get(i).getSpectrum();
				double[] spectrum2 = allData.get(j).getSpectrum();
				for (int k = 0; k < spectrum1.length; ++k) {
					double aux = (spectrum1[k] - spectrum2[k]) * (spectrum1[k] - spectrum2[k]);
					if (aux < variances[k]) aux = variances[k];
					diff += aux;
				}
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
