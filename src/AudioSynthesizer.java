import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;

import audioModelSupportedAlignment.NaiveDictionaryGenerator;

import common.AudioChunkExtractor;
import common.AudioLabel;
import common.Text;


public class AudioSynthesizer
{
	private HashMap<String, ArrayList<AudioLabel>> wordLabels = new HashMap<String, ArrayList<AudioLabel>>();
	private HashMap<String, ArrayList<AudioLabel>> phonemeLabels = new HashMap<String, ArrayList<AudioLabel>>();
	private AudioChunkExtractor extractor = null;
	
	public AudioSynthesizer(AudioInputStream audio, AudioLabel[] wordLabels, AudioLabel[] phonemeLabels)
	{
		for (AudioLabel wordLabel : wordLabels) {
			ArrayList<AudioLabel> labels = new ArrayList<AudioLabel>();
			if (this.wordLabels.containsKey(wordLabel.getLabel()))
				labels = this.wordLabels.get(wordLabel.getLabel());
			labels.add(wordLabel);
		}
		for (AudioLabel phonemeLabel : phonemeLabels) {
			ArrayList<AudioLabel> labels = new ArrayList<AudioLabel>();
			if (this.phonemeLabels.containsKey(phonemeLabel.getLabel()))
				labels = this.phonemeLabels.get(phonemeLabel.getLabel());
			labels.add(phonemeLabel);
		}
		this.extractor = new AudioChunkExtractor(audio);
	}
	
	public AudioInputStream synthesize(String text) throws IOException
	{
		String[] words = new Text(text, 0).getWords();
		ArrayList<ArrayList<AudioLabel>> candidates = new ArrayList<ArrayList<AudioLabel>>();
		
		for (String word : words) {
			if (this.wordLabels.containsKey(word))
				candidates.add(this.wordLabels.get(word));
			else candidates.addAll(createPhonemeRepr(word));
		}
		return extractAndMergeAudio(findBestSequence(candidates));
	}

	private AudioInputStream extractAndMergeAudio(ArrayList<AudioLabel> repr) throws IOException
	{
		AudioLabel[] copy = repr.toArray(new AudioLabel[repr.size()]);
		Arrays.sort(copy);
		
		AudioInputStream[] partial = new AudioInputStream[copy.length];
		HashMap<Double, Integer> timeToIndexMap = new HashMap<Double, Integer>();
		for (int i = 0; i < partial.length; ++i) {
			partial[i] = extractor.extract(copy[i].getStart(), copy[i].getEnd());
			timeToIndexMap.put(copy[i].getStart(), i);
		}
		
		AudioInputStream ret = null;
		for (AudioLabel label : repr)
			ret = merge(ret, partial[timeToIndexMap.get(label.getStart())]);
		
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

	private ArrayList<AudioLabel> findBestSequence(ArrayList<ArrayList<AudioLabel>> candidates)
	{
		//TODO: create more elaborate algorithm, which checks starts and ends of audio sequences
		ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
		for (ArrayList<AudioLabel> chunkCandidates : candidates)
			ret.add(chunkCandidates.get(0));
		return ret;
	}

	private ArrayList<ArrayList<AudioLabel>> createPhonemeRepr(String word)
	{
		String repr = new NaiveDictionaryGenerator(new Text(word, 0)).getDictionary().get(word)[0];
		String[] phonemes = repr.split(" ");
		
		ArrayList<ArrayList<AudioLabel>> ret = new ArrayList<ArrayList<AudioLabel>>();
		for (String phoneme : phonemes) {
			ret.add(this.phonemeLabels.get(phoneme));
		}
		return ret;
	}
}
