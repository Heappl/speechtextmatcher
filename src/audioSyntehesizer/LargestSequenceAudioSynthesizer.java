package audioSyntehesizer;

import graphemesToPhonemesConverters.GraphemesToRussianPhonemesConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;

import common.AudioLabel;
import common.Text;
import commonExceptions.ImplementationError;

public class LargestSequenceAudioSynthesizer
{
    private final Comparator<AudioLabel> timeComparator = new Comparator<AudioLabel>() {
        @Override
        public int compare(AudioLabel o1, AudioLabel o2)
        {
            if (o1.getStart() < o2.getStart()) return -1;
            if (o1.getStart() > o2.getStart()) return 1;
            return 0;
        }
    };
    
	private HashMap<String, ArrayList<AudioLabel>> wordLabels = new HashMap<String, ArrayList<AudioLabel>>();
	private ArrayList<AudioLabel> sortedWords = new ArrayList<AudioLabel>();
	private ArrayList<AudioLabel> sortedPhonemes = new ArrayList<AudioLabel>();
	private HashMap<String, ArrayList<AudioLabel>> phonemeLabels = new HashMap<String, ArrayList<AudioLabel>>();
	private AudioCandidatesChooser chooser = null;

	public LargestSequenceAudioSynthesizer(
			AudioInputStream audio, AudioLabel[] wordLabels, AudioLabel[] phonemeLabels)
	{
		this.chooser = new AudioCandidatesChooser(audio);
		for (AudioLabel wordLabel : wordLabels) {
			if (wordLabel.getEnd() <= wordLabel.getStart()) continue;
			String word = wordLabel.getLabel().toLowerCase();
			ArrayList<AudioLabel> labels = new ArrayList<AudioLabel>();
			if (this.wordLabels.containsKey(word))
				labels = this.wordLabels.get(word);
			this.sortedWords.add(wordLabel);
			labels.add(wordLabel);
			this.wordLabels.put(word, labels);
		}
		for (AudioLabel phonemeLabel : phonemeLabels) {
			if (phonemeLabel.getEnd() <= phonemeLabel.getStart()) continue;
			ArrayList<AudioLabel> labels = new ArrayList<AudioLabel>();
			if (this.phonemeLabels.containsKey(phonemeLabel.getLabel()))
				labels = this.phonemeLabels.get(phonemeLabel.getLabel());
			labels.add(phonemeLabel);
            this.sortedPhonemes.add(phonemeLabel);
			this.phonemeLabels.put(phonemeLabel.getLabel(), labels);
		}
		Collections.sort(this.sortedWords, this.timeComparator);
        Collections.sort(this.sortedPhonemes, this.timeComparator);
	}
	
	private class WordCandidates implements AudioCandidate
	{
		private ArrayList<AudioLabel> candidates;
		private HashMap<AudioLabel, byte[]> streams = new HashMap<AudioLabel, byte[]>();

		public WordCandidates(ArrayList<AudioLabel> candidates)
		{
			this.candidates = candidates;
		}

		@Override
		public ArrayList<AudioLabel> getNeededParts()
		{
			return candidates;
		}

		@Override
		public void solveInternal()
		{
			//intentionally left empty
		}

		@Override
		public int getNumberOfCandidates()
		{
			return this.candidates.size();
		}

		@Override
		public byte[] getCandidate(int k)
		{
			return this.streams.get(this.candidates.get(k));
		}

