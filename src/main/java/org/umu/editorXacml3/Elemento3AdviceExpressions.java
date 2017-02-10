package org.umu.editorXacml3;

import java.util.Map;

/**
 * Represents the element <AdviceExpressions> for XACML 3.0.
 * 
 * The <AdviceExpressions> element SHALL contain a set of <AdviceExpression>
 * elements.
 * 
 * @author Zehao Cheng
 */
public class Elemento3AdviceExpressions extends ElementoXACML {

	public static final String TIPO_ADVICEEXPRESSIONS = "AdviceExpressions";

	public Elemento3AdviceExpressions(Map ht) {
		super.setTipo(TIPO_ADVICEEXPRESSIONS);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "AdviceExpression" };
		return allowedChild;
	}

	public String[] getAllObligatory() {
		String[] AllObligatory = { "AdviceExpression" };
		return AllObligatory;
	}

	public String toString() {
		String aux = "<" + getTipo();
		if (esVacio())
			return aux + "/>";
		return aux + ">";
	}
}