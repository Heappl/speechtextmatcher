import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;

import audioModelSupportedAlignment.NaiveDictionaryGenerator;

import sphinx.GrammarAligner;

import common.AudioChunkExtractor;
import common.AudioLabel;
import common.Text;
import dataExporters.LinesExporter;
import edu.cmu.sphinx.decoder.search.Token;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.result.WordResult;
import edu.cmu.sphinx.util.props.PropertyException;


public class WordToPhonemeAligner
{
	private AudioChunkExtractor chunker = null;
	private URL acousticModel;
	private final static String tempDict = "word_to_phoneme_align.dict.temp";
	private double frameSize = 0;

	public WordToPhonemeAligner(AudioInputStream audio, URL acousticModel)
	{
		this.frameSize = (audio.getFormat().getSampleRate() / audio.getFormat().getSampleSizeInBits());
		this.chunker = new AudioChunkExtractor(audio);
		this.acousticModel = acousticModel;
	}
	
	public ArrayList<AudioLabel> align(AudioLabel word) throws PropertyException, MalformedURLException
	{
		HashMap<String, String[]> dict = new NaiveDictionaryGenerator(new Text(word.getLabel(), 0)).getDictionary();
		String[] phonemes = dict.get(word.getLabel())[0].split(" ");
		ArrayList<WordResult> phonemeMatching = scoreForPhonemeSeq(word, phonemes);
		System.err.println(phonemeMatching.size());
		
		ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
		for (WordResult wresult : phonemeMatching) {
			double start = (double)wresult.getStartFrame() / frameSize + word.getStart();
    		double end = (double)wresult.getEndFrame() / frameSize + word.getStart();
			ret.add(new AudioLabel(wresult.getPronunciation().getWord().toString(), start, end));
		}
		return ret;
	}

	private ArrayList<WordResult> scoreForPhonemeSeq(AudioLabel word, String[] phonemeSeq) throws PropertyException, MalformedURLException
	{
		double start = word.getStart();
		double end = word.getEnd();
		AudioInputStream stream = chunker.extract(start, end);
		String[] phonemeDict = new String[phonemeSeq.length];
		for (int i = 0; i < phonemeDict.length; ++i)
			phonemeDict[i] = phonemeSeq[i] + " " + phonemeSeq[i];
		new LinesExporter(tempDict).export(phonemeDict);
		GrammarAligner aligner = new GrammarAligner(acousticModel, new URL("file:" + tempDict), null);
		Result result = aligner.align(stream, join(phonemeSeq, " "));
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
