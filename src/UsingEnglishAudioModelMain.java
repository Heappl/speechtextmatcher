import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import edu.cmu.sphinx.result.WordResult;
import sphinx.Aligner;



public class UsingEnglishAudioModelMain
{
    public static void main(String[] args) throws Exception
    {
    	String inputWavePath = args[0];
    	String inputTextPath = args[1];
    	String outputPath = args[2];
    	String acousticModelResource = "resource:/WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz";
    	String dictTempPath = args[2] + ".temp";
    	
    	Text text = new Text(new TextImporter(inputTextPath), 10);

        new NaiveDictionaryGenerator(text).store(dictTempPath);
    	
    	ArrayList<WordResult> results = Aligner.align(
    			new String[]{acousticModelResource, dictTempPath, inputWavePath, inputTextPath});
    	
    	File outputFile = new File(outputPath);
		OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(outputFile));
        for (WordResult result : results) {
        	outputStream.write(((double)result.getStartFrame() / 1000)
                    + " " + ((double)result.getEndFrame() / 1000)
                    + " " + result.getPronunciation().getWord() + "\n");
        }
		outputStream.close();
    }
}
