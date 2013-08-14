package speechDetection;

import java.util.ArrayList;

import common.Data;
import common.DataSequence;
import common.Speech;
import common.Speeches;
import dataProducers.IWaveObserver;
import dataTransforms.OfflineDataNormalizer;
import diffCalculators.SpectrumWeights;

public class OfflineSpeechRecognizer implements IWaveObserver {

	private DataSequence allData = new DataSequence();
	private int spectrumSize = 0;
	private int speechGravity;
	private int nonSpeechGravity;
	
	public OfflineSpeechRecognizer(int speechGravity, int nonSpeechGravity)
	{
		this.speechGravity = speechGravity;
		this.nonSpeechGravity = nonSpeechGravity;
	}
    public OfflineSpeechRecognizer(int speechGravity, int nonSpeechGravity, boolean storeData)
    {
        this.speechGravity = speechGravity;
        this.nonSpeechGravity = nonSpeechGravity;
    }
	
	@Override
	public void process(double startTime, double endTime, double[] values)
	{
		allData.add(new Data(startTime, endTime, values));
		this.spectrumSize = Math.max(values.length, this.spectrumSize);
	}
	
	public DataSequence getAllData()
	{
		return allData;
	}
	
	public Speeches findSpeechParts()
	{
//        double[] weights = new SpectrumWeights(allData).getWeights();
//        for (int i = 0; i < spectrumSize; ++i) weights[i] *= 1000;
//        for (int i = 0; i < allData.size(); ++i) {
//            for (int j = 0; j < spectrumSize; ++j) {
//                allData.get(i).getSpectrum()[j] = Math.log(allData.get(i).getSpectrum()[j]) * weights[j];
//            }
//        }
	    
	    int endIndex = spectrumSize;
	    
	    double[] average = new double[endIndex];
	    for (int i = 0; i < allData.size(); ++i) {
	        for (int j = 0; j < endIndex; ++j) {
	            average[j] += allData.get(i).getSpectrum()[j];
	        }
	    }
        for (int j = 0; j < endIndex; ++j) average[j] /= allData.size();
        
        double[] backgroundAverage = new double[endIndex];
        int backgroundAverageCount = 0;
        int countThreshold = 0;
        for (int i = 0; i < allData.size(); ++i) {
            int count = 0;
            for (int j = 0; j < endIndex; ++j) {
                if (allData.get(i).getSpectrum()[j] >= average[j])
                    ++count;
            }
            if (count > countThreshold) {
                for (int j = 0; j < endIndex; ++j) {
                    backgroundAverage[j] += allData.get(i).getSpectrum()[j];
                }
                ++backgroundAverageCount;
            }
        }
        for (int j = 0; j < endIndex; ++j) backgroundAverage[j] /= backgroundAverageCount;
	    
//        double averagePower = 0;
//        for (int i = 0; i < allData.size(); ++i)
//        {
//            double power = calculatePower(allData.get(i));
//            averagePower += power;
//        }
//        averagePower /= allData.size();
//        
//        double backgroundAveragePower = 0;
//        int count = 0;
//        for (int i = 0; i < allData.size(); ++i)
//        {
//            double power = calculatePower(allData.get(i));
//            if (power < averagePower) {
//                backgroundAveragePower += power;
//                ++count;
//            }
//        }
//        backgroundAveragePower /= count;
//        
//        double[] backgroundAverage = new double[spectrumSize];
//        int backgroundAverageCount = 0;
//        for (int i = 0; i < allData.size(); ++i)
//        {
//            double power = calculatePower(allData.get(i));
//            if (power < backgroundAveragePower) {
//                for (int j = 0; j < spectrumSize; ++j)
//                    backgroundAverage[j] += allData.get(i).getSpectrum()[j];
//                ++backgroundAverageCount;
//            }
//        }
//        for (int j = 0; j < spectrumSize; ++j)
//            backgroundAverage[j] /= backgroundAverageCount;
//
//        for (int i = 0; i < allData.size(); ++i) {
//            for (int j = 0; j < spectrumSize; ++j) {
//                allData.get(i).getSpectrum()[j] -= backgroundAverage[j];
//            }
//        }
//        averagePower = 0;
//        for (int i = 0; i < allData.size(); ++i)
//        {
//            double power = calculatePower(allData.get(i));
//            averagePower += power;
//        }
//        averagePower /= allData.size();
//        
//        backgroundAveragePower = 0;
//        count = 0;
//        for (int i = 0; i < allData.size(); ++i)
//        {
//            double power = calculatePower(allData.get(i));
//            if (power < averagePower) {
//                backgroundAveragePower += power;
//                ++count;
//            }
//        }
//        backgroundAveragePower /= count;
        
//        double backgroundVariance = 0;
//        for (int i = 0; i < allData.size(); ++i)
//        {
//            double[] curr = allData.get(i).getSpectrum();
//            double sum = 0;
//            for (int j = 0; j < spectrumSize; ++j)
//                sum += curr[j] * curr[j];
//            sum /= spectrumSize;
//            if (Math.sqrt(sum) < average)
//                backgroundVariance += (Math.sqrt(sum) - average) * (Math.sqrt(sum) - average);
//        }
//        backgroundVariance = Math.sqrt(backgroundVariance / count);
        
        boolean[] isSpeech = new boolean[allData.size() + 2];
        for (int i = 0; i < allData.size(); ++i)
        {
            int count = 0;
            for (int j = 0; j < endIndex; ++j) {
                if (allData.get(i).getSpectrum()[j] >= backgroundAverage[j])
                    ++count;
            }
            isSpeech[i + 1] = (count > countThreshold);
        }
        
        fillHoles(isSpeech, true, this.speechGravity, 0);
        fillHoles(isSpeech, true, this.speechGravity, 0);
        fillHoles(isSpeech, false, this.nonSpeechGravity, 2 * this.nonSpeechGravity);
        fillHoles(isSpeech, false, this.nonSpeechGravity, 2 * this.nonSpeechGravity);
        
        int start = -1;
        ArrayList<Speech> out = new ArrayList<Speech>();
        for (int i = 0; i < allData.size(); ++i)
        {
            if ((start >= 0) && ((i == allData.size() - 1) || !isSpeech[i]))
            {
                Speech speech = new Speech(
                        allData.get(start).getStartTime(),
                        allData.get(i).getEndTime() + 0.2,
                        start,
                        i);
                out.add(speech);
                start = -1;
            }
            if ((start < 0) && isSpeech[i])
                start = i;
        }
        return new Speeches(out);
	}

