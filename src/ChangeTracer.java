import java.util.ArrayList;
import java.util.Collection;


public class ChangeTracer {

	DataSequence allData = null;
	Speeches speeches;
	int spectrumSize = 0;
	SpectrumDiffCalculator diffCalculator = null;
	double[] weights = null;
	
	public ChangeTracer(DataSequence allData, Speeches speeches)
	{
		this.allData = allData;
		this.speeches = speeches;
		this.spectrumSize = allData.get(0).getSpectrum().length;
		this.weights = new SpectrumWeights(allData).getWeights();
		for (int i = 0; i < spectrumSize; ++i) this.weights[i] = Math.pow(this.weights[i], 2);
		this.diffCalculator = new SpectrumDiffCalculator(this.weights);
	}

	AudioLabel[] process()
	{
		ArrayList<AudioLabel> auxLabels = new ArrayList<AudioLabel>();
		for (Speech speech : speeches) {
			auxLabels.addAll(processSpeech2(speech));
		}
		return auxLabels.toArray(new AudioLabel[0]);
	}
	
	private Collection<? extends AudioLabel> processSpeech2(Speech speech)
	{
		int startIndex = findIndex(speech.getStartTime(), 0, allData.size() - 1);
		int endIndex = findIndex(speech.getEndTime(), 0, allData.size() - 1);
		
		int size = endIndex - startIndex;
		double average = 0;
	    for (int i = startIndex; i < endIndex; ++i)
	    {
	    	double[] curr = allData.get(i).getSpectrum();
	    	for (int j = 0; j < spectrumSize; ++j) average += Math.log(curr[j]) * weights[j];
	    }
	    average /= size;
	    
	    double backgroundAverage = 0;
	    int count = 0;
	    for (int i = startIndex; i < endIndex; ++i)
	    {
	    	double[] curr = allData.get(i).getSpectrum();
	    	double sum = 0;
	    	for (int j = 0; j < spectrumSize; ++j)
	    		sum += Math.log(curr[j]) * weights[j];
	    	if (sum < average) {
	    		backgroundAverage += sum;
	    		count++;
	    	}
	    }
	    backgroundAverage /= count;
	    
	    boolean[] isSpeech = new boolean[size + 2];
	    for (int i = 0; i < size; ++i)
	    {
	    	double[] curr = allData.get(i + startIndex).getSpectrum();
	    	double sum = 0;
	    	for (int j = 0; j < spectrumSize; ++j) sum += Math.log(curr[j]) * weights[j];
	    	isSpeech[i + 1] = (sum > backgroundAverage);
	    }
        fillHoles(isSpeech, true, 2, 0);
        fillHoles(isSpeech, true, 2, 0);
        fillHoles(isSpeech, false, 2, 3);
        fillHoles(isSpeech, false, 2, 3);
//	    
	    int start = -1;
	    ArrayList<AudioLabel> out = new ArrayList<AudioLabel>();
	    count = 0;
	    for (int i = startIndex; i < endIndex; ++i)
	    {
	    	if ((start >= 0) && ((i == endIndex - 1) || !isSpeech[i - startIndex + 1]))
	    	{
	    		AudioLabel label = new AudioLabel(
	    				(count++) + "",
	    				allData.get(start).getStartTime(),
	    				allData.get(i).getEndTime());
	    		out.add(label);
	    		start = -1;
	    	}
	    	if ((start < 0) && isSpeech[i - startIndex + 1])
	    		start = i;
	    }
	    if (start >= 0) out.add(new AudioLabel(
	    		count + "",
	    		allData.get(start).getStartTime(),
	    		allData.get(endIndex - 1).getEndTime()));
		return out;
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
			double dist = diffCalculator.diffNorm2(totalDiffBackward, totalDiffForward);
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
	
	private void fillHoles(boolean[] data, boolean type, int gravity, int margin)
	{
		int countLeft = gravity;
		int countRight = gravity;
		boolean[] newData = new boolean[gravity + 1];
		int newDataInd = 0;
        for (int i = -gravity; i < data.length + 3 * gravity; ++i)
        {
        	int index = i - gravity;
        	
    		int dataReceding = index - gravity - 1;
    		if ((dataReceding >= 0) && (dataReceding < data.length)) {
    			data[dataReceding] = newData[newDataInd];
    		}
    		
    		if ((index >= margin) && (index < allData.size() - margin)
    			&& (countRight > 0) && (countLeft > 0)) {
    			newData[newDataInd] = type;
    		}
    		else if ((index >= 0) && (index < data.length)) {
    			newData[newDataInd] = data[index];
    		}
    		newDataInd = (newDataInd + 1) % newData.length;
        	
        	int recedingForLeft = index - gravity;
        	int incomingForLeft = index;
        	int recedingForRight = index + 1;
        	int incomingForRight = i + 1;
        	
        	if (isOfType(data, recedingForLeft, type)) countLeft--;
        	if (isOfType(data, incomingForLeft, type)) countLeft++;
        	if (isOfType(data, recedingForRight, type)) countRight--;
        	if (isOfType(data, incomingForRight, type)) countRight++;
        }
	}
	
	private boolean isOfType(boolean[] data, int index, boolean type)
	{
		return ((index < 0) || (index >= data.length) || (data[index] == type));
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
}
