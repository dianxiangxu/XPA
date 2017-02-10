package org.umu.editor;

import java.util.Map;

/**
 * Represents the element <Response> for XACML 3.0.
 * 
 * The <Response> element is an abstraction layer used by the policy language.
 * Any proprietary system using the XACML specification MUST transform an XACML
 * context <Response> element into the form of its authorization decision. The
 * <Response> element encapsulates the authorization decision produced by the
 * PDP. It includes a sequence of one or more results, with one <Result> element
 * per requested resource. Multiple results MAY be returned by some
 * implementations, in particular those that support the XACML Profile for
 * Requests for Multiple Resources [Multi]. Support for multiple results is
 * OPTIONAL.
 * 
 * @author Zehao Cheng
 */
public class Elemento3Response extends ElementoXACML {

	public static final String TIPO_RESPONSE = "Response";

	public Elemento3Response(Map ht) {
		super.setTipo(TIPO_RESPONSE);
		super.setAtributos(ht);
	}

	public String getID() {
		return "";
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "Result" };
		return allowedChild;
	}

	public String[] getAllObligatory() {
		String[] AllObligatory = { "Result" };
		return AllObligatory;
	}

	public String toString() {
		String aux = "<" + getTipo();
		if (esVacio())
			return aux + "/>";
		return aux + ">";
	}
}