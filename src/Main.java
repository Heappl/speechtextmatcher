import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import common.AudioLabel;
import dataExporters.AudacityLabelsExporter;
import dataProducers.AudacityLabelImporter;
import dataProducers.TextImporter;

public class Main {
	
    public static void main(String[] args)
    {
//    	String waveFile = "/home/bartek/workspace/speechtextmatcher/przedwiosnie-rodowod.wav";
//    	String textFile = "/home/bartek/workspace/speechtextmatcher/przedwiosnie_rodowod.txt";
    	
//    	String waveFile = "/home/bartek/workspace/speechtextmatcher/stefan-zeromski-doktor-piotr_test.wav";
//    	String textFile = "stefan-zeromski-doktor-piotr_test.txt";
//    	String waveFile = "/home/bartek/workspace/speechtextmatcher/song_test.wav";
//    	String textFile = "song_test.txt";
    	
//    	String waveFile = "/home/bartek/workspace/speechtextmatcher/stefan-zeromski-doktor-piotr.wav";
//    	String textFile = "/home/bartek/workspace/speechtextmatcher/doktor-piotr_2.txt";
//
//    	WaveImporter waveImporterForOfflineSpeechRecognition = new WaveImporter(waveFile, "config_nospeech_nomel.xml");
//    	WaveImporter waveImporterForPhonemeRecognition = new WaveImporter(waveFile, "config_all.xml");
//    	OnlineSpeechesExtractor speechExtractor = new OnlineSpeechesExtractor();
//    	OfflineSpeechRecognizer speechRecognizer = new OfflineSpeechRecognizer(20, 10);
////    	
//    	waveImporterForOfflineSpeechRecognition.registerObserver(speechRecognizer);
//    	waveImporterForPhonemeRecognition.registerSpeechObserver(speechExtractor);
//    	waveImporterForPhonemeRecognition.registerObserver(speechExtractor);
////    	
//    	waveImporterForOfflineSpeechRecognition.process();
//    	waveImporterForPhonemeRecognition.process();
//    	waveImporterForPhonemeRecognition.done();
//    	waveImporterForOfflineSpeechRecognition.done();
////    	
//    	Speeches speeches = speechRecognizer.findSpeechParts();
////    	Speeches speeches = speechExtractor.getSpeeches();
////    	DataSequence allData = speechRecognizer.getAllData();
//    	DataSequence allData = speechExtractor.getAllData();
////    	allData = new OfflineDataNormalizer(allData).normalize();
//    	System.err.println("total time: " + speeches.getTotalTime());
//        Text text = new Text(new TextImporter(textFile), speeches.getTotalTime());
//    	System.err.println("per char: " + text.getEstimatedTimePerCharacter());
//    	
//    	ArrayList<AudioLabel> aux = new ArrayList<AudioLabel>();
//    	int count = 0;
//    	for (Speech speech : speeches) {
//    		double start = speech.getStartTime();
//    		double end = speech.getEndTime();
//    		String label = "" + count;
//    		aux.add(new AudioLabel(label, start, end));
//    	}
//    	AudioLabel[] labels = aux.toArray(new AudioLabel[0]);
//    	
//    	AudioLabel[] words = new AudacityLabelImporter(new TextImporter(args[2])).getLabels();
    	AudioLabel[] phonemes = new AudacityLabelImporter(new TextImporter(args[3])).getLabels();
    	
    	SortedSet<AudioLabel> sorted = new TreeSet<AudioLabel>(new Comparator<AudioLabel>() {
            @Override
            public int compare(AudioLabel o1, AudioLabel o2)
            {
                double time1 = o1.getEnd() - o1.getStart();
                double time2 = o2.getEnd() - o2.getStart();
                if (time1 < time2) return 1;
                if (time1 > time2) return -1;
                return 0;
            }
        });
    	for (AudioLabel phoneme : phonemes) {
    	    sorted.add(phoneme);
    	}
    	
    	double minTime = 0.5;
    	ArrayList<AudioLabel> wrong = new ArrayList<AudioLabel>();
    	int count = 0;
    	for (;;) {
    	    if (sorted.first().getEnd() - sorted.first().getStart() < minTime) break;
    	    System.err.println(sorted.first());
    	    wrong.add(sorted.first());
    	    sorted.remove(sorted.first());
    	    ++count;
    	}
    	System.err.println(count);
    	
//    	
////    	PhonemeLearner phonemeLearner = new PhonemeLearner(prepared, allData);
////    	phonemeLearner.process();
//    	
////        StartingPhonemeFinder finder = new StartingPhonemeFinder(allData, text, prepared);
////        CommonWordPhonemesFinder finder = new CommonWordPhonemesFinder(allData, text, prepared);
////        AudioLabel[] phonemeLabels = finder.process();
    	new AudacityLabelsExporter("/home/bartek/workspace/speechtextmatcher/labels1.txt").export(wrong);
        System.err.println("END");
    }
}


