import java.util.ArrayList;


public class PhonemeDestructurer {
	
	Speeches speeches;
	ArrayList<Data> allData;
	int frames = 0;

	public PhonemeDestructurer(Speeches speeches, Text text, ArrayList<Data> allData)
	{
		this.allData = allData;
		this.speeches = speeches;
		double characterTime = 0.5 * text.getEstimatedTimePerCharacter();
		double frameTime = allData.get(1).getStartTime() - allData.get(0).getStartTime();
		this.frames = (int)Math.ceil(characterTime / frameTime);
	}

	public AudioLabel[] process(AudioLabel[] prepared)
	{
		System.err.println(frames);
		
		int spectrumSize = allData.get(0).getSpectrum().length;
		int totalPointsSize = (allData.size() - frames) * 2 / frames;
		double[][] points = new double[totalPointsSize][spectrumSize];
		for (int i = 0; i < allData.size() - 2 * frames; i += frames / 2) {
			int outer = i * 2 / frames;
			for (int j = i; j < i + frames; ++j) {
				for (int k = 0; k < spectrumSize; ++k) {
					points[outer][k] += allData.get(j).getSpectrum()[k];
				}
			}
			for (int k = 0; k < spectrumSize; ++k) points[outer][k] /= frames;
		}
		
		KMeans kmeans = new KMeans(points, 100);
		
		for (AudioLabel label : prepared) {
			System.err.println(label.getLabel() + ":");
			int start = findIndex(label.getStart(), 0, allData.size());
			int end = findIndex(label.getEnd(), 0, allData.size());
			System.err.println(label.getStart() + " " + allData.get(start).getStartTime());
			System.err.println(label.getEnd() + " " + allData.get(end).getStartTime());
			
			for (int i = start; i <= end; i += frames / 2) {
				int index = i * 2 / frames;
				System.err.print(kmeans.getClassification(index) + " ");
			}
			System.err.println();
		}
		
		return new AudioLabel[0];
	}

	private int findIndex(double time, int bottom, int top)
	{
		if (bottom == top) return bottom;
		int between = (top + bottom) / 2;
		double auxTime = (allData.get(between).getStartTime() + allData.get(between + 1).getEndTime()) / 2;
		if (time < auxTime) return findIndex(time, bottom, between);
		else return findIndex(time, between + 1, top);
	}

}
