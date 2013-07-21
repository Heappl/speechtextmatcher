package phonemeScorers.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import phonemeScorers.IPhonemeScorer;

public class PhonemeScorerExporter
{
    private String filePath;

    public PhonemeScorerExporter(String outputFile)
    {
        this.filePath = outputFile;
    }

    public boolean export(IPhonemeScorer[] scorers)
    {
        try {
            File outputFile = new File(filePath);
            OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(outputFile));
            for (IPhonemeScorer scorer : scorers)
                outputStream.write(scorer.serialize() + "\n");
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
