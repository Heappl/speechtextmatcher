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
//		File inputFile = new File("/home/bartek/workspace/speechtextmatcher/stefan-zeromski-doktor-piotr_test.wav");
		File inputFile = new File("/home/bartek/workspace/speechtextmatcher/stefan-zeromski-doktor-piotr.wav");
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
        int neigh = 1;
        int spectrumSize = 0;
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
        		
        		int upto = s;
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
        		spectrumSize = data.length;
        		
        		long lastSampleNumber = floatData.getFirstSampleNumber() + s;
        		lastTimeEnd = (double)lastSampleNumber / floatData.getSampleRate();
        		
        		allData.add(data);
        		dataTimes.add((double)floatData.getFirstSampleNumber() / floatData.getSampleRate());
        	}
        	aux = frontend.getData();
        }
        
        double[] averages = new double[spectrumSize];
        for (int i = 0; i < allData.size(); ++i)
        {
        	double[] curr = allData.get(i);
        	for (int j = 0; j < spectrumSize; ++j)
        		averages[j] += curr[j];
        }
        for (int j = 0; j < spectrumSize; ++j) averages[j] /= allData.size();
        double[] deviations = new double[spectrumSize];
        for (int i = 0; i < allData.size(); ++i)
        {
        	double[] curr = allData.get(i);
        	for (int j = 0; j < spectrumSize; ++j)
        		deviations[j] += Math.abs(averages[j] - curr[j]);// * (averages[j] - curr[j]);
        }
        for (int j = 0; j < spectrumSize; ++j) deviations[j] /= allData.size();
        
        boolean[] isSpeech = new boolean[allData.size() + 2];
        for (int i = 0; i < allData.size(); ++i)
        {
        	int count = 0;
        	double[] curr = allData.get(i);
        	for (int j = 0; j < spectrumSize; ++j)
        		if (Math.abs(curr[j] - averages[j]) > deviations[j])
        			++count;
        	isSpeech[i + 1] = (count > 0);
        }
        int dist = 70;
        for (int i = 1; i < allData.size() - 1; ++i)
        {
        	if (isSpeech[i]) continue;
        	boolean foundLeft = i < dist;
        	boolean foundRight = i > allData.size() - dist;
        	for (int j = 1; j < dist; ++j) {
        		if (foundLeft) break;
        		foundLeft |= isSpeech[i - j] ^ isSpeech[i];
        	}
        	for (int j = 1; j < dist; ++j) {
        		if (foundRight) break;
        		foundRight |= isSpeech[i + j] ^ isSpeech[i];
        	}
        	if (foundLeft && foundRight)
        		isSpeech[i] = !isSpeech[i];
        }
        dist = 30;
        for (int i = dist; i < allData.size() - dist; ++i)
        {
        	if (!isSpeech[i]) continue;
        	boolean foundLeft = i < dist;
        	boolean foundRight = i > allData.size() - dist;
        	for (int j = 1; j < dist; ++j) {
        		if (foundLeft) break;
        		foundLeft |= isSpeech[i - j] ^ isSpeech[i];
        	}
        	for (int j = 1; j < dist; ++j) {
        		if (foundRight) break;
        		foundRight |= isSpeech[i + j] ^ isSpeech[i];
        	}
        	if (foundLeft && foundRight)
        		isSpeech[i] = !isSpeech[i];
        }
        
        int start = -1;
        for (int i = 0; i < allData.size(); ++i)
        {
        	if ((start >= 0) && !isSpeech[i])
        	{
        		speechTimes.add(new Speech(dataTimes.get(start), dataTimes.get(i)));
        		start = -1;
        	}
        	if ((start < 0) && isSpeech[i])
        		start = i;
        }
        System.err.println("speeches " + speechTimes.size());
        
        String text = "";
        try {
			BufferedReader fileReader = new BufferedReader(new FileReader("/home/bartek/workspace/speechtextmatcher/doktor-piotr_2.txt"));
//			BufferedReader fileReader = new BufferedReader(new FileReader("stefan-zeromski-doktor-piotr_test.txt"));
			String line;
			while ((line = fileReader.readLine()) != null)
				text += line + "\n";
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        double wholeTime = 0;
        for (Speech elem : speechTimes) wholeTime += elem.end - elem.start;
        text = text.replaceAll("\n *\n", ".").replaceAll("[\\s\n]+", " ");
        SortedSet<Character> chars = new TreeSet<Character>();
        for (int i = 0; i < text.length(); ++i)
        	if (Character.isLetterOrDigit(text.charAt(i)))
        		chars.add(text.charAt(i));
        String charRegex = "";
        for (Character c : chars) charRegex += c;
        System.err.println("chars " + charRegex);
        String[] sentences = text.split(" *[^" + charRegex + "'\\s]+ *");
        String[] words = text.split("[^" + charRegex + "' ]+");
        int totalChars = 0;
        for (String word : words) totalChars += word.length();
        System.err.println("sentences: " + sentences.length);
        
        double timePerChar = wholeTime / totalChars;
        double timePerWord = wholeTime / words.length;
        double[] estimatedTimes = new double[sentences.length];
        for (int i = 0; i < sentences.length; ++i)
        {
        	String[] sentWords = sentences[i].split(" ");
        	int noChars = 0;
        	for (String word : sentWords) noChars += word.length();
        	estimatedTimes[i] = (noChars * timePerChar + sentWords.length * timePerWord) / 2.0;
        }
        
        //matching[i][j] - best matching when we matched `i` speeches and `j` sentences 
        double[] matchingScores = new double[sentences.length];
        int[][] matchingIndexes = new int[speechTimes.size()][sentences.length];
        double[][] estimates = new double[sentences.length][sentences.length];
        double totalEstTime = 0;
        for (int i = 0; i < sentences.length; ++i)
        {
        	totalEstTime += estimatedTimes[i];
        	estimates[i][0] = totalEstTime;
        	for (int j = 1; j <= i; ++j)
        		estimates[i][j] = totalEstTime - estimates[j - 1][0];
        	for (int j = i + 1; j < sentences.length; ++j)
        		estimates[i][j] = Double.MAX_VALUE;
        }
        for (int i = 0; i < sentences.length; ++i)
        {
        	double time = speechTimes.get(0).end - speechTimes.get(0).start;
        	double auxEst = (estimates[i][0] - time);
        	matchingScores[i] = auxEst * auxEst;
        	matchingIndexes[0][i] = 0;
        }
        
        for (int i = 1; i < speechTimes.size(); ++i)
        {
        	double time = speechTimes.get(i).end - speechTimes.get(i).start;
        	double[] newMatchingScores = new double[sentences.length];
        	for (int j = 0; j < sentences.length; ++j)
        	{
        		newMatchingScores[j] = Double.MAX_VALUE;
        		for (int k = 1; k <= j; ++k)
        		{
        			double prevScore = matchingScores[j - k];
        			double auxDiff = time - estimates[j][j - k + 1];
        			double diff = auxDiff * auxDiff;
        			double scoreCand = prevScore + diff;
        			if (scoreCand < newMatchingScores[j])
        			{
        				newMatchingScores[j] = scoreCand;
        				matchingIndexes[i][j] = j - k;
        			}
        		}
        	}
        	matchingScores = newMatchingScores;
        }
        
        int[] matching = new int[speechTimes.size()];
        matching[matching.length - 1] = sentences.length - 1;
        for (int i = speechTimes.size() - 2; i >= 0; --i)
        	matching[i] = matchingIndexes[i + 1][matching[i + 1]];
        
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
			
//			int count = 0;
//			for (Speech speech : speechTimes)
//				output.write(speech.start + " " + speech.end + " label_" + count++ + "\n");
//			output.flush();
			
			int lastMatching = 0;
			for (int i = 0; i < matching.length; ++i) {
				double startx = speechTimes.get(i).start;
				double end = speechTimes.get(i).end;
				String label = "";
				if (matching[i] >= sentences.length) break;
				for (int j = lastMatching; j <= matching[i]; ++j) label += " " + sentences[j];
				output.write(startx + " " + end + " " + label + "\n");
				output.flush();
				lastMatching = matching[i] + 1;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
