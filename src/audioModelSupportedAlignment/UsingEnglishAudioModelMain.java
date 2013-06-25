package audioModelSupportedAlignment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;


import common.AudioLabel;
import common.Text;
import dataExporters.AudacityLabelsExporter;
import dataProducers.TextImporter;


import edu.cmu.sphinx.result.WordResult;
import sphinx.GrammarAligner;



public class UsingEnglishAudioModelMain
{
	public static URL audioModelUrl() throws MalformedURLException
	{
    	URL[] urls = new URL[]{new File("sphinx/lib/WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz.jar").toURI().toURL()};
    	URLClassLoader classLoader = new URLClassLoader(urls);
    	return classLoader.findResource("WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz");
	}
	
    public static void main(String[] args) throws Exception
    {
    	String inputWavePath = args[0];
    	String inputTextPath = args[1];
    	String outputPath = args[2];
    	
        URL acousticModel = audioModelUrl();
    	String dictTempPath = args[2] + ".temp";
        URL dictionary = new URL("file:" + dictTempPath);

        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(inputWavePath));
        Text text = new Text(new TextImporter(inputTextPath), 1);
        String rawText = join(text.getWords(), " ");

        new NaiveDictionaryGenerator(text).store(dictTempPath);
    	
    	ArrayList<AudioLabel> results = Aligner.align(acousticModel, dictionary, stream, rawText);
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
