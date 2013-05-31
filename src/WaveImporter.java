import java.io.File;
import java.util.ArrayList;

import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataBlocker;
import edu.cmu.sphinx.frontend.DataProcessor;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;
import edu.cmu.sphinx.frontend.FrontEnd;
import edu.cmu.sphinx.frontend.filter.Preemphasizer;
import edu.cmu.sphinx.frontend.transform.DiscreteFourierTransform;
import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.frontend.window.RaisedCosineWindower;
import edu.cmu.sphinx.util.props.ConfigurationManager;


public class WaveImporter
{
	private String waveFilePath = "";
	private ArrayList<IWaveObserver> observers = new ArrayList<IWaveObserver>();
	
	public WaveImporter(String waveFilePath)
	{
		this.waveFilePath = waveFilePath;
	}
	
	public void registerObserver(IWaveObserver observer)
	{
		this.observers.add(observer);
	}
	
	public void process()
	{
		ConfigurationManager cm = new ConfigurationManager(Main.class.getResource("config.xml"));
		FrontEnd frontend = (FrontEnd)cm.lookup("frontend");
		AudioFileDataSource audioSource = (AudioFileDataSource)cm.lookup("audioFileDataSource");
		File sourceFile = new File(this.waveFilePath);
		audioSource.setAudioFile(sourceFile, null);
		
		Data data = null;
		while ((data = frontend.getData()) != null) {
			for (IWaveObserver observer : this.observers) {
				if (data.getClass() == DoubleData.class)
				{
					DoubleData doubleData = (DoubleData)data;
					double startTime = doubleData.getFirstSampleNumber() / doubleData.getSampleRate();
					double endTime = startTime + doubleData.getValues().length / doubleData.getSampleRate();
					observer.process(startTime, endTime, doubleData.getValues());
				}
				if (data.getClass() == FloatData.class)
				{
					FloatData floatData = (FloatData)data;
					double startTime = floatData.getFirstSampleNumber() / floatData.getSampleRate();
					double endTime = startTime + floatData.getValues().length / floatData.getSampleRate();
					double[] doubleData = new double[floatData.getValues().length];
					for (int i = 0; i < doubleData.length; ++i)
						doubleData[i] = floatData.getValues()[i];
					observer.process(startTime, endTime, doubleData);
				}
			}
		}
	}
}
