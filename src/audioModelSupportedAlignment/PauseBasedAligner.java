package audioModelSupportedAlignment;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;

import sphinx.GrammarAligner;

import common.AudioLabel;
import common.Speech;
import common.Speeches;
import common.Text;
import common.algorithms.AudioChunkExtractor;
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
	
	public ArrayList<AudioLabel> align(AudioInputStream stream, Text text, Speeches speeches) throws PropertyException, IOException
	{
        ArrayList<AudioLabel> results = new ArrayList<AudioLabel>();
        AudioChunkExtractor audioChunker = new AudioChunkExtractor(stream);
        
        String[] sentences = text.getSentences();
        int startingSentence = 0;
        double prevSpeechEndTime = 0;
        for (Speech speech : speeches) {
        	double chunkStartTime = prevSpeechEndTime;
        	double chunkEndTime = speech.getEndTime() + 0.2;
        	System.err.println("extracting from " + chunkStartTime + " to " + chunkEndTime);
        	
        	
        	BestResult bestResult = findBestCandidate(
        		startingSentence,
        		sentences,
        		chunkStartTime,
        		chunkEndTime,
        		text.getEstimatedTimePerCharacter(),
        		audioChunker);
        	
        	if (bestResult.bestLength == 0) continue;
        	for (WordResult result : bestResult.words) {
	        	String nextWord = result.getPronunciation().getWord().toString();
	    		double frameSize = (stream.getFormat().getSampleRate() / stream.getFormat().getSampleSizeInBits());
	    		double start = (double)result.getStartFrame() / frameSize + chunkStartTime;
	    		double end = (double)result.getEndFrame() / frameSize + chunkStartTime;
	    		if (nextWord.equalsIgnoreCase("<sil>")) continue;
	    		if (result.getEndFrame() < result.getStartFrame()) {
	    			end = chunkEndTime - 0.1;
	    			System.err.println("END OF FRAME " + (end - start));
	    		}
	    		AudioLabel label = new AudioLabel(nextWord, start, end);
	    		results.add(label);
        	}
        	startingSentence += bestResult.bestLength;
        	prevSpeechEndTime = results.get(results.size() - 1).getEnd() + 0.1;
        }
        
		return results;
	}
	
	class BestResult
	{
		int bestLength;
		ArrayList<WordResult> words;
		
		public BestResult(ArrayList<WordResult> words, int bestLength)
		{
			this.words = words;
			this.bestLength = bestLength;
		}
	}
	
	private BestResult findBestCandidate(
			int startIndex,
			String[] chunks,
			double chunkStartTime,
			double chunkEndTime,
			double timePerChar,
			AudioChunkExtractor audioChunker) throws PropertyException, IOException
	{
		double chunkTime = chunkEndTime - chunkStartTime;
		int initialNumberOfWords = calculateTnitialLength(chunkTime, chunks, timePerChar);
		int neigh = (int)Math.ceil(Math.sqrt(chunks.length));
		int fromLength = Math.max(1, initialNumberOfWords - neigh);
		int toLength = Math.min(chunks.length - startIndex - 1, initialNumberOfWords + neigh);
		String textCandidate = join(startIndex, fromLength, chunks);
		
//		System.err.println(fromLength + " " + toLength);
		ArrayList<WordResult> bestWordResult = new ArrayList<WordResult>();
		int bestLength = 0;
		boolean seenNonZero = false;
		for (int i = fromLength; i <= toLength; ++i) {
	        GrammarAligner aligner = new GrammarAligner(acousticModel, dictionary, null);
	    	AudioInputStream streamChunk = audioChunker.extract(chunkStartTime, chunkEndTime);
			Result result = aligner.align(streamChunk, textCandidate.toLowerCase());
			if (result == null) {
				System.err.println("NULL RESULT");
				break;
			}
			double candidateScore = calculateScore(result);
//			System.err.println(candidateScore);
			if (candidateScore != 0) {
				bestWordResult = result.getWords();
				bestLength = i;
				seenNonZero = true;
			} else if (seenNonZero || (i > initialNumberOfWords + 1)) break;
			textCandidate += " " + chunks[startIndex + i];
		}
		return new BestResult(bestWordResult, bestLength);
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

	private String join(int start, int numOfWords, String[] words)
	{
		String ret = "";
		for (int i = start; i < start + numOfWords; ++i) ret += " " + words[i];
		return ret.substring(1);
	}

	private int calculateTnitialLength(double chunkTime, String[] chunks, double timePerCharacter)
	{
		double bestTime = 0;
		double time = 0;
		int bestNumOfChunks = 0;
		for (int i = 0; i < chunks.length; ++i) {
			time += chunks[i].length() * timePerCharacter;
			if (Math.abs(time - chunkTime) < Math.abs(bestTime - chunkTime)) {
				bestTime = time;
				bestNumOfChunks = i;
			}
			if ((i + 1 < chunks.length) && (time > chunkTime))
				break;
		}
		return bestNumOfChunks;
	}
}
