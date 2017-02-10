package org.umu.editorXacml3;

import java.util.Map;

/**
 * Represents the element <AttributeAssignmentExpression> for XACML 3.0.
 * 
 * The <AttributeAssignmentExpression> element is used for including arguments
 * in obligations and advice. It SHALL contain an AttributeId and an expression
 * which SHALL by evaluated into the corresponding attribute value. The value
 * specified SHALL be understood by the PEP, but it is not further specified by
 * XACML. See Section 7.18. Section 4.2.4.3 provides a number of examples of
 * arguments included in obligations.
 * 
 * @author Zehao Cheng
 */
public class Elemento3AttributeAssignmentExpression extends ElementoXACML {

	public static final String TIPO_ATTRIBUTEASSIGNMENTEXPRESSION = "AttributeAssignmentExpression";

	public Elemento3AttributeAssignmentExpression(Map ht) {
		super.setTipo(TIPO_ATTRIBUTEASSIGNMENTEXPRESSION);
		super.setAtributos(ht);
	}

	public String getID() {
		return (String) super.getAtributos().get("AttributeId");
	}

	public String[] getAllowedChild() {
		return null;
	}

	public String[] getAllObligatory() {
		String[] AllObligatory = { super.Expression[0] };
		return AllObligatory;
	}

	public String getCategory() {
		return (String) super.getAtributos().get("Category");
	}
	
	public String getIssuer() {
		return (String) super.getAtributos().get("Issuer");
	}

	public String toString() {
		String aux = "<" + getTipo();
		if (esVacio())
			return aux + "/>";
		return aux + ">";
	}
}
