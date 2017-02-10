package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <AssociatedAdvice> for XACML 3.0.
 * 
 * The <AssociatedAdvice> element SHALL contain a set of <Advice> elements.
 * 
 * @author Zehao Cheng
 */
public class Elemento3AssociatedAdvice extends ElementoXACML {

	public static final String TIPO_ASSOCIATEDADVICE = "AssociatedAdvice";

	public Elemento3AssociatedAdvice(Map ht) {
		super.setTipo(TIPO_ASSOCIATEDADVICE);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "Advice" };
		return allowedChild;
	}

	public String[] getAllObligatory() {
		String[] AllObligatory = { "Advice" };
		return AllObligatory;
	}

	public String toString() {
		String aux = "<" + getTipo();
		if (esVacio())
			return aux + "/>";
		return aux + ">";
	}
}