	private double calculatePower(Data data)
    {
	    double ret = 0;
	    for (double value : data.getSpectrum())
	        ret += value;
        return ret;
    }
    public Speeches findSpeechParts2()
	{
		System.err.println("spectrum size: " + allData.get(0).getSpectrum().length);
		
		double[] weights = new SpectrumWeights(allData).getWeights();
		for (int i = 0; i < allData.size(); ++i) {
			for (int j = 0; j < spectrumSize; ++j) {
				allData.get(i).getSpectrum()[j] = Math.pow(allData.get(i).getSpectrum()[j], weights[j]);
			}
		}
		new OfflineDataNormalizer(allData).normalize();
		
	    boolean[] isSpeech = new boolean[allData.size() + 2];
	    
		int neigh = 5;
	    int[] freqSpeechCount = new int[spectrumSize / (2 * neigh) + 1];
		int minSize = 2;
		int maxSize = Integer.MAX_VALUE;
		for (int freqInd = neigh; freqInd < spectrumSize - neigh; freqInd += neigh) {
			int started = -1;
			for (int i = 0; i < allData.size(); ++i) {
				int count = 0;
				for (int freq = freqInd - neigh; freq < freqInd + neigh; ++freq) {
					if (allData.get(i).getSpectrum()[freq] >= 0) ++count;
				}
				int threshold = neigh;
				if ((count >= threshold) && (started < 0)) started = i;
				if ((count < threshold) && (started >= 0)) {
					int size = i - started;
					if ((size > minSize) && (size < maxSize))
						for (int j = started; j < i; ++j)
							isSpeech[j] = true;
					if ((size > minSize) && (size < maxSize))
						freqSpeechCount[freqInd / (2 * neigh)]++;
					started = -1;
				}
			}
		}
		for (int i = 0; i < freqSpeechCount.length; ++i) {
			System.err.println(freqSpeechCount[i]);
		}
	    
        fillHoles(isSpeech, true, this.speechGravity, 0);
        fillHoles(isSpeech, true, this.speechGravity, 0);
        fillHoles(isSpeech, false, this.nonSpeechGravity, 2 * this.nonSpeechGravity);
        fillHoles(isSpeech, false, this.nonSpeechGravity, 2 * this.nonSpeechGravity);
	    
	    int start = -1;
	    ArrayList<Speech> out = new ArrayList<Speech>();
	    for (int i = 0; i < allData.size(); ++i)
	    {
	    	if ((start >= 0) && ((i == allData.size() - 1) || !isSpeech[i]))
	    	{
	    		Speech speech = new Speech(
	    				allData.get(start).getStartTime(),
	    				allData.get(i).getEndTime() + 0.1,
	    				start,
	    				i);
	    		out.add(speech);
	    		start = -1;
	    	}
	    	if ((start < 0) && isSpeech[i])
	    		start = i;
	    }
//		double[] backgroundMaxes = new double[spectrumSize];
//		for (int i = 0; i < out.get(0).getStartDataIndex(); ++i) {
//			for (int k = 0; k < spectrumSize; ++k)
//				backgroundMaxes[k] = Math.max(backgroundMaxes[k], allData.get(i).getSpectrum()[k]);
//		}
//		for (int i = 1; i < out.size(); ++i) {
//			for (int j = out.get(i - 1).getEndDataIndex() + 1; j < out.get(i).getStartDataIndex(); ++j)
//				for (int k = 0; k < spectrumSize; ++k)
//					backgroundMaxes[k] = Math.max(backgroundMaxes[k], allData.get(j).getSpectrum()[k]);
//		}
	    
//		for (int i = 0; i < out.size(); ++i) {
//			double sum = 0;
//			for (int j = out.get(i).getStartDataIndex(); i < out.get(i).getEndDataIndex(); ++j)
//				for (int k = 0; k < spectrumSize; ++k)
//					sum += allData.get(j).getSpectrum()[k];
//		}
	    return new Speeches(out);
	}
	
