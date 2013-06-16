import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.ArrayList;

import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.databranch.DataBufferProcessor;
import edu.cmu.sphinx.frontend.transform.DiscreteFourierTransform;


public class DataSequenceDFT
{
	private int size;
	private int spectrumSize;
	private double[] data;
	
	public DataSequenceDFT(DataSequence sequence)
	{
		this.size = sequence.size();
		this.spectrumSize = sequence.get(0).getSpectrum().length;
		this.data = new double[size * spectrumSize];
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < spectrumSize; ++j) {
				data[i * spectrumSize + j] = sequence.get(i).getSpectrum()[j];
			}
		}
	}
	
	class SpectrumDataFeeder extends BaseDataProcessor
	{
		double[] data = null;
		int frame = 0;
		int size = 0;
		int spectrumSize = 0;
		
		public SpectrumDataFeeder(double[] data, int size)
		{
			this.data = data;
			this.size = size;
			this.spectrumSize = data.length / size;
		}
		@Override
		public Data getData() throws DataProcessingException
		{
			if (frame >= size) return null;
			double[] nextChunk = new double[spectrumSize];
			for (int i = 0; i < spectrumSize; ++i)
				nextChunk[i] = data[frame * spectrumSize + i];
			++frame;
			return new DoubleData(nextChunk, 1, 1);
		}
	}
	
	class WindowSingleFreqFeeder extends BaseDataProcessor
	{
		ArrayList<DoubleData> buffer = new ArrayList<DoubleData>();
		int frame = 0;
		
		@Override
		public Data getData() throws DataProcessingException
		{
			Data data;
			while ((data = getPredecessor().getData()) != null) {
				buffer.add((DoubleData)data);
			}
			int spectrumSize = buffer.get(0).getValues().length;
			if (frame >= spectrumSize) return null;
			
			double[] nextChunk = new double[buffer.size()];
			for (int i = 0; i < buffer.size(); ++i)
				nextChunk[i] = buffer.get(i).getValues()[frame];
			++frame;
			return new DoubleData(nextChunk, 1, 1);
		}
		
	}
	
	BufferedImage process()
	{
		DiscreteFourierTransform dftSpectrum = new DiscreteFourierTransform();
		WindowSingleFreqFeeder nextFeeder = new WindowSingleFreqFeeder();
		DiscreteFourierTransform dftWindow = new DiscreteFourierTransform();
		
		dftSpectrum.setPredecessor(new SpectrumDataFeeder(data, size));
		nextFeeder.setPredecessor(dftSpectrum);
		dftWindow.setPredecessor(nextFeeder);
		
		dftSpectrum.initialize();
		dftWindow.initialize();
		
		ArrayList<double[]> values = new ArrayList<double[]>();
		Data data;
		while ((data = dftWindow.getData()) != null) {
			double[] curr = ((DoubleData)data).getValues();
			for (int i = 0; i < curr.length; ++i) curr[i] = Math.log(curr[i]);
			values.add(curr);
		}
		return convertValuesToImage(values.toArray(new double[0][]));
	}

	private BufferedImage convertValuesToImage(double[][] values)
	{
		BufferedImage ret = new BufferedImage(values.length, values[0].length, BufferedImage.TYPE_INT_ARGB);
		int[][] imageData = new DataScaler().scale(values, 0, 256);
		for (int i = 0; i < values.length; ++i) {
			for (int j = 0; j < values[0].length; ++j) {
				int value = imageData[i][j];
				int rgb = new Color(value, value, value).getRGB();
				ret.setRGB(i, j, rgb);
			}
		}
		return ret;
	}
}
