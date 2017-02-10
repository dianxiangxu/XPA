package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <ObligationExpressions> for XACML 3.0.
 * 
 * The <ObligationExpressions> element SHALL contain a set of
 * <ObligationExpression> elements.
 * 
 * @author Zehao Cheng
 */
public class Elemento3ObligationExpressions extends ElementoXACML {

	public static final String TIPO_OBLIGATIONEXPRESSIONS = "ObligationExpressions";

	public Elemento3ObligationExpressions(Map ht) {
		super.setTipo(TIPO_OBLIGATIONEXPRESSIONS);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "ObligationExpression" };
		return allowedChild;
	}

	public String[] getAllObligatory() {
		String[] AllObligatory = { "ObligationExpression" };
		return AllObligatory;
	}

	public String toString() {
		String aux = "<" + getTipo();
		if (esVacio())
			return aux + "/>";
		return aux + ">";
	}
}
