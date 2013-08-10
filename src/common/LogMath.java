package common;

public class LogMath
{
    public static float logAdd(float first, float second)
    {
        if (first == Float.NEGATIVE_INFINITY) return Float.NEGATIVE_INFINITY;
        if (second == Float.NEGATIVE_INFINITY) return Float.NEGATIVE_INFINITY;
        if (first < second) return logAdd(second, first);
        
        return first + linearToLog(1 + logToLinear(second - first));
    }
    public static double logToLinear(float logValue)
    {
        return Math.exp((double)logValue);
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
        if (!resultIsSet()) return 0;
        return result;
    }
    public float getResult(float defaultValue)
    {
        if (!resultIsSet()) return defaultValue;
        return result;
    }
    public boolean resultIsSet()
    {
        return resultSet;
    }
}
