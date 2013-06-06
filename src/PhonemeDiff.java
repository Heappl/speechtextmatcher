import java.util.Comparator;


public class PhonemeDiff implements Comparator<PhonemeDiff> {
	private double diff;
	private int first;
	private int second;
	private int width;
	
	public PhonemeDiff(double diff, int first, int second, int width) {
		this.diff = diff;
		this.first = first;
		this.second = second;
	}
	
	public int getFirstStart() { return first; }
	public int getFirstEnd() { return first + width; }
	public int getSecondStart() { return second; }
	public int getSecondEnd() { return second + width; }
	
	@Override
	public int compare(PhonemeDiff o1, PhonemeDiff o2) {
		if (o1.diff < o2.diff) return -1;
		if (o1.diff > o2.diff) return 1;
		if (o1.first < o2.first) return -1;
		if (o1.first > o2.first) return 1;
		if (o1.second < o2.second) return -1;
		if (o1.second > o2.second) return 1;
		return 0;
	}
}

