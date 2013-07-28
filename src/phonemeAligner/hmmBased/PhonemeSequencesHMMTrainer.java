package phonemeAligner.hmmBased;

import graphemesToPhonemesConverters.GraphemesToPolishPhonemesConverter;
import graphemesToPhonemesConverters.TextToPhonemeSequenceConverter;

import java.util.ArrayList;

import common.AudioLabel;
import common.GenericListContainer;
import common.algorithms.DataByTimesExtractor;
import common.algorithms.hmm.HMMPathGraph;
import common.algorithms.hmm.HiddenMarkovModel;
import common.algorithms.hmm.MixtureModelWithHmmTraining;

public class PhonemeSequencesHMMTrainer
{
    private DataByTimesExtractor<double[]> extractor;
    private HMMGraphFromPhonemeSequenceCreator hmmGraphCreator = new HMMGraphFromPhonemeSequenceCreator();
    private TextToPhonemeSequenceConverter phonemeSeqCreator =
            new TextToPhonemeSequenceConverter(new GraphemesToPolishPhonemesConverter());
    
    public PhonemeSequencesHMMTrainer(ArrayList<double[]> audioData, double totalTime)
    {
        this.extractor = new DataByTimesExtractor<double[]>(
                new GenericListContainer<double[]>(audioData), totalTime, 0);
    }
    
    HiddenMarkovModel trainModel(ArrayList<AudioLabel> chunks)
    {
        double[][][] trainingData = new double[chunks.size()][][];
        HMMPathGraph[] chunkGraphs = new HMMPathGraph[chunks.size()];
        
        int count = 0;
        for (AudioLabel chunk : chunks) {
            ArrayList<double[]> extracted = this.extractor.extract(chunk.getStart(), chunk.getEnd());
            trainingData[count] = extracted.toArray(new double[0][0]);
            chunkGraphs[count] = this.hmmGraphCreator.create(this.phonemeSeqCreator.convert(chunk.getLabel()));
            ++count;
        }
        
        MixtureModelWithHmmTraining hmmTrainer = new MixtureModelWithHmmTraining();
        return hmmTrainer.trainModel(trainingData, chunkGraphs);
    }
}
