package common.algorithms.hmm.training;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import common.LogMath;
import common.algorithms.hmm.Arc;
import common.algorithms.hmm.Node;
import common.algorithms.hmm.State;
import common.algorithms.hmm.StateExit;
import common.exceptions.ImplementationError;

public class Trainer
{
    private double endingDifference = 0.001;
    
    public Trainer()
    {
    }
    public Trainer(double trainToDifference)
    {
        this.endingDifference = trainToDifference;
    }
    
    public void trainModel(double[][][] setOfDataSequences, Node[] subgraphs) throws ImplementationError
    {
        double likelihoodSoFar = Double.NEGATIVE_INFINITY;
        
        StatesTrainer statesTrainer = createStatesTrainer(subgraphs, setOfDataSequences);
        int iteration = 0;
        while (true) {
            System.err.println("hmm training iteration: " + iteration + "," +
                               " current likelihood: " + likelihoodSoFar);
            double likelihood = this.retrainToBetterModelUsingPreviouslyTrained(
                    setOfDataSequences, subgraphs, statesTrainer);
            if (Math.abs(likelihoodSoFar - likelihood) <= this.endingDifference) break;
            likelihoodSoFar = likelihood;
            iteration++;
        }
    }
    
    private StatesTrainer createStatesTrainer(Node[] subgraphs, double[][][] setOfDataSequences)
    {
        Set<State> allStates = new HashSet<State>();
        
        for (Node subgraph : subgraphs) {
            allStates.addAll(getAllStates(subgraph));
        }
        
        ArrayList<SingleStateTrainer> stateTrainers = new ArrayList<SingleStateTrainer>();
        for (State state : allStates) {
            stateTrainers.add(new SingleStateTrainer(state));
        }
        ArrayList<TransitionTrainer> transitionTrainers = new ArrayList<TransitionTrainer>();
        initiateStates(subgraphs, stateTrainers, setOfDataSequences);
        
        for (State state : allStates) {
            for (StateExit exit : state) {
                exit.updateLikelihood(LogMath.linearToLog(1.0 / (double)state.numOfExits()));
                transitionTrainers.add(new TransitionTrainer(exit));
            }
        }
        return new StatesTrainer(stateTrainers, transitionTrainers);
    }
    
    private void initiateStates(
        Node[] subgraphs,
        ArrayList<SingleStateTrainer> stateTrainers,
        double[][][] setOfDataSequences)
    {
        for (int i = 0; i < subgraphs.length; ++i) {
            Set<State> subgraphStates = getAllStates(subgraphs[i]);
            float logLikelihood = LogMath.linearToLog((double)1 / (double)subgraphStates.size());
            for (int j = 0; j < setOfDataSequences[i].length; ++j) {
                for (SingleStateTrainer stateTrainer : stateTrainers) {
                    if (!subgraphStates.contains(stateTrainer.getState())) continue;
                    stateTrainer.addObservation(setOfDataSequences[i][j], logLikelihood);
                }
            }
        }
        for (int i = 0; i < subgraphs.length; ++i) {
            Set<State> subgraphStates = getAllStates(subgraphs[i]);
            float logLikelihood = LogMath.linearToLog((double)1 / (double)subgraphStates.size());
            for (SingleStateTrainer stateTrainer : stateTrainers) {
                if (subgraphStates.contains(stateTrainer.getState())) {
                    for (int j = 0; j < setOfDataSequences[i].length; ++j)
                        stateTrainer.addObservationAgain(setOfDataSequences[i][j], logLikelihood);
                }
            }
        }
        for (SingleStateTrainer stateTrainer : stateTrainers) {
            stateTrainer.finish();
        }
    }
    
    private Set<State> getAllStates(Node subgraph)
    {
        ArrayList<Node> next = new ArrayList<Node>();
        Collection<Node> visited = new HashSet<Node>();
        Set<State> ret = new HashSet<State>();
        ret.add(subgraph.getState());
        visited.add(subgraph);
        next.add(subgraph);
        
        while (!next.isEmpty()) {
            Node current = next.remove(next.size() - 1);
            for (Arc arc : current) {
                if (visited.contains(arc.getLeadingToNode())) continue;
                if (arc.isExitState()) continue;
                visited.add(arc.getLeadingToNode());
                next.add(arc.getLeadingToNode());
                ret.add(arc.getLeadingToNode().getState());
            }
        }
        return ret;
    }
    
    private double retrainToBetterModelUsingPreviouslyTrained(
        double[][][] setOfDataSequences,
        Node[] subgraphs,
        StatesTrainer statesTrainer) throws ImplementationError
    {
        System.err.println("first phase");
        for (int i = 0; i < setOfDataSequences.length; ++i) {
            statesTrainer.retrainStateTrainersWithObservationSequenceForPossibleModel(
                    setOfDataSequences[i], subgraphs[i]);
        }
        System.err.println("second phase");
        for (int i = 0; i < setOfDataSequences.length; ++i) {
            statesTrainer.retrainStateTrainersSecondPhase(
                    setOfDataSequences[i], subgraphs[i]);
        }
        return statesTrainer.retrainingFinished();
    }
}
