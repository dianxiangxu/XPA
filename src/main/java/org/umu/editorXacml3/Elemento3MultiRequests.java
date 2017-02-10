package org.umu.editorXacml3;

import java.util.Map;

/**
 * Represents the element <MultiRequests> for XACML 3.0.
 * 
 * The <MultiRequests> element contains a list of requests by reference to
 * <Attributes> elements in the enclosing <Request> element. The semantics of
 * this element are defined in [Multi]. Support for this element is optional. If
 * an implementation does not support this element, but receives it, the
 * implementation MUST generate an “Indeterminate” response.
 * 
 * @author Zehao Cheng
 */
public class Elemento3MultiRequests extends ElementoXACML {

	public static final String TIPO_MULTIREQUESTS = "MultiRequests";

	public Elemento3MultiRequests(Map ht) {
		super.setTipo(TIPO_MULTIREQUESTS);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "RequestReference" };
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