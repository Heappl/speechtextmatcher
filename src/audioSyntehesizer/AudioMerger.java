package audioSyntehesizer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;

public class AudioMerger
{
	public AudioInputStream mergeAudio(ArrayList<AudioInputStream> repr) throws IOException
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
}
