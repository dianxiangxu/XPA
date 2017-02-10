package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <MissingAttributeDetail> for XACML 3.0.
 * 
 * The <MissingAttributeDetail> element conveys information about attributes
 * required for policy evaluation that were missing from the request context.
 * 
 * @author Zehao Cheng
 */
public class Elemento3MissingAttributeDetail extends ElementoXACML {

	public static final String TIPO_MISSINGATTRIBUTEDETAIL = "MissingAttributeDetail";

	public Elemento3MissingAttributeDetail(Map ht) {
		super.setTipo(TIPO_MISSINGATTRIBUTEDETAIL);
		super.setAtributos(ht);
	}

	public String getID() {
		return (String) super.getAtributos().get("AttributeId");
	}
	
	public String getCategory() {
		return (String) super.getAtributos().get("Category");
	}
	
	public String getDataType() {
		return (String) super.getAtributos().get("DataType");
	}
	
	public String getIssuer() {
		return (String) super.getAtributos().get("Issuer");
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "AttributeValue" };
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
}