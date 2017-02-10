package org.umu.editorXacml3;

import java.util.Map;

/**
 * This class represents the XACML AllOf Element
 * 
 * @author Zehao Cheng
 */
public class Elemento3AllOf extends ElementoXACML {

	public static final String TIPO_ALLOF = "AllOf";

	public Elemento3AllOf(Map ht) {
		super.setTipo(TIPO_ALLOF);
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
		String[] allowedChild = {"Match"};
		return allowedChild;
	}

	public String[] getAllObligatory() {
		String[] AllObligatory = { "Match" };
		return AllObligatory;
	}
}