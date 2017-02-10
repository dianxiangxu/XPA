package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <PolicyIssuer> for XACML 3.0.
 * 
 * The <PolicyIssuer> element contains attributes describing the issuer of the
 * policy or policy set. The use of the policy issuer element is defined in a
 * separate administration profile [XACMLAdmin]. A PDP which does not implement
 * the administration profile MUST report an error or return an Indeterminate
 * result if it encounters this element.
 * 
 * @author Zehao Cheng
 */
public class ElementoPolicyIssuer extends ElementoXACML {

	public static final String TIPO_POLICYISSUER = "PolicyIssuer";

	public ElementoPolicyIssuer(Map ht) {
		super.setTipo(TIPO_POLICYISSUER);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "Content", "Attribute" };
		return allowedChild;
	}

	public String[] getAllObligatory() {
		return null;
	}

	public int getMaxNumChild(ElementoXACML e) {
		if (e.getTipo().equals("Content"))
			return 1;
		return super.getMaxNumChild(e);
	}

	public String toString() {
		String aux = "<" + getTipo();
		if (esVacio())
			return aux + "/>";
		return aux + ">";
	}
}
