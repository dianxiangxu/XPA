package org.seal.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

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
    
    private JTextPane tPane;

    public MutationDiffFrame(List<DiffItem> originalContent, List<DiffItem> repairedContent)
    
    {
    	
    	this.setSize(new Dimension(980,100));
    	this.setMinimumSize( this.getSize() );

        this.setLocationRelativeTo(null);

        this.pack();
        this.setVisible(true);
        JPanel container = new JPanel();
   	 
        JPanel panelOne = new JPanel(new FlowLayout());
        JPanel panelTwo = new JPanel(new FlowLayout());
        
        panelOne.add(new JLabel("original"));
        panelTwo.add(new JLabel("repaired"));
        panelOne.repaint();
        
        panelOne = new JPanel();        

       
       
        tPane = new JTextPane();     
       
        panelOne.add(tPane);
        for(DiffItem item:originalContent){
        	appendToPane(tPane, item.line+"\n", item.color);
            	
        }
        JScrollPane panelOneScroll = new JScrollPane(panelOne);
        
        panelOneScroll.setViewportView(tPane);
                
        panelTwo = new JPanel();        
        tPane = new JTextPane(); 
       
        panelTwo.add(tPane);
        for(DiffItem item:repairedContent){
        	appendToPane(tPane, item.line+"\n", item.color);
        }
        JLabel jLabel = new JLabel("repaired");
        JScrollPane panelTwoScroll = new JScrollPane(panelTwo);
        panelTwoScroll.add(jLabel);
        
        panelTwoScroll.setViewportView(tPane);
        container.setLayout(new GridLayout(1,2));
        container.add(panelOneScroll);
        container.add(panelTwoScroll);

        this.add(container);

        
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