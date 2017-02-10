package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <AttributesReference> for XACML 3.0.
 * 
 * The <AttributesReference> element makes a reference to an <Attributes>
 * element. The meaning of this element is defined in [Multi]. Support for this
 * element is optional.
 * 
 * @author Zehao Cheng
 */
public class Elemento3AttributesReference extends ElementoXACML {

	public static final String TIPO_ATTRIBUTESREFERENCE = "AttributesReference";

	public Elemento3AttributesReference(Map ht) {
		super.setTipo(TIPO_ATTRIBUTESREFERENCE);
		super.setAtributos(ht);
	}

	public String getID() {
		return (String) super.getAtributos().get("ReferenceID");
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