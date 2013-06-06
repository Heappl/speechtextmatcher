import java.util.ArrayList;
import java.util.Iterator;


public class Speeches implements Iterable<Speech> {
	
	private ArrayList<Speech> speeches = new ArrayList<Speech>();
	private double totalTime = 0;  
	
	Speeches(ArrayList<Speech> speeches) {
		this.speeches = speeches;
        for (Speech elem : speeches) totalTime += elem.getTime();
	}
	Speeches() {}
	
	public double getTotalTime() { return this.totalTime; }
	public int size() { return this.speeches.size(); }
	public Speech get(int i) { return speeches.get(i); }
	
	public void add(Speech speech)
	{
		this.speeches.add(speech);
		this.totalTime += speech.getTime();
	}

	@Override
	public Iterator<Speech> iterator() {
		return speeches.iterator();
	}
}
