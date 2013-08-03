package phonemeAligner.hmmBased;

import java.util.ArrayList;

import graphemesToPhonemesConverters.TextToPhonemeSequenceConverter;
import common.algorithms.hmm.Node;

public class HMMGraphFromPhonemeSequenceCreator
{
    private final MapOfPhonemeStates phonemeStates = new MapOfPhonemeStates();
    private final TextToPhonemeSequenceConverter converter;
    
    public HMMGraphFromPhonemeSequenceCreator(TextToPhonemeSequenceConverter converter)
    {
        this.converter = converter;
    }
    
    public Node create(String text)
    {
        String[][] phonemeSequence = this.converter.convert(text);
        
        ArrayList<String> phonemes = new ArrayList<String>();
        for (String[] word : phonemeSequence)
            for (String phoneme : word)
                phonemes.add(phoneme);
        
        Node next = null;
        for (int i = phonemes.size() - 1; i >= 0; --i) {
            next = this.phonemeStates.createNode(next, phonemes.get(i));
        }
        return next;
    }
}
