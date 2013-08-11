package phonemeAligner.hmmBased;

import common.algorithms.hmm.ITrainableObservationLogLikelihoodCalculator;

public class SingleGaussianAdaptorOnMultipleGaussianTrainer implements ITrainableObservationLogLikelihoodCalculator
{
    private final GaussianObservationScorer singleGaussian;
    private final MultipleGaussianTrainer trainer;
    
    public SingleGaussianAdaptorOnMultipleGaussianTrainer(
        GaussianObservationScorer single,
        MultipleGaussianTrainer trainer)
    {
        this.singleGaussian = single;
        this.trainer = trainer;
    }

    @Override
    public void addObservation(double[] observation, float logLikelihood)
    {
        this.trainer.addObservation(observation, logLikelihood, singleGaussian);
    }

    @Override
    public void finishTraining()
    {
        this.trainer.finishTraining();
        this.singleGaussian.finishTraining();
    }

    @Override
    public void addObservationAgain(double[] observation, float logLikelihood)
    {
        this.trainer.addObservationAgain(observation, logLikelihood, singleGaussian);
    }

    @Override
    public float observationLogLikelihood(double[] observation)
    {
        float ret = this.singleGaussian.observationLogLikelihood(observation);
        return ret;
    }
}
