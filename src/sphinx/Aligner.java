package sphinx;
/*
 * Copyright 1999-2013 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import edu.cmu.sphinx.result.WordResult;

/**
 * This is a simple tool to align audio to text and dump a database
 * for the training/evaluation.
 * 
 * You need to provide a model, dictionary, audio and the text to align. 
 */
public class Aligner {
    
    static int diff = 200;

    public static ArrayList<WordResult> align(URL acousticModel, URL dictionary, AudioInputStream stream, String text) throws Exception
    {
        GrammarAligner aligner = new GrammarAligner(acousticModel, dictionary, null);
        ArrayList<WordResult> results = aligner.align(stream, text);
        return results;
    } 
}

