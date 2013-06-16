import java.util.ArrayList;
import java.util.HashMap;


public class PhonemeLearner
{
	private AudioLabel[] prepared;
	DataSequence allData;
	
	public PhonemeLearner(AudioLabel[] prepared, DataSequence allData)
	{
		this.prepared = prepared;
		this.allData = allData;
	}
	
	public void process()
	{
        HashMap<String, ArrayList<AudioLabel>> starts = new HashMap<String, ArrayList<AudioLabel>>();
        for (AudioLabel label : prepared) {
        	for (int i = 3; i < label.getLabel().length(); ++i) {
        		String prefix = label.getLabel().substring(0, i).toLowerCase();
        		if (starts.containsKey(prefix)) starts.get(prefix).add(label);
        		else {
            		ArrayList<AudioLabel> newLabels = new ArrayList<AudioLabel>();
            		newLabels.add(label);
        			starts.put(prefix, newLabels);
        		}
        	}
        }
        for (String prefix : starts.keySet())
        {
        	if (prefix.length() < 5) continue;
        	if (starts.get(prefix).size() < 10) continue;
        	learn(starts.get(prefix), prefix.length());
        	break;
        }
	}
	
	void learn(ArrayList<AudioLabel> entryset, int size)
	{
		for (AudioLabel label : entryset) {
			int start = findIndex(label.getStart(), 0, allData.size());
			int end = findIndex(label.getStart() + 1, 0, allData.size());
			System.err.println(label.getLabel() + " " + label.getStart() + " " + label.getEnd() + " " + (end - start));
			PhonemeDisplay display = new PhonemeDisplay();
			display.draw(new DataSequence(allData.subList(start, end)));
		}
	}

	private int findIndex(double time, int bottom, int top)
	{
		if (bottom == top) return bottom;
		int between = (top + bottom) / 2;
		double auxTime = (allData.get(between).getStartTime() + allData.get(between + 1).getEndTime()) / 2;
		if (time < auxTime) return findIndex(time, bottom, between);
		else return findIndex(time, between + 1, top);
	}
}
