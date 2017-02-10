package org.seal.combiningalgorithms;

import java.util.ArrayList;
import java.util.List;

import org.wso2.balana.Policy;
import org.wso2.balana.Rule;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.xacml3.Target;

public class PolicyInfoTester 
{
	public static void main(String[] args)
	{
		loadPolicy lp = new loadPolicy();
		// now we have the policy here
		Policy policy = lp.getPolicy("/home/turner/git/XPA/Experiments/obligation3/obligation3.xml");
		
		PolicyX policyx = new PolicyX(policy);
		
		// get rules in the policy 
		List<Rule> rules = policyx.getRuleFromPolicy(policy);
		
		// get policy target from the policy
		Target policyTarget = (Target)policy.getTarget();
		
		
		// get a rule from rule list
		Rule firstRule = rules.get(0);
		
		// get rule's target and condition
		Target ruleTarget = (Target) firstRule.getTarget();
		Condition ruleCondition = firstRule.getCondition();
		
		
		// parse objects to Z3-input;
		
		// e.g. make target evaluate to true
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		
		//StringBuffer ptest = policyx.True_Target(policyTarget, collector);  // return as stringbuffer
		StringBuffer rtest = new StringBuffer();
		//StringBuffer ctest = policyx.True_Condition(ruleCondition, collector);
		//System.out.println(ptest.toString() + "\n" + ctest.toString());
		System.out.println(collector.toString());
		Call_Z3str z3 = new Call_Z3str();
		//ArrayList<String> results = new ArrayList<String>();
		for(Rule r : rules)
		{
			rtest.append(policyx.True_Target((Target)r.getTarget(), collector).toString() + "\n");
			z3.buildZ3Input(rtest.toString(), policyx.nameMap, policyx.typeMap);
		}
	}
}
