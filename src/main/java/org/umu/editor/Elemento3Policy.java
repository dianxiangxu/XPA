/*
 * Copyright 2005, 2006 Alberto Jim?nez L?zaro
 *                      Pablo Galera Morcillo (umu-xacml-editor-admin@dif.um.es)
 *                      Dpto. de Ingenier?a de la Informaci?n y las Comunicaciones
 *                      (http://www.diic.um.es:8080/diic/index.jsp)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.umu.editor;

import java.util.Map;

/* **************************************************************
 * Name: ElementoPolicy
 *
 * Description: */
/**
 * This class represents the XACML Policy Element.
 * 
 * @author Alberto Jim?nez L?zaro y Pablo Galera Morcillo
 * @version 1.3
 ****************************************************************/
public class Elemento3Policy extends ElementoXACML {

	public static final String TIPO_POLICY = "Policy";

	public Elemento3Policy(Map ht) {
		super.setTipo(TIPO_POLICY);
		super.setAtributos(ht);
	}

	public String getID() {
		return (String) super.getAtributos().get("PolicyId");
	}

	public String[] getAllowedChild() {
		String[] allowedChild = { "Description", "PolicyIssuer", "PolicyDefaults", "CombinerParameters",
				"RuleCombinerParameters", "Target", "VariableDefinition", "Rule", "ObligationExpressions",
				"AdviceExpressions", };
		return allowedChild;
	}

	public int getPosicion(ElementoXACML e) {
		if (e.getTipo().equals("Description"))
			return 1;
		else if (e.getTipo().equals("PolicySetDefaults"))
			return 2;
		if (e.getTipo().equals("Target"))
			return 3;
		else if (e.getTipo().equals("CombinerParameters"))
			return 4;
		else if (e.getTipo().equals("RuleCombinerParameters"))
			return 4;
		else if (e.getTipo().equals("VariableDefinition"))
			return 4;
		else if (e.getTipo().equals("Rule"))
			return 4;
		else if (e.getTipo().equals("Obligations"))
			return 5;
		return super.getPosicion(e);
	}

	public String[] getAllObligatory() {
		String[] AllObligatory = { "Target" };
		return AllObligatory;
	}

	public int getMaxNumChild(ElementoXACML e) {
		if (e.getTipo().equals("Description")) {
			return 1;
		} else if (e.getTipo().equals("PolicyIssuer")) {
			return 1;
		} else if (e.getTipo().equals("PolicyDefaults")) {
			return 1;
		} else if (e.getTipo().equals("CombinerParameters")) {
			return 1;
		} else if (e.getTipo().equals("RuleCombinerParameters")) {
			return 1;
		} else if (e.getTipo().equals("Target")) {
			return 1;
		} else if (e.getTipo().equals("ObligationExpressions")) {
			return 1;
		} else if (e.getTipo().equals("AdviceExpressions")) {
			return 1;
		}

		return super.getMaxNumChild(e);
	}

}
