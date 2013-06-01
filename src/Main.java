import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import edu.cmu.sphinx.result.MAPConfidenceScorer;

public class Main {
	
    public static void main(String[] args) {
    	
//    	String waveFile = "/home/bartek/workspace/speechtextmatcher/stefan-zeromski-doktor-piotr_test.wav";
    	String waveFile = "/home/bartek/workspace/speechtextmatcher/stefan-zeromski-doktor-piotr.wav";
//    	String waveFile = "/home/bartek/workspace/speechtextmatcher/przedwiosnie-rodowod.wav";
    	
    	String textFile = "/home/bartek/workspace/speechtextmatcher/doktor-piotr_2.txt";
//    	String textFile = "/home/bartek/workspace/speechtextmatcher/przedwiosnie_rodowod.txt";
//    	String textFile = "stefan-zeromski-doktor-piotr_test.txt";

    	WaveImporter waveImporter = new WaveImporter(waveFile);
    	OfflineSpeechRecognizer speechRecognizer = new OfflineSpeechRecognizer(10, 10);
//    	WaveDisplay display = new WaveDisplay(); 
//    	waveImporter.registerObserver(display);//new WaveDataPacker(display, 1.0, 0.01));
    	waveImporter.registerObserver(speechRecognizer);//new WaveDataPacker(speechRecognizer, 1.0, 0.00001));
    	waveImporter.process();
    	
    	ArrayList<Speech> speechTimes = speechRecognizer.findSpeechParts();
//    	ArrayList<Data> allData = speechRecognizer.getAllData();
        System.err.println("speeches " + speechTimes.size());
        
        String text = new TextImporter(textFile).getText();
        
        AudioLabel[] labels = new TextToSpeechByLengthAligner().findMatching(text, speechTimes);
        
//        Map<String, Integer> prefixes = new TreeMap<String, Integer>();
//        
//        ArrayList<AudioLabel> newLabels = new ArrayList<AudioLabel>();
//        
//        int minPrefix = 5;
//        for (AudioLabel label : labels)
//        {
//        	String labelText = label.getLabel().replaceAll("[. ]+", " ");
//        	
//        	for (int i = minPrefix; i < Math.min(labelText.length(), minPrefix + 1); ++i)
//        	{
//        		String prefix = labelText.substring(0, i);
//        		if (!prefixes.containsKey(prefix))
//        			prefixes.put(prefix, 1);
//        		else
//        			prefixes.put(prefix, prefixes.get(prefix) + 1);
//        	}
//        }
//        for (AudioLabel label : labels)
//        {
//        	String labelText = label.getLabel().replaceAll("[. ]+", " ");
//        	if (labelText.length() < minPrefix) continue;
//        	String prefix = labelText.substring(0, minPrefix);
//        	if (!prefixes.containsKey(prefix)) continue;
//        	int n = prefixes.get(prefix);
//        	if (n > 2) newLabels.add(label);
//        }
//        labels = newLabels.toArray(new AudioLabel[0]);
    	
//    	AudioLabel[] labels = new AudioLabel[speechTimes.size()];
//    	for (int i = 0; i < labels.length; ++i)
//    		labels[i] = new AudioLabel("label" + i, speechTimes.get(i).getStartTime(), speechTimes.get(i).getEndTime());
        
        new AudacityLabelsExporter("/home/bartek/workspace/speechtextmatcher/labels.txt").export(labels);
        
//        class Similar
//        {
//        	double diff;
//        	int first;
//        	int second;
//        	
//        	public Similar(double diff, int first, int second) {
//        		this.diff = diff;
//        		this.first = first;
//        		this.second = second;
//			}
//        }
//        SortedSet<Similar> best = new TreeSet<Similar>(new Comparator<Similar>() {
//			@Override
//			public int compare(Similar o1, Similar o2) {
//				if (o1.diff < o2.diff) return -1;
//				if (o1.diff > o2.diff) return 1;
//				if (o1.first < o2.first) return -1;
//				if (o1.first > o2.first) return 1;
//				if (o1.second < o2.second) return -1;
//				if (o1.second > o2.second) return 1;
//				return 0;
//			}
//		});
//        
//        int window = 100;
//        int s = allData.size();
//        int bestSize = 10;
//        for (int i = 0; i < s - 3 * window; i += 2)
//        {
//        	if (i % 100 == 0) System.err.println(i + "/" + s);
//        	for (int j = i + window; j < s - window; j += 2)
//        	{
//        		double diff = 0;
//        		for (int k = 1; k < window; ++k)
//        		{
//        			double[] iprevdata = allData.get(i + k - 1);
//        			double[] jprevdata = allData.get(j + k - 1);
//        			double[] icurrdata = allData.get(i + k);
//        			double[] jcurrdata = allData.get(j + k);
//        			for (int l = 0; l < icurrdata.length; ++l)
//        			{
//        				double idiff = icurrdata[l];// - iprevdata[l];
//        				double jdiff = jcurrdata[l];// - jprevdata[l];
//        				diff += Math.abs(idiff - jdiff) / ((idiff + jdiff) / 2) * window / k;// * (idiff - jdiff);
//        			}
//        		}
//        		best.add(new Similar(diff, i, j));
//        		if (best.size() > bestSize)
//        		{
//        			best.remove(best.last());
//        		}
//        	}
//        }
        
		
//		int count = 0;
//    	for (Similar elem : best)
//    	{
//    		int bestInd1 = elem.first;
//    		int bestInd2 = elem.second;
//    		
//			try {
//				double firstStart = dataTimes.get(bestInd1);
//				double firstEnd = dataTimes.get(bestInd1 + window);
//				double secondStart = dataTimes.get(bestInd2);
//				double secondEnd = dataTimes.get(bestInd2 + window);
//				output.write(firstStart + " " + firstEnd + " " + count + "_first\n");
//				output.write(secondStart + " " + secondEnd + " " + count + "_second\n");
//				++count;
//				output.flush();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//    		System.err.println(bestInd1 + " " + bestInd2);
//    	}

        System.err.println("END");
    }
}
