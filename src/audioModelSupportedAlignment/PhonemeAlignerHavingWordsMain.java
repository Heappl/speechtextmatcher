package audioModelSupportedAlignment;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.TreeSet;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;


import graphemesToPhonemesConverters.GraphemesToPolishPhonemesConverter;
import graphemesToPhonemesConverters.GraphemesToRussianPhonemesConverter;

import speechDetection.OnlineSpeechesExtractor;

import common.AudioLabel;
import common.exceptions.ImplementationError;

import dataExporters.AudacityLabelsExporter;
import dataProducers.AudacityLabelImporter;
import dataProducers.PowerExtractor;
import dataProducers.TextImporter;
import dataProducers.WaveImporter;
import diffCalculators.SpectrumDiffCalculator;
import edu.cmu.sphinx.util.props.PropertyException;


public class PhonemeAlignerHavingWordsMain
{
	public static URL audioModelUrl() throws MalformedURLException
	{
//    	URL[] urls = new URL[]{new File("sphinx/lib/WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz.jar").toURI().toURL()};
//    	URLClassLoader classLoader = new URLClassLoader(urls);
//    	return classLoader.findResource("WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz");
		return new URL(
			"file:/home/bartek/workspace/speechtextmatcher/voxforge-ru-0.2/model_parameters/msu_ru_nsh.cd_cont_1000_8gau_16000/");
	}

	public static void main(String[] args) throws ImplementationError, PropertyException, IOException, UnsupportedAudioFileException
	{
		String waveFile = args[0];
		String labelsFile = args[1];
		String outputFile = args[2];
		
		AudioLabel[] prepared = new AudacityLabelImporter(new TextImporter(labelsFile)).getLabels();

        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(waveFile));
        double totalTime = (double)stream.getFrameLength() / (double)stream.getFormat().getFrameRate();

        WaveImporter waveImporterForPowers = new WaveImporter(
                waveFile, "../audioModelSupportedAlignment/config_nospeech_nomel.xml");
        WaveImporter waveImporterForAudioData = new WaveImporter(
                waveFile, "../audioModelSupportedAlignment/phonemeAlignmentConfig.xml");
        PowerExtractor powerExtractor = new PowerExtractor();
        
        waveImporterForPowers.registerObserver(powerExtractor.getPowerObserver());
        waveImporterForAudioData.registerObserver(powerExtractor.getAudioFeaturesObserver());
        
        waveImporterForPowers.process();
        waveImporterForAudioData.process();
        waveImporterForAudioData.done();
        waveImporterForPowers.done();
    	
    	WordToPhonemeAlignerBasedOnHMM aligner = new WordToPhonemeAlignerBasedOnHMM(
    			audioModelUrl(), prepared, new GraphemesToRussianPhonemesConverter());

		new AudacityLabelsExporter(outputFile).export(
				aligner.align(powerExtractor.getPowerData(), prepared, totalTime));
	}
}
