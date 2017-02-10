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
 * It's a panel for editing the AttributeSelector elements of the XACML 2.0
 * standard
 * 
 * @author Albero Jimenez Lazaro & Pablo Galera Morcillo
 * @version 1.3
 */
public class AttributeSelectorPanel extends ElementPanel {

	JLabel jlblreq0 = new JLabel("*");
	JLabel jlblCategory = new JLabel("Category:");
	JComboBox jcmbCategory = new JComboBox(ElementoXACML.getAllCategory());

	JLabel jlblDataType = new JLabel();
	JComboBox jcmbDataType = new JComboBox(ElementoXACML.getAllDataTypes());
	JLabel jlblreq1 = new JLabel("*");
	JLabel jlblreq2 = new JLabel("*");
	JLabel jlblrequerido = new JLabel("* Required");
	JCheckBox jcbMustBePresent = new JCheckBox("MustBePresent");
	JTextField jtxtPath = new JTextField();
	JLabel jlblPath = new JLabel("Path:");

	JLabel jlblcsId = new JLabel("ContextSelectorId:");
	JComboBox jcmbcsId = new JComboBox(ElementoXACML.getAllAttributeId());

	public AttributeSelectorPanel(DefaultMutableTreeNode n) {
		super(n);
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(new MiLayout());
		this.setPreferredSize(new Dimension(550, 150));

		// added for XACML 3.0
		jlblreq0.setForeground(Color.red);
		jlblreq0.setBounds(new Rectangle(15, 30, 10, 20));
		jlblCategory.setBounds(new Rectangle(25, 30, 100, 20));
		jcmbCategory.setPreferredSize(new Dimension(400, 20));
		jcmbCategory.setLocation(135, 30);
		jcmbCategory.setEditable(true);
		jcmbCategory.setSelectedItem((String) ((Elemento3AttributeSelector) elemento).getCategory());
		jcmbCategory.addItemListener(new MiElementItemAdapter(this));

		jlblcsId.setBounds(new Rectangle(25, 120, 100, 20));
		jcmbcsId.setPreferredSize(new Dimension(370, 20));
		jcmbcsId.setLocation(165, 120);
		jcmbcsId.setEditable(true);
		jcmbcsId.setSelectedItem((String) ((Elemento3AttributeSelector) elemento).getContextSelectorId());
		jcmbcsId.addItemListener(new MiElementItemAdapter(this));
		// end add

		jlblreq1.setForeground(Color.red);
		jlblreq1.setBounds(new Rectangle(15, 60, 10, 20));
		jlblDataType.setText("Data Type:");
		jlblDataType.setBounds(new Rectangle(25, 60, 100, 20));
		jcmbDataType.setEditable(true);
		jcmbDataType.setSelectedItem(((Elemento3AttributeSelector) elemento).getDataType());
		jcmbDataType.setPreferredSize(new Dimension(400, 20));
		jcmbDataType.setLocation(135, 60);
		jcmbDataType.addItemListener(new MiElementItemAdapter(this));

		jlblreq2.setForeground(Color.red);
		jlblreq2.setBounds(new Rectangle(15, 90, 10, 20));
		jlblPath.setBounds(new Rectangle(25, 90, 100, 20));
		jtxtPath.setPreferredSize(new Dimension(400, 20));
		jtxtPath.setLocation(135, 90);
		jtxtPath.setText((String) elemento.getAtributos().get("Path"));
		jtxtPath.addKeyListener(new MiElementKeyAdapter(this));

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
		jlblrequerido.setBounds(new Rectangle(135, 180, 100, 20));

		this.add(jlblreq0);
		this.add(jlblCategory);
		this.add(jcmbCategory);
		this.add(jcmbDataType);
		this.add(jlblDataType);
		this.add(jcbMustBePresent);
		this.add(jlblPath);
		this.add(jtxtPath);
		this.add(jlblreq1);
		this.add(jlblreq2);
		this.add(jlblcsId);
		this.add(jcmbcsId);
		this.add(jlblrequerido);
	}

	public void keyReleased(KeyEvent e) {
		Map at = elemento.getAtributos();
		if (e.getSource() == jtxtPath) {
			at.remove("Path");
			at.put("Path", jtxtPath.getText());
		}
		if (dtm != null) {
			dtm.nodeChanged(node);
		}
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == jcmbDataType) {
			((Elemento3AttributeSelector) elemento).setDataType((String) jcmbDataType.getSelectedItem());
		} else if (e.getSource() == jcmbDataType) {
			// added for XACML 3.0
			Map at = elemento.getAtributos();
			at.remove("Category");
			at.put("Category", jcmbCategory.getSelectedItem());
		} else if (e.getSource() == jcmbcsId) {
			Map at = elemento.getAtributos();
			at.remove("ContextSelectorId");
			at.put("ContextSelectorId", jcmbcsId.getSelectedItem());
			// end add
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
			miobservable.change(node);
			if (dtm != null) {
				dtm.nodeChanged(node);
			}
		}

	}
}
