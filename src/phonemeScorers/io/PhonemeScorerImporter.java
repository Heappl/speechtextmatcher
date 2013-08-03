package phonemeScorers.io;

import java.util.ArrayList;

import phonemeScorers.IPhonemeScorer;
import common.ITextProducer;
import common.exceptions.DeserializationException;

public class PhonemeScorerImporter
{
    private IPhonemeScorer[] scorers = null;

    public PhonemeScorerImporter(ITextProducer producer) throws ClassNotFoundException, InstantiationException, IllegalAccessException, DeserializationException
    {
        String[] lines = producer.getText().split("\n");
        ArrayList<IPhonemeScorer> aux = new ArrayList<IPhonemeScorer>();
        for (String line : lines) {
            aux.add(createScorer(line));
        }
        this.scorers = aux.toArray(new IPhonemeScorer[0]);
    }
    
    private IPhonemeScorer createScorer(String line) throws ClassNotFoundException, InstantiationException, IllegalAccessException, DeserializationException
    {
        String className = line.split("\\{")[0];
        Class<?> scorerClass = Class.forName(className);
        return ((IPhonemeScorer)scorerClass.newInstance()).deserialize(line);
    }

    public IPhonemeScorer[] getScorers() { return scorers; }
}
