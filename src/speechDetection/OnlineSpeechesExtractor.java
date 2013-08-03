package speechDetection;
import common.Data;
import common.DataSequence;
import common.Speech;
import common.Speeches;
import dataProducers.ISpeechObserver;
import dataProducers.IWaveObserver;


public class OnlineSpeechesExtractor implements IWaveObserver, ISpeechObserver
{
	
	private DataSequence allData = new DataSequence();
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
	
	public DataSequence getAllData()
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
