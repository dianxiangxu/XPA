package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <Result> for XACML 3.0.
 * 
 * The <Result> element represents an authorization decision result. It MAY
 * include a set of obligations that MUST be fulfilled by the PEP. If the PEP
 * does not understand or cannot fulfill an obligation, then the action of the
 * PEP is determined by its bias, see section 7.1. It MAY include a set of
 * advice with supplemental information which MAY be safely ignored by the PEP.
 * 
 * @author Zehao Cheng
 */
public class Elemento3Result extends ElementoXACML {

	public static final String TIPO_RESULT = "Result";

	public Elemento3Result(Map ht) {
		super.setTipo(TIPO_RESULT);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "Decision", "Status", "Obligations",
				"AssociatedAdvice", "Attributs", "PolicyIdentifierList" };
		return allowedChild;
	}

	public String[] getAllObligatory() {
		String[] AllObligatory = { "Decision" };
		return AllObligatory;
	}

	public String toString() {
		String aux = "<" + getTipo();
		if (esVacio())
			return aux + "/>";
		return aux + ">";
	}
	
	public int getMaxNumChild(ElementoXACML e) {
		if (e.getTipo().equals("Decision"))
			return 1;
		else if (e.getTipo().equals("Status"))
			return 1;
		else if (e.getTipo().equals("Obligations"))
			return 1;
		else if (e.getTipo().equals("AssociatedAdvice"))
			return 1;
		else if (e.getTipo().equals("Attributes"))
			return 1;
		else if (e.getTipo().equals("PolicyIdentifierList"))
			return 1;
		return super.getMaxNumChild(e);
	}
}
