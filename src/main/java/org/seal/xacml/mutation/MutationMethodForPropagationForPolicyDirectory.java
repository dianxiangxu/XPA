package org.seal.xacml.mutation;

import java.util.HashMap;
import java.util.Map;

public class MutationMethodForPropagationForPolicyDirectory {
	 private static Map<String,String> map;
	 static {
		 map = new HashMap<String,String>();
		 map.put("createPolicyTargetTrueMutants", "SELF");
		 map.put("createPolicyTargetFalseMutants", "SELF");
		 map.put("createCombiningAlgorithmMutants", "SELF");
		 map.put("createRuleEffectFlippingMutants", "SELF");
		 map.put("createRuleTargetTrueMutants", "createRemoveDefaultRulesMutant");
		 map.put("createRuleTargetFalseMutants", "SELF");
		 map.put("createRuleConditionTrueMutants", "createRemoveDefaultRulesMutant");
		 map.put("createRuleConditionFalseMutants", "SELF");
		 map.put("createAddNotFunctionMutants", "SELF");
		 map.put("createRemoveNotFunctionMutants", "SELF");
		 map.put("createFirstPermitRuleMutants", "SELF");
		 map.put("createFirstDenyRuleMutants", "SELF");
		 map.put("createRemoveRuleMutants", "SELF");
		 map.put("createRuleChangeComparisonFunctionMutants", "SELF");
		 map.put("createPolicyTargetChangeComparisonFunctionMutants", "SELF");
		 map.put("createAddNewRuleMutants", "createRemoveDefaultRulesMutant");
		 map.put("createRemoveDefaultRulesMutant", "SELF");
		 map.put("createRemoveParallelTargetElementMutants", "SELF");
		 map.put("createCombiningAlgorithmMutants", "SELF");
			
	 }
	 
	 public static String getMutationMethod(String method){
		 return map.get(method);
	 }
}
