package org.umu.editorXacml3;

import java.util.Map;

/**
 * Represents the element <AdviceExpression> for XACML 3.0.
 * 
 * The <AdviceExpression> element evaluates to an advice and SHALL contain an
 * identifier for an advice and a set of expressions that form arguments of the
 * supplemental information defined by the advice. The AppliesTo attribute SHALL
 * indicate the effect for which this advice must be provided to the PEP.
 * 
 * @author Zehao Cheng
 */
public class Elemento3AdviceExpression extends ElementoXACML {

	public static final String TIPO_ADVICEEXPRESSION = "AdviceExpression";

	public Elemento3AdviceExpression(Map ht) {
		super.setTipo(TIPO_ADVICEEXPRESSION);
		super.setAtributos(ht);
	}

	public String getID() {
		return (String) super.getAtributos().get("AdviceId");
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "AttributeAssignmentExpression" };
		return allowedChild;
	}

	public String[] getAllObligatory() {
		return null;
	}

	public String toString() {
		String aux = "<" + getTipo();
		if (esVacio())
			return aux + "/>";
		return aux + ">";
	}
}