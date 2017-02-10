package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <Decision> for XACML 3.0.
 * 
 * The <Decision> element contains the result of policy evaluation.
 * 
 * @author Zehao Cheng
 */
public class Elemento3Decision extends ElementoXACML {

	public static final String TIPO_DECISION = "Decision";

	public Elemento3Decision(Map ht) {
		super.setTipo(TIPO_DECISION);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
	}
	
	public String getDecision() {
		return (String) super.getAtributos().get("Decision");
	}

	public String[] getAllowedChild() {
		return null;
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