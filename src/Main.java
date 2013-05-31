import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;
import edu.cmu.sphinx.frontend.FrontEnd;
import edu.cmu.sphinx.frontend.Signal;
import edu.cmu.sphinx.frontend.SignalListener;
import edu.cmu.sphinx.frontend.endpoint.SpeechEndSignal;
import edu.cmu.sphinx.frontend.endpoint.SpeechStartSignal;
import edu.cmu.sphinx.frontend.feature.DeltasFeatureExtractor;
import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;

/**
 * A simple HelloWorld demo showing a simple speech application built using Sphinx-4. This application uses the Sphinx-4
 * endpointer, which automatically segments incoming audio into utterances and silences.
 */
public class Main {
	
    public static void main(String[] args) {
        ConfigurationManager cm;
 
        if (args.length > 0) {
            cm = new ConfigurationManager(args[0]);
        } else {
            cm = new ConfigurationManager(Main.class.getResource("helloworld.config.xml"));
        }

		// start the microphone or exit if the programm if this is not possible
		AudioFileDataSource fileSource = (AudioFileDataSource) cm.lookup("audioFileDataSource");
		File inputFile = new File("/home/bartek/workspace/speechtextmatcher/stefan-zeromski-doktor-piotr_test.wav");
//		File inputFile = new File("/home/bartek/workspace/speechtextmatcher/test3.wav");
		fileSource.setAudioFile(inputFile, null);
        
        FrontEnd frontend = (FrontEnd) cm.lookup("epFrontEnd");
        frontend.initialize();
//        WaveDisplay display = new WaveDisplay();
        
        class Speech
        {
        	double start;
        	double end;
        	Speech(double start, double end) {
        		this.start = start;
        		this.end = end;
        	}
			public double getStart() {
				return start;
			}
			public double getEnd() {
				return end;
			}
        }
        
        Data aux = frontend.getData();
        ArrayList<Speech> speechTimes = new ArrayList<Speech>();
        ArrayList<double[]> allData = new ArrayList<double[]>();
        ArrayList<Double> dataTimes = new ArrayList<Double>();
        double lastTimeEnd = 0;
        double speechStart = 0;
        int neigh = 30;
        while (aux != null)	{
        	if (aux.getClass() == SpeechStartSignal.class) {
        		speechStart = lastTimeEnd;
        	} else if (aux.getClass() == SpeechEndSignal.class) {
        		speechTimes.add(new Speech(speechStart, lastTimeEnd));
        	} else if (aux.getClass() == FloatData.class) {
        		FloatData floatData = (FloatData)aux;
        		int s = floatData.getValues().length;
        		
        		long[] longData = new long[s];
        		for (int i = neigh / 2; i < s  - neigh / 2; ++i)
        		{
        			for (int j = -neigh / 2; j < neigh / 2; ++j)
        				longData[i] +=
        					(long)Math.round(Math.log10(Math.abs(floatData.getValues()[i + j])) * 10000);
        		}
//        		display.drawData(longData);
        		
        		long lastSampleNumber = floatData.getFirstSampleNumber() + s;
        		lastTimeEnd = (double)lastSampleNumber / floatData.getSampleRate();
        		
        		double[] data = new double[s];
        		for (int i = 0; i < s; ++i) data[i] = floatData.getValues()[i];
        		allData.add(data);
        		dataTimes.add((double)floatData.getFirstSampleNumber() / floatData.getSampleRate());
        	} else if (aux.getClass() == DoubleData.class) {
        		DoubleData floatData = (DoubleData)aux;
        		int s = floatData.getValues().length;
        		
        		int upto = s / 2;
        		long[] longData = new long[upto / neigh];
        		double[] data = new double[upto / neigh];
        		for (int i = neigh / 2; i < upto - neigh / 2; i += neigh)
        		{
        			for (int j = -neigh / 2; j <= neigh / 2; ++j)
        			{
        				data[i / neigh] += floatData.getValues()[i + j];
        				longData[i / neigh] +=
        					(long)Math.round(data[i / neigh] * 10000);
        			}
        		}
//        		display.drawData(longData);
        		
        		long lastSampleNumber = floatData.getFirstSampleNumber() + s;
        		lastTimeEnd = (double)lastSampleNumber / floatData.getSampleRate();
        		
        		allData.add(data);
        		dataTimes.add((double)floatData.getFirstSampleNumber() / floatData.getSampleRate());
        	}
        	aux = frontend.getData();
        }
        
//        String text = "";
//        try {
//			BufferedReader fileReader = new BufferedReader(new FileReader("/home/bartek/workspace/speechtextmatcher/doktor-piotr_2.txt"));
//			String line;
//			while ((line = fileReader.readLine()) != null)
//				text += line;
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//        
//        double wholeTime = 0;
//        for (Speech elem : speechTimes) wholeTime += elem.end - elem.start;
//        text = text.replaceAll("   *", " ");
//        String[] sentences = text.split(" *[.,:\"!—”„…?;()]+ *");
//        String[] words = text.split("[.,:\"!—”„…?;() ]+");
//        int totalChars = 0;
//        for (String word : words) totalChars += word.length();
//        
//        double timePerChar = wholeTime / totalChars;
//        double timePerWord = wholeTime / words.length;
//        double[] estimatedTimes = new double[sentences.length];
//        for (int i = 0; i < sentences.length; ++i)
//        {
//        	String[] sentWords = sentences[i].split(" ");
//        	int chars = 0;
//        	for (String word : sentWords) chars += word.length();
//        	estimatedTimes[i] = (chars * timePerChar + sentWords.length * timePerWord) / 2.0;
//        }
//        
//        int[] matching = new int[speechTimes.size()];
//        int lastMatched = 0;
//        double carryOn = 0;
//        for (int j = 0; j < speechTimes.size(); ++j)
//        {
//        	double estTime = carryOn;
//        	int bestMatched = lastMatched;
//        	double smallestDiff = Double.MAX_VALUE;
//        	double time = speechTimes.get(j).end - speechTimes.get(j).start;
//        	for (int i = lastMatched; i < sentences.length; ++i)
//        	{
//        		estTime += estimatedTimes[i];
//        		double diff = time - estTime;
//        		if (Math.abs(smallestDiff) > Math.abs(diff))
//        		{
//        			smallestDiff = diff;
//        			bestMatched = i;
//        		}
//        		if (diff < 0) break;
//        	}
//        	if (Math.abs(smallestDiff) > timePerWord)
//        	{
//        		if (smallestDiff < 0)
//        			carryOn = (Math.ceil(smallestDiff / timePerWord) + 1) * timePerWord;
//        		else
//        			carryOn = (Math.floor(smallestDiff / timePerWord) - 1) * timePerWord;
//        		System.err.println("bigger " + carryOn);
//        	}
//        	matching[j] = bestMatched + 1;
//        	lastMatched = bestMatched + 1;
//        	System.err.println(j + " " + bestMatched + " " + time + " " + estTime);
//        }
        
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
        
        
    	BufferedWriter output = null;
		try {
			File outputFile = new File("/home/bartek/workspace/speechtextmatcher/labels.txt");
			OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(outputFile));
			output = new BufferedWriter(outputStream);
//			
//			int lastMatching = 0;
//			for (int i = 0; i < matching.length; ++i) {
//				double start = speechTimes.get(i).start;
//				double end = speechTimes.get(i).end;
//				String label = "";
//				if (matching[i] >= sentences.length) break;
//				for (int j = lastMatching; j < matching[i]; ++j) label += " " + sentences[j];
//				output.write(start + " " + end + " " + label + "\n");
//				output.flush();
//				lastMatching = matching[i];
//			}
//			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int count = 0;
    	for (Similar elem : best)
    	{
    		int bestInd1 = elem.first;
    		int bestInd2 = elem.second;
    		
			try {
				double firstStart = dataTimes.get(bestInd1);
				double firstEnd = dataTimes.get(bestInd1 + window);
				double secondStart = dataTimes.get(bestInd2);
				double secondEnd = dataTimes.get(bestInd2 + window);
				output.write(firstStart + " " + firstEnd + " " + count + "_first\n");
				output.write(secondStart + " " + secondEnd + " " + count + "_second\n");
				++count;
				output.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
    		System.err.println(bestInd1 + " " + bestInd2);
    	}
    	try {
			if (output != null) output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
 
//        Recognizer recognizer = (Recognizer) cm.lookup("recognizer");
//        recognizer.allocate();
// 
//        // start the microphone or exit if the programm if this is not possible
//        AudioFileDataSource fileSource = (AudioFileDataSource) cm.lookup("audioFileDataSource");
//        File inputFile = new File("/home/bartek/workspace/speechtextmatcher/stefan-zeromski-doktor-piotr_test.wav");
//        fileSource.setAudioFile(inputFile, null);
//        System.err.println("sample rate: " + fileSource.getSampleRate());
//        
//        // loop the recognition until the programm exits.
//        Result result;
//        while ((result = recognizer.recognize()) != null) {
//            System.out.println(result.getBestFinalResultNoFiller());
//        }
    }
}
