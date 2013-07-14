package audioSyntehesizer;

import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;

import common.AudioLabel;

public interface AudioCandidate {
	ArrayList<AudioLabel> getNeededParts();
	void supplyAudioPart(AudioLabel audioPart, AudioInputStream stream);
	void solveInternal();
	int getNumberOfCandidates();
	byte[] getCandidate(int k);
}
