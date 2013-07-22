package dataProducers;
import java.util.ArrayList;

public class PowerExtractor {
	
	ArrayList<double[]> powerData = new ArrayList<double[]>();
	int powerDataCount = 0;
	int audioDataCount = 0;
	int spectrumSize = 0;
	double lastTime = 0;
	final double minTime;
	final double maxTime;
	
	public PowerExtractor(double minTime, double maxTime)
    {
	    this.maxTime = maxTime;
	    this.minTime = minTime;
    }
    public PowerExtractor()
    {
        this.maxTime = Double.POSITIVE_INFINITY;
        this.minTime = Double.NEGATIVE_INFINITY;
    }
	
	public IWaveObserver getPowerObserver()
	{
	    return new IWaveObserver() {
            @Override
            public void process(double startTime, double endTime, double[] values)
            {
                synchronized (powerData) {
                    lastTime = Math.max(lastTime, endTime);
                }
                if (startTime > maxTime) return;
                if (endTime < minTime) return;
                double power = 0;
                for (int i = 0; i < values.length; ++i) power += values[i];
                power /= values.length;
                power = Math.sqrt(power);
                while (true) {
                    synchronized (powerData) {
                        if (spectrumSize == 0) continue;
                        if (powerDataCount >= audioDataCount) {
                            powerData.add(new double[spectrumSize]);
                        }
                        powerData.get(powerDataCount++)[0] = power;
                        if (spectrumSize > 0) break;
                    }
                }
            }
        };
	}
	
	public IWaveObserver getAudioFeaturesObserver()
	{
        return new IWaveObserver() {
            @Override
            public void process(double startTime, double endTime, double[] values)
            {
                synchronized (powerData) {
                    lastTime = Math.max(lastTime, endTime);
                }
                if (startTime > maxTime) return;
                if (endTime < minTime) return;
                synchronized (powerData) {
                    spectrumSize = values.length + 1;
                    if (audioDataCount >= powerDataCount) {
                        powerData.add(new double[spectrumSize]);
                    }
                    double[] aux = powerData.get(audioDataCount++);
                    for (int i = 0; i < values.length; ++i)
                        aux[i + 1] = values[i];
                }
            }
        };
	}
	
	public ArrayList<double[]> getPowerData()
	{
		return powerData;
	}
	
    public double getTotalTime()
    {
        return this.lastTime;
    }
}
