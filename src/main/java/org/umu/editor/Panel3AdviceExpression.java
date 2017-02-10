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
 * An editor panel for element <AdviceExpression>, which is defined in XACML
 * 3.0 standard.
 * 
 * @author Zehao Cheng
 * @version 1.0
 */
public class Panel3AdviceExpression extends ElementPanel {

	JLabel jlblAdviceId = new JLabel();
	// not sure what are the pre-defined Obligation IDs
	JComboBox jcmbAdviceId = new JComboBox();
	JLabel jlblreq1 = new JLabel("*");
	JLabel jlblrequired = new JLabel("* Required");

	JLabel jlblAppliesTo = new JLabel();
	// The Value of FulfillOn is the same as those in Effects
	JComboBox jcmbAppliesTo = new JComboBox(ElementoXACML.getAllEffects());
	JLabel jlblreq2 = new JLabel("*");

	/**
	 * Constructor of Obligation Expression Panel.
	 * 
	 * @param n
	 */
	public Panel3AdviceExpression(DefaultMutableTreeNode n) {
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

		jlblreq1.setForeground(Color.red);
		jlblreq1.setBounds(new Rectangle(15, 30, 10, 20));
		jlblAdviceId.setText("AdviceId:");
		jlblAdviceId.setBounds(new Rectangle(25, 30, 100, 20));
		jcmbAdviceId.setPreferredSize(new Dimension(400, 20));
		jcmbAdviceId.setLocation(135, 30);
		jcmbAdviceId.setEditable(true);
		jcmbAdviceId.setSelectedItem((String) ((Elemento3AdviceExpression) elemento).getID());
		jcmbAdviceId.addItemListener(new MiElementItemAdapter(this));

		jlblreq2.setForeground(Color.red);
		jlblreq2.setBounds(new Rectangle(15, 60, 10, 20));
		jlblAppliesTo.setText("AppliesTo:");
		jlblAppliesTo.setBounds(new Rectangle(25, 60, 100, 20));
		jcmbAppliesTo.setPreferredSize(new Dimension(400, 20));
		jcmbAppliesTo.setLocation(135, 60);
		jcmbAppliesTo.setEditable(true);
		jcmbAppliesTo.setSelectedItem((String) elemento.getAtributos().get("AppliesTo"));
		jcmbAppliesTo.addItemListener(new MiElementItemAdapter(this));

		jlblrequired.setForeground(Color.red);
		jlblrequired.setBounds(new Rectangle(135, 90, 100, 20));

		this.add(jlblreq1);
		this.add(jlblAdviceId);
		this.add(jcmbAdviceId);
		this.add(jlblreq2);
		this.add(jlblAppliesTo);
		this.add(jcmbAppliesTo);
		this.add(jlblrequired);

	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == jcmbAdviceId) {
			Map at = elemento.getAtributos();
			at.remove("AdviceId");
			at.put("AdviceId", jcmbAdviceId.getSelectedItem());
		} else if (e.getSource() == jcmbAppliesTo) {
		    Map mapa=elemento.getAtributos();
		    mapa.put("AppliesTo",(String)jcmbAppliesTo.getSelectedItem());
		}
		
		miobservable.change(node);
		if (dtm != null) {
			dtm.nodeChanged(node);
		}
	}
}