package org.umu.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * An editor panel for element <AttributeAssignmentExpression>, which is defined
 * in XACML 3.0 standard.
 * 
 * @author Zehao Cheng
 * @version 1.0
 */
public class Panel3AttributeAssignmentExpression extends ElementPanel {

	JLabel jlblAttributeId = new JLabel();
	JComboBox jcmbAttributeId = new JComboBox(ElementoXACML.getAllAttributeId());
	JLabel jlblreq1 = new JLabel("*");

	JLabel jlblCategory = new JLabel();
	JComboBox jcmbCategory = new JComboBox(ElementoXACML.getAllCategory());

	JTextField jtxtIssuer = new JTextField();
	JLabel jlblIssuer = new JLabel("Issuer:");

	JLabel jlblrequired = new JLabel("* Required");

	ButtonGroup bttnGrupo = new ButtonGroup();
	JRadioButton jrbApply = new JRadioButton();
	JRadioButton jrbValue = new JRadioButton();
	JRadioButton jrbVRef = new JRadioButton();
	JRadioButton jrbFunction = new JRadioButton();
	JRadioButton jrbSelector = new JRadioButton();
	JRadioButton jrbADesignator = new JRadioButton();
	JPanel panel = new JPanel(new MiLayout());
	ElementPanel panelActual;
	DefaultMutableTreeNode nodoHijo;
	ElementoXACML elementoHijo;

	/**
	 * Constructor of Obligation Expression Panel.
	 * 
	 * @param n
	 */
	public Panel3AttributeAssignmentExpression(DefaultMutableTreeNode n) {
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
		//this.setPreferredSize(new Dimension(600, 900));

		// AttributedID
		jlblreq1.setForeground(Color.red);
		jlblreq1.setBounds(new Rectangle(15, 30, 10, 20));
		jlblAttributeId.setText("AttributedId:");
		jlblAttributeId.setBounds(new Rectangle(25, 30, 100, 20));
		jcmbAttributeId.setPreferredSize(new Dimension(400, 20));
		jcmbAttributeId.setLocation(135, 30);
		jcmbAttributeId.setEditable(true);
		jcmbAttributeId.setSelectedItem((String) ((Elemento3AttributeAssignmentExpression) elemento).getID());
		jcmbAttributeId.addItemListener(new MiElementItemAdapter(this));

		// Category (Optional)
		jlblCategory.setText("Category:");
		jlblCategory.setBounds(new Rectangle(25, 60, 100, 20));
		jcmbCategory.setPreferredSize(new Dimension(400, 20));
		jcmbCategory.setLocation(135, 60);
		jcmbCategory.setEditable(true);
		jcmbCategory.setSelectedItem((String) ((Elemento3AttributeAssignmentExpression) elemento).getCategory());
		jcmbCategory.addItemListener(new MiElementItemAdapter(this));

		// Issuer (Optional)
		jlblIssuer.setBounds(new Rectangle(25, 90, 100, 20));
		jtxtIssuer.setPreferredSize(new Dimension(400, 20));
		jtxtIssuer.setLocation(135, 90);
		jtxtIssuer.setText((String) elemento.getAtributos().get("Issuer"));
		jtxtIssuer.addKeyListener(new MiElementKeyAdapter(this));

		// required element indication
		jlblrequired.setForeground(Color.red);
		jlblrequired.setBounds(new Rectangle(135, 120, 100, 20));

		this.add(jlblreq1);
		this.add(jlblAttributeId);
		this.add(jcmbAttributeId);
		this.add(jlblCategory);
		this.add(jcmbCategory);
		this.add(jlblIssuer);
		this.add(jtxtIssuer);
		this.add(jlblrequired);

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
		panel.setLocation(20, 150);
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
				panelActual.setLocation(20, 315);
				panelActual.setPreferredSize(new Dimension(580, 430));
				this.add(panelActual);
			}
		}
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
		if (e.getSource() == jcmbAttributeId) {
			Map at = elemento.getAtributos();
			at.remove("AttributeId");
			at.put("AttributeId", jcmbAttributeId.getSelectedItem());
		} else if (e.getSource() == jcmbCategory) {
			Map cg = elemento.getAtributos();
			cg.remove("Category");
			cg.put("Category", jcmbCategory.getSelectedItem());
		}

		miobservable.change(node);
		if (dtm != null) {
			dtm.nodeChanged(node);
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
			panelActual.setLocation(20, 315);
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
