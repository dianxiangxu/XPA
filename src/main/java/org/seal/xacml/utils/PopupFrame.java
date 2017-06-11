package org.seal.xacml.utils;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class PopupFrame extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String title;
	private static String content;
    public PopupFrame(String title, String content){
		JPanel container = new JPanel();
		JTextArea tArea = new JTextArea();  
		tArea.setText(content);
		container.add(tArea);
		JScrollPane scrollPane = new JScrollPane(container);
		
	    this.add(scrollPane);
	    if(title!=null){
	    	this.setTitle(title);
	    }
	    this.pack();
	    this.setLocationRelativeTo(null);
	    this.setVisible(true);
	}
    
    public static void showContent(String title, String content){
    	PopupFrame.title = title; 
    	PopupFrame.content = content;
    	SwingUtilities.invokeLater(new Runnable(){
    		public void run()
    		{
    			new PopupFrame(PopupFrame.title, PopupFrame.content);
        	}
	    });
    }
    
    public static void main(String[] args){
    	PopupFrame.showContent("hello", "content");
    }
}
