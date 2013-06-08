import java.util.ArrayList;


public class ChangeTracer {

	ArrayList<Data> allData = null;
	Speeches speeches;
	int spectrumSize = 0;
	double[] weights;
	
	public ChangeTracer(ArrayList<Data> allData, Speeches speeches)
	{
		this.allData = allData;
		this.speeches = speeches;
		spectrumSize = allData.get(0).getSpectrum().length;
		weights = new SpectrumWeights(allData).getWeights();
	}

	AudioLabel[] process()
	{
		ArrayList<AudioLabel> auxLabels = new ArrayList<AudioLabel>();
		for (Speech speech : speeches) {
			auxLabels.addAll(processSpeech(speech));
		}
		return auxLabels.toArray(new AudioLabel[0]);
	}
	
	ArrayList<AudioLabel> processSpeech(Speech speech)
	{
		int spectrumSize = allData.get(0).getSpectrum().length;
		int frames = 10;
		
		int startIndex = findIndex(speech.getStartTime(), 0, allData.size() - 1);
		int endIndex = findIndex(speech.getEndTime(), 0, allData.size() - 1);
		
		if (endIndex - startIndex - frames <= 0) return new ArrayList<AudioLabel>();
		
		double[] changes = new double[endIndex - startIndex - frames];
		for (int i = startIndex + frames; i < endIndex - frames; i++)
		{
			double[] totalDiffBackward = new double[spectrumSize];
			for (int j = i - frames + 1; j < i; ++j) {
				double[] diff = calculateDiff(allData.get(j - 1).getSpectrum(), allData.get(j).getSpectrum());
				totalDiffBackward = sumDiff(totalDiffBackward, diff);
			}
			double[] totalDiffForward = new double[spectrumSize];
			for (int j = i + 1; j < i + frames; ++j) {
				double[] diff = calculateDiff(allData.get(j - 1).getSpectrum(), allData.get(j).getSpectrum());
				totalDiffForward = sumDiff(totalDiffForward, diff);
			}
			double dist = calculateDist(totalDiffBackward, totalDiffForward);
			changes[i - frames - startIndex] = dist;
		}
		
		double average = 0;
		for (int i = 0; i < changes.length; ++i) {
			average += changes[i];
		}
		average /= changes.length;
		
		ArrayList<AudioLabel> auxLabels = new ArrayList<AudioLabel>();
		double start = -1;
		double end = -1;
		int count = 0;
		for (int i = 0; i < changes.length; ++i) {
			if (changes[i] < average) {
				if (start < 0) start = allData.get(i + startIndex).getStartTime();
				end = allData.get(i + 1 + startIndex).getStartTime();
			} else if (start > 0) {
				auxLabels.add(new AudioLabel("" + count, start, end));
				++count;
				start = -1;
			}
		}
		return auxLabels;
	}

	private int findIndex(double time, int bottom, int top)
	{
		if (bottom == top) return bottom;
		int between = (top + bottom) / 2;
		double auxTime = (allData.get(between).getStartTime() + allData.get(between + 1).getEndTime()) / 2;
		if (time < auxTime) return findIndex(time, bottom, between);
		else return findIndex(time, between + 1, top);
	}
	
	private double[] calculateDiff(double[] first, double[] second)
	{
		double[] out = new double[first.length];
		for (int i = 0; i < first.length; ++i) {
			out[i] = first[i] - second[i];
		}
		return out;
	}
	
	private double[] sumDiff(double[] first, double[] second)
	{
		double[] out = new double[first.length];
		for (int i = 0; i < first.length; ++i) {
			out[i] = first[i] + second[i];
		}
		return out;
	}

	private double calculateDist(double[] first, double[] second)
	{
		double diff = 0;
		for (int k = 0; k < first.length; ++k) {
//			double aux = Math.abs(first[k] - second[k]);// * (first[k] - second[k]);
			double aux = (first[k] - second[k]) * (first[k] - second[k]) * weights[k];
//			diff = Math.max(aux, diff);
			diff += aux;
		}
		return diff;
	}
}
