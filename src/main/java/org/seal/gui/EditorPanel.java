package org.seal.gui;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.umu.editor.XMLFileFilter;

public class EditorPanel extends AbstractPolicyEditor {
	private XPA xpa;
	
	private File workingPolicyFile;

	private JTextArea textArea = new JTextArea();
	
	public EditorPanel(XPA xpa) {
		this.xpa = xpa;
		textArea.setEditable(false);
		setLayout(new BorderLayout());
		add(new JScrollPane(textArea), BorderLayout.CENTER);
	}

	public File getWorkingPolicyFile(){
		return workingPolicyFile;
	}
	
	public void setWorkingPolicyFile(File newPolicyFile){
		workingPolicyFile = newPolicyFile;
	}

	public void openFile(){
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setCurrentDirectory(getCurrentDirectory());

		fileChooser.setFileFilter(new XMLFileFilter("xml"));
		fileChooser.setDialogTitle("Open Policy");

		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			workingPolicyFile = fileChooser.getSelectedFile();
			if (!workingPolicyFile.toString().endsWith(".xml")) {
					JOptionPane.showMessageDialog(null,
							"The open File is not a policy *.xml",
							"Error of Selection",
							JOptionPane.WARNING_MESSAGE);
			} else {
				try {
					File file = new File(workingPolicyFile.getParent() + File.separator + "test_suites");
					if(!file.isDirectory() && !file.exists()){
						file.mkdir();
					}
					
					textArea.setText(readTextFile(workingPolicyFile));
					xpa.setTitle("UMU-XACML-Editor - "
							+ workingPolicyFile.getAbsolutePath());

				}
				catch(Exception e){
				}
			}
		}
	}

	public static String readTextFile(File file){
		String text = "";
		if (file==null || !file.exists())
			return text;
		Scanner in = null; 
		try {
			in = new Scanner(new FileReader(file));
			while (in.hasNextLine())
				text += in.nextLine()+"\n";
		} catch (IOException ioe){
		}
		finally {
			if (in!=null)
				in.close();
		}
		return text;
	}

	
}
