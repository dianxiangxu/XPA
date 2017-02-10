package org.seal.gui;

import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class DebugPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private XPA xpa;
	
	public DebugPanel(XPA xpa) {
		this.xpa = xpa;	
	}

	
	public void localizeFault(){
		if (hasFault()) {

		}
		
	}

	public void fixFault(){
		if (hasFault()) {

		}
		
	}

	private boolean hasFault(){
		if (!xpa.hasWorkingPolicy()) {
            JOptionPane.showMessageDialog(xpa, "There is no policy!");
			return false;
		} 
		
		if (!xpa.hasTests()) {
            JOptionPane.showMessageDialog(xpa, "There are no tests!");
			return false;
		} 
		
		if (!xpa.hasTestFailure()) {
            JOptionPane.showMessageDialog(xpa, "No failure is reported!");
			return false;
			
		}
			
		return true;

	}
}
