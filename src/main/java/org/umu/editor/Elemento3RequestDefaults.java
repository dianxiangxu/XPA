package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <RequestDefaults> for XACML 3.0.
 * 
 * The <RequestDefaults> element SHALL specify default values that apply to the
 * <Request> element.
 * 
 * @author Zehao Cheng
 */
public class Elemento3RequestDefaults extends ElementoXACML {

	public static final String TIPO_REQUESTDEFAULTS= "RequestDefaults";

	public Elemento3RequestDefaults(Map ht) {
		super.setTipo(TIPO_REQUESTDEFAULTS);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "XPathVersion" };
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
		if (e.getTipo().equals("XPathVersion"))
			return 1;
		return super.getMaxNumChild(e);
	}
}
