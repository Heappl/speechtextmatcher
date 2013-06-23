package audioSupportedAlignment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import common.Text;



public class NaiveDictionaryGenerator
{
	HashMap<String, String[]> wordMap = new HashMap<String, String[]>();
	String[] phonemes = new String[]{
		"AA", "AE", "AH", "AO", "AW", "AY", "B", "CH", "D", "DH", "EH", "ER", "EY", "F", "G", "HH",
		"IH", "IY", "JH", "K", "L", "M", "N", "NG", "OW", "OY", "P", "R", "S", "SH", "T", "TH", "UH",
		"UW", "V", "W", "Y", "Z", "ZH"
	};
	String[][][] graphemes = new String[][][]{
			{{"a"}, {"AA", "AH"}},
			{{"ą"}, {"AW", "OW"}},
			{{"b"}, {"B"}},
			{{"bi"}, {"B Y"}},
			{{"c"}, {"T S"}},
			{{"cz"}, {"CH"}},
			{{"ć"}, {"CH", "CH Y"}},
			{{"d"}, {"D"}},
			{{"d"}, {"D Y"}},
			{{"dz"}, {"T S", "Z"}},
			{{"dż"}, {"JH"}},
			{{"dź"}, {"JH", "JH Y"}},
			{{"dzi"}, {"JH", "JH Y"}},
			{{"e"}, {"EH"}},
			{{"ę"}, {"ER W", "EH W"}},
			{{"f"}, {"F"}},
			{{"fi"}, {"F Y"}},
			{{"g"}, {"G"}},
			{{"h"}, {"HH"}},
			{{"ch"}, {"HH"}},
			{{"hi"}, {"HH Y"}},
			{{"chi"}, {"HH Y"}},
			{{"i"}, {"IH"}},
			{{"ij"}, {"IY", "IH Y"}},
			{{"j"}, {"Y"}},
			{{"k"}, {"K"}},
			{{"ki"}, {"K Y"}},
			{{"l"}, {"L"}},
			{{"li"}, {"L Y"}},
			{{"ł"}, {"W"}},
			{{"m"}, {"M"}},
			{{"mi"}, {"M Y"}},
			{{"n"}, {"N"}},
			{{"ni"}, {"N Y"}},
			{{"ń"}, {"N Y"}},
			{{"o"}, {"AO"}},
			{{"ó"}, {"UH"}},
			{{"p"}, {"P"}},
			{{"pi"}, {"P Y"}},
			{{"r"}, {"R"}},
			{{"ri"}, {"R Y"}},
			{{"rz"}, {"ZH"}},
			{{"s"}, {"S"}},
			{{"si"}, {"SH"}},
			{{"sz"}, {"SH"}},
			{{"ś"}, {"SH", "SH Y"}},
			{{"t"}, {"T"}},
			{{"ti"}, {"T Y"}},
			{{"u"}, {"UH"}},
			{{"v"}, {"F", "V"}},
			{{"w"}, {"V"}},
			{{"wi"}, {"V Y"}},
			{{"y"}, {"IH"}},
			{{"z"}, {"Z"}},
			{{"zi"}, {"Z Y"}},
			{{"ż"}, {"ZH"}},
			{{"ź"}, {"Z Y"}},
			{{"ü"}, {"UH"}}
	};
	
	public NaiveDictionaryGenerator(Text text)
	{
		for (String word : text.getWords()) {
			wordMap.put(word, generate(word.toLowerCase().replace("'", "")).toArray(new String[0]));
		}
	}
	
	boolean store(String outputFilePath)
	{
		try {
			File outputFile = new File(outputFilePath);
			OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(outputFile));
			for (String key : wordMap.keySet()) {
				String[] aux = wordMap.get(key);
				int count = 1;
        		for (String phonemes : aux) {
        			outputStream.write(key + ((count > 1) ? count : "") + "			" + phonemes + "\n");
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
	
	private ArrayList<String> generate(String word)
	{
		if (word.isEmpty()) {
			ArrayList<String> out = new ArrayList<String>();
			out.add("");
			return out;
		}
		String biggest = "";
		String[] phonemes = null;
		for (String[][] grapheme : graphemes) {
			if (word.startsWith(grapheme[0][0]) && (biggest.length() < grapheme[0][0].length())) {
				biggest = grapheme[0][0];
				phonemes = grapheme[1];
			}
		}
		if (biggest.isEmpty()) {
			throw new IllegalArgumentException("->" + word + "<-");
		}
		
		ArrayList<String> suffixPhonemes = generate(word.substring(biggest.length()));
		
		ArrayList<String> out = new ArrayList<String>();
		for (String phoneme : phonemes) {
			for (String suffix : suffixPhonemes) {
				out.add(phoneme + (suffix.isEmpty() ? "" : " ") + suffix);
			}
		}
		return out;
	}
}
