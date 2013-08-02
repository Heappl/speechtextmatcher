package common.algorithms.hmm2;

public class LogMath
{

    public static double logAdd(double first, double second)
    {
        double firstPow = Math.pow(Math.E, first);
        double secondPow = Math.pow(Math.E, second);
        return Math.log(firstPow + secondPow);
    }
}
