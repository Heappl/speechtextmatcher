package dataProducers;

public interface IWaveObserver {
	void process(double startTime, double endTime, double[] values);
}
