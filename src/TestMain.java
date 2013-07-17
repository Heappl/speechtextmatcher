import java.util.Random;

import commonExceptions.ImplementationError;

import algorithms.GaussianMixtureExpectedMaximalization;
import algorithms.MultivariateNormalDistribution;


public class TestMain
{
    private static Random rng = new Random();
    
    public static void main(String[] args) throws ImplementationError
    {
        int s = 1000;
        double[][] data = new double[s][2];
        
        for (int i = 0; i < s / 3; i++) {
            data[i] = generate(11.456, 5.432, 2.356, 1.023, -0.56);
        }
        for (int i = s / 3; i < 2 * s / 3; i++) {
            data[i] = generate(-5.6, 2.712, 2.356, 1.023, -1.2);
        }
        for (int i = 2 * s / 3; i < s; i++) {
            data[i] = generate(0, 1, 0, 1, 0);
        }
        
        GaussianMixtureExpectedMaximalization em = new GaussianMixtureExpectedMaximalization();
        MultivariateNormalDistribution[] models = em.calculate(data, 3);
        
        for (int i = 0; i < models.length; ++i) {
            System.err.println(models[i]);
        }
    }
    
    static double[] generate(double mean1, double deviation1, double mean2, double deviation2, double coefficient)
    {
        double first = rng.nextGaussian() * deviation1 + mean1;
        double second = rng.nextGaussian() * deviation2 + mean2;
        
        double[] ret = new double[2];
        ret[0] = first;
        ret[1] = first * coefficient + second;
        return ret;
    }
}
