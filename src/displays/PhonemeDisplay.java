package displays;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import common.DataSequence;

import dataTransforms.DataScaler;

class NextNotifier  implements ActionListener
{
	private Object objToNotify;
	
	public NextNotifier(Object objToNotify)
	{
		this.objToNotify = objToNotify;
	}
	public void actionPerformed(ActionEvent arg)
	{
		synchronized (objToNotify) {
			objToNotify.notify();
		}
	}
}

public class PhonemeDisplay extends JFrame {
	int width = 600;
	int height = 300;
	JLabel imageLabelUp = null;
	JLabel imageLabelDown = null;
	boolean last = true;
	
	public PhonemeDisplay()
	{
		init();
	}
	public PhonemeDisplay(int width, int height)
	{
		this.width = width;
		this.height = height;
		init();
	}
	private void init()
	{
		setTitle("PhonemeDisplay");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(width + 200, 2 * height + 100));
		createNextButton();
		setVisible(true);
	}
	private void createNextButton()
	{
		JButton nextButton = new JButton("next");
		nextButton.addActionListener(new NextNotifier(this));
		Container panel = getContentPane();
		panel.setLayout(new BorderLayout());
		panel.add(nextButton, BorderLayout.EAST);
	}

	public void draw(DataSequence dataSequence)
	{
		BufferedImage image = convertToImage(dataSequence);
		draw(image, !last);
		last = !last;
//		BufferedImage dfted = new DataSequenceDFT(dataSequence).process();
//		draw(dfted, true);
		try {
			synchronized (this) {
				this.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void draw(BufferedImage image, boolean isBottom)
	{
		if (isBottom) {
			if (imageLabelDown != null) remove(imageLabelDown);
		} else {
			if (imageLabelUp != null) remove(imageLabelUp);
		}
		Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		Container panel = getContentPane();
		panel.setLayout(new BorderLayout());
		
		JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
		panel.add(imageLabel, (isBottom) ? BorderLayout.SOUTH : BorderLayout.NORTH);
		imageLabel.setVisible(true);
		pack();
		
		if (isBottom) imageLabelDown = imageLabel;
		else imageLabelUp = imageLabel;
	}
	
	private BufferedImage convertToImage(DataSequence dataSequence)
	{
		if (dataSequence.isEmpty()) return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		
		int size = dataSequence.size();
		int spectrumSize = dataSequence.get(0).getSpectrum().length;
		
		BufferedImage ret = new BufferedImage(size, spectrumSize, BufferedImage.TYPE_INT_RGB);
		int[][] scaled = new DataScaler().scale(dataSequence.getRawData(), 0, 256);
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < spectrumSize; ++j) {
				int value = scaled[i][j];
				int blue = value;
				int green = value;
				int red = value;
//				if (value < maxSingleColor / 2) {
//					green = maxSingleColor - 1;
//					blue = maxSingleColor - 1 - value;
//					red = maxSingleColor - 1 - value;
//				} else if (value < maxSingleColor) {
//					value -= maxSingleColor / 2;
//					green = maxSingleColor - 1 - value;
//					blue = maxSingleColor / 2 + value;
//					red = maxSingleColor / 2;
//				} else if (value < 3 * maxSingleColor / 2) {
//					value -= maxSingleColor;
//					green = maxSingleColor / 2;
//					blue = maxSingleColor - 1 - value;
//					red = maxSingleColor / 2 + value;
//				} else if (value < 2 * maxSingleColor) {
//					value -= 3 * maxSingleColor / 2;
//					green = maxSingleColor / 2 - value;
//					blue = maxSingleColor / 2 - value;
//					red = maxSingleColor - 1;
//				}
				int rgb = new Color(red, green, blue).getRGB();
				ret.setRGB(i, j, rgb);
			}
		}
		return ret;
	}
}
