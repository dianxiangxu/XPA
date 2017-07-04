package org.seal.xacml.mutation;

import java.util.HashMap;
import java.util.Map;

public class MutationMethodAbbrDirectory {
	 private static Map<String,String> map;
	 static {
		 map = new HashMap<String,String>();
		 map.put("createPolicyTargetTrueMutants", "PTT");
		 map.put("createPolicyTargetFalseMutants", "PTF");
		 map.put("createCombiningAlgorithmMutants", "CRC");
		 map.put("createRuleEffectFlippingMutants", "CRE");
		 map.put("createRuleTargetTrueMutants", "RTT");
		 map.put("createRuleTargetFalseMutants", "RTF");
		 map.put("createRuleConditionTrueMutants", "RCT");
		 map.put("createRuleConditionFalseMutants", "RCF");
		 map.put("createAddNotFunctionMutants", "ANF");
		 map.put("createRemoveNotFunctionMutants", "RNF");
		 map.put("createFirstPermitRuleMutants", "FPR");
		 map.put("createFirstDenyRuleMutants", "FDR");
		 map.put("createRemoveRuleMutants", "RER");
		 map.put("createRuleChangeComparisonFunctionMutants", "RCCF");
		 map.put("createPolicyTargetChangeComparisonFunctionMutants", "PTCC");
		 map.put("createAddNewRuleMutants", "ANR");
		 map.put("createRemoveDefaultRulesMutant", "RDR");
	 }
	 
	 public static String getAbbr(String method){
		 return map.get(method);
	 }

}
