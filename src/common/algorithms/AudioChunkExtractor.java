package common.algorithms;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;


public class AudioChunkExtractor
{
	private ArrayList<byte[]> audioData = new ArrayList<byte[]>();
	private AudioFormat format = null;
	private AudioInputStream stream;
	private long start = 0;
	
	public AudioChunkExtractor(AudioInputStream stream)
	{
		this.format = stream.getFormat();
		this.stream = stream;
	}
	
	public AudioInputStream extract(double startTime, double endTime) throws IOException
	{
		startTime = Math.max(0, startTime);
		endTime = Math.max(0, endTime);
		readAndDeleteBytes((int)((long)Math.floor(startTime * format.getFrameRate())));
		int startFrame = (int)((long)Math.floor(startTime * format.getFrameRate()) - start);
		int endFrame = (int)((long)Math.ceil(endTime * format.getFrameRate()) - start);
		int chunkSize = (endFrame - startFrame) * format.getFrameSize();
		
		readBytes(endFrame);
		if (startFrame >= audioData.size()) return null;
		endFrame = Math.min(endFrame, audioData.size());
		
		byte[] chunkData = new byte[chunkSize];
		for (int i = startFrame; i < endFrame; ++i) {
			for (int j = 0; j < format.getFrameSize(); ++j) {
				chunkData[(i - startFrame) * format.getFrameSize() + j] = audioData.get(i)[j];
			}
		}
		deleteBytes(startFrame);
		ByteArrayInputStream byteStream = new ByteArrayInputStream(chunkData);
		return new AudioInputStream(
				byteStream,
				format,
				chunkData.length / (format.getSampleSizeInBits() / Byte.SIZE));
	}

	private void deleteBytes(int toIndex)
	{
		audioData = new ArrayList<byte[]>(audioData.subList(toIndex, audioData.size()));
		start += toIndex;
	}
	private void readAndDeleteBytes(int i) throws IOException
	{
		for (long j = start; j < i; j += 1000) {
			readBytes((int)(j - start));
			deleteBytes((int)(j - start));
		}
		readBytes((int)(i - start));
		deleteBytes((int)(i - start));
//		stream.skip(i * format.getFrameSize());
//		start += i;
	}
	private void readBytes(int i)
	{
		int readBytes = 0;
		while ((readBytes >= 0) && (audioData.size() < i)) {
			byte[] frame = new byte[format.getFrameSize()];
			try {
				readBytes = stream.read(frame, 0, format.getFrameSize());
				audioData.add(frame); 
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}
}

