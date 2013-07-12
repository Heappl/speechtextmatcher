package audioSyntehesizer;
import graphemesToPhonemesConverters.DictionaryGenerator;
import graphemesToPhonemesConverters.GraphemesToRussianPhonemesConverter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;


import common.AudioChunkExtractor;
import common.AudioLabel;
import common.Text;


public class AudioSynthesizer
{
	private HashMap<String, ArrayList<AudioLabel>> wordLabels = new HashMap<String, ArrayList<AudioLabel>>();
	private HashMap<String, ArrayList<AudioLabel>> phonemeLabels = new HashMap<String, ArrayList<AudioLabel>>();
	private AudioChunkExtractor extractor = null;
	private AudioFormat audioFormat = null;
	
	public AudioSynthesizer(AudioInputStream audio, AudioLabel[] wordLabels, AudioLabel[] phonemeLabels)
	{
		this.audioFormat = audio.getFormat();
		for (AudioLabel wordLabel : wordLabels) {
			if (wordLabel.getEnd() <= wordLabel.getStart()) continue;
			String word = wordLabel.getLabel().toLowerCase();
			ArrayList<AudioLabel> labels = new ArrayList<AudioLabel>();
			if (this.wordLabels.containsKey(word))
				labels = this.wordLabels.get(word);
			labels.add(wordLabel);
			this.wordLabels.put(word, labels);
		}
		for (AudioLabel phonemeLabel : phonemeLabels) {
			if (phonemeLabel.getEnd() <= phonemeLabel.getStart()) continue;
			ArrayList<AudioLabel> labels = new ArrayList<AudioLabel>();
			if (this.phonemeLabels.containsKey(phonemeLabel.getLabel()))
				labels = this.phonemeLabels.get(phonemeLabel.getLabel());
			labels.add(phonemeLabel);
			this.phonemeLabels.put(phonemeLabel.getLabel(), labels);
		}
		this.extractor = new AudioChunkExtractor(audio);
	}
	
	public AudioInputStream synthesize(String text) throws IOException
	{
		String[] words = new Text(text.toLowerCase(), 0).getWords();
		ArrayList<ArrayList<AudioLabel>> candidates = new ArrayList<ArrayList<AudioLabel>>();
		
		for (String word : words) {
			if (this.wordLabels.containsKey(word))
			{
				candidates.add(this.wordLabels.get(word));
			}
			else candidates.addAll(createPhonemeRepr(word));
		}
		return mergeAudio(findBestSequence(candidates));
	}

	private AudioInputStream mergeAudio(ArrayList<AudioInputStream> repr) throws IOException
	{
		if (repr.size() == 0) return null;
		if (repr.size() == 1) return repr.get(0);
		
		AudioInputStream ret = merge(repr.get(0), repr.get(1));
		for (int i = 2; i < repr.size(); ++i)
			ret = merge(ret, repr.get(i));
		return ret;
	}

	private AudioInputStream merge(AudioInputStream first, AudioInputStream second) throws IOException
	{
		int firstChunkSize = (int)(first.getFrameLength() * first.getFormat().getFrameSize());
		int secondChunkSize = (int)(second.getFrameLength() * second.getFormat().getFrameSize());
		byte[] chunkData = new byte[firstChunkSize + secondChunkSize];
		first.read(chunkData, 0, firstChunkSize);
		second.read(chunkData, firstChunkSize, secondChunkSize);
		ByteArrayInputStream byteStream = new ByteArrayInputStream(chunkData);
		return new AudioInputStream(
				byteStream,
				first.getFormat(),
				chunkData.length / (first.getFormat().getSampleSizeInBits() / Byte.SIZE));
	}
	
	private class AudioElement
	{
		int index1;
		int index2;
		AudioLabel label;
		public AudioElement(int index1, int index2, AudioLabel label) {
			this.index1 = index1;
			this.index2 = index2;
			this.label = label;
		}
	};
	private class AudioCandidate
	{
		int index1;
		int index2;
		byte[] bytes;
		
		public AudioCandidate(AudioInputStream streamCopy, AudioElement element) throws IOException {
			this.index1 = element.index1;
			this.index2 = element.index2;
			this.bytes = new byte[(int)(streamCopy.getFrameLength() * audioFormat.getFrameSize())];
			streamCopy.read(bytes, 0, bytes.length);
		}

		public byte[] getBytes() {
			return bytes;
		}

		public void trimEnd(int i)
		{
			byte[] newBytes = new byte[bytes.length - i * audioFormat.getFrameSize()];
			for (int j = 0; j < newBytes.length; ++j) newBytes[j] = bytes[j];
			bytes = newBytes;
		}

		public void trimStart(int i)
		{
			byte[] newBytes = new byte[bytes.length - i * audioFormat.getFrameSize()];
			for (int j = 0; j < newBytes.length; ++j) newBytes[j] = bytes[i + j];
			bytes = newBytes;
		}
	};
	
