package phonemeAligner.simpleGaussianTraining;

import graphemesToPhonemesConverters.GraphemesToPolishPhonemesConverter;

import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;

import phonemeScorers.io.PhonemeScorerExporter;
import phonemeScorers.trainers.IterativePhonemeScorerTraining;

import common.AudioLabel;
import common.exceptions.ImplementationError;

import dataExporters.AudacityLabelsExporter;
import dataProducers.AudacityLabelImporter;
import dataProducers.PowerExtractor;
import dataProducers.TextImporter;
import dataProducers.WaveImporter;

public class MainLargeChunkPhonemeAlignment
{
    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, ImplementationError
    {
        String waveFile = args[0];
        String labelsFile = args[1];
        String outputFile = args[2];
        String outputPhonemeLabelsFile = args[3];
        
        AudioLabel[] prepared = new AudacityLabelImporter(new TextImporter(labelsFile)).getLabels();
        WaveImporter waveImporterForAudioData = new WaveImporter(
                waveFile, "../phonemeAligner/phonemeAlignmentConfig.xml");
        WaveImporter waveImporterForPowerData = new WaveImporter(
                waveFile, "../phonemeAligner/config_nospeech_nomel.xml");
        PowerExtractor dataExtractor = new PowerExtractor();
        
        waveImporterForAudioData.registerObserver(dataExtractor.getAudioFeaturesObserver());
        waveImporterForPowerData.registerObserver(dataExtractor.getPowerObserver());
        
        waveImporterForAudioData.process();
        waveImporterForPowerData.process();
        waveImporterForPowerData.done();
        waveImporterForAudioData.done();

        IterativePhonemeScorerTraining aligner =
            new IterativePhonemeScorerTraining(
                Double.POSITIVE_INFINITY,
                prepared,
                dataExtractor.getPowerData(),
                new GraphemesToPolishPhonemesConverter(),
                dataExtractor.getTotalTime());

        new PhonemeScorerExporter(outputFile).export(aligner.train(30));
        new AudacityLabelsExporter(outputPhonemeLabelsFile).export(aligner.getLastResults());
        System.err.println("END");
    }
}
