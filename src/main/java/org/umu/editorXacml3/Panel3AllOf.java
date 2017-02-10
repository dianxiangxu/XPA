package org.umu.editorXacml3;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Map;
import java.util.Observable;
import java.util.Vector;

/**
 * This panel should be able to show the information of <match> elements, which
 * are under <AllOf>
 * 
 * @author Zehao Cheng
 */
public class Panel3AllOf extends ElementPanel {
	JLabel jlblMatch1 = new JLabel("Match Preview");
	JScrollPane jspMatchPane;
	JList jlMatchList = new JList();

	/**
	 * @param node
	 *            that represents the element to be dealt with
	 */
	public Panel3AllOf(DefaultMutableTreeNode n) {
		super(n);
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * Create main contents of the panel.
	 * 
	 * @throws Exception
	 */
	private void jbInit() throws Exception {
		this.setLayout(new MiLayout());
		this.setPreferredSize(new Dimension(550, 180));

		jspMatchPane = new JScrollPane(jlMatchList);
		jlMatchList.setEnabled(true);
		jlMatchList.setSelectionBackground(Color.CYAN);
		jlMatchList.addListSelectionListener(new Listener());
		jspMatchPane.setAutoscrolls(true);
		jspMatchPane.setPreferredSize(new Dimension(640, 380));
		jspMatchPane.setLocation(10, 30);

		jlblMatch1.setBounds(new Rectangle(10, 10, 100, 20));
		this.add(jlblMatch1);
		this.add(jspMatchPane);
		listUpdate();
	}

	/**
	 * Update the list for <Match> elements 
	 */
	public void listUpdate() {
		int listIndex = 1;
		Elemento3Match elemMatch;
		Enumeration<DefaultMutableTreeNode> subElem;
		Enumeration<DefaultMutableTreeNode> matchSubElem;
		DefaultMutableTreeNode matchAV;
		Vector<String> vec1;
		
		subElem = node.children();
		vec1 = new Vector<String>();
		while (subElem.hasMoreElements()) {
			DefaultMutableTreeNode dmt = (DefaultMutableTreeNode) subElem.nextElement();
			if (dmt.getUserObject() instanceof Elemento3Match) {
				elemMatch = (Elemento3Match) dmt.getUserObject();
				vec1.add(listIndex + ". Match ID: " + elemMatch.getID());

				matchSubElem = dmt.children();
				matchAV = (DefaultMutableTreeNode) matchSubElem.nextElement();
				Object objAV = matchAV.getUserObject(); // the object should be an ElementoAttributeValue
				if (objAV instanceof ElementoAttributeValue) {
					vec1.add("     <AttributeValue>");
					vec1.add("          DataType: " + ((ElementoAttributeValue) objAV).getDataType());
					vec1.add("          Value: " + ((ElementoAttributeValue) objAV).getContenido());
				}
				
				matchAV = (DefaultMutableTreeNode) matchSubElem.nextElement();
				objAV = matchAV.getUserObject();
				if (objAV instanceof Elemento3AttributeDesignator) {
					vec1.add("       <AttributeDesignator>");
					vec1.add("            Category: " + ((Elemento3AttributeDesignator) objAV).getCategory());
					vec1.add("            AttributeId: " + ((Elemento3AttributeDesignator) objAV).getID());
					vec1.add("            DataType: " + ((Elemento3AttributeDesignator) objAV).getDataType());
				} else if (objAV instanceof Elemento3AttributeSelector) {
					vec1.add("       <AttributeSelector>");
					vec1.add("            Category: " + ((Elemento3AttributeSelector) objAV).getCategory());
					vec1.add("            DataType: " + ((Elemento3AttributeSelector) objAV).getDataType());
					vec1.add("            Path: " + ((Elemento3AttributeSelector) objAV).getPath());
				}
				
				listIndex++;
			}
		}
		jlMatchList.setListData(vec1);
	}
	
	private class Listener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			//System.out.println(e.getFirstIndex());
			
			int selected = e.getFirstIndex();
			
		}
	}

	// TODO: implement this method as needed
	public void keyReleased(KeyEvent e) {
		/*
		 * Map at = elemento.getAtributos(); if (e.getSource() == jtxtIssuer) {
		 * at.remove("Issuer"); at.put("Issuer", jtxtIssuer.getText()); } if
		 * (dtm != null) { dtm.nodeChanged(node); }
		 */
	}

		
	private void ListListener(ItemEvent e) {
		
	}
	// TODO: implement this method as needed
	public void itemStateChanged(ItemEvent e) {
		/*
		 * if (e.getSource() == jcmbDataType) {
		 * ((ElementoAttributeDesignatorAbstract) elemento).setDataType((String)
		 * jcmbDataType.getSelectedItem()); } else if (e.getSource() ==
		 * jcmbCategory) { // added for XACML 3.0 Map at =
		 * elemento.getAtributos(); at.remove("Category"); at.put("Category",
		 * jcmbCategory.getSelectedItem()); // end add } else if (e.getSource()
		 * == jcmbAttributeId) { Map at = elemento.getAtributos();
		 * at.remove("AttributeId"); at.put("AttributeId",
		 * jcmbAttributeId.getSelectedItem()); } else if (e.getSource() ==
		 * jcbMustBePresent) { Map at = elemento.getAtributos(); if
		 * (elemento.getAtributos().get("MustBePresent") != null) { if
		 * (elemento.getAtributos().get("MustBePresent").equals("true")) {
		 * jcbMustBePresent.setSelected(false); at.put("MustBePresent",
		 * "false"); } else { jcbMustBePresent.setSelected(true);
		 * at.put("MustBePresent", "true"); } } else {
		 * jcbMustBePresent.setSelected(true); at.put("MustBePresent", "true");
		 * } } miobservable.change(node); if (dtm != null) {
		 * dtm.nodeChanged(node); }
		 */
	}

}
