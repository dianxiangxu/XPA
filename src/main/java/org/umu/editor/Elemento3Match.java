package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <Match> for XACML 3.0.
 * 
 * The <Match> element SHALL identify a set of entities by matching attribute
 * values in an <Attributes> element of the request context with the embedded
 * attribute value.
 * 
 * @author Zehao Cheng
 */
public class Elemento3Match extends ElementoMatchAbstract {

	public static final String TIPO_MATCH = "Match";

	public Elemento3Match(Map ht) {
		super.setTipo(TIPO_MATCH);
		super.setAtributos(ht);
	}

	public String toString() {
		String aux = "<" + getTipo();
		if (esVacio())
			return aux + "/>";
		return aux + ">";
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "AttributeValue" };
		return allowedChild;
	}

	public String[] getAllObligatory() {
		String[] AllObligatory = { "AttributeValue", "AttributeDesignator" };
		return AllObligatory;
	}
	
	public int getMaxNumChild(ElementoXACML e) {
		if (e.getTipo().equals("AttributeValue"))
			return 1;
		return super.getMaxNumChild(e);
	}
	
	public int getPosicion(ElementoXACML e) {
		if (e.getTipo().equals("AttributeValue"))
			return 1;
		else if (e.getTipo().equals("AttributeDesignator"))
			return 2;
		return super.getPosicion(e);
	}
}
