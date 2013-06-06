import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.cmu.sphinx.tools.audio.AudioData;


public class StartingPhonemeFinder {
	Speeches speeches;
	ArrayList<Data> allData;
	Text text;
	
	public StartingPhonemeFinder(Speeches speeches, ArrayList<Data> allData, Text text) {
		this.speeches = speeches;
		this.allData = allData;
		this.text = text;
	}
	
	AudioLabel[] process()
	{
		String[] words = text.getWords();
		ArrayList<Double> similarEstTimes = new ArrayList<Double>();

		double chars = words[0].length();
		int prefixSize = 3;
		String searchedFor = words[0].substring(0, prefixSize).toLowerCase();
		for (int i = 1; i < words.length; ++i) {
			double estTime = (chars * text.getEstimatedTimePerCharacter() * 0.9 + 0.1 * i * text.getEstimatedTimePerWord());

			boolean found = false;
			for (int j = 0; j < words[i].length() - prefixSize + 1; ++j)
				if (words[i].substring(j, j + prefixSize).toLowerCase().equals(searchedFor))
					found = true;
			
			if (found) similarEstTimes.add(estTime);
			chars += words[i].length();
		}
		System.err.println("found: " + similarEstTimes.size());
		
		double estTime = 3 * text.getEstimatedTimePerCharacter();
		double maxTimeDiff = Math.sqrt(speeches.getTotalTime());
		
		System.err.println("estTime: " + estTime + " maxTimeDiff:" + maxTimeDiff);
		
		double[] averages = calcAverages();
		double[] variances = calcVariances(averages);

		AudioLabel[] result = new AudioLabel[similarEstTimes.size() + 1];
		int targetStartIndex = speeches.get(0).getStartDataIndex();
		result[0] = findBestWithin(targetStartIndex, estTime, 0, estTime * 2, "orig", variances);
		for (int i = 0; i < similarEstTimes.size(); ++i) {
			double startTime = similarEstTimes.get(i) - maxTimeDiff;
			double endTime = similarEstTimes.get(i) + maxTimeDiff;
			if (i > 0)
				startTime = Math.max(startTime, (similarEstTimes.get(i) + similarEstTimes.get(i - 1)) / 2);
			if (i < similarEstTimes.size() - 1)
				endTime = Math.min(endTime, (similarEstTimes.get(i) + similarEstTimes.get(i + 1)) / 2);
			
			System.err.println("searching between " + startTime + " - " + endTime);
			result[i + 1] = findBestWithin(targetStartIndex, estTime, startTime, endTime, i + "", variances);
		}
		
		return result;
	}
	
	private double[] calcVariances(double[] averages) {
		int spectrumSize = allData.get(0).getSpectrum().length;
		double[] out = new double[spectrumSize];
		
		for (Data data : allData) {
			for (int i = 0; i < spectrumSize; ++i)
				out[i] += (averages[i] - data.getSpectrum()[i]) * (averages[i] - data.getSpectrum()[i]);
		}
		for (int i = 0; i < spectrumSize; ++i) out[i] /= allData.size();
//		for (int i = 0; i < spectrumSize; ++i) out[i] = Math.sqrt(out[i]);
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

	private AudioLabel findBestWithin(
		int targetStartIndex, double estTime, double startTime, double endTime, String label, double[] variances)
	{
		double frameTime = allData.get(1).getStartTime() - allData.get(0).getStartTime();
		int frames = (int)Math.ceil(estTime / frameTime);
		
		double smallestDiff = Double.MAX_VALUE;
		int bestIndex = 0;
		for (Speech speech : speeches)
		{
			if (speech.getEndTime() <= startTime) continue;
			if (speech.getStartTime() >= endTime) break;
			
			for (int i = speech.getStartDataIndex(); i < speech.getEndDataIndex() - frames; ++i)
			{
				double diff = 0;
				for (int j = i; j < i + frames; ++j)
				{
					double[] spectrum = allData.get(j).getSpectrum();
					double[] targetSpectrum = allData.get(targetStartIndex + j - i).getSpectrum();
					for (int k = 0; k < spectrum.length; ++k)
					{
						double aux = (spectrum[k] - targetSpectrum[k]) * (spectrum[k] - targetSpectrum[k]);
						if (aux < variances[k]) aux = variances[k];
						diff += aux;
					}
				}
				if (diff < smallestDiff) {
					smallestDiff = diff;
					bestIndex = i;
				}
			}
		}
		double bestStartTime = allData.get(bestIndex).getStartTime();
		double bestEndTime = allData.get(bestIndex + frames).getEndTime() + 0.5;
		return new AudioLabel(label, bestStartTime, bestEndTime);
	}
}
