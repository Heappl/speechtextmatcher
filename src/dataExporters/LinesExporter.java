package dataExporters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class LinesExporter
{

	private String filePath;
	
	public LinesExporter(String outputFilePath)
	{
		this.filePath = outputFilePath;
	}

	public boolean export(String[] lines)
	{
		try {
			File outputFile = new File(filePath);
			OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(outputFile));
			for (String line : lines)
				outputStream.write(line + "\n");
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
