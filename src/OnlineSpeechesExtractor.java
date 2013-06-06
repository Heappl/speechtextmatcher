import java.util.ArrayList;


public class OnlineSpeechesExtractor implements IWaveObserver, ISpeechObserver
{
	
	private ArrayList<Data> allData = new ArrayList<Data>();
	private Speeches speeches = new Speeches();
	
	private double speechStartTime;
	private double speechEndTime;
	private int speechStartIndex;
	private int speechEndIndex;
	private int speechStarted = 0;
	
	@Override
	public void process(double startTime, double endTime, double[] values)
	{
		if (speechStarted == 1) {
			speechStartTime = startTime;
			speechStartIndex = allData.size();
			speechStarted = 2;
		} else if (speechStarted == 2) {
			speechEndTime = endTime;
			speechEndIndex = allData.size();
		}
		
		allData.add(new Data(startTime, endTime, values));
	}
	
	public ArrayList<Data> getAllData()
	{
		return allData;
	}

	@Override
	public void speechStarted()
	{
		speechStarted = 1;
	}

	@Override
	public void speechEnded()
	{
		speeches.add(new Speech(speechStartTime, speechEndTime, speechStartIndex, speechEndIndex));
		speechStarted = 0;
	}

	public Speeches getSpeeches() {
		return speeches;
	}
}
