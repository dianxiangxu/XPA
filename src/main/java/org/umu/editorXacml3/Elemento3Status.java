package org.umu.editorXacml3;

import java.util.Map;

/**
 * Represents the element <Status> for XACML 3.0.
 * 
 * The <Status> element represents the status of the authorization decision result.
 * 
 * @author Zehao Cheng
 */
public class Elemento3Status extends ElementoStatusAbstract{
	public static final String TIPO_STATUS = "Status";

	public Elemento3Status(Map ht) {
		super.setTipo(TIPO_STATUS);
		super.setAtributos(ht);
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "StatusCode", "StatusMessage", "StatusDetail" };
		return allowedChild;
	}
	
	public String[] getAllObligatory() {
		String[] AllObligatory = { "StatusCode" };
		return AllObligatory;
	}
	
	public int getMaxNumChild(ElementoXACML e) {
		if (e.getTipo().equals("StatusCode"))
			return 1;
		else if (e.getTipo().equals("StatusMessage"))
			return 1;
		else if (e.getTipo().equals("StatusDetail"))
			return 1;
		return super.getMaxNumChild(e);
	}
}
