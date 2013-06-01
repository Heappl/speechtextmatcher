
public class Data {
	private double[] spectrum;
	private double startTime;
	private double endTime;
	
	Data(double startTime, double endTime, double[] spectrum)
	{
		this.spectrum = spectrum;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	double[] getSpectrum() { return spectrum; }
	double getStartTime() { return startTime; }
	double getEndTime() { return endTime; }
}
