package org.seal.xacml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.seal.xacml.mutation.MutationBasedTestGenerator;
import org.seal.xacml.policyUtils.PolicyLoader;
import org.seal.xacml.semanticMutation.Mutant;
import org.seal.xacml.semanticMutation.Mutator;
import org.seal.xacml.utils.ExceptionUtil;

public class Test {
	public static void main(String[] args) throws Exception{
		String kMarketBlue = "/home/roshanshrestha/Project/XPA/Experiments/HL7/HL7-PTT.xml";
		//String kMarketBlue = "/home/roshanshrestha/Project/XPA/Experiments/kmarket-blue-policy/kmarket-blue-policy.xml";
		MutationBasedTestGenerator testGenerator;
		List<TaggedRequest> taggedRequests;
		/*Mutator mutator = new Mutator(new Mutant(PolicyLoader.loadPolicy(new File(kMarketBlue)),new ArrayList<Integer>(),""));
		List<String> mutationMthds = new ArrayList<String>();
		mutationMthds.add("createRemoveParallelTargetElementMutants");
		mutator.generateSelectedMutants(mutationMthds);*/
		String policy = kMarketBlue;
		try{
			testGenerator = new MutationBasedTestGenerator(policy);
			List<String> mutationMethods = new ArrayList<String>();
			//mutationMethods.add("createPolicyTargetTrueMutants");
			mutationMethods.add("createPolicyTargetFalseMutants");
			/*mutationMethods.add("createRuleEffectFlippingMutants");
			mutationMethods.add("createRuleTargetTrueMutants");
			mutationMethods.add("createRuleTargetFalseMutants");
			mutationMethods.add("createRuleConditionTrueMutants");
			mutationMethods.add("createRuleConditionFalseMutants");
			mutationMethods.add("createAddNotFunctionMutants");
			mutationMethods.add("createRemoveNotFunctionMutants");
			mutationMethods.add("createRemoveRuleMutants");
			mutationMethods.add("createFirstPermitRuleMutants");
			mutationMethods.add("createFirstDenyRuleMutants");
			mutationMethods.add("createRemoveParallelTargetElementMutants");*/
			
			taggedRequests = testGenerator.generateRequests(mutationMethods);
			String hi = "";
		}catch(Exception e){
			ExceptionUtil.handleInDefaultLevel(e);
		}
	}
}
