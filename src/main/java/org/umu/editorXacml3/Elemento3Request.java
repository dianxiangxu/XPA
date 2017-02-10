package org.umu.editorXacml3;

import java.util.Map;

/**
 * Represents the element <Request> for XACML 3.0.
 * 
 * The <Request> element is an abstraction layer used by the policy language.
 * For simplicity of expression, this document describes policy evaluation in
 * terms of operations on the context. However a conforming PDP is not required
 * to actually instantiate the context in the form of an XML document. But, any
 * system conforming to the XACML specification MUST produce exactly the same
 * authorization decisions as if all the inputs had been transformed into the
 * form of an <Request> element. The <Request> element contains <Attributes>
 * elements. There may be multiple <Attributes> elements with the same Category
 * attribute if the PDP implements the multiple decision profile, see [Multi].
 * Under other conditions, it is a syntax error if there are multiple
 * <Attributes> elements with the same Category (see Section 7.19.2 for error
 * codes).
 * 
 * @author Zehao Cheng
 */
public class Elemento3Request extends ElementoXACML {

	public static final String TIPO_REQUEST = "Request";

	public Elemento3Request(Map ht) {
		super.setTipo(TIPO_REQUEST);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
	}
	
	public String getCombinedDecision() {
		return (String) super.getAtributos().get("CombinedDecision");
	}
	
	// unimplemented method
	public String[] getReturnPolicyIdList() {
		return null;
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "RequestDefaults", "Attributes", "MultiRequests" };
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
	
	public int getMaxNumChild(ElementoXACML e) {
		if (e.getTipo().equals("RequestDefaults"))
			return 1;
		else if (e.getTipo().equals("MultiRequests"))
			return 1;
		return super.getMaxNumChild(e);
	}
}
