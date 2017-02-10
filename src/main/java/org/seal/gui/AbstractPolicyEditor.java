package org.seal.gui;

import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;

public abstract class AbstractPolicyEditor extends JPanel{

	abstract public File getWorkingPolicyFile();	
	
	abstract public void openFile();

	public void newFile() {
		
	}

	public void saveFile() {
		
	}
	
	public void saveAsFile(){
		
	}

	public static File getCurrentDirectory() {
		File resultFile = null;
		File dir1 = new File(".");
		try {
			resultFile = new File(dir1.getCanonicalPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultFile;
	}

}
