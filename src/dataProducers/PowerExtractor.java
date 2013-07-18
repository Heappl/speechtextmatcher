package dataProducers;
import java.util.ArrayList;

public class PowerExtractor {
	
	ArrayList<double[]> powerData = new ArrayList<double[]>();
	int powerDataCount = 0;
	int audioDataCount = 0;
	int spectrumSize = 0;
	
	public IWaveObserver getPowerObserver()
	{
	    return new IWaveObserver() {
            @Override
            public void process(double startTime, double endTime, double[] values)
            {
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
}
