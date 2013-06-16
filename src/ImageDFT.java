import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;


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
	
	BufferedImage process()
	{
		double[][] values = new double[N][N];
		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < N; ++j) {
				values[i][j] = calculatePoint(i, j);
			}
		}
		return convertValuesToImage(values);
	}

	private BufferedImage convertValuesToImage(double[][] values)
	{
		BufferedImage ret = new BufferedImage(N, N, BufferedImage.TYPE_INT_ARGB);
		int[][] imageData = new DataScaler().scale(values, 0, 256);
		for (int i = 0; i < N; ++i)
			for (int j = 0; j < N; ++j) {
				int value = imageData[i][j];
				int rgb = new Color(value, value, value).getRGB();
				ret.setRGB(i, j, rgb);
			}
		System.err.println("Image DFT calculated");
		return ret;
	}

	private double calculatePoint(int k, int l)
	{
		Complex ret = new Complex(0, 0);
		for (int i = 0; i < N; ++i) {
			for (int j = 0; j < N; ++j) {
				ret = ret.add(getPower(k, l, i, j, N).exp().multiply(data[i * N + j]));
			}
		}
		return ret.magnitude();
	}
	
	private Complex getPower(int k, int l, int i, int j, int N)
	{
		Complex ck = new Complex(k);
		Complex cl = new Complex(l);
		Complex ci = new Complex(i);
		Complex cj = new Complex(j);
		Complex ii = new Complex(0, -1);
		Complex ckci = ck.multiply(ci);
		Complex clcj = cl.multiply(cj);
		Complex ckci_plus_clcj_divided_by_N = ckci.add(clcj).divide(N);
		Complex minus_i2pi = ii.multiply(2 * Math.PI);
		return ckci_plus_clcj_divided_by_N.multiply(minus_i2pi);
	}
}