	private ArrayList<AudioInputStream> findBestSequence(ArrayList<ArrayList<AudioLabel>> candidates) throws IOException
	{
		int maxCandidates = Integer.MIN_VALUE;
		ArrayList<AudioElement> elementsAux = new ArrayList<AudioSynthesizer.AudioElement>();
		for (int i = 0; i < candidates.size(); ++i) {
			if (candidates.get(i).size() > maxCandidates)
				maxCandidates = candidates.get(i).size();
			for (int j = 0; j < candidates.get(i).size(); ++j) {
				AudioLabel audioPart = candidates.get(i).get(j);
				elementsAux.add(new AudioElement(i, j, audioPart));
			}
		}
		AudioElement[] elements = elementsAux.toArray(new AudioElement[0]);
		Arrays.sort(elements, new Comparator<AudioElement>() {
			public int compare(AudioElement o1, AudioElement o2) {
				if (o1.label.getStart() < o2.label.getStart()) return -1;
				if (o1.label.getStart() > o2.label.getStart()) return 1;
				return 0;
			}
		});
		
		HashMap<Integer, HashMap<Integer, AudioInputStream>> streamMap = new HashMap<Integer, HashMap<Integer,AudioInputStream>>();
		AudioCandidate[][] audioCandidates = new AudioCandidate[candidates.size()][maxCandidates];
		
		for (AudioElement element : elements) {
			if (element.label.getEnd() <= element.label.getStart()) continue;
			AudioInputStream stream = extractor.extract(element.label.getStart(), element.label.getEnd());
			HashMap<Integer, AudioInputStream> internal =
				(streamMap.containsKey(element.index1)) ? streamMap.get(element.index1) : new HashMap<Integer, AudioInputStream>();
			internal.put(element.index2, stream);
			streamMap.put(element.index1, internal);
			
			AudioInputStream streamCopy = extractor.extract(element.label.getStart(), element.label.getEnd());
			audioCandidates[element.index1][element.index2] = new AudioCandidate(streamCopy, element);
		}
		
		AudioCandidate[] bestCandidates = findBestSequence(audioCandidates);
		
		ArrayList<AudioInputStream> ret = new ArrayList<AudioInputStream>();
		for (AudioCandidate bestCand : bestCandidates) {
			ret.add(new AudioInputStream(new ByteArrayInputStream(bestCand.getBytes()), this.audioFormat,
					bestCand.bytes.length / (audioFormat.getSampleSizeInBits() / Byte.SIZE)));
		}
		return ret;
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

	private AudioCandidate[] findBestSequence(AudioCandidate[][] audioCandidates)
	{
		double[] current = new double[audioCandidates[0].length];
		int[][][] previous = new int[audioCandidates.length][audioCandidates[0].length][3];
		for (int i = 1; i < audioCandidates.length; ++i) {
			double[] next = new double[current.length];
			
			for (int j = 0; j < next.length; ++j) {
				if (audioCandidates[i][j] == null) {
					next[j] = Double.MAX_VALUE;
					continue;
				}
				for (int k = 0; k < current.length; ++k) {
					if (audioCandidates[i - 1][k] == null) continue;
					AudioMergeDiff diff = calcDiff(audioCandidates[i - 1][k], audioCandidates[i][j]);
					double currScore = current[k] + diff.score;
					if (currScore < next[j]) {
						next[j] = currScore;
						previous[i][j][0] = k;
						previous[i][j][1] = diff.endingIndex;
						previous[i][j][2] = diff.startingIndex;
					}
				}
			}
			
			current = next;
		}

		int cand = 0;
		double bestScore = Double.MAX_VALUE;
		for (int i = 0; i < current.length; ++i) {
			if (current[i] < bestScore) {
				cand = i;
				bestScore = current[i];
			}
		}
		
		AudioCandidate[] ret = new AudioCandidate[audioCandidates.length];
		ret[ret.length - 1] = audioCandidates[ret.length - 1][cand];
		for (int i = 1; i < audioCandidates.length; ++i) {
			cand = previous[ret.length - i][cand][0];
			ret[ret.length - i - 1] = audioCandidates[ret.length - i - 1][cand];
			ret[ret.length - i - 1].trimEnd(previous[ret.length - i][cand][1]);
			ret[ret.length - i].trimStart(previous[ret.length - i][cand][2]);
		}
		return ret;
	}

	private AudioMergeDiff calcDiff(AudioCandidate ending, AudioCandidate starting)
	{
		int neigh = 30;
		
		double bestScore = Double.MAX_VALUE;
		int bestEnding = 0;
		int bestStart = 0;
		for (int i = 0; i < neigh; ++i) {
			byte[] endingFrameBytes = new byte[this.audioFormat.getFrameSize()];
			for (int j = 0; j < this.audioFormat.getFrameSize(); ++j)
				endingFrameBytes[j] =
					ending.bytes[ending.bytes.length - (i + 1) * this.audioFormat.getFrameSize() + j];
			
			for (int j = 0; j < neigh; ++j) {
				byte[] startingFrameBytes = new byte[this.audioFormat.getFrameSize()];
				for (int k = 0; k < this.audioFormat.getFrameSize(); ++k)
					startingFrameBytes[k] = starting.bytes[j * this.audioFormat.getFrameSize() + k];
				double diff = 0;
				for (int k = 0; k < this.audioFormat.getFrameSize(); ++k) {
					diff += (endingFrameBytes[k] - startingFrameBytes[k]) * (endingFrameBytes[k] - startingFrameBytes[k]);
				}
				if (diff < bestScore) {
					bestScore = diff;
					bestEnding = ending.bytes.length / this.audioFormat.getFrameSize() - i;
					bestStart = j;
				}
			}
		}
		return new AudioMergeDiff(bestScore, bestEnding, bestStart);
	}

	private ArrayList<ArrayList<AudioLabel>> createPhonemeRepr(String word)
	{
		String repr = new DictionaryGenerator(
				new Text(word, 0),
				new GraphemesToRussianPhonemesConverter()
			).getDictionary().get(word)[0];
		String[] phonemes = repr.split(" ");
		
		ArrayList<ArrayList<AudioLabel>> ret = new ArrayList<ArrayList<AudioLabel>>();
		for (String phoneme : phonemes) {
			System.err.println(phoneme + " " + this.phonemeLabels.containsKey(phoneme));
			ret.add(this.phonemeLabels.get(phoneme));
		}
		return ret;
	}
}
