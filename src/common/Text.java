package common;

import java.util.SortedSet;
import java.util.TreeSet;

class FromStringProducer implements ITextProducer
{
	String str;
	public FromStringProducer(String str) { this.str = str; }
	public String getText() { return this.str; }
}

public class Text {

	private String text;
	private String charRegex = "";
	private double timePerWord = 0;
	private double timePerCharacter = 0;
	
	public Text(ITextProducer producer, double totalTime)
	{
		this.text = producer.getText().replaceAll("\n\\s*\n", ".").replaceAll("[\\s]+", " ");
		SortedSet<Character> chars = new TreeSet<Character>();
        for (int i = 0; i < text.length(); ++i)
        	if (Character.isLetterOrDigit(text.charAt(i)))
        		chars.add(text.charAt(i));
        for (Character c : chars) charRegex += c;
        
        String[] words = getWords();
        this.timePerWord = totalTime / (double)words.length;
        
        int characters = 0;
        for (String word : words) characters += word.length();
        this.timePerCharacter = totalTime / (double) characters;
	}
	public Text(String str, double totalTime)
	{
		this(new FromStringProducer(str), totalTime);
	}
	
	public String[] getSentences()
	{
		return text.split("(\\s*[^" + charRegex + "'\\s]+\\s*)+");
	}
	public String[] getWords()
	{
		return text.split("[^" + charRegex + "']+");
	}
	public double getEstimatedTimePerWord()
	{
		return timePerWord; 
	}
	public double getEstimatedTimePerCharacter()
	{
		return timePerCharacter; 
	}
}
