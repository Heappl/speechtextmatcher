
public class Speech {
	private double startTime;
	private double endTime;
	private int startDataIndex;
	private int endDataIndex;

	public Speech(double startTime, double endTime, int startIndex, int endIndex) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.startDataIndex = startIndex;
		this.endDataIndex = endIndex;
	}
	
	public double getStartTime() { return this.startTime; }
	public double getEndTime() { return this.endTime; }
	public double getStartDataIndex() { return this.startDataIndex; }
	public double getEndDataIndex() { return this.endDataIndex; }

	public double getTime() { return getEndTime() - getStartTime(); }
}

