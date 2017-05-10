package org.seal.xpa.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.seal.semanticMutation.Mutant;

public class MutantUtil {
	public static Vector<Vector<Object>> getVectorsForMutants(List<Mutant> mutants){
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		int i = 1;
		for(Mutant mutant:mutants){
			Vector<Object> vector = new Vector<Object>();
			vector.add(i++);		// sequence number
			vector.add(mutant.getName());	// mutant name
			vector.add(mutant.getName() + "." + PropertiesLoader.getProperties("config").getProperty("mutantFileExtension"));
			vector.add(Arrays.toString(mutant.getFaultLocations().toArray()));
			vector.add("");	
			data.add(vector);
		}
		return data;
	}
	
	public static Vector<Object> getVector(Mutant mutant, String policyName, String extension){
		Vector<Object> vector = new Vector<Object>();
		vector.add("");		// sequence number
		vector.add(policyName + "_" +mutant.getName());	// mutant name
		vector.add(policyName + "_" +mutant.getName() + extension);
		vector.add(Arrays.toString(mutant.getFaultLocations().toArray()));
		vector.add("");	
		return vector;
	}
	
	public static String getMutantFileName(Mutant mutant){
		return mutant.getName() + "." + PropertiesLoader.getProperties("config").getProperty("mutantFileExtension");
	}
	
	public static String getPolicyName(String mutantsSpreadSheetFile){
		String[] tokens = mutantsSpreadSheetFile.split(File.separator);
		return tokens[tokens.length-1].split("_")[0];
	}
	
	public static File getMutantsFolderForPolicyFile(File policyFile){
		return new File(policyFile.getParent()+File.separator+PropertiesLoader.getProperties("config").getProperty("mutantsFolderName"));
		
	}
	
	public static void main(String args[]){
	   String file1 = "/home/roshanshrestha/Project/XPA/Experiments/conference3/mutants/conference3_CRE11-repaired.xml"; 
       String file2 = "/home/roshanshrestha/Project/XPA/Experiments/conference3/mutants/conference3_CRE11.xml"; 

       final JTextArea edit = new JTextArea(30, 60);
       edit.setText("one\ntwo\nthree");
       edit.append("\nfour\nfive");

       JButton read = new JButton("Read TextAreaLoad.txt");
       read.addActionListener( new ActionListener()
       {
           public void actionPerformed(ActionEvent e)
           {
               try
               {
                   FileReader reader = new FileReader( "TextAreaLoad.txt" );
                   BufferedReader br = new BufferedReader(reader);
                   edit.read( br, null );
                   br.close();
                   edit.requestFocus();
               }
               catch(Exception e2) { System.out.println(e2); }
           }
       });

       JButton write = new JButton("Write TextAreaLoad.txt");
       write.addActionListener( new ActionListener()
       {
           public void actionPerformed(ActionEvent e)
           {
               try
               {
                   FileWriter writer = new FileWriter( "TextAreaLoad.txt" );
                   BufferedWriter bw = new BufferedWriter( writer );
                   edit.write( bw );
                   bw.close();
                   edit.setText("");
                   edit.requestFocus();
               }
               catch(Exception e2) {}
           }
       });

       JFrame frame = new JFrame("TextArea Load");
       frame.getContentPane().add( new JScrollPane(edit), BorderLayout.NORTH );
       frame.getContentPane().add(read, BorderLayout.WEST);
       frame.getContentPane().add(write, BorderLayout.EAST);
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       frame.pack();
       frame.setLocationRelativeTo( null );
       frame.setVisible(true);
       
	}

}
