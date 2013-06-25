package audioModelSupportedAlignment;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import common.AudioChunkExtractor;
import common.AudioLabel;
import sphinx.GrammarAligner;
import edu.cmu.sphinx.result.WordResult;

public class Aligner {
    
    static double chunkTime = 1000;

    public static ArrayList<AudioLabel> align(URL acousticModel, URL dictionary, AudioInputStream stream, String text) throws Exception
    {
        
        double totalTime = (double)stream.getFrameLength() / (double)stream.getFormat().getFrameRate();
        double timePerChar = totalTime / text.length();
        System.err.println(totalTime + " " + stream.getFrameLength());
        
        ArrayList<AudioLabel> results = new ArrayList<AudioLabel>();
        AudioChunkExtractor audioChunker = new AudioChunkExtractor(stream);
       
        text = text.toLowerCase();
        double chunkStart = 0;
        int count = 0;
        double prevChunkStart = -1;
        while (!text.isEmpty()) {
        	double endTime = Math.min(chunkStart + chunkTime, totalTime + 5.0);
    		System.err.println("extracting " + chunkStart + " " + endTime);
        	AudioInputStream streamChunk = audioChunker.extract(chunkStart, endTime);
        	if (streamChunk == null) break;
        	int textChunkEnd = Math.min((int)Math.ceil(chunkTime / timePerChar), text.length() - 1);
    		String auxtext = text.substring(0, textChunkEnd);
    		int lastSpace =  auxtext.lastIndexOf(' ');
    		if (lastSpace < 0) lastSpace = auxtext.length();
    		auxtext = auxtext.substring(0, lastSpace);
    		System.err.println(streamChunk.getFormat().getFrameSize() + " " +
    						   streamChunk.getFrameLength() + " " +
    						   auxtext);
            GrammarAligner aligner = new GrammarAligner(acousticModel, dictionary, null);
        	ArrayList<WordResult> partial = aligner.align(streamChunk, auxtext);
        	System.err.println("partial alignment finished " + partial.size());
        	if (partial.isEmpty()) break;
        	int partialPartIndex = Math.min(partial.size() - 1, Math.max(20, partial.size() / 10));
        	double chunkMove = 0;
        	streamChunk.close();
        	
//          	File tempWavFile = new File("/home/bartek/workspace/speechtextmatcher/chunk.temp.wav." + count);
//    		streamChunk = audioChunker.extract(chunkStart, (chunkStart + chunkTime));
//          	System.err.println("saving: " + chunkStart + " " + (chunkStart + chunkTime) + " " + streamChunk.getFrameLength());
//    		AudioSystem.write(streamChunk, AudioFileFormat.Type.WAVE, tempWavFile);
//    		streamChunk.close();
        	
        	ArrayList<AudioLabel> partialLabels = new ArrayList<AudioLabel>();
        	String partialText = text;
        	int added = 0;
        	for (WordResult result : partial) {
        		String nextWord = result.getPronunciation().getWord().toString();
        		double frameSize = (stream.getFormat().getSampleRate() / stream.getFormat().getSampleSizeInBits());
        		double start = (double)result.getStartFrame() / frameSize;
        		double end = (double)result.getEndFrame() / frameSize;
        		AudioLabel label = new AudioLabel(nextWord, start + chunkStart, end + 0.1 + chunkStart);
        		if (!nextWord.equalsIgnoreCase("<sil>")) {
        			partialLabels.add(label);
	        		int index = partialText.indexOf(nextWord);
	        		if (index != 0) break;
	        		partialText = partialText.substring(nextWord.length() + 1);
        		} else if ((end - start > 0.4) || (partialLabels.size() == partial.size())) {
        			results.addAll(partialLabels);
        			added += partialLabels.size();
        			partialLabels = new ArrayList<AudioLabel>();
            		chunkMove = start + 0.1;
            		text = partialText;
            		if (added > partialPartIndex) break;
        		}
        	}
        	prevChunkStart = chunkStart;
        	chunkStart += chunkMove;
        	++count;
        	if (prevChunkStart == chunkStart) break;
        }
        return results;
    } 
}

