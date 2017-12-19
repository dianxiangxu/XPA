package org.seal.xacml.components;

import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class ProgressBarUtil {
	JFrame frame ;
	public ProgressBarUtil() {
		frame = new JFrame();
		frame.setLayout(new FlowLayout());
        
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 30);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        
	}
	public void showProgress(double cValue,int max) {
		int percent = (int)((cValue/max)*100);
		frame.setTitle(percent + " % completed ..");
		frame.revalidate();
		frame.repaint();
		
   	}
	
	public void close() {
		frame.setVisible(false);
		frame.dispose();
	}
}
