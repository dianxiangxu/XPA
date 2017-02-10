package org.umu.editor;

import java.util.Map;

/**
 * This class represents the XACML AnyOf Element
 * 
 * @author Zehao Cheng
 */
public class Elemento3AnyOf extends ElementoXACML {

	public static final String TIPO_ANYOF = "AnyOf";

	public Elemento3AnyOf(Map ht) {
		super.setTipo(TIPO_ANYOF);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
	}

	public String toString() {
		String aux = "<" + getTipo();
		if (esVacio())
			return aux + "/>";
		return aux + ">";
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "AllOf" };
		return allowedChild;
	}

	public String[] getAllObligatory() {
		String[] AllObligatory = { "AllOf" };
		return AllObligatory;
	}

}
