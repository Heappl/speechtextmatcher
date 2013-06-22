package common;

public class AudioLabel {
	
	private String label;
	private double start;
	private double end;
	
	public AudioLabel(String label, double start, double end) {
		this.start = start;
		this.end = end;
		this.label = label;
	}
	
	public String getLabel() { return this.label; }
	public double getStart() { return this.start; }
	public double getEnd() { return this.end; }
}
