import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.cmu.sphinx.result.MAPConfidenceScorer;

public class Main {
	
    public static void main(String[] args) {
    	
    	
//    	String waveFile = "/home/bartek/workspace/speechtextmatcher/przedwiosnie-rodowod.wav";
//    	String textFile = "/home/bartek/workspace/speechtextmatcher/przedwiosnie_rodowod.txt";
    	
    	String waveFile = "/home/bartek/workspace/speechtextmatcher/stefan-zeromski-doktor-piotr_test.wav";
    	String textFile = "stefan-zeromski-doktor-piotr_test.txt";
    	
//    	String waveFile = "/home/bartek/workspace/speechtextmatcher/stefan-zeromski-doktor-piotr.wav";
//    	String textFile = "/home/bartek/workspace/speechtextmatcher/doktor-piotr_2.txt";

    	WaveImporter waveImporter = new WaveImporter(waveFile);
    	OnlineSpeechesExtractor speechExtractor = new OnlineSpeechesExtractor();
//    	OfflineSpeechRecognizer speechRecognizer = new OfflineSpeechRecognizer(20, 10);
//    	WaveDisplay display = new WaveDisplay(); 
//    	waveImporter.registerObserver(display);//new WaveDataPacker(display, 1.0, 0.00001));
//    	waveImporter.registerObserver(speechRecognizer);//new WaveDataPacker(speechRecognizer, 1.0, 0.1));
    	waveImporter.registerObserver(speechExtractor);
    	waveImporter.registerSpeechObserver(speechExtractor);
    	waveImporter.process();
//    	
    	Speeches speeches = speechExtractor.getSpeeches();
    	ArrayList<Data> allData = speechExtractor.getAllData();
//        System.err.println("speeches " + speeches.size());
//        
        Text text = new Text(new TextImporter(textFile), speeches.getTotalTime());
//        StartingPhonemeFinder fonemFinder = new StartingPhonemeFinder(speeches, allData, text);
        CommonWordPhonemesFinder fonemFinder = new CommonWordPhonemesFinder(speeches, allData, text);
        
//       
//        AudioLabel[] labels = new TextToSpeechByLengthAligner().findMatching(text, speeches);
        
//        Map<String, Integer> prefixes = new TreeMap<String, Integer>();
//        
//        ArrayList<AudioLabel> newLabels = new ArrayList<AudioLabel>();
//        
//        int minPrefix = 3;
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
//        	if (n > 10) newLabels.add(new AudioLabel(prefix, label.getStart(), label.getStart() + 0.5));
//        }
//        labels = newLabels.toArray(new AudioLabel[0]);
    	
//    	AudioLabel[] labels = new AudioLabel[speeches.size()];
//    	for (int i = 0; i < labels.length; ++i)
//    		labels[i] = new AudioLabel("label" + i, speeches.get(i).getStartTime(), speeches.get(i).getEndTime());
        
//    	ArrayList<Data> newAllData = new ArrayList<Data>();
//    	
//    	for (Speech speech : speeches)
//    	{
//    		for (int i = speech.getStartDataIndex(); i < speech.getEndDataIndex(); ++i)
//    		{
//    			newAllData.add(allData.get(i));
//    		}
//    	}
//    	allData = newAllData;
//    	newAllData = new ArrayList<Data>();
//    	int neigh = 2;
//    	for (int i = neigh; i <= allData.size(); i += neigh)
//    	{
//    		int spectrumSize = allData.get(i - 1).getSpectrum().length;
//    		double[] spectrum = new double[spectrumSize];
//    		for (int j = i - neigh; j < i; ++j)
//    			for (int k = 0; k < spectrumSize; ++k)
//    				spectrum[k] += allData.get(j).getSpectrum()[k];
//    		Data temp = new Data(
//    				allData.get(i - neigh).getStartTime(),
//    				allData.get(i - 1).getEndTime(),
//    				spectrum);
////    		display.process(0, 0, spectrum);
//    		newAllData.add(temp);
//    	}
//    	allData = newAllData;
//        
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
//        SortedSet<Similar> best = new TreeSet<Similar>();
//        
//        double max = 0;
//        for (Data elem : allData)
//        	for (double v : elem.getSpectrum())
//        		if (v > max) max = v;
//        
//
//        int s = allData.size();
//        double[][] diffs = new double[s][s];
//        
//        System.err.println("calculating diffs");
//        int promile = 40;
//        SortedSet<Double> percentileCont = new TreeSet<Double>();
//        int percentileSize = (int)((long)allData.size() * (allData.size() - 1) / 2 * promile / 1000);
//        System.err.println(percentileSize + " " + s);
//        double averageDiff = 0;
//        int count = 0;
//        double maxDiff = 0;
//        for (int i = 0; i < s; ++i)
//        {
//        	if (i % 100 == 0) System.err.println(i + "/" + s);
//			double[] icurrdata = allData.get(i).getSpectrum();
//        	for (int j = i + 1; j < s; ++j)
//        	{
//    			double[] jcurrdata = allData.get(j).getSpectrum();
//    			double diff = 0;
//        		for (int k = 0; k < jcurrdata.length; ++k)
//        			diff += Math.abs(jcurrdata[k] - icurrdata[k]);// * (jcurrdata[k] * 10 - icurrdata[k] * 10);
//        		diffs[i][j] = diff;
//        		++count;
//        		averageDiff += diff;
//        		if (diff > maxDiff) maxDiff = diff;
//        		percentileCont.add(diff);
//        		if (percentileCont.size() > percentileSize)
//        			percentileCont.remove(percentileCont.last());
//        	}
//        }
//        
//        double percentile = percentileCont.last();
//        System.err.println("Percentile: " + percentile + " " + percentileCont.first());
//        System.err.println("max " + maxDiff);
//        
//        int skip = 80 / neigh;
//        int minSize = skip;
//        
//        System.err.println("calculating labels");
//        ArrayList<AudioLabel> auxLabels = new ArrayList<AudioLabel>();
//        for (int i = 0; i < s;)
//        {
//        	double startTime1 = allData.get(i).getStartTime();
//        	int bestK = 1;
//        	for (int j = i + skip; j < s;)
//        	{
//        		int k = 1;
//        		double sum = diffs[i][j];
//        		while ((j + k + 1 < s) && (sum <= percentile * k)) {
//        			sum += diffs[i][j + k];
//        			++k;
//        		}
//        		bestK = Math.max(k, bestK);
//        		if (k < minSize) { j += k;  continue; }
//        		String label1 = (auxLabels.size() / 2) + "_l";
//        		String label2 = (auxLabels.size() / 2) + "_r";
//        		double endTime1 = allData.get(i + k).getEndTime();
//        		double startTime2 = allData.get(j).getStartTime();
//        		double endTime2 = allData.get(j + k).getEndTime();
//        		
//        		AudioLabel alabel1 = new AudioLabel(label1, startTime1, endTime1);
//        		AudioLabel alabel2 = new AudioLabel(label2, startTime2, endTime2);
//        		auxLabels.add(alabel1);
//        		auxLabels.add(alabel2);
//        		j += k;
//        	}
//        	i += bestK;
//        	if (auxLabels.size() > 300) break;
//        }
        
        AudioLabel[] labels = fonemFinder.process();//auxLabels.toArray(new AudioLabel[0]);
    	new AudacityLabelsExporter("/home/bartek/workspace/speechtextmatcher/labels.txt").export(labels);
        System.err.println("END");
    }
}

