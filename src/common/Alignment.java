package common;

import java.util.ArrayList;


public class Alignment
{
    private ArrayList<AudioLabel> labels;
    private double score;
    
    public Alignment(ArrayList<AudioLabel> labels, double score)
    {
        this.labels = labels;
        this.score = score;
    }
    
    public ArrayList<AudioLabel> getLabels() { return labels; }
    public double getScore() { return score; }
}