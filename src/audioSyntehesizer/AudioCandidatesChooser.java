package audioSyntehesizer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import common.AudioChunkExtractor;
import common.AudioLabel;

public class AudioCandidatesChooser
{
	private AudioChunkExtractor extractor = null;
	private AudioFormat audioFormat = null;
	
	private static final int DIFF_CHECK_SIZE = 30;
	
	public AudioCandidatesChooser(AudioInputStream audio)
	{
		this.extractor = new AudioChunkExtractor(audio);
		this.audioFormat = audio.getFormat();
	}
	
	private class CandidateNeededPart implements Comparable<CandidateNeededPart>
	{
		private AudioLabel audioPart;
		private AudioCandidate owner;
		public CandidateNeededPart(AudioLabel audioPart, AudioCandidate owner)
		{
			this.audioPart = audioPart;
			this.owner = owner;
		}
		
		public int compareTo(CandidateNeededPart other)
		{
			if (audioPart.getStart() < other.audioPart.getStart()) return -1;
			if (audioPart.getStart() > other.audioPart.getStart()) return 1;
			if (audioPart.getEnd() < other.audioPart.getEnd()) return -1;
			if (audioPart.getEnd() > other.audioPart.getEnd()) return 1;
			if (owner.hashCode() < other.owner.hashCode()) return -1;
			if (owner.hashCode() > other.owner.hashCode()) return 1;
			return 0;
		}
	}
	
	ArrayList<AudioInputStream> chooseCandidates(ArrayList<AudioCandidate> candidates) throws IOException
	{
		ArrayList<CandidateNeededPart> neededParts = new ArrayList<CandidateNeededPart>();
		for (AudioCandidate candidate : candidates) {
			for (AudioLabel part : candidate.getNeededParts()) {
				neededParts.add(new CandidateNeededPart(part, candidate));
			}
		}
		Collections.sort(neededParts);
		
		for (CandidateNeededPart part : neededParts) {
			AudioInputStream stream = extractor.extract(part.audioPart.getStart(), part.audioPart.getEnd());
			part.owner.supplyAudioPart(part.audioPart, stream);
		}

		for (AudioCandidate candidate : candidates) {
			candidate.solveInternal();
		}
		return findBestSequence(candidates);
	}

	private ArrayList<AudioInputStream> findBestSequence(ArrayList<AudioCandidate> candidates)
	{
		int maxCandidateSize = 0;
		for (AudioCandidate candidate : candidates) {
			maxCandidateSize = Math.max(maxCandidateSize, candidate.getNumberOfCandidates());
		}
		
		double[] current = new double[maxCandidateSize];
		int[][][] previous = new int[candidates.size()][maxCandidateSize][3];
		for (int i = 1; i < previous.length; ++i)
		{
			double[] next = new double[current.length];
            for (int j = 0; j < next.length; ++j) next[j] = Double.MAX_VALUE;
			for (int j = 0; j < current.length; ++j) {
				if (candidates.get(i - 1).getNumberOfCandidates() <= j) continue;
				for (int k = 0; k < next.length; ++k) {
					if (candidates.get(i).getNumberOfCandidates() <= k) continue;
					AudioMergeDiff diff = calcDiff(
							candidates.get(i - 1).getCandidate(j), candidates.get(i).getCandidate(k));
					if (diff.score < next[j]) {
						next[k] = diff.score;
						previous[i][k][0] = j;
						previous[i][k][1] = diff.endingIndex;
						previous[i][k][2] = diff.startingIndex;
					}
				}
			}
			
			current = next;
		}

		int cand = -1;
		double bestScore = Double.MAX_VALUE;
		for (int i = 0; i < current.length; ++i) {
			if (current[i] < bestScore) {
				cand = i;
				bestScore = current[i];
			}
		}
		
		ArrayList<AudioInputStream> ret = new ArrayList<AudioInputStream>();
		ret.add(createAudio(
				candidates.get(candidates.size() - 1).getCandidate(cand),
				previous[previous.length - 1][cand][2],
				Integer.MAX_VALUE));
		for (int i = 1; i < candidates.size(); ++i) {
		    int prevCand = cand;
			cand = previous[previous.length - i][cand][0];
			ret.add(createAudio(
					candidates.get(candidates.size() - 1 - i).getCandidate(cand),
					previous[previous.length - 1 - i][cand][2],
					previous[previous.length - i][prevCand][1]));
		}
		Collections.reverse(ret);
		return ret;
	}
	
	private AudioInputStream createAudio(byte[] candidate, int start, int end)
	{
	    end = Math.min(candidate.length / audioFormat.getFrameSize(), end);
	    System.err.println(start + " " + end + " " + (double)(end - start) / this.audioFormat.getFrameRate());
		byte[] actualBytes = new byte[(end - start) * this.audioFormat.getFrameSize()];
		for (int i = start; i < end; ++i) {
			for (int j = i * audioFormat.getFrameSize(); j < (i + 1) * audioFormat.getFrameSize(); ++j) {
				actualBytes[j - start * audioFormat.getFrameSize()] = candidate[j];
			}
		}
		return new AudioInputStream(
				new ByteArrayInputStream(actualBytes),
				this.audioFormat,
				actualBytes.length / (audioFormat.getSampleSizeInBits() / Byte.SIZE));
	}

	private class AudioMergeDiff
	{
		double score;
		int endingIndex;
		int startingIndex;
		public AudioMergeDiff(double score, int ending, int start)
		{
			this.score = score;
			this.endingIndex = ending;
			this.startingIndex = start;
		}
	}
	
	private AudioMergeDiff calcDiff(byte[] ending, byte[] starting)
	{
		double bestScore = Double.MAX_VALUE;
		int bestEnding = 0;
		int bestStart = 0;
		
		int neigh = Math.min(
		        Math.min(ending.length / audioFormat.getFrameSize() / 10,
		                 starting.length / audioFormat.getFrameSize() / 10),
		        DIFF_CHECK_SIZE);
		for (int i = 0; i < neigh; ++i) {
			byte[] endingFrameBytes = new byte[this.audioFormat.getFrameSize()];
			for (int j = 0; j < this.audioFormat.getFrameSize(); ++j)
				endingFrameBytes[j] = ending[ending.length - (i + 1) * this.audioFormat.getFrameSize() + j];
			
			for (int j = 0; j < neigh; ++j) {
				byte[] startingFrameBytes = new byte[this.audioFormat.getFrameSize()];
				for (int k = 0; k < this.audioFormat.getFrameSize(); ++k)
					startingFrameBytes[k] = starting[j * this.audioFormat.getFrameSize() + k];
				double diff = 0;
				for (int k = 0; k < this.audioFormat.getFrameSize(); ++k) {
					diff += (endingFrameBytes[k] - startingFrameBytes[k]) * (endingFrameBytes[k] - startingFrameBytes[k]);
				}
				if (diff < bestScore) {
					bestScore = diff;
					bestEnding = ending.length / this.audioFormat.getFrameSize() - i;
					bestStart = j;
				}
			}
		}
		return new AudioMergeDiff(bestScore, bestEnding, bestStart);
	}
}
