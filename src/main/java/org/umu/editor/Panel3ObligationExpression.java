package org.umu.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * An editor panel for element <ObligationExpression>, which is defined in XACML
 * 3.0 standard.
 * 
 * @author Zehao Cheng
 * @version 1.0
 */
public class Panel3ObligationExpression extends ElementPanel {

	JLabel jlblObligationId = new JLabel();
	// not sure what are the pre-defined Obligation IDs
	JComboBox jcmbObligationId = new JComboBox();
	JLabel jlblreq1 = new JLabel("*");
	JLabel jlblrequired = new JLabel("* Required");

	JLabel jlblFulfillOn = new JLabel();
	// The Value of FulfillOn is the same as those in Effects
	JComboBox jcmbFulfillOn = new JComboBox(ElementoXACML.getAllEffects());
	JLabel jlblreq2 = new JLabel("*");

	/**
	 * Constructor of Obligation Expression Panel.
	 * 
	 * @param n
	 */
	public Panel3ObligationExpression(DefaultMutableTreeNode n) {
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

		// ObligationId
		jlblreq1.setForeground(Color.red);
		jlblreq1.setBounds(new Rectangle(15, 30, 10, 20));
		jlblObligationId.setText("ObligationId:");
		jlblObligationId.setBounds(new Rectangle(25, 30, 100, 20));
		jcmbObligationId.setPreferredSize(new Dimension(400, 20));
		jcmbObligationId.setLocation(135, 30);
		jcmbObligationId.setEditable(true);
		jcmbObligationId.setSelectedItem((String) ((Elemento3ObligationExpression) elemento).getID());
		jcmbObligationId.addItemListener(new MiElementItemAdapter(this));

		// FullfillOn
		jlblreq2.setForeground(Color.red);
		jlblreq2.setBounds(new Rectangle(15, 60, 10, 20));
		jlblFulfillOn.setText("FulfillOn:");
		jlblFulfillOn.setBounds(new Rectangle(25, 60, 100, 20));
		jcmbFulfillOn.setPreferredSize(new Dimension(400, 20));
		jcmbFulfillOn.setLocation(135, 60);
		jcmbFulfillOn.setEditable(true);
		jcmbFulfillOn.setSelectedItem((String) elemento.getAtributos().get("FulfillOn"));
		jcmbFulfillOn.addItemListener(new MiElementItemAdapter(this));

		jlblrequired.setForeground(Color.red);
		jlblrequired.setBounds(new Rectangle(135, 90, 100, 20));

		this.add(jlblreq1);
		this.add(jlblObligationId);
		this.add(jcmbObligationId);
		this.add(jlblreq2);
		this.add(jlblFulfillOn);
		this.add(jcmbFulfillOn);
		this.add(jlblrequired);

	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == jcmbObligationId) {
			Map at = elemento.getAtributos();
			at.remove("ObligationId");
			at.put("ObligationId", jcmbObligationId.getSelectedItem());
		} else if (e.getSource() == jcmbFulfillOn) {
		    Map mapa=elemento.getAtributos();
		    mapa.put("FulfillOn",(String)jcmbFulfillOn.getSelectedItem());
		}
		
		miobservable.change(node);
		if (dtm != null) {
			dtm.nodeChanged(node);
		}
	}
}
