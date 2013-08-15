package audioModelSupportedAlignment;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import common.AudioLabel;
import common.Speeches;
import common.Text;
import dataExporters.AudacityLabelsExporter;
import dataProducers.TextImporter;
import dataProducers.WaveImporter;
import graphemesToPhonemesConverters.DictionaryGenerator;
import graphemesToPhonemesConverters.GraphemesToRussianPhonemesConverter;
import speechDetection.OfflineSpeechRecognizer;

public class UsingAudioModelMain
{
	public static URL audioModelUrl() throws MalformedURLException
	{
//    	URL[] urls = new URL[]{new File("sphinx/lib/WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz.jar").toURI().toURL()};
//    	URLClassLoader classLoader = new URLClassLoader(urls);
//    	return classLoader.findResource("WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz");
		return new URL(
			"file:/home/bartek/workspace/speechtextmatcher/voxforge-ru-0.2/model_parameters/msu_ru_nsh.cd_cont_1000_8gau_16000/");
	}
	
    public static void main(String[] args) throws Exception
    {
    	String inputWavePath = args[0];
    	String inputTextPath = args[1];
    	String outputPath = args[2];
    	
        URL acousticModel = audioModelUrl();
    	String dictTempPath = args[2] + ".temp";
        URL dictionary = new URL("file:" + dictTempPath);

        Speeches speeches = null;
        {
	    	WaveImporter waveImporterForOfflineSpeechRecognition = new WaveImporter(inputWavePath, "config_nospeech_nomel.xml");
	    	OfflineSpeechRecognizer speechRecognizer = new OfflineSpeechRecognizer(20, 10);
	//    	
	    	waveImporterForOfflineSpeechRecognition.registerObserver(speechRecognizer);
	//    	
	    	waveImporterForOfflineSpeechRecognition.process();
	    	waveImporterForOfflineSpeechRecognition.done();   	
	        

	        speeches = speechRecognizer.findSpeechParts();
        }
        
        Text text = new Text(new TextImporter(inputTextPath), speeches.getTotalTime());
        String rawText = join(text.getWords(), " ");
        new DictionaryGenerator(text, new GraphemesToRussianPhonemesConverter()).store(dictTempPath);
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(inputWavePath));
    	ArrayList<AudioLabel> results = new Aligner().align(acousticModel, dictionary, stream, rawText);
//    	ArrayList<AudioLabel> results = new PauseBasedAligner(acousticModel, dictionary).align(stream, text, speeches);
    	new AudacityLabelsExporter(outputPath).export(results.toArray(new AudioLabel[0]));
    	stream.close();
    }

	private static String join(String[] words, String delimiter)
	{
		if (words.length == 0) return "";
		String res = words[0];
		for (int i = 1; i < words.length; ++i) res += delimiter + words[i];
		return res;
	}
}
