package graphemesToPhonemesConverters;
import common.Text;

public class TextToPhonemeSequenceConverter
{
    private final IWordToPhonemesConverter converter;

    public TextToPhonemeSequenceConverter(IWordToPhonemesConverter converter)
    {
        this.converter = converter;
    }

    public String[][] convert(String text)
    {
        String[] words = new Text(text, 0).getWords();
        String[][] ret = new String[words.length][];
        
        for (int i = 0; i < words.length; ++i) {
            ret[i] = this.converter.convert(words[i]).get(0).split(" ");
        }
        return ret;
    }
}
