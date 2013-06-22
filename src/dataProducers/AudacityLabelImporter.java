package dataProducers;
import java.util.ArrayList;

import common.AudioLabel;
import common.ITextProducer;



public class AudacityLabelImporter
{
	private AudioLabel[] labels = null;

	public AudacityLabelImporter(ITextProducer producer) {
		String[] lines = producer.getText().split("\n");
		ArrayList<AudioLabel> auxLabels = new ArrayList<AudioLabel>();
		for (String line : lines) {
			String tokens[] = line.split("\\s+", 3);
			if (tokens.length < 3) continue;
			double start = Double.valueOf(tokens[0].replace(",", "."));
			double end = Double.valueOf(tokens[1].replace(",", "."));
			auxLabels.add(new AudioLabel(tokens[2], start, end));
		}
		labels = auxLabels.toArray(new AudioLabel[0]);
	}
	
	public AudioLabel[] getLabels() { return labels; }
}
