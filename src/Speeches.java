import java.util.ArrayList;
import java.util.Iterator;


public class Speeches implements Iterable<Speech> {
	
	private ArrayList<Speech> speeches = null;
	private double totalTime = 0;  
	
	Speeches(ArrayList<Speech> speeches) {
		this.speeches = speeches;
        for (Speech elem : speeches) totalTime += elem.getTime();
	}
	
	public double getTotalTime() { return this.totalTime; }
	public int size() { return this.speeches.size(); }
	public Speech get(int i) { return speeches.get(i); }

	@Override
	public Iterator<Speech> iterator() {
		return speeches.iterator();
	}
}
