package org.seal.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public class MutationDiffFrame extends JFrame
{
    
        public MutationDiffFrame(List<DiffItem> originalContent, List<DiffItem> repairedContent){
        JPanel container = new JPanel();
   	    JPanel panelOne = new JPanel();
        
   	    JTextPane tPaneOne = new JTextPane();     
        panelOne.add(tPaneOne);
        tPaneOne.insertComponent(new JLabel("Original"));
        
        appendToPane(tPaneOne, "\n\n", Color.white);
        
        for(DiffItem item:originalContent){
        	appendToPane(tPaneOne, item.line+"\n", item.color);
            	
        }
        JScrollPane panelOneScroll = new JScrollPane(panelOne);
        panelOneScroll.setViewportView(tPaneOne);
                
        JPanel panelTwo = new JPanel();
        JTextPane tPaneTwo = new JTextPane(); 
       
        panelTwo.add(tPaneTwo);
        tPaneTwo.insertComponent(new JLabel("Repaired"));
        appendToPane(tPaneTwo, "\n\n", Color.white);
        
        for(DiffItem item:repairedContent){
        	appendToPane(tPaneTwo, item.line+"\n", item.color);
        }
        
        
        
        
        JScrollPane panelTwoScroll = new JScrollPane(panelTwo);
        panelTwoScroll.setViewportView(tPaneTwo);
        container.setLayout(new GridLayout(1,2));
        container.add(panelOneScroll);
        container.add(panelTwoScroll);
        container.setLayout(new GridLayout(2,0));
        
        
        JPanel panelThree = new JPanel();
        JButton b = new JButton("OK");
    	Dimension d = new Dimension(100,30);
        b.setPreferredSize(d);

        panelThree.add(b);        
        
        container.add(panelThree);
    	
    	
        this.add(container);
        this.setTitle("Difference between original policy and repaired policy");
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
       

        b.addActionListener(new ActionListener() {
			 
            public void actionPerformed(ActionEvent e)
            {
            	dispose();
            }
        });      
 
    }

    private void appendToPane(JTextPane tp, String msg, Color c)
    {
    	StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);

    }

    
   public static class DiffItem{
    	public DiffItem(Color c, String l){
    		color = c;
    		line = l;
    	}
    	Color color;
    	String line;
    }
}