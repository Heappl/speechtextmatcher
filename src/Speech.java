
public class Speech {
	private double startTime;
	private double endTime;

	public Speech(double startTime, double endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public double getStartTime() { return this.startTime; }
	public double getEndTime() { return this.endTime; }

	public double getTime() {
		return getEndTime() - getStartTime();
	}
}
