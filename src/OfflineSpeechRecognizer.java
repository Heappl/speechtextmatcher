import java.util.ArrayList;

class Data
{
	double[] spectrum;
	double startTime;
	double endTime;
	
	Data(double startTime, double endTime, double[] spectrum)
	{
		this.spectrum = spectrum;
		this.startTime = startTime;
		this.endTime = endTime;
	}
}

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
		this.spectrumSize = values.length;
	}
	
	public ArrayList<Speech> findSpeechParts()
	{
		double[] averages = new double[this.spectrumSize];
	    for (int i = 0; i < allData.size(); ++i)
	    {
	    	double[] curr = allData.get(i).spectrum;
	    	for (int j = 0; j < spectrumSize; ++j)
	    		averages[j] += curr[j];
	    }
	    for (int j = 0; j < spectrumSize; ++j) averages[j] /= allData.size();
	    double[] deviations = new double[spectrumSize];
	    for (int i = 0; i < allData.size(); ++i)
	    {
	    	double[] curr = allData.get(i).spectrum;
	    	for (int j = 0; j < spectrumSize; ++j)
	    		deviations[j] += Math.abs(averages[j] - curr[j]);
	    }
	    for (int j = 0; j < spectrumSize; ++j) deviations[j] /= allData.size();
	    
	    boolean[] isSpeech = new boolean[allData.size() + 2];
	    for (int i = 0; i < allData.size(); ++i)
	    {
	    	int count = 0;
	    	double[] curr = allData.get(i).spectrum;
	    	for (int j = 0; j < spectrumSize; ++j)
	    		if (Math.abs(curr[j] - averages[j]) > deviations[j])
	    			++count;
	    	isSpeech[i + 1] = (count > 0);
	    }
	    fillHoles(isSpeech, true, this.speechGravity, 1);
	    fillHoles(isSpeech, false, this.nonSpeechGravity, this.nonSpeechGravity);
	    
	    int start = -1;
	    ArrayList<Speech> out = new ArrayList<Speech>();
	    for (int i = 0; i < allData.size(); ++i)
	    {
	    	if ((start >= 0) && !isSpeech[i])
	    	{
	    		out.add(new Speech(allData.get(start).startTime, allData.get(i).startTime));
	    		start = -1;
	    	}
	    	if ((start < 0) && isSpeech[i])
	    		start = i;
	    }
	    return out;
	}
	
	private void fillHoles(boolean[] data, boolean type, int gravity, int edgeStart)
	{
		edgeStart = Math.max(1, edgeStart);
	    for (int i = edgeStart; i < allData.size() - edgeStart; ++i)
	    {
	    	if (data[i] == type) continue;
	    	boolean foundLeft = i < gravity;
	    	boolean foundRight = i > allData.size() - gravity;
	    	for (int j = 1; j < gravity; ++j) {
	    		if (foundLeft) break;
	    		foundLeft |= data[i - j] ^ type;
	    	}
	    	for (int j = 1; j < gravity; ++j) {
	    		if (foundRight) break;
	    		foundRight |= data[i + j] ^ type;
	    	}
	    	if (foundLeft && foundRight)
	    		data[i] = type;
	    }
	}
}
