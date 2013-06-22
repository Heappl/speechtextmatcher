import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import common.AudioLabel;


public class AudacityLabelsExporter {

	private String filePath;
	
	public AudacityLabelsExporter(String outputFilePath) {
		this.filePath = outputFilePath;
	}
	
	public boolean export(AudioLabel[] labels)
	{
		try {
			File outputFile = new File(filePath);
			OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(outputFile));
			for (AudioLabel label : labels)
				outputStream.write(label.getStart() + " " + label.getEnd() + " " + label.getLabel() + "\n");
			outputStream.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
