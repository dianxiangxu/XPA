package org.seal.xacml.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.JButton;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PopupFrame extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String title;
	private static String content;
    public PopupFrame(String title, String content){
    	
    	////
    	JButton b = new JButton("OK");
    	
    	////
    	
		JPanel container = new JPanel();
		JTextArea tArea = new JTextArea(); 
		tArea.setEditable(false);
		tArea.setText(content);

	    JScrollPane scroll = new JScrollPane(tArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBounds(10,11,455,259);

		container.add(scroll);
		
		tArea.setPreferredSize(new Dimension(700, 800));
		container.setPreferredSize(new Dimension(800, 800));
		
		//////
		container.add(b, BorderLayout.SOUTH);

		
		b.addActionListener(new ActionListener() {
			 
	            public void actionPerformed(ActionEvent e)
	            {
	            	dispose();
	            }
	        });      
	 
	
		//////
		 
		 
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
