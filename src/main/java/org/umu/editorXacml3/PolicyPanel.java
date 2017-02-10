/*
 * Copyright 2005, 2006 Alberto Jim?nez L?zaro
 *                      Pablo Galera Morcillo (umu-xacml-editor-admin@dif.um.es)
 *                      Dpto. de Ingenier?a de la Informaci?n y las Comunicaciones
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
package org.umu.editorXacml3;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

/* *************************************************************************
 * Title: PolicyPanel
 *
 * Description:*//**
 * This panel is used for setting up a Policy Element.
 * 
 * @author Alberto Jim?nez L?zaro,Pablo Galera Morcillo
 * 
 * @version 1.3
 ***************************************************************************/
public class PolicyPanel extends ElementPanel {

	JScrollPane jscrllPanel;
	JLabel jlblSchema = new JLabel();
	JComboBox jcmbSchema = new JComboBox(ElementoXACML.getAllSchemas());
	JComboBox jcmbRCA = new JComboBox(ElementoXACML.getAllRuleCombiningAlgorithm());
	JLabel jlblId = new JLabel();
	JTextField jtxtId = new JTextField();
	JLabel jlblVer = new JLabel();
	JTextField jtxtVer = new JTextField();
	JLabel jlblreq0 = new JLabel("*");
	JLabel jlblreq1 = new JLabel("*");
	JLabel jlblreq2 = new JLabel("*");
	JLabel jlblreq3 = new JLabel("*");
	JLabel jlblrequerido = new JLabel("* Required");

	JLabel jlblRCA = new JLabel();
	JLabel jlblDescription = new JLabel();
	JTextArea jtxtaDescription = new JTextArea();
	ElementoDescription descripcion;

	public PolicyPanel(DefaultMutableTreeNode n) {
		super(n);
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(new MiLayout());

		jlblreq0.setForeground(Color.red);
		jlblreq0.setBounds(new Rectangle(15, 30, 10, 20));
		jlblSchema.setText("xmlns:");
		jlblSchema.setBounds(new Rectangle(25, 30, 100, 20));
		jcmbSchema.setLocation(135, 30);
		jcmbSchema.setPreferredSize(new Dimension(450, 20));
		jcmbSchema.setEditable(true);
		jcmbSchema.setSelectedItem((String) elemento.getAtributos().get("xmlns"));
		jcmbSchema.addItemListener(new MiElementItemAdapter(this));
		jcmbSchema.setSelectedItem("urn:oasis:names:tc:xacml:3.0:core:schema:wd-17");
		
		jlblreq1.setForeground(Color.red);
		jlblreq1.setBounds(new Rectangle(15, 60, 10, 20));
		jlblVer.setText("Version:");
		jlblVer.setBounds(new Rectangle(25, 60, 100, 20));
		jtxtVer.setText((String) elemento.getAtributos().get("Version"));
		jtxtVer.setLocation(135, 60);
		jtxtVer.setPreferredSize(new Dimension(75, 20));
		jtxtVer.addKeyListener(new MiElementKeyAdapter(this));

		jlblreq2.setForeground(Color.red);
		jlblreq2.setBounds(new Rectangle(15, 90, 10, 20));
		jlblId.setText("PolicyId:");
		jlblId.setBounds(new Rectangle(25, 90, 100, 20));
		jtxtId.setText(elemento.getID());
		jtxtId.setLocation(135, 90);
		jtxtId.setPreferredSize(new Dimension(450, 20));
		jtxtId.addKeyListener(new MiElementKeyAdapter(this));

		jlblreq3.setForeground(Color.red);
		jlblreq3.setBounds(new Rectangle(15, 120, 10, 20));
		jlblRCA.setText("RuleCombAlg:");
		jlblRCA.setBounds(new Rectangle(25, 120, 100, 20));
		jcmbRCA.setPreferredSize(new Dimension(450, 20));
		jcmbRCA.setLocation(135, 120);
		jcmbRCA.setEditable(true);
		jcmbRCA.setSelectedItem((String) elemento.getAtributos().get("RuleCombiningAlgId"));
		jcmbRCA.addItemListener(new MiElementItemAdapter(this));

		jlblDescription.setText("Description:");
		jlblDescription.setBounds(new Rectangle(25, 150, 100, 20));
		Enumeration subelementos = node.children();
		while (subelementos.hasMoreElements()) {
			DefaultMutableTreeNode aux = (DefaultMutableTreeNode) subelementos.nextElement();
			Object eaux = aux.getUserObject();
			if (eaux instanceof ElementoDescription) {
				descripcion = (ElementoDescription) eaux;
				jtxtaDescription.setText(((ElementoDescription) eaux).getContenido());
			}
		}

		jtxtaDescription.addKeyListener(new MiElementKeyAdapter(this));

		jscrllPanel = new JScrollPane(jtxtaDescription);
		jscrllPanel.setAutoscrolls(true);
		jscrllPanel.setLocation(135, 150);
		jscrllPanel.setPreferredSize(new Dimension(450, 120));

		jlblrequerido.setForeground(Color.red);
		jlblrequerido.setBounds(new Rectangle(135, 300, 100, 20));

		if (((DefaultMutableTreeNode) node.getParent()).isRoot()) {
			this.add(jlblreq0);
			this.add(jlblSchema);
			this.add(jcmbSchema);
		}
		
		this.add(jlblreq1);
		this.add(jlblreq2);
		this.add(jlblreq3);
		this.add(jlblrequerido);
		this.add(jlblVer);
		this.add(jtxtVer);
		this.add(jlblId);
		this.add(jtxtId);
		this.add(jcmbRCA);
		this.add(jlblRCA);
		this.add(jlblDescription);
		this.add(jscrllPanel);
	}

	public void keyReleased(KeyEvent e) {
		if (e.getSource() == jtxtVer) {
			Map at = elemento.getAtributos();
			at.remove("Version");
			at.put("Version", jtxtVer.getText());
		} else if (e.getSource() == jtxtId) {
			Map at = elemento.getAtributos();
			at.remove("PolicyId");
			at.put("PolicyId", jtxtId.getText());
		} else {

			// There may be no element Description
			if (descripcion == null) {
				descripcion = (ElementoDescription) ElementoXACMLFactoryImpl.getInstance().obtenerElementoXACML(
						"Description", new Hashtable());

				DefaultMutableTreeNode naux = new DefaultMutableTreeNode(descripcion);
				dtm.insertNodeInto(naux, node, 0);
			}
			descripcion.setContenido(jtxtaDescription.getText());
		}
		if (dtm != null) {
			dtm.nodeChanged(node);
		}
	}

	// modify the tree
	public void itemStateChanged(ItemEvent e) {
		Map mapa = elemento.getAtributos();
		if (e.getSource() == jcmbSchema) {
			mapa.put("xmlns", (String) jcmbSchema.getSelectedItem());
		} else {
			mapa.put("RuleCombiningAlgId", (String) jcmbRCA.getSelectedItem());
		}
		elemento.setAtributos(mapa);
		if (dtm != null) {
			dtm.nodeChanged(node);
		}
	}
}
