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
    	
//    	String waveFile = "/home/bartek/workspace/speechtextmatcher/stefan-zeromski-doktor-piotr_test.wav";
//    	String textFile = "stefan-zeromski-doktor-piotr_test.txt";
//    	String waveFile = "/home/bartek/workspace/speechtextmatcher/song_test.wav";
//    	String textFile = "song_test.txt";
    	
    	String waveFile = "/home/bartek/workspace/speechtextmatcher/stefan-zeromski-doktor-piotr.wav";
    	String textFile = "/home/bartek/workspace/speechtextmatcher/doktor-piotr_2.txt";

    	WaveImporter waveImporterForOfflineSpeechRecognition = new WaveImporter(waveFile, "config_nospeech_nomel.xml");
    	WaveImporter waveImporterForPhonemeRecognition = new WaveImporter(waveFile, "config_all.xml");
    	OnlineSpeechesExtractor speechExtractor = new OnlineSpeechesExtractor();
    	OfflineSpeechRecognizer speechRecognizer = new OfflineSpeechRecognizer(20, 10);
    	
    	waveImporterForOfflineSpeechRecognition.registerObserver(speechRecognizer);
    	waveImporterForPhonemeRecognition.registerSpeechObserver(speechExtractor);
    	waveImporterForPhonemeRecognition.registerObserver(speechExtractor);
    	
//    	WaveDisplay display = new WaveDisplay(); 
//    	waveImporter.registerObserver(display);//new WaveDataPacker(display, 1.0, 0.00001));
//    	waveImporter.registerObserver(speechRecognizer);//new WaveDataPacker(speechRecognizer, 1.0, 0.1));
    	
    	waveImporterForOfflineSpeechRecognition.process();
    	waveImporterForPhonemeRecognition.process();
    	waveImporterForPhonemeRecognition.done();
    	waveImporterForOfflineSpeechRecognition.done();
//    	
//    	Speeches speeches = speechExtractor.getSpeeches();
    	Speeches speeches = speechRecognizer.findSpeechParts();
    	ArrayList<Data> allData = speechExtractor.getAllData();

        
        AudioLabel[] speechLabels = new AudioLabel[speeches.size()];
        for (int i = 0; i < speechLabels.length; ++i) {
        	speechLabels[i] = new AudioLabel(i + "", speeches.get(i).getStartTime(), speeches.get(i).getEndTime());
        }
    	new AudacityLabelsExporter("/home/bartek/workspace/speechtextmatcher/labels1.txt").export(speechLabels);
    
//    	WaveDisplay display = new WaveDisplay(); 
//    	for (Data data : speechRecognizer.getAllData()) {
//    		display.process(0, 0, data.getSpectrum());
//    	}
    	
        Text text = new Text(new TextImporter(textFile), speeches.getTotalTime());
        
//        AudioLabel[] prepared = new AudacityLabelImporter(new TextImporter("labels.txt")).getLabels();
        TextToSpeechByLengthAligner byLengthAligner = new TextToSpeechByLengthAligner();
        IncrementalTextToSpeechAligner incrementalAligner =
        		new IncrementalTextToSpeechAligner(byLengthAligner);
        AudioLabel[] matched = incrementalAligner.findMatching(text, speeches);
        
    	new AudacityLabelsExporter("/home/bartek/workspace/speechtextmatcher/labels2.txt").export(matched);
  
//        ChangeTracer changeTracer = new ChangeTracer(speechRecognizer.getAllData(), speeches);
//        AudioLabel[] bigChanges = changeTracer.process();
        
//        StartingPhonemeFinder finder = new StartingPhonemeFinder(allData, text, matched);
//        CommonWordPhonemesFinder finder = new CommonWordPhonemesFinder(allData, text, matched);
//        AudioLabel[] phonemeLabels = finder.process();
//    	new AudacityLabelsExporter("/home/bartek/workspace/speechtextmatcher/labels3.txt").export(phonemeLabels);
        System.err.println("END");
    }
}

