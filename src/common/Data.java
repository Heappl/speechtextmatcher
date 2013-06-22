package common;

public class Data {
	private double[] spectrum;
	private double startTime;
	private double endTime;
	
	public Data(double startTime, double endTime, double[] spectrum)
	{
		this.spectrum = spectrum;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public double[] getSpectrum() { return spectrum; }
	public double getStartTime() { return startTime; }
	public double getEndTime() { return endTime; }
}
