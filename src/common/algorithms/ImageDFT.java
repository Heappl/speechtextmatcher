package common.algorithms;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

import dataTransforms.DataScaler;

import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.transform.DiscreteFourierTransform;


public class ImageDFT
{
	private int N;
	private int[] data;
	
	public ImageDFT(BufferedImage image)
	{
		this.N = Math.max(image.getHeight(), image.getWidth());
		Image auxImage = image.getScaledInstance(N, N, Image.SCALE_SMOOTH);
		this.data = new int[N * N];
		try {
			new PixelGrabber(auxImage, 0, 0, N - 1, N - 1, data, 0, N).grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	class SpectrumDataFeeder extends BaseDataProcessor
	{
		int[] data = null;
		int N = 0;
		int frame = 0;
		
		public SpectrumDataFeeder(int[] data)
		{
			this.data = data;
			this.N = (int)Math.round(Math.sqrt(data.length));
		}
		@Override
		public Data getData() throws DataProcessingException
		{
			if (frame >= N) return null;
			double[] nextChunk = new double[N];
			for (int i = 0; i < N; ++i)
				nextChunk[i] = new Color(data[frame * N + i]).getRed();
			++frame;
			return new DoubleData(nextChunk, 1, 1);
		}
	}
	
	BufferedImage process()
	{
		DiscreteFourierTransform dft = new DiscreteFourierTransform();
		dft.setPredecessor(new SpectrumDataFeeder(data));
		dft.initialize();
		
		double[][] values = new double[N][];
		for (int i = 0; i < N; ++i) {
			values[i] = ((DoubleData)dft.getData()).getValues();
		}
		return convertValuesToImage(values);
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
		System.err.println("Image DFT calculated");
		return ret;
	}

//	private double calculatePoint(int k, int l)
//	{
//		Complex ret = new Complex(0, 0);
//		for (int i = 0; i < N; ++i) {
//			for (int j = 0; j < N; ++j) {
//				ret = ret.add(getPower(k, l, i, j, N).exp().multiply(data[i * N + j]));
//			}
//		}
//		return ret.magnitude();
//	}
	
//	private Complex getPower(int k, int l, int i, int j, int N)
//	{
//		Complex ck = new Complex(k);
//		Complex cl = new Complex(l);
//		Complex ci = new Complex(i);
//		Complex cj = new Complex(j);
//		Complex ii = new Complex(0, -1);
//		Complex ckci = ck.multiply(ci);
//		Complex clcj = cl.multiply(cj);
//		Complex ckci_plus_clcj_divided_by_N = ckci.add(clcj).divide(N);
//		Complex minus_i2pi = ii.multiply(2 * Math.PI);
//		return ckci_plus_clcj_divided_by_N.multiply(minus_i2pi);
//	}
}
