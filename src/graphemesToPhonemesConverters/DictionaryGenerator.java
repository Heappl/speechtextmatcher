package graphemesToPhonemesConverters;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import common.Text;

public class DictionaryGenerator
{
	HashMap<String, String[]> wordMap = new HashMap<String, String[]>();
	
	public DictionaryGenerator(Text text, IWordToPhonemesConverter converter)
	{
		for (String word : text.getWords()) {
			wordMap.put(word, converter.convert(word.toLowerCase().replace("'", "")).toArray(new String[0]));
		}
	}
	
	public HashMap<String, String[]> getDictionary()
	{
		return wordMap;
	}
	
	public boolean store(String outputFilePath)
	{
		try {
			File outputFile = new File(outputFilePath);
			OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(outputFile));
			for (String key : wordMap.keySet()) {
				String[] aux = wordMap.get(key);
				int count = 1;
        		for (String phonemes : aux) {
        			outputStream.write(key + ((count > 1) ? "(" + count + ")" : "") + "			" + phonemes + "\n");
        			++count;
        		}
			}
			outputStream.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
