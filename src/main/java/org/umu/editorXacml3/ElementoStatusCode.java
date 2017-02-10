package org.umu.editorXacml3;

import java.util.Map;

/**
 * Represents the element <StatusCode> for XACML 3.0.
 * 
 * The <StatusCode> element contains a major status code value and an optional
 * sequence of minor status codes.
 * 
 * @author Zehao Cheng
 */
public class ElementoStatusCode extends ElementoStatusAbstract {
	public static final String TIPO_STATUSCODE = "StatusCode";

	public ElementoStatusCode(Map ht) {
		super.setTipo(TIPO_STATUSCODE);
		super.setAtributos(ht);
	}
	
	public String getValue() {
		return (String) super.getAtributos().get("Value");
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "StatusCode" };
		return allowedChild;
	}
}
