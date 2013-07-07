package graphemesToPhonemesConverters;

import java.util.ArrayList;

public class NaiveGraphemesToPhonemesConverter implements IWordToPhonemesConverter
{
	String[][][] graphemes = null;
	
	protected NaiveGraphemesToPhonemesConverter(String[][][] graphemes)
	{
		this.graphemes = graphemes;
	}
	
	public ArrayList<String> convert(String word)
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
		
		ArrayList<String> suffixPhonemes = convert(word.substring(biggest.length()));
		
		ArrayList<String> out = new ArrayList<String>();
		for (String phoneme : phonemes) {
			for (String suffix : suffixPhonemes) {
				out.add(phoneme + (suffix.isEmpty() ? "" : " ") + suffix);
			}
		}
		return out;
	}
}
