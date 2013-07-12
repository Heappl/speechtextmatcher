import java.util.ArrayList;

import common.Data;
import common.DataSequence;

import dataProducers.IWaveObserver;


public class PowerExtractor implements IWaveObserver {
	
	DataSequence powerData = new DataSequence();
	
	@Override
	public void process(double startTime, double endTime, double[] values)
	{
		double power = 0;
		for (int i = 0; i < values.length; ++i) power += values[i];
		power /= values.length;
		powerData.add(new Data(startTime, endTime, new double[]{Math.sqrt(power)}));
	}

	public DataSequence getPowerData()
	{
		return powerData;
	}
}
