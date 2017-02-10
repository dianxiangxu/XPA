package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <ObligationExpression> for XACML 3.0.
 * 
 * The <ObligationExpression> element evaluates to an obligation and SHALL
 * contain an identifier for an obligation and a set of expressions that form
 * arguments of the action defined by the obligation. The FulfillOn attribute
 * SHALL indicate the effect for which this obligation must be fulfilled by the
 * PEP.
 * 
 * @author Zehao Cheng
 */
public class Elemento3ObligationExpression extends ElementoXACML {

	public static final String TIPO_OBLIGATIONEXPRESSION = "ObligationExpression";

	public Elemento3ObligationExpression(Map ht) {
		super.setTipo(TIPO_OBLIGATIONEXPRESSION);
		super.setAtributos(ht);
	}

	public String getID() {
		return (String) super.getAtributos().get("ObligationId");
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