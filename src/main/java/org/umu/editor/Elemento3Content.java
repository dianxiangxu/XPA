package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <Content> for XACML 3.0.
 * 
 * The <Content> element is a notional placeholder for additional attributes,
 * typically the content of the resource.
 * 
 * @author Zehao Cheng
 */
public class Elemento3Content extends ElementoXACML {
	public static final String TIPO_CONTENT = "Content";

	public Elemento3Content(Map ht) {
		super.setTipo(TIPO_CONTENT);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
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