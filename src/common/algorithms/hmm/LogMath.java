package common.algorithms.hmm;

public class LogMath
{
    public static float logAdd(float first, float second)
    {
        if (first == Float.NEGATIVE_INFINITY) return second;
        if (second == Float.NEGATIVE_INFINITY) return first;
        return linearToLog(logToLinear(first) + logToLinear(second));
    }
    public static double logToLinear(float logValue)
    {
        return Math.exp(logValue);
    }
    public static float linearToLog(double value)
    {
        if (value <= 0) return -Float.MAX_VALUE;
        return (float)Math.min(Float.MAX_VALUE, Math.max(-Float.MAX_VALUE, Math.log(value)));
    }
}
