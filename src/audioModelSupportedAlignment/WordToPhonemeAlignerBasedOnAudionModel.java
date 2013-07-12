package audioModelSupportedAlignment;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;


import sphinx.GrammarAligner;

import common.AudioChunkExtractor;
import common.AudioLabel;
import common.Text;
import dataExporters.LinesExporter;
import edu.cmu.sphinx.decoder.search.Token;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.result.WordResult;
import edu.cmu.sphinx.util.props.PropertyException;
import graphemesToPhonemesConverters.DictionaryGenerator;
import graphemesToPhonemesConverters.GraphemesToRussianPhonemesConverter;


public class WordToPhonemeAlignerBasedOnAudionModel
{
	private AudioChunkExtractor chunker = null;
	private URL acousticModel;
	private final static String tempDict = "word_to_phoneme_align.dict.temp";
	private double frameSize = 0;

	public WordToPhonemeAlignerBasedOnAudionModel(AudioInputStream audio, URL acousticModel)
	{
		this.frameSize = (audio.getFormat().getSampleRate() / audio.getFormat().getSampleSizeInBits());
		this.chunker = new AudioChunkExtractor(audio);
		this.acousticModel = acousticModel;
	}
	
	public ArrayList<AudioLabel> align2(AudioLabel word) throws PropertyException, IOException
	{
		if (word.getLabel().length() < 4) {
			ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
			ret.add(word);
			return ret;
		}
		HashMap<String, String[]> dict = new DictionaryGenerator(
				new Text(word.getLabel(), 0),
				new GraphemesToRussianPhonemesConverter()
			).getDictionary();
		String[] phonemes = dict.get(word.getLabel())[0].split(" ");
		ArrayList<WordResult> phonemeMatching = scoreForPhonemeSeq(word, phonemes);
		
		ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
		for (WordResult wresult : phonemeMatching) {
			double start = (double)wresult.getStartFrame() / frameSize + word.getStart();
    		double end = (double)wresult.getEndFrame() / frameSize + word.getStart();
    		if (start > end) end = word.getEnd();
			ret.add(new AudioLabel(wresult.getPronunciation().getWord().toString(), start, end));
		}
		return ret;
	}
	public ArrayList<AudioLabel> align(AudioLabel word) throws PropertyException, IOException
	{
		return splitToPhonemes(word);
	}
	
	private ArrayList<AudioLabel> splitToPhonemes(AudioLabel word) throws PropertyException, IOException
	{
		ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
		HashMap<String, String[]> dict = new DictionaryGenerator(
				new Text(word.getLabel(), 0),
				new GraphemesToRussianPhonemesConverter()
			).getDictionary();
		String[] phonemes = dict.get(word.getLabel())[0].split(" ");
		if (word.getLabel().length() < 4){
			ret.add(new AudioLabel(dict.get(word.getLabel())[0], word.getStart(), word.getEnd()));
			return ret;
		}

		double start = word.getStart();
		double end = word.getEnd();
		
		int div = 2;
		
		double[] starts = new double[phonemes.length / div];
		double[] ends = new double[phonemes.length / div];

		System.err.println(word.getLabel());
		for (int i = 1; i < phonemes.length / div; ++i) {
			AudioInputStream stream = chunker.extract(start, end);
			new LinesExporter(tempDict).export(createDict(phonemes, i * div));
			GrammarAligner aligner = new GrammarAligner(acousticModel, new URL("file:" + tempDict), null);
			Result result = aligner.align(stream, createText(phonemes, i * div));
			if ((result == null) || (result.getWords().size() < 2)) {
				ret.add(new AudioLabel(dict.get(word.getLabel())[0], word.getStart(), word.getEnd()));
				return ret;
			}
			ArrayList<AudioLabel> words = new ArrayList<AudioLabel>();
			for (WordResult wresult : result.getWords()) {
				String wordPart = wresult.getPronunciation().getWord().getSpelling();
				if (wordPart.equals("<sil>")) continue;
				double partStart = wresult.getStartFrame() / frameSize + word.getStart();
				double partEnd = wresult.getEndFrame() / frameSize + word.getStart();
				words.add(new AudioLabel(wordPart, partStart, partEnd));
			}
			if (words.size() < 2) {
				ret.add(new AudioLabel(dict.get(word.getLabel())[0], word.getStart(), word.getEnd()));
				return ret;
			}

			if (2 * i < phonemes.length / 2) {
				starts[i] = words.get(1).getStart();
				ends[i - 1] = words.get(1).getStart();
			} else {
				starts[i] = words.get(0).getEnd();
				ends[i - 1] = words.get(0).getEnd();
			}
		}
		starts[0] = word.getStart();
		ends[ends.length - 1] = word.getEnd();
		
		for (int i = 0; i < phonemes.length / 2; ++i) {
			String phonemeSeq = phonemes[2 * i] + phonemes[2 * i + 1];
			if ((i + 1 >= phonemes.length / 2) && (2 * (i + 1) < phonemes.length))
				phonemeSeq += phonemes[2 * i + 2];
			ret.add(new AudioLabel(phonemeSeq, starts[i], ends[i]));
		}
		
		return ret;
	}

	private String createText(String[] phonemes, int i)
	{
		return join(phonemes, "", 0, i) + " " + join(phonemes, "", i, phonemes.length);
	}

	private String[] createDict(String[] phonemes, int i)
	{
		String first = join(phonemes, "", 0, i) + " " + join(phonemes, " ", 0, i);
		String second = join(phonemes, "", i, phonemes.length) + " " + join(phonemes, " ", i, phonemes.length);
		return new String[]{first, second};
	}
	
	String join(String[] strs, String delimiter, int from, int to)
	{
		if (to - from <= 0) return "";
		String ret = "";
		for (int i = from; i < to; ++i) {
			ret += delimiter + strs[i];
		}
		return ret.substring(delimiter.length());
	}

	private ArrayList<WordResult> scoreForPhonemeSeq(AudioLabel word, String[] phonemeSeq) throws PropertyException, IOException
	{
		double start = word.getStart();
		double end = word.getEnd();
		AudioInputStream stream = chunker.extract(start, end);
		int div = 2;
		
		String[] phonemeDict = new String[phonemeSeq.length / div];
		String textToAlign = "";
		for (int i = div - 1; i < phonemeSeq.length; i += div) {
			String temp = "";
			String phonemes = "";
			for (int j = i - div + 1; j <= i; ++j) {
				temp += phonemeSeq[j];
				phonemes += " " + phonemeSeq[j];
			}
			if (i + div >= phonemeSeq.length) {
				for (int j = i + 1; j < phonemeSeq.length; ++j) {
					temp += phonemeSeq[j];
					phonemes += " " + phonemeSeq[j];
				}
			}
			textToAlign += " " + temp;
			phonemeDict[i / div] = temp + phonemes;
		}
		new LinesExporter(tempDict).export(phonemeDict);
		GrammarAligner aligner = new GrammarAligner(acousticModel, new URL("file:" + tempDict), null);
		Result result = aligner.align(stream, textToAlign.substring(1));
		if ((result == null) || (calculateScore(result) == 0)) return new ArrayList<WordResult>();
		return result.getWords();
	}

	private String join(String[] strings, String delimiter)
	{
		String ret = "";
		for (String str : strings) ret += delimiter + str;
		return ret.substring(delimiter.length());
	}

	private double calculateScore(Result result)
	{
		if (result == null) return Double.MAX_VALUE;
		double ret = 0;
		for (Token token : result.getResultTokens())
		{
			if (token == null) continue;
			ret += token.getScore();
		}
		return ret;
	}
}
