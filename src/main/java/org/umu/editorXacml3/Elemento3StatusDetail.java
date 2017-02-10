package org.umu.editorXacml3;

import java.util.Map;

/**
 * Represents the element <Status> for XACML 3.0.
 * 
 * The <StatusDetail> element qualifies the <Status> element with additional
 * information.
 * 
 * @author Zehao Cheng
 */
public class Elemento3StatusDetail extends ElementoStatusAbstract {
	public static final String TIPO_STATUSDETAIL = "StatusDetail";

	public Elemento3StatusDetail(Map ht) {
		super.setTipo(TIPO_STATUSDETAIL);
		super.setAtributos(ht);
	}
}