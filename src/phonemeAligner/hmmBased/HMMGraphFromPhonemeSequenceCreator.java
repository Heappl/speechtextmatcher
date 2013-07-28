package phonemeAligner.hmmBased;

import common.algorithms.hmm.HMMPathGraph;
import common.algorithms.hmm.HMMPathNode;
import common.algorithms.hmm.HmmPathArc;

public class HMMGraphFromPhonemeSequenceCreator
{
    public HMMPathGraph create(String[][] phonemeSequences)
    {
        HMMPathNode initial = new HMMPathNode("sil");
        initial.add(new HmmPathArc(initial, 0));
        HMMPathNode last = initial;
        for (String[] wordSeq : phonemeSequences) {
            if (wordSeq.length == 0) continue;
            {
                HMMPathNode firstFromWord = new HMMPathNode(wordSeq[0]);
                last.add(new HmmPathArc(firstFromWord, 1));
                firstFromWord.add(new HmmPathArc(firstFromWord, 0));
                last = firstFromWord;
            }
            for (int i = 1; i < wordSeq.length; ++i) {
                HMMPathNode next = new HMMPathNode(wordSeq[i]);
                HMMPathNode inWordSil = new HMMPathNode("_sil_");
                last.add(new HmmPathArc(last, 0));
                last.add(new HmmPathArc(inWordSil, 1));
                last.add(new HmmPathArc(next, 2));
                inWordSil.add(new HmmPathArc(inWordSil, 0));
                inWordSil.add(new HmmPathArc(next, 1));
                
                last = next;
            }
            last.add(new HmmPathArc(new HMMPathNode("sil"), 1));
            last.add(new HmmPathArc(last, 0));
        }
        last.add(new HmmPathArc(null, 1));
        return new HMMPathGraph(initial);
    }
}
