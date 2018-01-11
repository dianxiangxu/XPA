package org.seal.xacml.components;

import java.util.Timer;

import javax.swing.JPanel;

public class JPanelPB extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Timer timer;
	protected ProgressBarTask timerTask;
	
	public void startProgressStatus() {
		timer = new Timer();
		timerTask = new ProgressBarTask();
		timer.scheduleAtFixedRate(timerTask, 1800, 500);
	}
	
	public void stopProgressStatus() {
		timerTask.cancel();
		timer.cancel();
		timerTask.close();
	}
	
}
