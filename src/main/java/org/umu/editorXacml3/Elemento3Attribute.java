package org.umu.editorXacml3;

import java.util.Map;

/**
 * Represents the element <Attribute> for XACML 3.0.
 * 
 * The <Attribute> element is the central abstraction of the request context. It
 * contains attribute meta-data and one or more attribute values. The attribute
 * meta-data comprises the attribute identifier and the attribute issuer.
 * <AttributeDesignator> elements in the policy MAY refer to attributes by means
 * of this meta-data.
 * 
 * @author Zehao Cheng
 */
public class Elemento3Attribute extends ElementoXACML {
	public static final String TIPO_ATTRIBUTE = "Attribute";

	public Elemento3Attribute(Map ht) {
		super.setTipo(TIPO_ATTRIBUTE);
		super.setAtributos(ht);
	}

	public String getID() {
	    return (String)super.getAtributos().get("AttributeId");
	}
	
	public String getIssuer() {
	    return (String)super.getAtributos().get("Issuer");
	}
	
	public String getIncludeInResult() {
	    return (String)super.getAtributos().get("IncludeInResult");
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
