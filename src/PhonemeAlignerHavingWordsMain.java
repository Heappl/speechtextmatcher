import graphemesToPhonemesConverters.GraphemesToPolishPhonemesConverter;

import speechDetection.OnlineSpeechesExtractor;

import common.AudioLabel;
import commonExceptions.ImplementationError;

import dataExporters.AudacityLabelsExporter;
import dataProducers.AudacityLabelImporter;
import dataProducers.TextImporter;
import dataProducers.WaveImporter;
import diffCalculators.SpectrumDiffCalculator;


public class PhonemeAlignerHavingWordsMain
{

	public static void main(String[] args) throws ImplementationError
	{
		String waveFile = args[0];
		String labelsFile = args[1];
		String outputFile = args[2];
		
		AudioLabel[] prepared = new AudacityLabelImporter(new TextImporter(labelsFile)).getLabels();
		WaveImporter waveImporterForPhonemeRecognition = new WaveImporter(waveFile, "config_all.xml");
    	OnlineSpeechesExtractor speechExtractor = new OnlineSpeechesExtractor();
    	waveImporterForPhonemeRecognition.registerObserver(speechExtractor);
    	waveImporterForPhonemeRecognition.process();
    	waveImporterForPhonemeRecognition.done();
    	
    	WordToPhonemeAligner aligner = new WordToPhonemeAligner(
    			prepared, speechExtractor.getAllData(), new GraphemesToPolishPhonemesConverter(),
    			new SpectrumDiffCalculator());
		
		new AudacityLabelsExporter(outputFile).export(aligner.align(20).toArray(new AudioLabel[0]));
	}
}
