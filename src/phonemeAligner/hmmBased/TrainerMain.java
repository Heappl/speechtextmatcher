package phonemeAligner.hmmBased;

import graphemesToPhonemesConverters.GraphemesToPolishPhonemesConverter;
import phonemeScorers.io.PhonemeScorerExporter;
import phonemeScorers.trainers.IterativePhonemeScorerTraining;

import common.AudioLabel;

import dataExporters.AudacityLabelsExporter;
import dataProducers.AudacityLabelImporter;
import dataProducers.PowerExtractor;
import dataProducers.TextImporter;
import dataProducers.WaveImporter;

public class TrainerMain
{

    public static void main(String[] args)
    {
        String waveFile = args[0];
        String labelsFile = args[1];
        String outputFile = args[2];
        String outputPhonemeLabelsFile = args[3];
        System.err.println("started " + waveFile);
        
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

        AudioLabel[] prepared = new AudacityLabelImporter(new TextImporter(labelsFile)).getLabels();
        PhonemeSequencesHMMTrainer trainer = 
                new PhonemeSequencesHMMTrainer(
                        dataExtractor.getPowerData(), dataExtractor.getTotalTime());

        PhonemeHMM model = trainer.trainModel(prepared);
        
        PhonemeHMMBasedAligner aligner =
                new PhonemeHMMBasedAligner(
                        model, dataExtractor.getPowerData(), dataExtractor.getTotalTime());
        
//        new PhonemeScorerExporter(outputFile).export(al);
        new AudacityLabelsExporter(outputPhonemeLabelsFile).export(aligner.align(prepared));
        System.err.println("END");
    }
}
