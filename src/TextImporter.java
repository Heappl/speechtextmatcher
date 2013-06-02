import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class TextImporter implements ITextProducer {
	private String filePath;
	public TextImporter(String filePath) {
		this.filePath = filePath;
	}
	public String getText()
	{
        String text = "";
        try {
			BufferedReader fileReader = new BufferedReader(new FileReader(filePath));
			String line;
			while ((line = fileReader.readLine()) != null)
				text += line + "\n";
			fileReader.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return text;
	}
}
