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
package org.umu.editor;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Hashtable;

/* ****************************************************************
 * Title: ConditionPanel
 *
 * Description:*//**
 * This panel is made up of severals panels which correspond
 * with the panels of its child nodes. This panel is used for
 * 
 * @author Alberto Jim?nez L?zaro & Pablo Galera Morcillo
 * 
 * @version 1.3
 ***************************************************************/
public class Panel3Condition extends ElementPanel {

	ButtonGroup bttnGrupo = new ButtonGroup();
	JRadioButton jrbApply = new JRadioButton();
	JRadioButton jrbValue = new JRadioButton();
	JRadioButton jrbVRef = new JRadioButton();
	JRadioButton jrbFunction = new JRadioButton();
	JRadioButton jrbSelector = new JRadioButton();
	JRadioButton jrbADesignator = new JRadioButton();
	JRadioButton jrbRDesignator = new JRadioButton();
	JRadioButton jrbSDesignator = new JRadioButton();
	JRadioButton jrbEDesignator = new JRadioButton();
	JPanel panel = new JPanel(new MiLayout());
	ElementPanel panelActual;
	DefaultMutableTreeNode nodoHijo;
	ElementoXACML elementoHijo;

	public Panel3Condition(DefaultMutableTreeNode n) {
		super(n);
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void setTreeModel(DefaultTreeModel d) {
		super.setTreeModel(d);
		panelActual.setTreeModel(dtm);
	}

	private void jbInit() throws Exception {
		this.setLayout(new MiLayout());
		jrbApply.setText("Apply");
		jrbApply.setBounds(new Rectangle(20, 20, 80, 20));
		jrbApply.addActionListener(new MiElementActionAdapter(this));
		jrbFunction.setText("Function");
		jrbFunction.setBounds(new Rectangle(20, 40, 90, 20));
		jrbFunction.addActionListener(new MiElementActionAdapter(this));
		jrbValue.setText("AttributeValue");
		jrbValue.setBounds(new Rectangle(20, 60, 130, 20));
		jrbValue.addActionListener(new MiElementActionAdapter(this));
		jrbSelector.setText("AttributeSelector");
		jrbSelector.setBounds(new Rectangle(20, 80, 130, 20));
		jrbSelector.addActionListener(new MiElementActionAdapter(this));
		jrbVRef.setText("VariableReference");
		jrbVRef.setBounds(new Rectangle(20, 100, 100, 20));
		jrbVRef.addActionListener(new MiElementActionAdapter(this));
		jrbADesignator.setText("AttributeDesignator");
		jrbADesignator.setBounds(new Rectangle(20, 120, 130, 20));
		jrbADesignator.addActionListener(new MiElementActionAdapter(this));
		TitledBorder miborde = new TitledBorder(new EtchedBorder(), "Expression Type");
		panel.setBorder(miborde);
		bttnGrupo.add(jrbApply);
		bttnGrupo.add(jrbValue);
		bttnGrupo.add(jrbVRef);
		bttnGrupo.add(jrbFunction);
		bttnGrupo.add(jrbSelector);
		bttnGrupo.add(jrbADesignator);
		panel.add(jrbApply);
		panel.add(jrbValue);
		panel.add(jrbVRef);
		panel.add(jrbFunction);
		panel.add(jrbSelector);
		panel.add(jrbADesignator);
		panel.setLocation(20, 20);
		panel.setPreferredSize(new Dimension(580, 150));
		this.add(panel);

		Enumeration subelementos = node.children();
		while (subelementos.hasMoreElements()) {
			nodoHijo = (DefaultMutableTreeNode) subelementos.nextElement();
			panelActual = XACMLPanelFactoryImpl.getInstance().obtenerPanel(nodoHijo);
			if (panelActual != null) {
				elementoHijo = (ElementoXACML) nodoHijo.getUserObject();
				if (elementoHijo instanceof Elemento3Apply) {
					jrbApply.setSelected(true);
				} else if (elementoHijo instanceof ElementoAttributeValue) {
					jrbValue.setSelected(true);
				} else if (elemento instanceof ElementoFunction) {
					jrbFunction.setSelected(true);
				} else if (elemento instanceof Elemento3VariableReference) {
					jrbVRef.setSelected(true);
				} else if (elementoHijo instanceof Elemento3AttributeSelector) {
					jrbSelector.setSelected(true);
				} else if (elementoHijo instanceof Elemento3AttributeDesignator) {
					jrbADesignator.setSelected(true);
				} else {
					jrbApply.setSelected(true);
				}
				TitledBorder miborde2 = new TitledBorder(new EtchedBorder(), "<" + elementoHijo.getTipo() + ">");
				panelActual.setBorder(miborde2);
				panelActual.setLocation(20, 180);
				panelActual.setPreferredSize(new Dimension(580, 430));
				this.add(panelActual);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		String nuevoTipo = "";
		if (e.getSource() == jrbApply) {
			nuevoTipo = "Apply";
		}
		if (e.getSource() == jrbValue) {
			nuevoTipo = "AttributeValue";
		}
		if (e.getSource() == jrbFunction) {
			nuevoTipo = "Function";
		}
		if (e.getSource() == jrbVRef) {
			nuevoTipo = "VariableReference";
		}
		if (e.getSource() == jrbSelector) {
			nuevoTipo = "AttributeSelector";
		}
		if (e.getSource() == jrbADesignator) {
			nuevoTipo = "AttributeDesignator";
		}
		if (nuevoTipo != "") {
			if (panelActual != null) {
				this.remove(panelActual);
				DefaultMutableTreeNode padre = (DefaultMutableTreeNode) nodoHijo.getParent();
				nodoHijo.removeAllChildren();
				if (padre.getChildCount() == 0) {
					if (padre.getUserObject() instanceof ElementoXACML) {
						((ElementoXACML) padre.getUserObject()).setVacio(true);
					}
				}
				if (dtm != null)
					dtm.reload(node);
			}
			elementoHijo = ElementoXACMLFactoryImpl.getInstance().obtenerElementoXACML(nuevoTipo, new Hashtable());
			elementoHijo.setVacio(true);
			nodoHijo.setUserObject(elementoHijo);
			panelActual = XACMLPanelFactoryImpl.getInstance().obtenerPanel(nodoHijo);
			TitledBorder miborde2 = new TitledBorder(new EtchedBorder(), "<" + elementoHijo.getTipo() + ">");
			panelActual.setBorder(miborde2);
			panelActual.setLocation(20, 180);
			panelActual.setPreferredSize(new Dimension(580, 430));
			panelActual.setTreeModel(dtm);
			this.add(panelActual);
			this.validate();
			this.repaint();
			if (dtm != null) {
				dtm.nodeChanged(nodoHijo);
			}
		}
	}
}
