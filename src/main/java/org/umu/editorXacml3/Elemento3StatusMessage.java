package org.umu.editorXacml3;

import java.util.Map;

/**
 * Represents the element <StatusMessage> for XACML 3.0.
 * 
 * The <StatusMessage> element is a free-form description of the status code.
 * 
 * @author Zehao Cheng
 */
public class Elemento3StatusMessage extends ElementoStatusAbstract{
	public static final String TIPO_STATUSMESSAGE = "StatusMessage";

	public Elemento3StatusMessage(Map ht) {
		super.setTipo(TIPO_STATUSMESSAGE);
		super.setAtributos(ht);
	}
}