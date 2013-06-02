import java.util.ArrayList;


public class Speeches {
	
	private ArrayList<Speech> speeches = null;
	private double totalTime = 0;  
	
	Speeches(ArrayList<Speech> speeches) {
		this.speeches = speeches;
        for (Speech elem : speeches) totalTime += elem.getTime();
	}
	
	public double getTotalTime() { return this.totalTime; }
	public int size() { return this.speeches.size(); }
	public Speech get(int i) { return speeches.get(i); }
}
