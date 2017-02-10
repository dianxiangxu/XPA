package org.umu.editorXacml3;

import java.util.Map;

/**
 * Represents the element <AttributeDesignator> for XACML 3.0.
 * 
 * The <AttributeDesignator> element retrieves a bag of values for a named
 * attribute from the request context. A named attribute SHALL be considered
 * present if there is at least one attribute that matches the criteria set out
 * below. The <AttributeDesignator> element SHALL return a bag containing all
 * the attribute values that are matched by the named attribute. In the event
 * that no matching attribute is present in the context, the MustBePresent
 * attribute governs whether this element returns an empty bag or
 * “Indeterminate”. See Section 7.3.5. The <AttributeDesignator> MAY appear in
 * the <Match> element and MAY be passed to the <Apply> element as an argument.
 * The <AttributeDesignator> element is of the AttributeDesignatorType complex
 * type.
 * 
 * @author Zehao Cheng
 */
public class Elemento3AttributeDesignator extends ElementoAttributeDesignatorAbstract {
	// modified to support XACML3.0
	public static final String TIPO_ATTRIBUTEDESIGNATOR = "AttributeDesignator";

	public Elemento3AttributeDesignator(Map ht) {
		super.setTipo(TIPO_ATTRIBUTEDESIGNATOR);
		super.setAtributos(ht);
	}
	
	public String getCategory() {
		return (String) super.getAtributos().get("Category");
	}
}
