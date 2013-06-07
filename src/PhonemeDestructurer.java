import java.util.ArrayList;


public class PhonemeDestructurer {
	
	Speeches speeches;
	double characterTime = 0;
	ArrayList<Data> allData;

	public PhonemeDestructurer(Speeches speeches, Text text, ArrayList<Data> allData)
	{
		this.allData = allData;
		this.characterTime = text.getEstimatedTimePerCharacter();
		this.speeches = speeches;
	}

	public AudioLabel[] process()
	{
		return null;
	}

}
