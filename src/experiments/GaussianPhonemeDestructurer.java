package experiments;

import java.util.ArrayList;


import common.AudioLabel;
import common.algorithms.gaussian.GaussianMixtureExpectedMaximalization;
import common.algorithms.gaussian.MultivariateNormalDistribution;
import common.exceptions.ImplementationError;

public class GaussianPhonemeDestructurer
{
    double[][] allData;
    double frameTime;

    public GaussianPhonemeDestructurer(ArrayList<double[]> allData, double totalTime)
    {
        this.allData = allData.toArray(new double[0][]);
        this.frameTime = allData.size() / totalTime;
    }

    public AudioLabel[] process(AudioLabel[] prepared, int models) throws ImplementationError
    {
        GaussianMixtureExpectedMaximalization em = new GaussianMixtureExpectedMaximalization();
        MultivariateNormalDistribution[] cs = em.calculate(allData, models);
        
        for (AudioLabel label : prepared) {
            System.err.println(label.getLabel() + ":");
            int start = findIndex(label.getStart());
            int end = findIndex(label.getEnd());
            
            for (int i = start; i <= end; i++) {
                
                double bestScore = Double.NEGATIVE_INFINITY;
                double bestIndex = 0;
                for (int j = 0; j < cs.length; ++j) {
                    double score = cs[j].logLikelihood(allData[i]);
                    if (score > bestScore) {
                        bestScore = score;
                        bestIndex = j;
                    }
                }
                System.err.print(bestIndex + " ");
            }
            System.err.println();
        }
        
        return new AudioLabel[0];
    }

    private int findIndex(double time)
    {
        return (int)Math.round(time / frameTime); 
    }
}
