import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;

import common.AudioLabel;
import common.DataSequence;
import commonExceptions.ImplementationError;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;
import edu.cmu.sphinx.frontend.FrontEnd;
import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.linguist.acoustic.AcousticModel;
import edu.cmu.sphinx.linguist.acoustic.UnitManager;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.ConfigurationManagerUtils;
import edu.cmu.sphinx.util.props.PropertyException;
import graphemesToPhonemesConverters.IWordToPhonemesConverter;


public class WordToPhonemeAlignerBasedOnHMM {
	PhonemeSearch phonemeSearch;
	FrontEnd frontend;
	AudioFileDataSource dataSource;

	public WordToPhonemeAlignerBasedOnHMM(
			URL acousticModelUrl,
			AudioLabel[] prepared,
			IWordToPhonemesConverter converter) throws PropertyException, IOException
	{
		ConfigurationManager cm = new ConfigurationManager(
				new URL("file:/home/bartek/workspace/speechtextmatcher/src/phonemeAlignmentConfig.xml"));
//				ConfigurationManagerUtils.resourceToURL("resource:/edu/cmu/sphinx/config/aligner.xml"));
		cm.setGlobalProperty("acousticModel", acousticModelUrl.toString());
		cm.setGlobalProperty("filler", acousticModelUrl.toString() + "/noisedict");
		cm.setGlobalProperty("g2p", "");
		cm.setGlobalProperty("dictionary", "");
		
		this.dataSource = (AudioFileDataSource)cm.lookup("audioFileDataSource");
		UnitManager unitManager = (UnitManager)cm.lookup("unitManager");
		AcousticModel acousticModel = (AcousticModel)cm.lookup("wsj");
		acousticModel.allocate();
		this.phonemeSearch = new PhonemeSearch(converter, unitManager, acousticModel);
		this.frontend = (FrontEnd)cm.lookup("frontend");
	}

	public ArrayList<AudioLabel> align(AudioInputStream stream, AudioLabel[] words, DataSequence dataSequence) throws ImplementationError
	{
		dataSource.setInputStream(stream, "input");
		this.frontend.initialize();
		
		ArrayList<FloatData> allData = new ArrayList<FloatData>();
		Data data = null;
		while ((data = this.frontend.getData()) != null) {
			if (data.getClass() != FloatData.class) continue;
			allData.add((FloatData)data);
		}
		ArrayList<AudioLabel> ret = new ArrayList<AudioLabel>();
		int count = 5;
		for (AudioLabel word : words) {
			FloatData[] wordSequence = extractWordData(word, allData);
			common.Data[] wordSpectrumSequence = new common.Data[0];// = extractWordData(word, dataSequence);
			ret.addAll(this.phonemeSearch.findPhonemes(word, wordSequence, wordSpectrumSequence));
//			if (count-- <= 0) break;
		}
		
		return ret;
	}

	private common.Data[] extractWordData(AudioLabel word, DataSequence dataSequence)
	{
		int start = findIndex(word.getStart(), 0, dataSequence.size() - 1, dataSequence);
		int end = findIndex(word.getEnd(), 0, dataSequence.size() - 1, dataSequence);
		return dataSequence.subList(start, end).toArray(new common.Data[0]);
	}

	private FloatData[] extractWordData(AudioLabel word, ArrayList<FloatData> allData)
	{
		int start = findIndex(word.getStart(), 0, allData.size() - 1, allData);
		int end = findIndex(word.getEnd(), 0, allData.size() - 1, allData);
		return allData.subList(start, end).toArray(new FloatData[0]);
	}

	private int findIndex(double time, int bottom, int top, ArrayList<FloatData> allData)
	{
		if (bottom == top) return bottom;
		int between = (top + bottom) / 2;
		double auxTime = (getStartTime(allData, between) + getStartTime(allData, between + 1)) / 2;
		if (time < auxTime) return findIndex(time, bottom, between, allData);
		else return findIndex(time, between + 1, top, allData);
	}

	private int findIndex(double time, int bottom, int top, DataSequence allData)
	{
		if (bottom == top) return bottom;
		int between = (top + bottom) / 2;
		double auxTime = (getStartTime(allData, between) + getStartTime(allData, between + 1)) / 2;
		if (time < auxTime) return findIndex(time, bottom, between, allData);
		else return findIndex(time, between + 1, top, allData);
	}

	private double getStartTime(DataSequence allData, int i)
	{
		return allData.get(i).getStartTime();
	}

	private double getStartTime(ArrayList<FloatData> allData, int i)
	{
		return (double)allData.get(i).getFirstSampleNumber() / allData.get(i).getSampleRate();
	}
}