		@Override
		public void supplyAudioPart(AudioLabel audioPart, AudioInputStream stream)
		{
			byte[] streamBytes = new byte[(int)stream.getFrameLength() * stream.getFormat().getFrameSize()];
			try {
				stream.read(streamBytes, 0, streamBytes.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.streams.put(audioPart, streamBytes);
		}
	}
	
	public AudioInputStream synthesize(String text) throws IOException, ImplementationError
	{
		String[] words = new Text(text.toLowerCase(), 0).getWords();
		
		ArrayList<AudioCandidate> candidates = new ArrayList<AudioCandidate>();
		for (int i = 0; i < words.length; ++i)
		{
			if (wordLabels.containsKey(words[i]))
				candidates.add(new WordCandidates(wordLabels.get(words[i])));
			else
				candidates.add(synthesizeWord(words[i]));
		}
		
		
		ArrayList<AudioInputStream> partials = chooser.chooseCandidates(candidates);
		return new AudioMerger().mergeAudio(partials);
	}
	
	private class PhonemeSequenceWordCandidates implements AudioCandidate
	{
		private ArrayList<ArrayList<PhonemeSequenceCandidate>> wordCandidates;
		private byte[] solved = null;
		private HashMap<AudioLabel, byte[]> streams = new HashMap<AudioLabel, byte[]>();
		private int frameSize = 0;
		
		public PhonemeSequenceWordCandidates(ArrayList<ArrayList<PhonemeSequenceCandidate>> wordCandidates)
		{
			this.wordCandidates = wordCandidates;
		}
		@Override
		public ArrayList<AudioLabel> getNeededParts()
		{
			ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
			for (ArrayList<PhonemeSequenceCandidate> item : wordCandidates) {
				for (PhonemeSequenceCandidate seqCandidate : item) {
					ret.add(seqCandidate.getNeededPart());
				}
			}
			return ret;
		}
		@Override
		public void solveInternal()
		{
			for (PhonemeSequenceCandidate initialCandidate : wordCandidates.get(0)) {
				initialCandidate.resetScore();
			}
			for (int i = 1; i < wordCandidates.size(); ++i) {
				for (int j = 0; j < wordCandidates.get(i - 1).size(); ++j) {
					for (int k = 0; k < wordCandidates.get(i).size(); ++k) {
						wordCandidates.get(i).get(k).score(wordCandidates.get(i - 1).get(j), streams, frameSize);
					}
				}
			}
			PhonemeSequenceCandidate bestCandidate = null;
			for (PhonemeSequenceCandidate candidate : wordCandidates.get(wordCandidates.size() - 1)) {
				if ((bestCandidate == null) || (bestCandidate.getScore() > candidate.getScore())) {
					bestCandidate = candidate;
				}
			}
			
			ArrayList<byte[]> sequence = bestCandidate.createSequence(streams, frameSize, Integer.MAX_VALUE);
			int totalSize = 0;
			for (byte[] part : sequence) totalSize += part.length;
			solved = new byte[totalSize];
			int index = 0;
			for (byte[] part : sequence)
				for (byte b : part)
					solved[index++] = b;
		}
		
		@Override
		public int getNumberOfCandidates()
		{
			return 1;
		}
		@Override
		public byte[] getCandidate(int k)
		{
			return solved;
		}
		@Override
		public void supplyAudioPart(AudioLabel audioPart, AudioInputStream stream)
		{
			byte[] streamBytes = new byte[(int)stream.getFrameLength() * stream.getFormat().getFrameSize()];
			this.frameSize = stream.getFormat().getFrameSize(); //assumed all are the same
			try {
				stream.read(streamBytes, 0, streamBytes.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.streams.put(audioPart, streamBytes);
		}
	}
	private class PhonemeSequenceCandidate
	{
		AudioLabel totalSequence;
		ArrayList<AudioLabel> phonemes;
		PhonemeSequenceCandidate bestPrevious = null;
		double bestScore = Double.MAX_VALUE;
		int bestPreviousIndex = Integer.MAX_VALUE;
		int bestCurrentIndex = 0;
		
		public PhonemeSequenceCandidate(ArrayList<AudioLabel> phonemes)
		{
			double totalSeqStart = phonemes.get(0).getStart();
			double totalSeqEnd = phonemes.get(phonemes.size() - 1).getEnd();
			this.totalSequence = new AudioLabel("", totalSeqStart, totalSeqEnd);
			this.phonemes = phonemes;
		}
		
		public AudioLabel getNeededPart()
		{
			return totalSequence;
		}

		public void resetScore()
		{
			this.bestScore = 0;
		}

		public ArrayList<byte[]> createSequence(
				HashMap<AudioLabel, byte[]> streams, int frameSize, int trimSize)
		{
			ArrayList<byte[]> ret = new ArrayList<byte[]>();
			if (bestPrevious != null) {
				ret = bestPrevious.createSequence(streams, frameSize, bestPreviousIndex);
			}
			byte[] currentBytes = streams.get(totalSequence);
			trimSize = Math.min(currentBytes.length, trimSize);
			trimSize -= trimSize % frameSize;
			bestCurrentIndex -= bestCurrentIndex % frameSize;
			int newSize = trimSize - bestCurrentIndex;
			byte[] actualBytes = new byte[newSize];
			
			for (int i = bestCurrentIndex; i < trimSize; ++i)
				actualBytes[i - bestCurrentIndex] = currentBytes[i];
			
			ret.add(actualBytes);
			return ret;
		}

		public double getScore()
		{
			return bestScore;
		}

		public void score(
				PhonemeSequenceCandidate prev,
				HashMap<AudioLabel, byte[]> streams, 
				int frameSize)
		{
			if (!streams.containsKey(totalSequence)) return;
			
			byte[] currentBytes = streams.get(totalSequence);
			double currentTotalTime = totalSequence.getEnd() - totalSequence.getStart();
			double currentFrameTime = currentTotalTime / (double)(currentBytes.length / frameSize);
			
			byte[] previousBytes = streams.get(prev.totalSequence);
			double previousTotalTime = prev.totalSequence.getEnd() - prev.totalSequence.getStart();
			double previousFrameTime = previousTotalTime / (double)(previousBytes.length / frameSize);
			
			double currentMergePhonemeStart = phonemes.get(0).getStart() - totalSequence.getStart();
			double currentMergePhonemeEnd = phonemes.get(0).getEnd() - totalSequence.getStart();
			double currentMergePhonemeTime = currentMergePhonemeEnd - currentMergePhonemeStart;
			double currentMergePhonemeMiddle = currentMergePhonemeTime / 2.0;
			int currentMergePhonemeMiddleIndex = (int)Math.round(currentMergePhonemeMiddle / currentFrameTime);
			
			double previousMergePhonemeStart = prev.phonemes.get(1).getStart() - prev.totalSequence.getStart();
			double previousMergePhonemeEnd = prev.phonemes.get(1).getEnd() - prev.totalSequence.getStart();
			int previousPhonemeStartIndex = (int)Math.round(previousMergePhonemeStart / previousFrameTime);
			double previousMergePhonemeTime = previousMergePhonemeEnd - previousMergePhonemeStart;
			double previousMergePhonemeMiddle = previousMergePhonemeTime / 2.0;
			int previousMergePhonemeMiddleIndex = (int)Math.round(previousMergePhonemeMiddle / previousFrameTime) + previousPhonemeStartIndex;
			
			int currentNeighSize = (int)Math.floor(currentMergePhonemeTime / currentFrameTime / 8.0);
			int previousNeighSize = (int)Math.floor(previousMergePhonemeTime / previousFrameTime / 8.0);
			int neigh = Math.min(currentNeighSize / frameSize, previousNeighSize / frameSize);
			
			for (int i = -neigh; i < neigh; ++i) {
				int currentStartIndex = i * frameSize + currentMergePhonemeMiddleIndex * frameSize;
				int previousStartIndex = i * frameSize + previousMergePhonemeMiddleIndex * frameSize;
				double diff = 0;
				for (int j = 0; j < frameSize; ++j) {
				    if ((currentStartIndex + j >= currentBytes.length)
				         || (previousStartIndex + j >= previousBytes.length)) {
				        diff = Double.MAX_VALUE;
				        break;
				    }
					double auxDiff = currentBytes[currentStartIndex + j] - previousBytes[previousStartIndex + j];
					diff += auxDiff * auxDiff;
				}
				if (diff < bestScore) {
					bestScore = diff;
					bestPrevious = prev;
					bestPreviousIndex = previousStartIndex * frameSize;
					bestCurrentIndex = currentStartIndex * frameSize;
				}
			}
		}
	}
	
	private PhonemeSequenceWordCandidates synthesizeWord(String word) throws ImplementationError
	{
		String[] phonemes = new GraphemesToRussianPhonemesConverter().convert(word).get(0).split(" +");
		
		int minimumSize = 2;
		ArrayList<ArrayList<PhonemeSequenceCandidate>> wordCandidates = new ArrayList<ArrayList<PhonemeSequenceCandidate>>();
		for (int i = 0; i <= phonemes.length - minimumSize; ++i) {
			ArrayList<PhonemeSequenceCandidate> phonemeSequenceCandidates =
					findPhonemeSequenceCandidates(phonemes, i, minimumSize);
			wordCandidates.add(phonemeSequenceCandidates);
		}
		return new PhonemeSequenceWordCandidates(wordCandidates);
	}

	private ArrayList<PhonemeSequenceCandidate> findPhonemeSequenceCandidates(
			String[] phonemes, int i, int minimumSize) throws ImplementationError
	{
		ArrayList<PhonemeSequenceCandidate> ret = new ArrayList<PhonemeSequenceCandidate>();
		if (!phonemeLabels.containsKey(phonemes[i])) return ret;

		for (AudioLabel label : phonemeLabels.get(phonemes[i])) {
			AudioLabel wordLabel = findWordContaining(label);
			ArrayList<AudioLabel> wordSeq = getWordSequenceStartingFrom(wordLabel, label);
			ArrayList<AudioLabel> matchingSeq = new ArrayList<AudioLabel>();
			int index = i;
			for (AudioLabel phoneme : wordSeq) {
			    if (index >= phonemes.length) break;
			    if (!usablePhoneme(phoneme.getLabel(), phonemes[index])) break;
				matchingSeq.add(phoneme);
				++index;
			}
			if (matchingSeq.size() >= minimumSize) {
				ret.add(new PhonemeSequenceCandidate(matchingSeq));
			}
		}
		System.err.println(i + " " + phonemes[i] + " " + ret.size());
		return ret;
	}

	private boolean usablePhoneme(String candidate, String phoneme)
    {
	    if (candidate.equalsIgnoreCase(phoneme)) return true;
	    if (candidate.equalsIgnoreCase(phoneme + phoneme)) return true;
	    if (phoneme.equalsIgnoreCase(candidate + candidate)) return true;
        return false;
    }

    private ArrayList<AudioLabel> getWordSequenceStartingFrom(AudioLabel wordLabel, AudioLabel label)
	{
	    int index = Collections.binarySearch(sortedPhonemes, label, timeComparator);
	    ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
	    for (int i = index; i < sortedPhonemes.size(); ++i) {
	        if (sortedPhonemes.get(i).getStart() >= wordLabel.getEnd()) break;
	        ret.add(sortedPhonemes.get(i));
	    }
		return ret;
	}

	private AudioLabel findWordContaining(AudioLabel label) throws ImplementationError
	{
        int index = Collections.binarySearch(sortedWords, label, timeComparator);
        if (index < 0) index = -index - 2;
        if ((sortedWords.get(index).getStart() <= label.getStart())
            && (sortedWords.get(index).getEnd() >= label.getEnd()))
            return sortedWords.get(index);
        for (int i = Math.max(0, index - 10); i < Math.min(index + 10, sortedWords.size()); ++i)
            System.err.println(sortedWords.get(i));
        throw new ImplementationError("word not found " + label + " " + sortedWords.get(index));
	}
}