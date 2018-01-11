package org.seal.xacml.components;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.util.TimerTask;

import javax.swing.JFrame;

public class ProgressBarTask extends TimerTask {
	JFrame frame ;
	int count ;
	String msg;
	
	public ProgressBarTask() {
		frame = new JFrame();
		frame.setLayout(new FlowLayout());
        
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 30);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
        msg = "This may take a while ";
    	
 	}
	
	
    @Override
	public void run() {
    	frame.setVisible(true);
        
		int mod = count%3; 
		if(mod == 0) {
			frame.setTitle(msg + ".");
				
		} else if(mod == 1) {
			frame.setTitle(msg + "..");
			
		} else {
			frame.setTitle(msg + "...");
			
		}
		frame.revalidate();
		frame.repaint();
		count++;
		
	}
	
	public void close() {
		frame.setVisible(false);
		frame.dispose();
	}
	
	
}
