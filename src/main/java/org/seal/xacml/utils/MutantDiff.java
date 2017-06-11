package org.seal.xacml.utils;

import java.awt.Color;
import java.io.*;
import java.util.*;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringEscapeUtils;
import org.seal.gui.MutationDiffFrame;

import difflib.*;

public class MutantDiff {
	static List<MutationDiffFrame.DiffItem> originalContents;
    static List<MutationDiffFrame.DiffItem> repairedContents;
    
  private static List<String> fileToLines(String filename) {
    List<String> lines = new LinkedList<String>();
    String line = "";
    try {
      BufferedReader in = new BufferedReader(new FileReader(filename));
      while ((line = in.readLine()) != null) {
        lines.add(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return lines;
  }

  public static void show(String originalFile, String repairedFile) {
	List<String> original = fileToLines(originalFile);
    List<String> revised  = fileToLines(repairedFile);

    StringBuilder sb = new StringBuilder();
    DiffRowGenerator.Builder builder = new DiffRowGenerator.Builder();
    DiffRowGenerator dfg = builder.build();
    List<DiffRow> rows = dfg.generateDiffRows(original, revised);
    originalContents = new ArrayList<MutationDiffFrame.DiffItem>();
    repairedContents = new ArrayList<MutationDiffFrame.DiffItem>();
    
    for (final DiffRow diffRow : rows)
    {
    	String newLine = StringEscapeUtils.unescapeHtml4(diffRow.getNewLine()).replaceAll("<br>", "");
    	String oldLine = StringEscapeUtils.unescapeHtml4(diffRow.getOldLine()).replaceAll("<br>", "");
      if (diffRow.getTag().equals(DiffRow.Tag.INSERT)) 
      {
        repairedContents.add(new MutationDiffFrame.DiffItem(Color.GREEN,"+ " + newLine));
      }
      else if (diffRow.getTag().equals(DiffRow.Tag.DELETE))
      {
    	originalContents.add(new MutationDiffFrame.DiffItem(Color.RED,"- " + oldLine));
          
      }
      else if (diffRow.getTag().equals(DiffRow.Tag.CHANGE))
      {
    	 repairedContents.add(new MutationDiffFrame.DiffItem(Color.RED,newLine));
    	 originalContents.add(new MutationDiffFrame.DiffItem(Color.BLACK,oldLine));
      }
    }

    SwingUtilities.invokeLater(new Runnable()
    {
        public void run()
        {
            new MutationDiffFrame(originalContents,repairedContents);
        }
    });
    
     }
  
  public static void main(String args[]){
	  String file1 = "conference3_CRE14.xml";
	  String file2 = "conference3_CRE14-repaired.xml";
	  String base = "/home/roshanshrestha/Project/XPA/Experiments/conference3/mutants/";
	  MutantDiff.show(base+file1, base+file2);
	  
  }
  
   
}