	private boolean isOfType(boolean[] data, int index, boolean type)
	{
		return ((index < 0) || (index >= data.length) || (data[index] == type));
	}
	
	private void fillHoles(boolean[] data, boolean type, int gravity, int margin)
	{
		int countLeft = gravity;
		int countRight = gravity;
		boolean[] newData = new boolean[gravity + 1];
		int newDataInd = 0;
        for (int i = -gravity; i < data.length + 3 * gravity; ++i)
        {
        	int index = i - gravity;
        	
    		int dataReceding = index - gravity - 1;
    		if ((dataReceding >= 0) && (dataReceding < data.length)) {
    			data[dataReceding] = newData[newDataInd];
    		}
    		
    		if ((index >= margin) && (index < allData.size() - margin)
    			&& (countRight > 0) && (countLeft > 0)) {
    			newData[newDataInd] = type;
    		}
    		else if ((index >= 0) && (index < data.length)) {
    			newData[newDataInd] = data[index];
    		}
    		newDataInd = (newDataInd + 1) % newData.length;
        	
        	int recedingForLeft = index - gravity;
        	int incomingForLeft = index;
        	int recedingForRight = index + 1;
        	int incomingForRight = i + 1;
        	
        	if (isOfType(data, recedingForLeft, type)) countLeft--;
        	if (isOfType(data, incomingForLeft, type)) countLeft++;
        	if (isOfType(data, recedingForRight, type)) countRight--;
        	if (isOfType(data, incomingForRight, type)) countRight++;
        }
	}
}

