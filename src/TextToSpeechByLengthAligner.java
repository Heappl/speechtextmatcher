import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;


public class TextToSpeechByLengthAligner {

	AudioLabel[] findMatching(Text text, Speeches speeches)
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
        
        //matchingScores[i][j] - best matching when we matched `i` speeches and `j` sentences 
        //however we only need previous (for `i - 1`) results 
        double[] matchingScores = new double[sentences.length + 1];
        int[][] matchingIndexes = new int[speeches.size()][sentences.length + 1];
        double[][] estimates = new double[sentences.length][sentences.length + 1];
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
        for (int i = 0; i < sentences.length; ++i)
        {
        	double time = speeches.get(0).getTime();
        	double auxEst = (estimates[i][0] * 10.0 - time * 10.0);
        	matchingScores[i + 1] = auxEst * auxEst;
        	matchingIndexes[0][i + 1] = 0;
        }
        matchingScores[0] = speeches.get(0).getTime() * speeches.get(0).getTime();
        matchingIndexes[0][0] = -1;
        
        for (int i = 1; i < speeches.size(); ++i)
        {
        	double time = speeches.get(i).getTime();
        	double[] newMatchingScores = new double[sentences.length];
        	for (int j = 0; j < sentences.length; ++j)
        	{
        		newMatchingScores[j] = Double.MAX_VALUE;
        		for (int k = 0; k <= j; ++k)
        		{
        			double prevScore = matchingScores[j - k];
        			double auxDiff = time * 10.0 - estimates[j][j - k + 1] * 10.0;
        			double diff = auxDiff * auxDiff;
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
			for (int j = lastMatching; j <= matching[i]; ++j) label += ". " + sentences[j];
			
			while ((i + 1 < matching.length) && (matching[i + 1] < matching[i] + 1)) {
				++i;
				end = speeches.get(i).getEndTime();
			}
			
			labels.add(new AudioLabel(label, startx, end));
			lastMatching = matching[i] + 1;
		}
		return labels.toArray(new AudioLabel[0]);
	}
}
