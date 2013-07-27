package phonemeAligner.simpleGaussianTraining;

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
import dataProducers.AudioDataExtractor;
import dataProducers.PowerExtractor;
import dataProducers.TextImporter;
import dataProducers.WaveImporter;
import diffCalculators.SpectrumDiffCalculator;
import edu.cmu.sphinx.util.props.PropertyException;


public class MainPhonemeAlignerHavingWords
{
	public static void main(String[] args) throws ImplementationError, PropertyException, IOException, UnsupportedAudioFileException
	{
		String waveFile = args[0];
		String labelsFile = args[1];
		String outputFile = args[2];
		
		AudioLabel[] prepared = new AudacityLabelImporter(new TextImporter(labelsFile)).getLabels();
		WaveImporter waveImporterForPowers = new WaveImporter(
		        waveFile, "../phonemeAligner/config_nospeech_nomel.xml");
        WaveImporter waveImporterForAudioData = new WaveImporter(
                waveFile, "../phonemeAligner/phonemeAlignmentConfig.xml");
    	PowerExtractor powerExtractor = new PowerExtractor();
    	
    	waveImporterForPowers.registerObserver(powerExtractor.getPowerObserver());
    	waveImporterForAudioData.registerObserver(powerExtractor.getAudioFeaturesObserver());
    	
    	waveImporterForPowers.process();
    	waveImporterForAudioData.process();
    	waveImporterForAudioData.done();
    	waveImporterForPowers.done();

    	IterativeTrainingPhonemeAligner aligner = new IterativeTrainingPhonemeAligner(
    	        prepared,
    	        powerExtractor.getPowerData(),
    	        new GraphemesToPolishPhonemesConverter(),
    	        powerExtractor.getTotalTime());

		new AudacityLabelsExporter(outputFile).export(aligner.align(15));
		System.err.println("END");
	}
}
