import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sound.sampled.AudioInputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import edu.cmu.sphinx.result.MAPConfidenceScorer;

public class Main {
	
    public static void main(String[] args) {
//    	String waveFile = "/home/bartek/workspace/speechtextmatcher/przedwiosnie-rodowod.wav";
//    	String textFile = "/home/bartek/workspace/speechtextmatcher/przedwiosnie_rodowod.txt";
    	
//    	String waveFile = "/home/bartek/workspace/speechtextmatcher/stefan-zeromski-doktor-piotr_test.wav";
//    	String textFile = "stefan-zeromski-doktor-piotr_test.txt";
//    	String waveFile = "/home/bartek/workspace/speechtextmatcher/song_test.wav";
//    	String textFile = "song_test.txt";
    	
    	String waveFile = "/home/bartek/workspace/speechtextmatcher/stefan-zeromski-doktor-piotr.wav";
//    	String textFile = "/home/bartek/workspace/speechtextmatcher/doktor-piotr_2.txt";

    	WaveImporter waveImporterForOfflineSpeechRecognition = new WaveImporter(waveFile, "config_nospeech_nomel.xml");
    	WaveImporter waveImporterForPhonemeRecognition = new WaveImporter(waveFile, "config_all.xml");
    	OnlineSpeechesExtractor speechExtractor = new OnlineSpeechesExtractor();
    	OfflineSpeechRecognizer speechRecognizer = new OfflineSpeechRecognizer(20, 10);
//    	
    	waveImporterForOfflineSpeechRecognition.registerObserver(speechRecognizer);
    	waveImporterForPhonemeRecognition.registerSpeechObserver(speechExtractor);
    	waveImporterForPhonemeRecognition.registerObserver(speechExtractor);
//    	
    	waveImporterForOfflineSpeechRecognition.process();
    	waveImporterForPhonemeRecognition.process();
    	waveImporterForPhonemeRecognition.done();
    	waveImporterForOfflineSpeechRecognition.done();
//    	
//    	Speeches speeches = speechRecognizer.findSpeechParts();
    	DataSequence allData = speechRecognizer.getAllData();
//    	DataSequence allData = speechExtractor.getAllData();
//    	allData = new OfflineDataNormalizer(allData).normalize();
//        Text text = new Text(new TextImporter(textFile), speeches.getTotalTime());
    	
    	AudioLabel[] prepared = new AudacityLabelImporter(new TextImporter("by_length_labels.txt")).getLabels();
    	
    	PhonemeLearner phonemeLearner = new PhonemeLearner(prepared, allData);
    	phonemeLearner.process();
    	
//    	PhonemeDisplay display = new PhonemeDisplay();
//    	DataSequence sequence = new DataSequence();
//    	for (int i = 0; i < 10; ++i) {
//    		double[] spectrum = new double[10];
//    		for (int j = 0; j < spectrum.length; ++j) spectrum[j] = ((i + j) % 5 == 0) ? 100 : 0;
//    		sequence.add(new Data(0, 0, spectrum));
//    	}
//    	display.draw(sequence);
    	
//        ChangeTracer changeTracer = new ChangeTracer(speechRecognizer.getAllData(), speeches);
//        AudioLabel[] bigChanges = changeTracer.process();
        
//        StartingPhonemeFinder finder = new StartingPhonemeFinder(allData, text, matched);
//        CommonWordPhonemesFinder finder = new CommonWordPhonemesFinder(allData, text, matched);
//        AudioLabel[] phonemeLabels = finder.process();
//    	new AudacityLabelsExporter("/home/bartek/workspace/speechtextmatcher/labels3.txt").export(phonemeLabels);
        System.err.println("END");
    }
}

