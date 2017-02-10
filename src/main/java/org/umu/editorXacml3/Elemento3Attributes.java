package org.umu.editorXacml3;

import java.util.Map;

/**
 * Represents the element <Attributes> for XACML 3.0.
 * 
 * The <Attributes> element specifies attributes of a subject, resource, action,
 * environment or another category by listing a sequence of <Attribute> elements
 * associated with the category.
 * 
 * @author Zehao Cheng
 */
public class Elemento3Attributes extends ElementoXACML {

	public static final String TIPO_ATTRIBUTES = "Attributes";

	public Elemento3Attributes(Map ht) {
		super.setTipo(TIPO_ATTRIBUTES);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
	}
	
	public String getCategory() {
		return (String) super.getAtributos().get("Category");
	}
	
	public String xmlId() {
		return (String) super.getAtributos().get("xml:id");
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "Content", "Attribute" };
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
		if (e.getTipo().equals("Content"))
			return 1;
		return super.getMaxNumChild(e);
	}
}