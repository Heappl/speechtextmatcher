package textAligners;
import common.AudioLabel;
import common.Speeches;
import common.Text;


public interface ITextToSpeechAligner {
	public AudioLabel[] findMatching(Text text, Speeches speeches);
}
