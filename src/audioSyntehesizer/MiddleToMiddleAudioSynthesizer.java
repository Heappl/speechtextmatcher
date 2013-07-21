package audioSyntehesizer;

import graphemesToPhonemesConverters.GraphemesToPolishPhonemesConverter;
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
import common.exceptions.ImplementationError;

public class MiddleToMiddleAudioSynthesizer
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

	public MiddleToMiddleAudioSynthesizer(
			AudioInputStream audio, AudioLabel[] wordLabels, AudioLabel[] phonemeLabels) throws ImplementationError
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
		Collections.sort(this.sortedWords, this.timeComparator);
		AudioLabel previousWord = null;
		ArrayList<AudioLabel> wordPhonemeLabels = new ArrayList<AudioLabel>();
		for (AudioLabel phonemeLabel : phonemeLabels) {
		    if (phonemeLabel.getLabel().equals("sil")) continue;
		    AudioLabel wordContaing = findWordContaining(phonemeLabel);
		    if (wordContaing == previousWord) {
		        wordPhonemeLabels.add(phonemeLabel);
		        continue;
		    }
		    previousWord = wordContaing;
		    
		    boolean ok = true;
		    for (AudioLabel wordPhoneme : wordPhonemeLabels) {
	            if ((wordPhoneme.getEnd() <= wordPhoneme.getStart())
	                || (wordPhoneme.getEnd() - wordPhoneme.getStart() > 1.0)
	                || (wordPhoneme.getEnd() - wordPhoneme.getStart() < 0.0001))
	                ok = false;
		    }
		    if (!ok) { wordPhonemeLabels.clear(); continue; }
            for (AudioLabel wordPhoneme : wordPhonemeLabels) {
    			ArrayList<AudioLabel> labels = new ArrayList<AudioLabel>();
    			if (this.phonemeLabels.containsKey(wordPhoneme.getLabel()))
    				labels = this.phonemeLabels.get(wordPhoneme.getLabel());
    			labels.add(wordPhoneme);
                this.sortedPhonemes.add(wordPhoneme);
    			this.phonemeLabels.put(wordPhoneme.getLabel(), labels);
            }
            wordPhonemeLabels.clear();
		}
        Collections.sort(this.sortedPhonemes, this.timeComparator);
	}
	
	private class WordCandidates implements AudioCandidate
	{
		private ArrayList<AudioLabel> candidates = new ArrayList<AudioLabel>();
		private HashMap<AudioLabel, byte[]> streams = new HashMap<AudioLabel, byte[]>();

		public WordCandidates(ArrayList<AudioLabel> candidates)
		{
		    for (AudioLabel label : candidates) {
		        if (label.getEnd() - label.getStart() < 0.1) continue;
		        this.candidates.add(label);
		    }
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

        @Override
        public ArrayList<AudioLabel> getCandidateLabels(int k)
        {
            ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
            ret.add(candidates.get(k));
            return ret;
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
		private ArrayList<AudioLabel> labels;
		
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
                for (int k = 0; k < wordCandidates.get(i).size(); ++k) {
                    for (int prev = 0; prev < i; ++prev) {
                        for (int j = 0; j < wordCandidates.get(prev).size(); ++j) {
                            wordCandidates.get(i).get(k).score(
                                    wordCandidates.get(prev).get(j), i - prev, streams, frameSize);
                        }
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
			labels = bestCandidate.createLabelSequence(streams, frameSize, Integer.MAX_VALUE);
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
        public ArrayList<AudioLabel> getCandidateLabels(int k)
        {
            return labels;
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
            double currentTotalTime = totalSequence.getEnd() - totalSequence.getStart();
            double currentFrameTime = currentTotalTime / (double)(currentBytes.length / frameSize);
            double maxTrimTime = phonemes.get(phonemes.size() - 1).getEnd();
            int maxTrimIndex = (int)Math.floor(maxTrimTime / currentFrameTime) / frameSize;
			trimSize = Math.min(currentBytes.length, Math.min(maxTrimIndex, trimSize));
			trimSize -= trimSize % frameSize;
			bestCurrentIndex -= bestCurrentIndex % frameSize;
			int newSize = trimSize - bestCurrentIndex;
			byte[] actualBytes = new byte[newSize];
			
			for (int i = bestCurrentIndex; i < trimSize; ++i)
				actualBytes[i - bestCurrentIndex] = currentBytes[i];
			
			ret.add(actualBytes);
			return ret;
		}
        public ArrayList<AudioLabel> createLabelSequence(HashMap<AudioLabel, byte[]> streams, int frameSize, int trimSize)
        {
            ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
            if (bestPrevious != null) {
                ret = bestPrevious.createLabelSequence(streams, frameSize, bestPreviousIndex);
            }
            byte[] currentBytes = streams.get(totalSequence);
            double currentTotalTime = totalSequence.getEnd() - totalSequence.getStart();
            double currentFrameTime = currentTotalTime / (double)(currentBytes.length / frameSize);
            double maxTrimTime = phonemes.get(phonemes.size() - 1).getEnd();
            int maxTrimIndex = (int)Math.floor(maxTrimTime / currentFrameTime) / frameSize;
            trimSize = Math.min(currentBytes.length, Math.min(maxTrimIndex, trimSize));
            trimSize -= trimSize % frameSize;
            bestCurrentIndex -= bestCurrentIndex % frameSize;
            
            ret.add(phonemes.get(0));
            ret.add(phonemes.get(1));
            return ret;
        }

		public double getScore()
		{
			return bestScore;
		}

		public void score(
				PhonemeSequenceCandidate prev,
				int phonemeDiff,
				HashMap<AudioLabel, byte[]> streams, 
				int frameSize)
		{
			if (!streams.containsKey(totalSequence)) return;
			if (prev.phonemes.size() <= phonemeDiff) return;
			
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
			
			double previousMergePhonemeStart = prev.phonemes.get(phonemeDiff).getStart() - prev.totalSequence.getStart();
			double previousMergePhonemeEnd = prev.phonemes.get(phonemeDiff).getEnd() - prev.totalSequence.getStart();
			int previousPhonemeStartIndex = (int)Math.round(previousMergePhonemeStart / previousFrameTime);
			double previousMergePhonemeTime = previousMergePhonemeEnd - previousMergePhonemeStart;
			double previousMergePhonemeMiddle = previousMergePhonemeTime / 2.0;
			int previousMergePhonemeMiddleIndex = (int)Math.round(previousMergePhonemeMiddle / previousFrameTime) + previousPhonemeStartIndex;
			
			int currentNeighSize = (int)Math.floor(currentMergePhonemeTime / currentFrameTime / 16.0);
			int previousNeighSize = (int)Math.floor(previousMergePhonemeTime / previousFrameTime / 16.0);
			int neigh = Math.min(currentNeighSize / frameSize, previousNeighSize / frameSize);
			
			int maxPass = Math.max(1, neigh / 8);
			int diffSize = Math.max(1, maxPass / 8);
			for (int i = -neigh; i < neigh; ++i) {
				int currentStartIndex = i * frameSize + currentMergePhonemeMiddleIndex * frameSize;
				for (int o = -maxPass; o < maxPass; ++o) {
				    int previousStartIndex = (i + o) * frameSize + previousMergePhonemeMiddleIndex * frameSize;
    				double diff = prev.getScore();
    				for (int f = -diffSize; f < diffSize; ++f) {
        				for (int j = 0; j < frameSize; ++j) {
        				    int currentIndex = currentStartIndex + f * frameSize + j;
        				    int previousIndex = previousStartIndex + f * frameSize + j;
        				    if ((currentIndex < 0) || (currentIndex >= currentBytes.length)
        				        || (previousIndex < 0) || (previousIndex >= previousBytes.length)) {
        				        diff = Double.MAX_VALUE;
        				        break;
        				    }
        					double auxDiff = currentBytes[currentIndex] - previousBytes[previousIndex];
        					diff += auxDiff * auxDiff;
        				}
    				}
    				if (diff < bestScore) {
    					bestScore = diff;
    					bestPrevious = prev;
    					bestPreviousIndex = previousStartIndex;
    					bestCurrentIndex = currentStartIndex;
    				}
				}
			}
		}
	}
	
	private PhonemeSequenceWordCandidates synthesizeWord(String word) throws ImplementationError
	{
		String[] phonemes = new GraphemesToPolishPhonemesConverter().convert(word).get(0).split(" +");
		int minimumSize = 3;
		ArrayList<ArrayList<PhonemeSequenceCandidate>> wordCandidates = new ArrayList<ArrayList<PhonemeSequenceCandidate>>();
		int lastSize = minimumSize;
		for (int i = 0; i <= phonemes.length - minimumSize; ++i) {
		    lastSize = minimumSize;
			ArrayList<PhonemeSequenceCandidate> phonemeSequenceCandidates =
					findPhonemeSequenceCandidates(phonemes, i, minimumSize);
			if (phonemeSequenceCandidates.size() < 3) {
			    lastSize = minimumSize - 1;
			    phonemeSequenceCandidates =
	                    findPhonemeSequenceCandidates(phonemes, i, lastSize);
			}
			if (phonemeSequenceCandidates.isEmpty()) {
			    System.err.println("empty for " + word + " " + phonemes[i] + " " + phonemes[i + 1]);
			}
			wordCandidates.add(phonemeSequenceCandidates);
		}
		if (lastSize < minimumSize) {
            ArrayList<PhonemeSequenceCandidate> phonemeSequenceCandidates =
                    findPhonemeSequenceCandidates(phonemes, phonemes.length - lastSize, lastSize);
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
        if ((sortedWords.get(index).getStart() <= label.getStart() + 0.1)
            && (sortedWords.get(index).getEnd() >= label.getEnd() - 0.1))
            return sortedWords.get(index);
        throw new ImplementationError("word not found " + label + " " + sortedWords.get(index));
	}
}
