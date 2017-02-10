package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <RequestReference> for XACML 3.0.
 * 
 * The <RequestReference> element defines an instance of a request in terms of
 * references to <Attributes> elements. The semantics of this element are
 * defined in [Multi]. Support for this element is optional.
 * 
 * @author Zehao Cheng
 */
public class Elemento3RequestReference extends ElementoXACML {

	public static final String TIPO_REQUESTREFERENCE = "RequestReference";

	public Elemento3RequestReference(Map ht) {
		super.setTipo(TIPO_REQUESTREFERENCE);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "AttributesReference" };
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