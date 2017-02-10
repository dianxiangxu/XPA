/*
 * Copyright 2005, 2006 Alberto Jim??nez L??zaro
 *                      Pablo Galera Morcillo (umu-xacml-editor-admin@dif.um.es)
 *                      Dpto. de Ingenier??a de la Informaci??n y las Comunicaciones
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
package org.umu.editorXacml3;

import java.util.Map;

/**
 * Implementation of the ElementoXACMLFactory. This class is a Singleton.
 * 
 * @author Alberto Jim??nez L??zaro y Pablo Galera Morcillo
 * @version 1.3
 */
public class ElementoXACMLFactoryImpl implements ElementoXACMLFactory {

	protected static ElementoXACMLFactory instancia = new ElementoXACMLFactoryImpl();

	private ElementoXACMLFactoryImpl() {
	}

	public ElementoXACML obtenerElementoXACML(String tipo, Map atributos) {
		ElementoXACML elem = null;

		// added to support new elements defined in XCAML 3.0
		// NOTE: the order of 'if' statements may needs to be changed, in order
		// to utilize the program.
		if (tipo.equals(Elemento3AllOf.TIPO_ALLOF)) {
			elem = new Elemento3AllOf(atributos);
		} else if (tipo.equals(Elemento3AnyOf.TIPO_ANYOF)) {
			elem = new Elemento3AnyOf(atributos);
		} else if (tipo.equals(Elemento3Match.TIPO_MATCH)) {
			elem = new Elemento3Match(atributos);
		} else if (tipo.equals(Elemento3AttributeDesignator.TIPO_ATTRIBUTEDESIGNATOR)) {
			elem = new Elemento3AttributeDesignator(atributos);
		} else if (tipo.equals(ElementoPolicyIssuer.TIPO_POLICYISSUER)) {
			elem = new ElementoPolicyIssuer(atributos);
		} else if (tipo.equals(Elemento3Content.TIPO_CONTENT)) {
			elem = new Elemento3Content(atributos);
		} else if (tipo.equals(Elemento3Attribute.TIPO_ATTRIBUTE)) {
			elem = new Elemento3Attribute(atributos);
		} else if (tipo.equals(Elemento3ObligationExpressions.TIPO_OBLIGATIONEXPRESSIONS)) {
			elem = new Elemento3ObligationExpressions(atributos);
		} else if (tipo.equals(Elemento3ObligationExpression.TIPO_OBLIGATIONEXPRESSION)) {
			elem = new Elemento3ObligationExpression(atributos);
		} else if (tipo.equals(Elemento3AttributeAssignmentExpression.TIPO_ATTRIBUTEASSIGNMENTEXPRESSION)) {
			elem = new Elemento3AttributeAssignmentExpression(atributos);
		} else if (tipo.equals(Elemento3AdviceExpressions.TIPO_ADVICEEXPRESSIONS)) {
			elem = new Elemento3AdviceExpressions(atributos);
		} else if (tipo.equals(Elemento3AdviceExpression.TIPO_ADVICEEXPRESSION)) {
			elem = new Elemento3AdviceExpression(atributos);
		} else if (tipo.equals(Elemento3MultiRequests.TIPO_MULTIREQUESTS)) {
			elem = new Elemento3MultiRequests(atributos);
		} else if (tipo.equals(Elemento3RequestReference.TIPO_REQUESTREFERENCE)) {
			elem = new Elemento3RequestReference(atributos);
		} else if (tipo.equals(Elemento3AttributesReference.TIPO_ATTRIBUTESREFERENCE)) {
			elem = new Elemento3AttributesReference(atributos);
		} else if (tipo.equals(Elemento3MissingAttributeDetail.TIPO_MISSINGATTRIBUTEDETAIL)) {
			elem = new Elemento3MissingAttributeDetail(atributos);
		} else if (tipo.equals(Elemento3Attributes.TIPO_ATTRIBUTES)) {
			elem = new Elemento3Attributes(atributos);
		} else if (tipo.equals(Elemento3AssociatedAdvice.TIPO_ASSOCIATEDADVICE)) {
			elem = new Elemento3AssociatedAdvice(atributos);
		} else if (tipo.equals(Elemento3Advice.TIPO_ADVICE)) {
			elem = new Elemento3Advice(atributos);
		} else if (tipo.equals(Elemento3Request.TIPO_REQUEST)) {
			elem = new Elemento3Request(atributos);
		} else if (tipo.equals(Elemento3RequestDefaults.TIPO_REQUESTDEFAULTS)) {
			elem = new Elemento3RequestDefaults(atributos);
		} else if (tipo.equals(Elemento3Response.TIPO_RESPONSE)) {
			elem = new Elemento3Response(atributos);
		} else if (tipo.equals(Elemento3Result.TIPO_RESULT)) {
			elem = new Elemento3Result(atributos);
		} else if (tipo.equals(Elemento3Decision.TIPO_DECISION)) {
			elem = new Elemento3Decision(atributos);
		} else if (tipo.equals(Elemento3Status.TIPO_STATUS)) {
			elem = new Elemento3Status(atributos);
		} else if (tipo.equals(ElementoStatusCode.TIPO_STATUSCODE)) {
			elem = new ElementoStatusCode(atributos);
		} else if (tipo.equals(Elemento3StatusMessage.TIPO_STATUSMESSAGE)) {
			elem = new Elemento3StatusMessage(atributos);
		} else if (tipo.equals(Elemento3StatusDetail.TIPO_STATUSDETAIL)) {
			elem = new Elemento3StatusDetail(atributos);
		} else if (tipo.equals(Elemento3PolicyIdentifierList.TIPO_POLICYIDENTIFIERLIST)) {
			elem = new Elemento3PolicyIdentifierList(atributos);
		}
		// end add

		if (tipo.equals(Elemento3PolicySet.TIPO_POLICYSET)) {
			elem = new Elemento3PolicySet(atributos);
		}

		if (tipo.equals(ElementoPolicySetDefaults.TIPO_POLICYSETDEFAULTS)) {
			elem = new ElementoPolicySetDefaults(atributos);
		}

		if (tipo.equals(ElementoXPathVersion.TIPO_XPATHVERSION)) {
			elem = new ElementoXPathVersion(atributos);
		} else if (tipo.equals(Elemento3Policy.TIPO_POLICY)) {
			elem = new Elemento3Policy(atributos);
		} else if (tipo.equals(Elemento3VariableDefinition.TIPO_VARIABLEDEFINITION)) {
			elem = new Elemento3VariableDefinition(atributos);
		}

		if (tipo.equals(ElementoPolicyDefaults.TIPO_POLICYDEFAULTS)) {
			elem = new ElementoPolicyDefaults(atributos);
		} else if (tipo.equals(Elemento3Rule.TIPO_RULE)) {
			elem = new Elemento3Rule(atributos);
		} else if (tipo.equals(Elemento3Target.TIPO_TARGET)) {
			elem = new Elemento3Target(atributos);
		} else if (tipo.equals(ElementoSubjects.TIPO_SUBJECTS)) {
			elem = new ElementoSubjects(atributos);
		} else if (tipo.equals(ElementoActions.TIPO_ACTIONS)) {
			elem = new ElementoActions(atributos);
		} else if (tipo.equals(ElementoResources.TIPO_RESOURCES)) {
			elem = new ElementoResources(atributos);
		} else if (tipo.equals(ElementoAnySubject.TIPO_ANY_SUBJECT)) {
			elem = new ElementoAnySubject(atributos);
		} else if (tipo.equals(ElementoAnyAction.TIPO_ANY_ACTION)) {
			elem = new ElementoAnyAction(atributos);
		} else if (tipo.equals(ElementoAnyResource.TIPO_ANY_RESOURCE)) {
			elem = new ElementoAnyResource(atributos);
		} else if (tipo.equals(ElementoEnvironments.TIPO_ENVIRONMENTS)) {
			elem = new ElementoEnvironments(atributos);
		} else if (tipo.equals(ElementoAttributeValue.TIPO_ATTRIBUTEVALUE)) {
			elem = new ElementoAttributeValue(atributos);
		} else if (tipo.equals(ElementoAction.TIPO_ACTION)) {
			elem = new ElementoAction(atributos);
		} else if (tipo.equals(ElementoActionMatch.TIPO_ACTIONMATCH)) {
			elem = new ElementoActionMatch(atributos);
		} else if (tipo.equals(ElementoActionAttributeDesignator.TIPO_ACTIONATTRIBUTEDESIGNATOR)) {
			elem = new ElementoActionAttributeDesignator(atributos);
		} else if (tipo.equals(Elemento3AttributeSelector.TIPO_ATTRIBUTESELECTOR)) {
			elem = new Elemento3AttributeSelector(atributos);
		} else if (tipo.equals(ElementoSubject.TIPO_SUBJECT)) {
			elem = new ElementoSubject(atributos);
		} else if (tipo.equals(ElementoSubjectMatch.TIPO_SUBJECTMATCH)) {
			elem = new ElementoSubjectMatch(atributos);
		} else if (tipo.equals(ElementoSubjectAttributeDesignator.TIPO_SUBJECTATTRIBUTEDESIGNATOR)) {
			elem = new ElementoSubjectAttributeDesignator(atributos);
		} else if (tipo.equals(ElementoResource.TIPO_RESOURCE)) {
			elem = new ElementoResource(atributos);
		} else if (tipo.equals(ElementoResourceMatch.TIPO_RESOURCEMATCH)) {
			elem = new ElementoResourceMatch(atributos);
		} else if (tipo.equals(ElementoResourceAttributeDesignator.TIPO_RESOURCEATTRIBUTEDESIGNATOR)) {
			elem = new ElementoResourceAttributeDesignator(atributos);
		} else if (tipo.equals(ElementoEnvironment.TIPO_ENVIRONMENT)) {
			elem = new ElementoEnvironment(atributos);
		} else if (tipo.equals(ElementoEnvironmentMatch.TIPO_ENVIRONMENTMATCH)) {
			elem = new ElementoEnvironmentMatch(atributos);
		} else if (tipo.equals(ElementoEnvironmentAttributeDesignator.TIPO_ENVIRONMENTATTRIBUTEDESIGNATOR)) {
			elem = new ElementoEnvironmentAttributeDesignator(atributos);
		} else if (tipo.equals(Elemento3Condition.TIPO_CONDITION)) {
			elem = new Elemento3Condition(atributos);
		} else if (tipo.equals(Elemento3Apply.TIPO_APPLY)) {
			elem = new Elemento3Apply(atributos);
		} else if (tipo.equals(ElementoDescription.TIPO_DESCRIPTION)) {
			elem = new ElementoDescription(atributos);
		} else if (tipo.equals(ElementoObligations.TIPO_OBLIGATIONS)) {
			elem = new ElementoObligations(atributos);
		} else if (tipo.equals(Elemento3Obligation.TIPO_OBLIGATION)) {
			elem = new Elemento3Obligation(atributos);
		} else if (tipo.equals(Elemento3AttributeAssignment.TIPO_ATTRIBUTEASSIGNMENT)) {
			elem = new Elemento3AttributeAssignment(atributos);
		} else if (tipo.equals(ElementoCombinerParameters.TIPO_COMBINERPARAMETERS)) {
			elem = new ElementoCombinerParameters(atributos);
		} else if (tipo.equals(ElementoCombinerParameter.TIPO_COMBINERPARAMETER)) {
			elem = new ElementoCombinerParameter(atributos);
		} else if (tipo.equals(ElementoRuleCombinerParameters.TIPO_RULECOMBINERPARAMETERS)) {
			elem = new ElementoRuleCombinerParameters(atributos);
		} else if (tipo.equals(ElementoPolicyCombinerParameters.TIPO_POLICYCOMBINERPARAMETERS)) {
			elem = new ElementoPolicyCombinerParameters(atributos);
		} else if (tipo.equals(ElementoPolicySetCombinerParameters.TIPO_POLICYSETCOMBINERPARAMETERS)) {
			elem = new ElementoPolicySetCombinerParameters(atributos);
		} else if (tipo.equals(ElementoFunction.TIPO_FUNCTION)) {
			elem = new ElementoFunction(atributos);
		} else if (tipo.equals(Elemento3VariableReference.TIPO_VARIABLEREFERENCE)) {
			elem = new Elemento3VariableReference(atributos);
		} else if (tipo.equals(ElementoPolicySetIdReference.TIPO_POLICYSETIDREFERENCE)) {
			elem = new ElementoPolicySetIdReference(atributos);
		} else if (tipo.equals(ElementoPolicyIdReference.TIPO_POLICYIDREFERENCE)) {
			elem = new ElementoPolicyIdReference(atributos);
		}

		return elem;
	}

	public static ElementoXACMLFactory getInstance() {
		return instancia;
	}

}
