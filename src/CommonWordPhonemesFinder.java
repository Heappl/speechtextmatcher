import java.util.ArrayList;
import java.util.Comparator;
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
	
	public CommonWordPhonemesFinder(Speeches speeches, ArrayList<Data> allData, Text text) {
		this.speeches = speeches;
		this.allData = allData;
		this.text = text;
		this.frameTime = allData.get(1).getStartTime() - allData.get(0).getStartTime();
		this.averages = calcAverages();
		this.variances = calcVariances(averages);
	}
	
	AudioLabel[] process()
	{
		String[] words = text.getWords();
		Map<String, ArrayList<Double>> phonemesEstTimes = new TreeMap<String, ArrayList<Double>>();
		int minSize = 5;
		double chars = 0;
		Set<String> biggestPhonemes = new HashSet<String>();
		for (String word : words) {
			word = word.toLowerCase();
			if (word.length() < minSize) continue;
			ArrayList<Double> times = new ArrayList<Double>();
			if (phonemesEstTimes.containsKey(word)) times = phonemesEstTimes.get(word);
			double estTime = chars * text.getEstimatedTimePerCharacter();
			times.add(estTime);
			phonemesEstTimes.put(word, times);
			if (times.size() > 1) {
				System.err.println(word);
				biggestPhonemes.add(word);
			}
			chars += word.length();
		}
		System.err.println("found: " + biggestPhonemes.size());
		for (String str : biggestPhonemes) System.err.print(str + " ");
		System.err.println();
		
		double maxTimeDiff = Math.sqrt(speeches.getTotalTime());
		
		ArrayList<AudioLabel> labels = new ArrayList<AudioLabel>();
		for (String str : biggestPhonemes) {
			ArrayList<Double> estTimes = phonemesEstTimes.get(str);
			System.err.println("words " + estTimes.size() + str);
			for (int i = 1; i < estTimes.size(); ++i)
			{
				AudioLabel[] aux = findBestPairWithin(
						estTimes.get(i - 1) - maxTimeDiff,
						estTimes.get(i - 1) + maxTimeDiff,
						estTimes.get(i) - maxTimeDiff,
						estTimes.get(i) + maxTimeDiff,
						str.length() * text.getEstimatedTimePerCharacter(),
						str);
				for (AudioLabel label : aux) labels.add(label);
			}
			break;
		}
		return labels.toArray(new AudioLabel[0]);
	}
	
	private AudioLabel[] findBestPairWithin(
		double start1, double end1, double start2, double end2, double phonemeTime, String str)
	{
		System.err.println("finding " + str + " (" + start1 + ", " + end1 + ") (" + start2 + ", " + end2 + ")");
		int frames = (int)Math.ceil(phonemeTime / frameTime); 
		ArrayList<Speech> firstData = new ArrayList<Speech>();
		ArrayList<Speech> secondData = new ArrayList<Speech>();
		for (Speech speech : speeches)
		{
			if ((speech.getEndTime() > start1) && (speech.getStartTime() < end1))
				firstData.add(speech);
			else if ((speech.getEndTime() > start2) && (speech.getStartTime() < end2))
				secondData.add(speech);
		}
		System.err.println("speech sizes: " + firstData.size() + " " + secondData.size());
		PhonemeDiff closestPhonems = null;
		for (Speech speech1 : firstData) {
			for (Speech speech2 : secondData) {
				if (speech1 == speech2) continue;
				PhonemeDiff diff = findBest(speech1, speech2, frames);
				if ((closestPhonems == null) || (diff.compare(diff, closestPhonems) < 0))
					closestPhonems = diff;
			}
		}
		AudioLabel[] result = new AudioLabel[2];
		result[0] = new AudioLabel(
				str,
				allData.get(closestPhonems.getFirstStart()).getStartTime(),
				allData.get(closestPhonems.getFirstEnd()).getEndTime()
				);
		result[1] = new AudioLabel(
				str,
				allData.get(closestPhonems.getSecondStart()).getStartTime(),
				allData.get(closestPhonems.getSecondEnd()).getEndTime()
				);
		return result;
	}

	private PhonemeDiff findBest(Speech speech1, Speech speech2, int frames)
	{
		System.err.println("find best in speeches: " +
			(speech1.getEndDataIndex() - speech1.getStartDataIndex() - frames) + " " +
			(speech2.getEndDataIndex() - speech2.getStartDataIndex() - frames) + " " +
			frames);
		double smallestDiff = Double.MAX_VALUE;
		int bestFirstIndex = 0;
		int bestSecondIndex = 0;
		for (int i = speech1.getStartDataIndex(); i < speech1.getEndDataIndex() - frames; ++i) {
			if ((i - speech1.getStartDataIndex()) % 100 == 0)
				System.err.println((i - speech1.getStartDataIndex()) + "/" + (speech1.getEndDataIndex() - frames - speech1.getStartDataIndex()));
			for (int j = speech2.getStartDataIndex(); j < speech2.getEndDataIndex() - frames; ++j) {
				double diff = calculateDiff(i, j, frames);
				if (diff < smallestDiff) {
					smallestDiff = diff;
					bestFirstIndex = i;
					bestSecondIndex = j;
				}
			}
		}
		return new PhonemeDiff(smallestDiff, bestFirstIndex, bestSecondIndex, frames);
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
