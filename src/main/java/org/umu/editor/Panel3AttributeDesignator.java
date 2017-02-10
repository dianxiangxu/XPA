/*
 * Copyright 2005, 2006 Alberto Jim??nez L??zaro
 *                      Pablo Galera Morcillo (umu-xacml-editor-admin@dif.um.es)
 *                      Dpto. de Ingenier??a de la Informaci??n y las Comunicaciones
 *                      (http://www.diic.um.es:8080/diic/index.jsp)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.umu.editor;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * It's a panel for editing the AttributeDesignator elements of the XACML 2.0
 * standard
 * 
 * @author Albero Jimenez Lazaro & Pablo Galera Morcillo
 * @version 1.3
 */
public class Panel3AttributeDesignator extends ElementPanel {

	JLabel jlblDataType = new JLabel();
	JLabel jlblAttributeId = new JLabel();
	JComboBox jcmbDataType = new JComboBox(ElementoXACML.getAllDataTypes());
	JComboBox jcmbAttributeId = new JComboBox(ElementoXACML.getAllAttributeId());
	JLabel jlblreq1 = new JLabel("*");
	JLabel jlblreq2 = new JLabel("*");
	JLabel jlblrequerido = new JLabel("* Required");
	JCheckBox jcbMustBePresent = new JCheckBox("MustBePresent");
	JTextField jtxtIssuer = new JTextField();
	JLabel jlblIssuer = new JLabel("Issuer:");
	
	// added for XACML 3.0
	JLabel jlblreq0 = new JLabel("*");
	JLabel jlblCategory = new JLabel();
	JComboBox jcmbCategory = new JComboBox(ElementoXACML.getAllCategory());
	// end add

	public Panel3AttributeDesignator(DefaultMutableTreeNode n) {
		super(n);
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(new MiLayout());
		this.setPreferredSize(new Dimension(550, 180));
		
		// added for XACML 3.0
		jlblreq0.setForeground(Color.red);
		jlblreq0.setBounds(new Rectangle(15, 30, 10, 20));
		jlblCategory.setText("Category:");
		jlblCategory.setBounds(new Rectangle(25, 30, 100, 20));
		jcmbCategory.setPreferredSize(new Dimension(400, 20));
		jcmbCategory.setLocation(135, 30);
		jcmbCategory.setEditable(true);
		jcmbCategory.setSelectedItem((String) ((Elemento3AttributeDesignator)elemento).getCategory());
		jcmbCategory.addItemListener(new MiElementItemAdapter(this));
		
		this.add(jlblreq0);
		this.add(jlblCategory);
		this.add(jcmbCategory);
		// end add
		
		jlblreq1.setForeground(Color.red);
		jlblreq1.setBounds(new Rectangle(15, 60, 10, 20));
		jlblAttributeId.setText("AttributeId:");
		jlblAttributeId.setBounds(new Rectangle(25, 60, 100, 20));
		jcmbAttributeId.setPreferredSize(new Dimension(400, 20));
		jcmbAttributeId.setLocation(135, 60);
		jcmbAttributeId.setEditable(true);
		jcmbAttributeId.setSelectedItem((String) elemento.getID());
		jcmbAttributeId.addItemListener(new MiElementItemAdapter(this));
		jlblreq2.setForeground(Color.red);
		jlblreq2.setBounds(new Rectangle(15, 90, 10, 20));
		jlblDataType.setText("Data Type:");
		jlblDataType.setBounds(new Rectangle(25, 90, 100, 20));
		jcmbDataType.setEditable(true);
		jcmbDataType.setSelectedItem(((ElementoAttributeDesignatorAbstract) elemento).getDataType());
		jcmbDataType.setPreferredSize(new Dimension(400, 20));
		jcmbDataType.setLocation(135, 90);
		jcmbDataType.addItemListener(new MiElementItemAdapter(this));

		jlblIssuer.setBounds(new Rectangle(25, 120, 100, 20));
		jtxtIssuer.setPreferredSize(new Dimension(400, 20));
		jtxtIssuer.setLocation(135, 120);
		jtxtIssuer.setText((String) elemento.getAtributos().get("Issuer"));
		jtxtIssuer.addKeyListener(new MiElementKeyAdapter(this));

		jcbMustBePresent.setPreferredSize(new Dimension(150, 20));
		jcbMustBePresent.setLocation(25, 150);
		if (elemento.getAtributos().get("MustBePresent") != null) {
			if (elemento.getAtributos().get("MustBePresent").equals("true")) {
				jcbMustBePresent.setSelected(true);
			} else {
				jcbMustBePresent.setSelected(false);
			}
		}
		jcbMustBePresent.addItemListener(new MiElementItemAdapter(this));

		jlblrequerido.setForeground(Color.red);
		jlblrequerido.setBounds(new Rectangle(135, 190, 100, 20));
		this.add(jlblAttributeId);
		this.add(jcmbDataType);
		this.add(jcmbAttributeId);
		this.add(jlblDataType);
		this.add(jcbMustBePresent);
		this.add(jlblIssuer);
		this.add(jtxtIssuer);
		this.add(jlblreq1);
		this.add(jlblreq2);
		this.add(jlblrequerido);
	}

	public void keyReleased(KeyEvent e) {
		Map at = elemento.getAtributos();
		if (e.getSource() == jtxtIssuer) {
			at.remove("Issuer");
			at.put("Issuer", jtxtIssuer.getText());
		}
		if (dtm != null) {
			dtm.nodeChanged(node);
		}
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == jcmbDataType) {
			((ElementoAttributeDesignatorAbstract) elemento).setDataType((String) jcmbDataType.getSelectedItem());
		} else if (e.getSource() == jcmbCategory) {
			// added for XACML 3.0
			Map at = elemento.getAtributos();
			at.remove("Category");
			at.put("Category", jcmbCategory.getSelectedItem());
			// end add
		} else if (e.getSource() == jcmbAttributeId) {
			Map at = elemento.getAtributos();
			at.remove("AttributeId");
			at.put("AttributeId", jcmbAttributeId.getSelectedItem());
		} else if (e.getSource() == jcbMustBePresent) {
			Map at = elemento.getAtributos();
			if (elemento.getAtributos().get("MustBePresent") != null) {
				if (elemento.getAtributos().get("MustBePresent").equals("true")) {
					jcbMustBePresent.setSelected(false);
					at.put("MustBePresent", "false");
				} else {
					jcbMustBePresent.setSelected(true);
					at.put("MustBePresent", "true");
				}
			} else {
				jcbMustBePresent.setSelected(true);
				at.put("MustBePresent", "true");
			}
		}
		miobservable.change(node);
		if (dtm != null) {
			dtm.nodeChanged(node);
		}
	}
}
