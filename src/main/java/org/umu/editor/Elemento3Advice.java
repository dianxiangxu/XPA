package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <Advice> for XACML 3.0.
 * 
 * The <Advice> element SHALL contain an identifier for the advice and a set of
 * attributes that form arguments of the supplemental information defined by the
 * advice.
 * 
 * @author Zehao Cheng
 */
public class Elemento3Advice extends ElementoXACML {

	public static final String TIPO_ADVICE = "Advice";

	public Elemento3Advice(Map ht) {
		super.setTipo(TIPO_ADVICE);
		super.setAtributos(ht);
	}

	public String getID() {
		return (String) super.getAtributos().get("AdviceId");
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "AttributeAssignment" };
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
		if (e.getTipo().equals("AttributeAssignment")) {
			return 1;
		}
		return super.getMaxNumChild(e);
	}
}