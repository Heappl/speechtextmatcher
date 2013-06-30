package audioModelSupportedAlignment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;

import sphinx.GrammarAligner;

import common.AudioChunkExtractor;
import common.AudioLabel;
import common.Speech;
import common.Speeches;
import common.Text;
import edu.cmu.sphinx.decoder.search.Token;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.result.WordResult;
import edu.cmu.sphinx.util.props.PropertyException;

public class PauseBasedAligner {

	URL acousticModel;
	URL dictionary;

	public PauseBasedAligner(URL acousticModel, URL dictionary)
	{
		this.acousticModel = acousticModel;
		this.dictionary = dictionary;
	}

	public ArrayList<AudioLabel> align(AudioInputStream stream, Text text, Speeches speeches) throws PropertyException, MalformedURLException
	{
        ArrayList<AudioLabel> results = new ArrayList<AudioLabel>();
        AudioChunkExtractor audioChunker = new AudioChunkExtractor(stream);
        
        String[] words = text.getWords();
        int startingWord = 0;
        double prevSpeechEndTime = 0;
        for (Speech speech : speeches) {
        	double chunkStartTime = prevSpeechEndTime + 0.2;
        	double chunkEndTime = speech.getEndTime();
        	double chunkTime = chunkEndTime - chunkStartTime;
        	System.err.println("extracting from " + chunkStartTime + " to " + chunkEndTime);
        	
        	int initialNumberOfWords = calculateTnitialNumberOfWords(chunkTime, words, text.getEstimatedTimePerCharacter());
        	int neigh = 2 * (int)Math.ceil(Math.sqrt(initialNumberOfWords));
        	int fromNumOfWords = Math.max(1, initialNumberOfWords - neigh);
        	int toNumOfWords = Math.min(words.length - startingWord, initialNumberOfWords + neigh);
        	String textCandidate = joinWords(startingWord, fromNumOfWords, words);
        	
        	double bestScore = Double.MAX_VALUE;
        	ArrayList<WordResult> bestWordResult = new ArrayList<WordResult>();
        	for (int i = fromNumOfWords; i < toNumOfWords; ++i) {
                GrammarAligner aligner = new GrammarAligner(acousticModel, dictionary, null);
            	AudioInputStream streamChunk = audioChunker.extract(chunkStartTime, chunkEndTime);
        		Result result = aligner.align(streamChunk, textCandidate.toLowerCase());
        		if (result == null) {
        			System.err.println("NULL RESULT");
        			break;
        		}
        		double candidateScore = calculateScore(result);
        		System.err.println(candidateScore);
        		if (candidateScore < bestScore) {
        			bestWordResult = result.getWords();
        			bestScore = candidateScore;
        		}
        		textCandidate += " " + words[startingWord + i];
        	}
        	int added = 0;
        	for (WordResult result : bestWordResult) {
	        	String nextWord = result.getPronunciation().getWord().toString();
	    		double frameSize = (stream.getFormat().getSampleRate() / stream.getFormat().getSampleSizeInBits());
	    		double start = (double)result.getStartFrame() / frameSize + chunkStartTime;
	    		double end = (double)result.getEndFrame() / frameSize + chunkStartTime;
	    		if (nextWord.equalsIgnoreCase("<sil>")) continue;
	    		AudioLabel label = new AudioLabel(nextWord, start, end);
	    		results.add(label);
	    		++added;
        	}
        	if (added == 0) break;
        	startingWord += added;
        	prevSpeechEndTime = speech.getEndTime() + 0.1;
        }
        
		return results;
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

	private String joinWords(int start, int numOfWords, String[] words)
	{
		String ret = "";
		for (int i = start; i < start + numOfWords; ++i) ret += " " + words[i];
		return ret.substring(1);
	}

	private int calculateTnitialNumberOfWords(double chunkTime, String[] words, double timePerCharacter)
	{
		double bestTime = 0;
		double time = 0;
		int bestNumOfWords = 0;
		for (int i = 0; i < words.length; ++i) {
			time += words[i].length() * timePerCharacter;
			if (Math.abs(time - chunkTime) < Math.abs(bestTime - chunkTime)) {
				bestTime = time;
				bestNumOfWords = i;
			}
			if ((i + 1 < words.length) && (time > chunkTime))
				break;
		}
		return bestNumOfWords;
	}
}
