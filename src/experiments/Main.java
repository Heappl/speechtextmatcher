package experiments;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import common.AudioLabel;
import commonExceptions.ImplementationError;
import dataProducers.AudacityLabelImporter;
import dataProducers.PowerExtractor;
import dataProducers.TextImporter;
import dataProducers.WaveImporter;

public class Main
{
    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, ImplementationError
    {
        String waveFile = args[0];
        String labelsFile = args[1];
        
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(waveFile));
        double totalTime = (double)stream.getFrameLength() / (double)stream.getFormat().getFrameRate();
        System.err.println("total time: " + totalTime);
        
        WaveImporter waveImporterForPowers = new WaveImporter(
                waveFile, "../phonemeAligner/config_nospeech_nomel.xml");
        WaveImporter waveImporterForAudioData = new WaveImporter(
                waveFile, "../phonemeAligner/phonemeAlignmentConfig.xml");
//        totalTime = 500;
        PowerExtractor powerExtractor = new PowerExtractor(-1, totalTime);
        
        waveImporterForPowers.registerObserver(powerExtractor.getPowerObserver());
        waveImporterForAudioData.registerObserver(powerExtractor.getAudioFeaturesObserver());
        
        waveImporterForPowers.process();
        waveImporterForAudioData.process();
        waveImporterForAudioData.done();
        waveImporterForPowers.done();
    
        AudioLabel[] prepared =
                new AudacityLabelImporter(new TextImporter(labelsFile)).getLabels();
        PerWordPhonemeDestructurer destructurer =
                new PerWordPhonemeDestructurer(prepared, powerExtractor.getPowerData(), totalTime);
        
        destructurer.process();
//        GaussianPhonemeDestructurer destructurer =
//                new GaussianPhonemeDestructurer(powerExtractor.getPowerData(), totalTime);
//        destructurer.process(prepared, 60);
    
        System.err.println("END");
    }
}
