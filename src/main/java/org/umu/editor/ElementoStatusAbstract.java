package org.umu.editor;

import java.util.Map;

/**
 * Abstract class for <Status>, <StatusCode>, <StatusMessage>, and <StatusDetail>
 * 
 * @author Zehao Cheng
 */
public abstract class ElementoStatusAbstract extends ElementoXACML {

	public String getID() {
		return "";
	}

	public String[] getAllowedChild() {
		return null;
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