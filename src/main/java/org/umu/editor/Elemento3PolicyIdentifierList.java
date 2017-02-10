package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <PolicyIdentifierList> for XACML 3.0.
 * 
 * The <PolicyIdentifierList> element contains a list of policy and policy set
 * identifiers of policies which have been applicable to a request. The list is
 * unordered.
 * 
 * @author Zehao Cheng
 */
public class Elemento3PolicyIdentifierList extends ElementoXACML {

	public static final String TIPO_POLICYIDENTIFIERLIST = "PolicyIdentifierList";

	public Elemento3PolicyIdentifierList(Map ht) {
		super.setTipo(TIPO_POLICYIDENTIFIERLIST);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "PolicyIdReference", "PolicySetIdReference" };
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