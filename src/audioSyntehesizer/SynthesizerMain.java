package audioSyntehesizer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;


import common.AudioLabel;
import commonExceptions.ImplementationError;

import dataExporters.AudacityLabelsExporter;
import dataProducers.AudacityLabelImporter;
import dataProducers.TextImporter;


public class SynthesizerMain {
	public static URL audioModelUrl() throws MalformedURLException
	{
//    	URL[] urls = new URL[]{new File("sphinx/lib/WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz.jar").toURI().toURL()};
//    	URLClassLoader classLoader = new URLClassLoader(urls);
//    	return classLoader.findResource("WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz");
		return new URL(
			"file:/home/bartek/workspace/speechtextmatcher/voxforge-ru-0.2/model_parameters/msu_ru_nsh.cd_cont_1000_8gau_16000/");
	}
	
	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, ImplementationError
	{
		
		String wavFile = args[0];
		String labelsFile = args[1];
		String phonemeLabelsFile = args[2];
		String text = args[3];
		String outputFile = args[4];
		
		AudioLabel[] prepared = new AudacityLabelImporter(new TextImporter(labelsFile)).getLabels();
		AudioLabel[] phonemes = new AudacityLabelImporter(new TextImporter(phonemeLabelsFile)).getLabels();
		AudioInputStream stream = AudioSystem.getAudioInputStream(new File(wavFile));
		
//		WordToPhonemeAligner phonemeAligner = new WordToPhonemeAligner(stream, audioModelUrl());
//		ArrayList<AudioLabel> allPhonemes = new ArrayList<AudioLabel>();
//		for (AudioLabel label : prepared) {
//			allPhonemes.addAll(phonemeAligner.align(label));
//		}
//		new AudacityLabelsExporter("test.labels.txt").export(allPhonemes.toArray(new AudioLabel[0]));
        
//        SimpleAudioSynthesizer synthesizer = new SimpleAudioSynthesizer(stream, prepared, phonemes);
		MiddleToMiddleAudioSynthesizer synthesizer = new MiddleToMiddleAudioSynthesizer(stream, prepared, phonemes);
        AudioInputStream synthesized = synthesizer.synthesize(text);
        AudioSystem.write(synthesized, AudioFileFormat.Type.WAVE, new File(outputFile));
	}
}
