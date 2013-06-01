import java.util.ArrayList;

public class OfflineSpeechRecognizer implements IWaveObserver {

	private ArrayList<Data> allData = new ArrayList<Data>();
	private int spectrumSize = 0;
	private int speechGravity;
	private int nonSpeechGravity;
	
	public OfflineSpeechRecognizer(int speechGravity, int nonSpeechGravity) {
		this.speechGravity = speechGravity;
		this.nonSpeechGravity = nonSpeechGravity;
	}
	
	@Override
	public void process(double startTime, double endTime, double[] values) {
		allData.add(new Data(startTime, endTime, values));
		this.spectrumSize = Math.max(values.length, this.spectrumSize);
	}
	
	public ArrayList<Data> getAllData()
	{
		return allData;
	}
	
	public ArrayList<Speech> findSpeechParts()
	{
//		double[] averages = new double[this.spectrumSize];
//	    for (int i = 0; i < allData.size(); ++i)
//	    {
//	    	double[] curr = allData.get(i).getSpectrum();
//	    	for (int j = 0; j < spectrumSize; ++j)
//	    		averages[j] += curr[j];
//	    }
//	    for (int j = 0; j < spectrumSize; ++j) averages[j] /= allData.size();
//	    double[] deviations = new double[spectrumSize];
//	    for (int i = 0; i < allData.size(); ++i)
//	    {
//	    	double[] curr = allData.get(i).getSpectrum();
//	    	for (int j = 0; j < spectrumSize; ++j)
//	    		deviations[j] += Math.abs(averages[j] - curr[j]);
//	    }
//	    for (int j = 0; j < spectrumSize; ++j) deviations[j] /= allData.size();
//	    
//	    boolean[] isSpeech = new boolean[allData.size() + 2];
//	    for (int i = 0; i < allData.size(); ++i)
//	    {
//	    	int count = 0;
//	    	double[] curr = allData.get(i).getSpectrum();
//	    	for (int j = 0; j < spectrumSize; ++j)
//	    		if ((curr[j] - averages[j]) > deviations[j])
//	    			++count;
//	    	isSpeech[i + 1] = (count > 0);
//	    }
		
		double average = 0;
	    for (int i = 0; i < allData.size(); ++i)
	    {
	    	double[] curr = allData.get(i).getSpectrum();
	    	for (int j = 0; j < spectrumSize; ++j)
	    		average += Math.log(curr[j]);
	    }
	    average /= allData.size();
	    
	    double backgroundAverage = 0;
	    int count = 0;
	    for (int i = 0; i < allData.size(); ++i)
	    {
	    	double[] curr = allData.get(i).getSpectrum();
	    	double sum = 0;
	    	for (int j = 0; j < spectrumSize; ++j)
	    		sum += Math.log(curr[j]);
	    	if (sum < average) {
	    		backgroundAverage += sum;
	    		count++;
	    	}
	    }
	    backgroundAverage /= count;
	    
	    boolean[] isSpeech = new boolean[allData.size() + 2];
	    for (int i = 0; i < allData.size(); ++i)
	    {
	    	double[] curr = allData.get(i).getSpectrum();
	    	double sum = 0;
	    	for (int j = 0; j < spectrumSize; ++j) sum += Math.log(curr[j]);
	    	isSpeech[i + 1] = (sum > backgroundAverage);
	    }
	    
        fillHoles(isSpeech, true, this.speechGravity, 0);
        fillHoles(isSpeech, true, this.speechGravity, 0);
        fillHoles(isSpeech, false, this.nonSpeechGravity, 2 * this.nonSpeechGravity);
        fillHoles(isSpeech, false, this.nonSpeechGravity, 2 * this.nonSpeechGravity);
	    
	    
	    int start = -1;
	    ArrayList<Speech> out = new ArrayList<Speech>();
	    for (int i = 0; i < allData.size(); ++i)
	    {
	    	if ((start >= 0) && ((i == allData.size() - 1) || !isSpeech[i]))
	    	{
	    		Speech speech = new Speech(
	    				allData.get(start).getStartTime(),
	    				allData.get(i).getStartTime());
	    		out.add(speech);
	    		start = -1;
	    	}
	    	if ((start < 0) && isSpeech[i])
	    		start = i;
	    }
	    return out;
	}
	
	private boolean isOfType(boolean[] data, int index, boolean type)
	{
		return ((index < 0) || (index >= data.length) || (data[index] == type));
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
}
