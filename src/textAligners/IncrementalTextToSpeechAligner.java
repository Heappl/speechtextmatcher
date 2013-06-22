package textAligners;
import java.util.ArrayList;


import common.AudioLabel;
import common.ITextProducer;
import common.Speeches;
import common.Text;



public class IncrementalTextToSpeechAligner implements ITextToSpeechAligner {

	ITextToSpeechAligner aligner = null;
	
	public IncrementalTextToSpeechAligner(ITextToSpeechAligner concreteAligner)
	{
		this.aligner = concreteAligner;
	}
	
	class ReducableTextProducer implements ITextProducer
	{
		private ArrayList<String> sentences = null;
		
		public ReducableTextProducer(String[] sentences)
		{
			this.sentences = new ArrayList<String>();
			for (String sentence : sentences) this.sentences.add(sentence);
		}
		
		void removeAllFromFront(String text)
		{
			while (!sentences.isEmpty() && (text.indexOf(sentences.get(0)) >= 0)) {
				sentences.remove(0);
			}
		}
		void removeAllFromBack(String text)
		{
			while (!sentences.isEmpty() && (text.indexOf(sentences.get(sentences.size() - 1)) >= 0)) {
				sentences.remove(sentences.size() - 1);
			}
		}
		int size() {
			return sentences.size();
		}

		public String getText()
		{
			return join(".");
		}
		private String join(String delimiter)
		{
			if (sentences.isEmpty()) return "";
			String ret = sentences.get(0);
			for (int i = 1; i < sentences.size(); ++i) {
				ret += delimiter + sentences.get(i);
			}
			return ret;
		}
	};
	
	@Override
	public AudioLabel[] findMatching(Text text, Speeches speeches)
	{
		ArrayList<AudioLabel> front = new ArrayList<AudioLabel>();
		ArrayList<AudioLabel> back = new ArrayList<AudioLabel>();
		
		ReducableTextProducer textProducer = new ReducableTextProducer(text.getSentences());
		while (speeches.size() > 0) {
			System.err.println("left for alignment: " + speeches.size() + " " + textProducer.size());
			AudioLabel[] curr = aligner.findMatching(new Text(textProducer, speeches.getTotalTime()), speeches);
			textProducer.removeAllFromFront(curr[0].getLabel());
			removeAllFromFront(speeches, curr[0].getEnd() - curr[0].getStart());
			textProducer.removeAllFromBack(curr[curr.length - 1].getLabel());
			removeAllFromBack(speeches, curr[curr.length - 1].getEnd() - curr[curr.length - 1].getStart());
			front.add(curr[0]);
			back.add(curr[curr.length - 1]);
		}
		
		for (AudioLabel label : back) front.add(label);
		return front.toArray(new AudioLabel[0]);
	}

	private void removeAllFromBack(Speeches speeches, double time)
	{
		while ((time > 0.1) && (speeches.size() > 0)) {
			double speechTime = speeches.get(speeches.size() - 1).getTime();
			if (time - speechTime > -0.1) {
				time -= speechTime;
				speeches.pop_back();
			} else break;
		}
	}

	private void removeAllFromFront(Speeches speeches, double time)
	{
		while ((time > 0.1) && (speeches.size() > 0)) {
			double speechTime = speeches.get(0).getTime();
			if (time - speechTime > -0.1) {
				time -= speechTime;
				speeches.pop_front();
			} else break;
		}
	}
}
