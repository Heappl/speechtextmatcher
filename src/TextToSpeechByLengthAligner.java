import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;


public class TextToSpeechByLengthAligner {

	AudioLabel[] findMatching(String text, ArrayList<Speech> speechTimes)
	{
		double wholeTime = 0;
        for (Speech elem : speechTimes) wholeTime += elem.getTime();
        text = text.replaceAll("\n\\s*\n", ".").replaceAll("[\\s]+", " ");
        SortedSet<Character> chars = new TreeSet<Character>();
        for (int i = 0; i < text.length(); ++i)
        	if (Character.isLetterOrDigit(text.charAt(i)))
        		chars.add(text.charAt(i));
        String charRegex = "";
        for (Character c : chars) charRegex += c;
        String[] sentences = text.split("\\s*[^" + charRegex + "'\\s]+\\s*");
        String[] words = text.split("[^" + charRegex + "']+");
        int totalChars = 0;
        for (String word : words) totalChars += word.length();
        System.err.println("sentences: " + sentences.length);
        
        double timePerChar = wholeTime / totalChars;
        double timePerWord = wholeTime / words.length;
        double[] estimatedTimes = new double[sentences.length];
        for (int i = 0; i < sentences.length; ++i)
        {
        	String[] sentWords = sentences[i].split(" ");
        	int noChars = 0;
        	for (String word : sentWords) noChars += word.length();
        	estimatedTimes[i] = (noChars * timePerChar + sentWords.length * timePerWord) / 2.0;
        }
        
        //matchingScores[i][j] - best matching when we matched `i` speeches and `j` sentences 
        //however we only need previous (for `i - 1`) results 
        double[] matchingScores = new double[sentences.length + 1];
        int[][] matchingIndexes = new int[speechTimes.size()][sentences.length + 1];
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
        	double time = speechTimes.get(0).getTime();
        	double auxEst = (estimates[i][0] - time);
        	matchingScores[i + 1] = auxEst * auxEst;
        	matchingIndexes[0][i + 1] = 0;
        }
        matchingScores[0] = speechTimes.get(0).getTime() * speechTimes.get(0).getTime();
        matchingIndexes[0][0] = -1;
        
        for (int i = 1; i < speechTimes.size(); ++i)
        {
        	double time = speechTimes.get(i).getTime();
        	double[] newMatchingScores = new double[sentences.length];
        	for (int j = 0; j < sentences.length; ++j)
        	{
        		newMatchingScores[j] = Double.MAX_VALUE;
        		for (int k = 0; k <= j; ++k)
        		{
        			double prevScore = matchingScores[j - k];
        			double auxDiff = time - estimates[j][j - k + 1];
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
        
        int[] matching = new int[speechTimes.size()];
        matching[matching.length - 1] = sentences.length - 1;
        for (int i = speechTimes.size() - 2; i >= 0; --i)
        	matching[i] = matchingIndexes[i + 1][matching[i + 1]];
        
        ArrayList<AudioLabel> labels = new ArrayList<AudioLabel>();
		int lastMatching = 0;
		for (int i = 0; i < matching.length; ++i) {
			double startx = speechTimes.get(i).getStartTime();
			double end = speechTimes.get(i).getEndTime();
			String label = "";
			if (matching[i] >= sentences.length) break;
			for (int j = lastMatching; j <= matching[i]; ++j) label += ". " + sentences[j];
			
			while ((i + 1 < matching.length) && (matching[i + 1] == matching[i])) {
				++i;
				end = speechTimes.get(i).getEndTime();
			}
			
			labels.add(new AudioLabel(label, startx, end));
			lastMatching = matching[i] + 1;
		}
		return labels.toArray(new AudioLabel[0]);
	}
}
