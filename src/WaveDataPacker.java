
public class WaveDataPacker implements IWaveObserver {
	
	private IWaveObserver next;
	private double upto;
	private double neigh;

	public WaveDataPacker(IWaveObserver observer, double uptoOnSpectrum, double packCoefficient) {
		this.next = observer;
		this.upto = uptoOnSpectrum;
		this.neigh = packCoefficient;
	}
	
	@Override
	public void process(double startTime, double endTime, double[] values) {
		int upto = (int)Math.round(values.length * this.upto);
		int neigh = (int)Math.round(values.length * this.neigh);
		
		double[] nextData = new double[upto / neigh];
		for (int i = neigh / 2; i < upto - neigh / 2; i += neigh)
			for (int j = -neigh / 2; j <= neigh / 2; ++j)
				nextData[i / neigh] += values[i + j];
		this.next.process(startTime, endTime, nextData);
	}
}
