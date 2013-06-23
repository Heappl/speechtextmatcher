import speechDetection.OfflineSpeechRecognizer;
import speechDetection.OnlineSpeechesExtractor;

import common.AudioLabel;
import common.DataSequence;
import common.Speeches;
import common.Text;
import dataExporters.AudacityLabelsExporter;
import dataProducers.AudacityLabelImporter;
import dataProducers.TextImporter;
import dataProducers.WaveImporter;

public class Main {
	
    public static void main(String[] args)
    {
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
    	Speeches speeches = speechExtractor.getSpeeches();
//    	DataSequence allData = speechRecognizer.getAllData();
    	DataSequence allData = speechExtractor.getAllData();
//    	allData = new OfflineDataNormalizer(allData).normalize();
    	System.err.println("total time: " + speeches.getTotalTime());
        Text text = new Text(new TextImporter(textFile), speeches.getTotalTime());
    	
    	AudioLabel[] prepared = new AudacityLabelImporter(new TextImporter("by_length_labels.txt")).getLabels();
    	
//    	PhonemeLearner phonemeLearner = new PhonemeLearner(prepared, allData);
//    	phonemeLearner.process();
    	
        StartingPhonemeFinder finder = new StartingPhonemeFinder(allData, text, prepared);
//        CommonWordPhonemesFinder finder = new CommonWordPhonemesFinder(allData, text, prepared);
        AudioLabel[] phonemeLabels = finder.process();
    	new AudacityLabelsExporter("/home/bartek/workspace/speechtextmatcher/labels3.txt").export(phonemeLabels);
        System.err.println("END");
    }
}

