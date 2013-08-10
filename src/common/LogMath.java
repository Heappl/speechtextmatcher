package common;

public class LogMath
{
    public static float logAdd(float first, float second)
    {
        if (first == Float.NEGATIVE_INFINITY) return Float.NEGATIVE_INFINITY;
        if (second == Float.NEGATIVE_INFINITY) return Float.NEGATIVE_INFINITY;
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
    
    private float result = 0;
    private boolean resultSet = false;
    
    public float logAdd(float value)
    {
        if (!resultSet) {
            result = value;
            resultSet = true;
        } else {
            result = logAdd(result, value);
        }
        return result;
    }
    public float getResult()
    {
        return result;
    }
    public boolean resultIsSet()
    {
        return resultSet;
    }
}
