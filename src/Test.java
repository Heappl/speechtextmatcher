import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;
import edu.cmu.sphinx.frontend.endpoint.SpeechEndSignal;
import edu.cmu.sphinx.frontend.endpoint.SpeechStartSignal;


public class Test extends BaseDataProcessor 
{
	double lastTime = 0;
	double startSpeechTime = 0;
	int speechLabel = 0;
	int count = 0;
	BufferedWriter output = null;
	
	public Test() {
		try {
			File outputFile = new File("/home/bartek/workspace/speechtextmatcher/parts_labels.txt");
			OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(outputFile));
			output = new BufferedWriter(outputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Data getData() throws DataProcessingException {
		Data prevData = this.getPredecessor().getData();
		if (prevData == null) return prevData;
//		System.err.println(prevData.getClass().getSimpleName());
//		if (prevData.getClass() == FloatData.class)
//		{
//			FloatData floatData = FloatData.toFloatData(prevData);
//			System.err.println(count++ + ":");
//			for (float elem : floatData.getValues())
//				System.err.print(elem + " ");
//			System.err.println();
//		}
		if (prevData.getClass() == SpeechStartSignal.class)
			startSpeechTime = lastTime;
		if (prevData.getClass() == SpeechEndSignal.class)
			try {
				System.err.println(startSpeechTime + " " + lastTime + " label" + speechLabel);
				output.write(startSpeechTime + " " + lastTime + " label" + speechLabel++ + "\n");
				output.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		if (prevData.getClass() == DoubleData.class)
		{
			DoubleData doubleData = (DoubleData)prevData;
			double time = (double)doubleData.getFirstSampleNumber() / (double)16000;
//			double sum = 0;
//			for (int j = 0; j < doubleData.getValues().length; j++)
//				sum += doubleData.getValues()[j];
			lastTime = time;
//			System.err.println(count++ + " " + doubleData.getValues().length);
//			for (double elem : doubleData.getValues())
//				System.err.print(elem + " ");
//			System.err.println();
//			try {
//				output.write(time + "," + (int)(sum / (double)1600) + "\n");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
		return prevData;
	}
}

