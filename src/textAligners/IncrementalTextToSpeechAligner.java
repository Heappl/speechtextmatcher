package textAligners;
import java.util.ArrayList;
import java.util.Collections;


import common.AudioLabel;
import common.ITextProducer;
import common.Speeches;
import common.Text;
import common.exceptions.ImplementationError;



public class IncrementalTextToSpeechAligner implements ITextToSpeechAligner {

	ITextToSpeechAligner aligner = null;
	
	public IncrementalTextToSpeechAligner(ITextToSpeechAligner concreteAligner)
	{
		this.aligner = concreteAligner;
	}
	
	class ReducableTextProducer implements ITextProducer
	{
		private ArrayList<String> sentences = new ArrayList<String>();
		
		public ReducableTextProducer(String[] sentences)
		{
			for (String sentence : sentences) this.sentences.add(sentence);
		}
		
		void removeAllFromFront(String text)
		{
		    while (!this.sentences.isEmpty() && !text.isEmpty()) {
		        String current = this.sentences.get(0);
		        int index = text.indexOf(current);
		        if (index != 0)
		            throw new ImplementationError("something missing: ->" + text + "<- " + current);
		        this.sentences.remove(0);
		        if (text.length() == current.length()) break;
		        text = text.substring(current.length() + 1);
		    }
		}
		void removeAllFromBack(String text)
		{
            while (!this.sentences.isEmpty() && !text.isEmpty()) {
                String current = this.sentences.get(this.size() - 1);
                int index = text.indexOf(current, text.length() - current.length());
                if (index + current.length() != text.length())
                    throw new ImplementationError("something missing: ->" + text + "<- " + current);
                this.sentences.remove(this.size() - 1);
                if (text.length() == current.length()) break;
                text = text.substring(0, index - 1);
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
			removeAllFromFront(speeches, curr[0].getEnd());
			textProducer.removeAllFromBack(curr[curr.length - 1].getLabel());
			removeAllFromBack(speeches, curr[curr.length - 1].getStart());
			front.add(curr[0]);
			back.add(curr[curr.length - 1]);
		}
		
		Collections.reverse(back);
		for (AudioLabel label : back) front.add(label);
		return front.toArray(new AudioLabel[0]);
	}

	private void removeAllFromBack(Speeches speeches, double time)
	{
		while (speeches.size() > 0) {
		    if (speeches.get(speeches.size() - 1).getStartTime() + 0.1 >= time)
    			speeches.pop_back();
			else break;
		}
	}

	private void removeAllFromFront(Speeches speeches, double time)
	{
        while (speeches.size() > 0) {
            if (speeches.get(0).getEndTime() - 0.1 <= time)
                speeches.pop_front();
            else break;
        }
	}
}
