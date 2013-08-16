package textAligners;

import graphemesToPhonemesConverters.GraphemesToPolishPhonemesConverter;
import phonemeScorers.IPhonemeScorer;
import phonemeScorers.io.PhonemeScorerImporter;
import speechDetection.OfflineSpeechRecognizer;
import common.Speeches;
import common.Text;
import common.exceptions.DeserializationException;
import common.exceptions.ImplementationError;
import dataExporters.AudacityLabelsExporter;
import dataProducers.PowerExtractor;
import dataProducers.TextImporter;
import dataProducers.WaveImporter;

public class MainTextToSpeechByGaussianMatcher
{
    public static void main(String[] args)
            throws  ClassNotFoundException, InstantiationException,
                    IllegalAccessException, DeserializationException,
                    ImplementationError
    {
        String waveFile = args[0];
        String textFile = args[1];
        String gaussianFile = args[2];
        String labelsOutputPath = args[3];

        Speeches speeches = null;
        {
            System.err.println("importing fft data");
            WaveImporter waveImporterForOfflineSpeechRecognition =
                    new WaveImporter(waveFile, "../textAligners/config_nospeech_nomel.xml");
            OfflineSpeechRecognizer speechRecognizer = new OfflineSpeechRecognizer(20, 10);
            waveImporterForOfflineSpeechRecognition.registerObserver(speechRecognizer);
            waveImporterForOfflineSpeechRecognition.process();
            waveImporterForOfflineSpeechRecognition.done();
            
            System.err.println("searching for speech parts");
            speeches = speechRecognizer.findSpeechParts();
        }
        PowerExtractor extractor = new PowerExtractor();
        {
            System.err.println("importing audio features data");
            WaveImporter waveImporterForAudioFeatures =
                    new WaveImporter(waveFile, "../textAligners/config_all_frontend.xml");
            WaveImporter waveImporterForOfflineSpeechRecognition =
                    new WaveImporter(waveFile, "../textAligners/config_nospeech_nomel_17_2.xml");
            
            waveImporterForAudioFeatures.registerObserver(extractor.getAudioFeaturesObserver());
            waveImporterForOfflineSpeechRecognition.registerObserver(extractor.getPowerObserver());
        
            waveImporterForAudioFeatures.process();
            waveImporterForOfflineSpeechRecognition.process();
            waveImporterForOfflineSpeechRecognition.done();
            waveImporterForAudioFeatures.done();
        }

        System.err.println("importing text");
        Text text = new Text(new TextImporter(textFile), speeches.getTotalTime());
        System.err.println("importing gaussian scorers");
        IPhonemeScorer[] scorers = new PhonemeScorerImporter(new TextImporter(gaussianFile)).getScorers();

        System.err.println("aligning");
//        GaussianBasedAligner aligner = new GaussianBasedAligner(
//                scorers,
//                new GraphemesToPolishPhonemesConverter(),
//                extractor.getPowerData(),
//                extractor.getTotalTime());
//        new AudacityLabelsExporter(labelsOutputPath).export(aligner.align(text, speeches));
        
//        IterativeGaussianSearchAligner aligner = new IterativeGaussianSearchAligner(
//                scorers,
//                new GraphemesToPolishPhonemesConverter(),
//                extractor.getPowerData(),
//                extractor.getTotalTime());
//        new AudacityLabelsExporter(labelsOutputPath).export(aligner.align(text));
        
        BreadthSearchBestCutAligner aligner = new BreadthSearchBestCutAligner(
                scorers,
                new GraphemesToPolishPhonemesConverter(),
                100);
        new AudacityLabelsExporter(labelsOutputPath).export(
                aligner.align(text, extractor.getPowerData(), extractor.getTotalTime()));
        System.err.println("END");
    }
}
