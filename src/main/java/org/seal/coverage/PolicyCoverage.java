package org.seal.coverage;

import java.util.ArrayList;

public class PolicyCoverage {
	private String testID;
	private String verdict;
	private int oracle;
	private String policyID;
	private int numberOfRules;
	private int targetMatchResult;
	private ArrayList<RuleCoverage> ruleCoverages;
	private int decision;

	public PolicyCoverage(String testID, int oracle, String policyID, int numberOfRules, int targetMatchResult){
		this(testID, oracle, policyID, numberOfRules, targetMatchResult, new ArrayList<RuleCoverage>());
	}

	public PolicyCoverage(String testID, int oracle, String policyID, int numberOfRules, int targetMatchResult, ArrayList<RuleCoverage> ruleCoverages){
		this.testID = testID;
		this.oracle = oracle;
		this.policyID = policyID;
		this.numberOfRules = numberOfRules;
		this.targetMatchResult = targetMatchResult;
		this.ruleCoverages = ruleCoverages;
	}

	public String getTestID(){
		return testID;
	}

	public int getOracle(){
		return oracle;
	}

	public String getPolicyID(){
		return policyID;
	}


	public int getNumberOfRules(){
		return numberOfRules;
	}
	
	public int getTargetMatchResult(){
		return targetMatchResult;
	}
	
	public void addRuleCoverage(RuleCoverage ruleCoverage){
		ruleCoverages.add(ruleCoverage);
	}
	
	public void setDecision(int decision){
		if (oracle<0) 
			verdict="";
		else
			verdict = oracle==decision? "PASS": "FAIL";			
		this.decision = decision;
	}
	
	public String getVerdict(){
		return verdict;
	}
	
	public int getDecision(){
		return decision;
	}
	
	public ArrayList<RuleCoverage> getRuleCoverages(){
		return ruleCoverages;
	}
}
