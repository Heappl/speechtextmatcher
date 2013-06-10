import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;


public class TextToSpeechByLengthAligner implements ITextToSpeechAligner {

	double[][] estimates = null;
	double averageIsSpeechValue = 0;
	public TextToSpeechByLengthAligner() {}
	
	public AudioLabel[] findMatching(Text text, Speeches speeches)
	{
        String[] sentences = text.getSentences();
        double timePerChar = text.getEstimatedTimePerCharacter();
        double timePerWord = text.getEstimatedTimePerWord();
        System.err.println("sentences: " + sentences.length);
        
        double[] estimatedTimes = new double[sentences.length];
        for (int i = 0; i < sentences.length; ++i)
        {
        	String[] sentWords = sentences[i].split(" ");
        	int noChars = 0;
        	for (String word : sentWords) noChars += word.length();
        	estimatedTimes[i] = noChars * timePerChar * 0.95 + 0.05 * sentWords.length * timePerWord;
        }
        
        //matchingScores[i][j] - best matching when we matched `i` speeches and `j+1` sentences 
        //however we only need previous (for `i - 1`) results
        double[] matchingScores = new double[sentences.length];
        //indexes of previous matchings to recreate whole matching
        //for [i][j] we keep index of previous matching (for i-1) used to obtain current result
        int[][] matchingIndexes = new int[speeches.size()][sentences.length];
        // estimates of time for sentences form i to j (including)
        // with special value [i][i+1] meaning empty estimate
        // for [i][i+k] (k>1) it is invalid so inf
        estimates = new double[sentences.length][sentences.length + 1];
        double totalEstTime = 0;
        for (int i = 0; i < sentences.length; ++i)
        {
        	totalEstTime += estimatedTimes[i];
        	estimates[i][0] = totalEstTime;
        	for (int j = 0; j <= i; ++j)
        		estimates[i][j + 1] = totalEstTime - estimates[j][0];
        	for (int j = i + 1; j < sentences.length; ++j)
        		estimates[i][j + 1] = Double.MAX_VALUE;
        }
        
        //initial state is for matching first speech
        //calculating a difference of matching first speech to i sentences
        for (int i = 0; i < sentences.length; ++i)
        {
        	matchingScores[i] = calculateDiff1(speeches.get(0), sentences, 0, i);
        }
        
        for (int i = 1; i < speeches.size(); ++i)
        {
        	double[] newMatchingScores = new double[sentences.length];
        	for (int j = 0; j < sentences.length; ++j)
        	{
        		newMatchingScores[j] = Double.MAX_VALUE;
        		for (int k = 0; k <= j; ++k)
        		{
        			double prevScore = matchingScores[j - k];
        			double diff = calculateDiff1(speeches.get(i), sentences, j - k + 1, j);
        			double scoreCand = prevScore + diff;
        			if (scoreCand < newMatchingScores[j])
        			{
        				newMatchingScores[j] = scoreCand;
        				matchingIndexes[i][j] = j - k;
        			}
        		}
        	}
        	matchingScores = newMatchingScores;
        }
        
        int[] matching = new int[speeches.size()];
        matching[matching.length - 1] = sentences.length - 1;
        for (int i = speeches.size() - 2; i >= 0; --i)
        	matching[i] = matchingIndexes[i + 1][matching[i + 1]];
        
        ArrayList<AudioLabel> labels = new ArrayList<AudioLabel>();
		int lastMatching = 0;
		for (int i = 0; i < matching.length; ++i) {
			double startx = speeches.get(i).getStartTime();
			double end = speeches.get(i).getEndTime();
			String label = "";
			if (matching[i] >= sentences.length) break;
			for (int j = lastMatching; j <= matching[i]; ++j) label += (label.isEmpty() ? "" : ".") + sentences[j];
			
			while ((i + 1 < matching.length) && (matching[i + 1] < matching[i] + 1)) {
				++i;
				end = speeches.get(i).getEndTime();
			}
			
			labels.add(new AudioLabel(label, startx, end));
			lastMatching = matching[i] + 1;
		}
		return labels.toArray(new AudioLabel[0]);
	}

	private double calculateDiff1(Speech speech, String[] sentences, int s, int e)
	{
		double aux = speech.getTime() * 10 - estimates[e][s] * 10;
		return aux * aux;
	}
}
