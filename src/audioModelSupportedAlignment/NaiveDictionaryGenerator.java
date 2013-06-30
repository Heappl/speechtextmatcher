package audioModelSupportedAlignment;
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
	String[][][] russianGraphemes = new String[][][]{
			{{"a"}, {"aa"}},
			{{"ą"}, {"oo l"}},
			{{"b"}, {"b"}},
			{{"c"}, {"c"}},
			{{"ci"}, {"ch ii"}},
			{{"cia"}, {"ch aa"}},
			{{"cią"}, {"ch oo l"}},
			{{"cie"}, {"ch ee"}},
			{{"cię"}, {"ch ee l"}},
			{{"cio"}, {"ch oo"}},
			{{"ció"}, {"ch u"}},
			{{"ciu"}, {"ch u"}},
			{{"ć"}, {"ch"}},
			{{"cz"}, {"t sh"}},
			{{"d"}, {"d"}},
			{{"di"}, {"dd ii"}},
			{{"dia"}, {"dd aa"}},
			{{"dią"}, {"dd oo l"}},
			{{"die"}, {"dd ee"}},
			{{"dię"}, {"dd ee l"}},
			{{"dio"}, {"dd oo"}},
			{{"dió"}, {"dd u"}},
			{{"diu"}, {"dd u"}},
			{{"dz"}, {"d z"}},
			{{"dzi"}, {"d zz ii"}},
			{{"dzia"}, {"d zz aa"}},
			{{"dzią"}, {"d zz oo l"}},
			{{"dzie"}, {"d zz ee"}},
			{{"dzię"}, {"d zz ee l"}},
			{{"dzio"}, {"d zz oo"}},
			{{"dzió"}, {"d zz u"}},
			{{"dziu"}, {"d zz u"}},
			{{"dż"}, {"d zh"}},
			{{"dź"}, {"d zh"}},
			{{"e"}, {"e"}},
			{{"ę"}, {"e l"}},
			{{"f"}, {"f"}},
			{{"fi"}, {"ff ii"}},
			{{"fia"}, {"ff aa"}},
			{{"fią"}, {"ff oo l"}},
			{{"fie"}, {"ff ee"}},
			{{"fię"}, {"ff ee l"}},
			{{"fio"}, {"ff oo"}},
			{{"fió"}, {"ff u"}},
			{{"fiu"}, {"ff u"}},
			{{"g"}, {"g"}},
			{{"gi"}, {"gg ii"}},
			{{"gia"}, {"gg aa"}},
			{{"gią"}, {"gg oo l"}},
			{{"gie"}, {"gg ee"}},
			{{"gię"}, {"gg ee l"}},
			{{"gio"}, {"gg oo"}},
			{{"gió"}, {"gg u"}},
			{{"giu"}, {"gg u"}},
			{{"h"}, {"h"}},
			{{"hi"}, {"hh ii"}},
			{{"hia"}, {"hh aa"}},
			{{"hią"}, {"hh oo l"}},
			{{"hie"}, {"hh ee"}},
			{{"hię"}, {"hh ee l"}},
			{{"hio"}, {"hh oo"}},
			{{"hió"}, {"hh u"}},
			{{"hiu"}, {"hh u"}},
			{{"ch"}, {"h"}},
			{{"chi"}, {"hh ii"}},
			{{"chia"}, {"hh aa"}},
			{{"chią"}, {"hh oo l"}},
			{{"chie"}, {"hh ee"}},
			{{"chię"}, {"hh ee l"}},
			{{"chio"}, {"hh oo"}},
			{{"chió"}, {"hh u"}},
			{{"chiu"}, {"hh u"}},
			{{"chrz"}, {"h sh"}},
			{{"chż"}, {"h sh"}},
			{{"i"}, {"i"}},
			{{"j"}, {"j"}},
			{{"k"}, {"k"}},
			{{"ki"}, {"kk ii"}},
			{{"kia"}, {"kk aa"}},
			{{"kią"}, {"kk oo l"}},
			{{"kie"}, {"kk ee"}},
			{{"kię"}, {"kk ee l"}},
			{{"kio"}, {"kk oo"}},
			{{"kió"}, {"kk u"}},
			{{"kiu"}, {"kk u"}},
			{{"krz"}, {"k sh"}},
			{{"kż"}, {"k sh"}},
			{{"l"}, {"ll"}},
			{{"ł"}, {"l"}},
			{{"m"}, {"m"}},
			{{"mi"}, {"mm ii"}},
			{{"mia"}, {"mm aa"}},
			{{"mią"}, {"mm oo l"}},
			{{"mie"}, {"mm ee"}},
			{{"mię"}, {"mm ee l"}},
			{{"mio"}, {"mm oo"}},
			{{"mió"}, {"mm u"}},
			{{"miu"}, {"mm u"}},
			{{"n"}, {"n"}},
			{{"ni"}, {"nn ii"}},
			{{"nia"}, {"nn aa"}},
			{{"nią"}, {"nn oo l"}},
			{{"nie"}, {"nn ee"}},
			{{"nię"}, {"nn ee l"}},
			{{"nio"}, {"nn oo"}},
			{{"nió"}, {"nn u"}},
			{{"niu"}, {"nn u"}},
			{{"ń"}, {"nn"}},
			{{"o"}, {"oo", "ay"}},
			{{"ó"}, {"uu"}},
			{{"p"}, {"p"}},
			{{"pi"}, {"pp ii"}},
			{{"pia"}, {"pp aa"}},
			{{"pią"}, {"pp oo l"}},
			{{"pie"}, {"pp ee"}},
			{{"pię"}, {"pp ee l"}},
			{{"pio"}, {"pp oo"}},
			{{"pió"}, {"pp u"}},
			{{"piu"}, {"pp u"}},
			{{"prz"}, {"p sh"}},
			{{"r"}, {"r"}},
			{{"ri"}, {"rr ii"}},
			{{"ria"}, {"rr aa"}},
			{{"rią"}, {"rr oo l"}},
			{{"rie"}, {"rr ee"}},
			{{"rię"}, {"rr ee l"}},
			{{"rio"}, {"rr oo"}},
			{{"rió"}, {"rr u"}},
			{{"riu"}, {"rr u"}},
			{{"rz"}, {"zh"}},
			{{"s"}, {"s"}},
			{{"sia"}, {"sh aa"}},
			{{"sią"}, {"sh oo l"}},
			{{"sie"}, {"sh ee"}},
			{{"się"}, {"sh ee l"}},
			{{"sio"}, {"sh oo"}},
			{{"sió"}, {"sh u"}},
			{{"siu"}, {"sh u"}},
			{{"sz"}, {"sh"}},
			{{"ś"}, {"sh"}},
			{{"t"}, {"t"}},
			{{"ti"}, {"tt ii"}},
			{{"tia"}, {"tt aa"}},
			{{"tią"}, {"tt oo l"}},
			{{"tie"}, {"tt ee"}},
			{{"tię"}, {"tt ee l"}},
			{{"tio"}, {"tt oo"}},
			{{"tió"}, {"tt u"}},
			{{"tiu"}, {"tt u"}},
			{{"trz"}, {"t sh"}},
			{{"tż"}, {"t sh"}},
			{{"u"}, {"u"}},
			{{"v"}, {"v", "f"}},
			{{"w"}, {"v", "f"}},
			{{"y"}, {"y"}},
			{{"z"}, {"z", "ss"}},
			{{"ż"}, {"zh"}},
			{{"ź"}, {"zh"}},
			{{"ü"}, {"u"}}
	};
	String[][][] englishGraphemes = new String[][][]{
			{{"a"}, {"AA"}},
			{{"ą"}, {"AW"}},
			{{"b"}, {"B"}},
			{{"c"}, {"T S"}},
			{{"ci"}, {"CH IH"}},
			{{"cia"}, {"CH Y AA"}},
			{{"cią"}, {"CH Y AW"}},
			{{"cie"}, {"CH Y EH"}},
			{{"cię"}, {"CH Y ER W"}},
			{{"cio"}, {"CH Y AO"}},
			{{"ció"}, {"CH Y UH"}},
			{{"ciu"}, {"CH Y UH"}},
			{{"ć"}, {"CH"}},
			{{"cz"}, {"T CH"}},
			{{"d"}, {"D"}},
			{{"dz"}, {"JH"}},
			{{"dzia"}, {"JH Y AA"}},
			{{"dzią"}, {"JH Y AW"}},
			{{"dzie"}, {"JH Y EH"}},
			{{"dzię"}, {"JH Y ER W"}},
			{{"dzio"}, {"JH Y AO"}},
			{{"dzió"}, {"JH Y UH"}},
			{{"dziu"}, {"JH Y UH"}},
			{{"dż"}, {"JH"}},
			{{"dź"}, {"JH"}},
			{{"e"}, {"EH"}},
			{{"ę"}, {"ER W"}},
			{{"f"}, {"F"}},
			{{"g"}, {"G"}},
			{{"h"}, {"HH"}},
			{{"ch"}, {"HH"}},
			{{"chrz"}, {"HH SH"}},
			{{"i"}, {"IH"}},
			{{"ij"}, {"IY"}},
			{{"ia"}, {"Y AA"}},
			{{"ią"}, {"Y AW"}},
			{{"ie"}, {"Y EH"}},
			{{"ię"}, {"Y ER W"}},
			{{"io"}, {"Y AO"}},
			{{"ió"}, {"Y UH"}},
			{{"iu"}, {"Y UH"}},
			{{"j"}, {"Y"}},
			{{"k"}, {"K"}},
			{{"krz"}, {"K SH"}},
			{{"kż"}, {"K SH"}},
			{{"l"}, {"L"}},
			{{"ł"}, {"W"}},
			{{"m"}, {"M"}},
			{{"n"}, {"N"}},
			{{"ń"}, {"N Y"}},
			{{"o"}, {"AO"}},
			{{"ó"}, {"UH"}},
			{{"p"}, {"P"}},
			{{"prz"}, {"P SH"}},
			{{"pi"}, {"P Y"}},
			{{"r"}, {"R"}},
			{{"rz"}, {"ZH"}},
			{{"s"}, {"S"}},
			{{"sia"}, {"SH AA"}},
			{{"sią"}, {"SH AW"}},
			{{"sie"}, {"SH EH"}},
			{{"się"}, {"SH ER W"}},
			{{"sio"}, {"SH AO"}},
			{{"sió"}, {"SH UH"}},
			{{"siu"}, {"SH UH"}},
			{{"sz"}, {"SH"}},
			{{"ś"}, {"SH"}},
			{{"t"}, {"T"}},
			{{"trz"}, {"T SH"}},
			{{"tż"}, {"T SH"}},
			{{"u"}, {"UH"}},
			{{"v"}, {"F", "V"}},
			{{"w"}, {"V"}},
			{{"y"}, {"IH"}},
			{{"z"}, {"Z"}},
			{{"ż"}, {"ZH"}},
			{{"ź"}, {"ZH"}},
			{{"ü"}, {"UH"}},
			{{"części"}, {"T CH ER W SH CH Y IH"}}
	};
	String[][][] graphemes = russianGraphemes;
	
	public NaiveDictionaryGenerator(Text text)
	{
		for (String word : text.getWords()) {
			wordMap.put(word, generate(word.toLowerCase().replace("'", "")).toArray(new String[0]));
		}
	}
	
	public HashMap<String, String[]> getDictionary()
	{
		return wordMap;
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
