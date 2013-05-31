import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

class WaveFormPanel extends JPanel {
	
	int width;
	int height;
	double barScale = Double.MAX_VALUE;
	Image image = null;

	public WaveFormPanel(int width, int height) {
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		this.width = width;
		this.height = height;
	}
	
	public void drawData(long[] data)
	{
		Graphics g = getGraphics();
		int barWidth = width / data.length;
		
		long maxValue = data[0];
		long minValue = data[0];
		for (long elem : data)
		{
			if (elem > maxValue) maxValue = elem;
			else if (elem < minValue) minValue = elem;
		}
		double prevBarScale = barScale;
		barScale = Math.min(barScale, this.height / 2.0 / Math.max(Math.abs(maxValue), Math.abs(minValue)));
		if (prevBarScale != barScale) System.err.println("new scale " + barScale);
		
		for (int i = 0; i < data.length; ++i)
		{
			int x = barWidth * i;
			int height = (int)(data[i] * barScale);
			g.setColor(Color.WHITE);
			g.fillRect(x, 0, barWidth, this.height);
			g.setColor(Color.BLUE);
			g.fillRect(x, this.height / 2 - height - 1, barWidth, height + 1);
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}
}

public class WaveDisplay extends JFrame implements IWaveObserver {
	int width = 1600;
	int height = 600;
	WaveFormPanel wavePanel = new WaveFormPanel(width, height);
	
	public WaveDisplay() {
		setTitle("WaveDisplay");
		setSize(width, height);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		add(wavePanel);
		setVisible(true);
	}

	@Override
	public void process(double startTime, double endTime, double[] values) {
		long[] data = new long[values.length];
//		System.err.println(values.length);
		for (int i = 0; i < values.length; ++i)
			data[i] = Math.round(Math.log10(values[i]) * 100000);
		wavePanel.drawData(data);
	}
}
