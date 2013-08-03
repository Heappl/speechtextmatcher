package common.algorithms.hmm.training;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.algorithms.hmm.Node;
import common.algorithms.hmm.State;
import common.algorithms.hmm.StateExit;

public class StatesTrainer
{
    private Map<State, SingleStateTrainer> stateTrainers =
            new HashMap<State, SingleStateTrainer>();
    private Map<StateExit, TransitionTrainer> transitionTrainers =
            new HashMap<StateExit, TransitionTrainer>();
    private double totalLikelihood = 0; 
    
    public StatesTrainer(ArrayList<SingleStateTrainer> stateTrainers,
                         ArrayList<TransitionTrainer> transitionTrainers)
    {
        for (SingleStateTrainer trainer : stateTrainers) {
            this.stateTrainers.put(trainer.getState(), trainer);
        }
        for (TransitionTrainer trainer : transitionTrainers) {
            this.transitionTrainers.put(trainer.getTransition(), trainer);
        }
    }

    public void retrainStateTrainersWithObservationSequenceForPossibleModel(
        double[][] observationSequence, Node possibleModel)
    {
        NodeLogLikelihoodsCalculator likelihoodsCalculator = new NodeLogLikelihoodsCalculator();
        ObservationSequenceLogLikelihoods sequenceLikelihoods =
            likelihoodsCalculator.calculate(observationSequence, possibleModel);
        
        for (NodeLogLikelihoods likelihood : sequenceLikelihoods) {
            State nodeState = likelihood.getNode().getState();
            double[] observation = likelihood.getObservation();
            this.stateTrainers.get(nodeState).addObservation(observation, likelihood.getLogLikelihood());
            for (ArcLogLikelihood arcLikelihood : likelihood) {
                StateExit arcStateExit = arcLikelihood.getArc().getExit();
                this.transitionTrainers.get(arcStateExit)
                    .addObservation(observation, arcLikelihood.getLogLikelihood());
                this.transitionTrainers.get(arcStateExit)
                    .addStateObservation(observation, likelihood.getLogLikelihood());
            }
        }
        this.totalLikelihood += sequenceLikelihoods.getLogLikelihood();
    }

    public double retrainingFinished()
    {
        for (TransitionTrainer trainer : this.transitionTrainers.values()) {
            trainer.finish();
        }
        for (SingleStateTrainer trainer : this.stateTrainers.values())
            trainer.finish();
        return this.totalLikelihood;
    }

    public void retrainStateTrainersSecondPhase(double[][] observationSequence, Node possibleModel)
    {
        NodeLogLikelihoodsCalculator likelihoodsCalculator = new NodeLogLikelihoodsCalculator();
        ObservationSequenceLogLikelihoods sequenceLikelihoods =
            likelihoodsCalculator.calculate(observationSequence, possibleModel);
        
        for (NodeLogLikelihoods likelihood : sequenceLikelihoods) {
            State nodeState = likelihood.getNode().getState();
            double[] observation = likelihood.getObservation();
            this.stateTrainers.get(nodeState).addObservationAgain(observation, likelihood.getLogLikelihood());
        }
    }
}
