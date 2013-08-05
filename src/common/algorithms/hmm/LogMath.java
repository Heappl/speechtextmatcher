package common.algorithms.hmm;

public class LogMath
{
    public final static float minLogValue = linearToLog(-Float.MAX_VALUE);
    public final static float maxLogValue = linearToLog(Float.MAX_VALUE);

    public static float logAdd(float first, float second)
    {
        return linearToLog(logToLinear(first) + logToLinear(second));
    }
    public static double logToLinear(float logValue)
    {
        if (logValue < minLogValue) return -Double.MAX_VALUE;
        else if (logValue > maxLogValue) return Double.MAX_VALUE;
        return Math.exp(logValue);
    }
    public static float linearToLog(double value)
    {
        if (value <= 0) return -Float.MAX_VALUE;
        return (float)Math.min(Float.MAX_VALUE, Math.max(-Float.MAX_VALUE, Math.log(value)));
    }
}
