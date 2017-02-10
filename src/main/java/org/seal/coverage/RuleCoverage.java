package org.seal.coverage;

public class RuleCoverage {
	
	public static enum TargetConditionIndividualCoverage {TRUE, FALSE, ERROR, EMPTY, NOTEVALUATED};
	
	public static enum TargetConditionCombinationalCoverage {BOTHTRUE, FALSETARGET, FALSECONDITION, ERRORTARGET, ERRORCONDITION};
	public static enum RuleDecisionCoverage {EFFECT, NA, INDETERMINATE};

	private String id;
	private TargetConditionIndividualCoverage targetResult;
	private TargetConditionIndividualCoverage conditionResult;
	private int ruleResult;
	
	public RuleCoverage(String id, TargetConditionIndividualCoverage targetResult, TargetConditionIndividualCoverage conditionResult, int ruleResult){
		this.id = id;
		this.targetResult = targetResult;
		this.conditionResult = conditionResult;
		this.ruleResult = ruleResult;
	}
	
	public String getId(){
		return id;
	}
		
	public TargetConditionIndividualCoverage getTargetResult(){
		return targetResult;
	}
	
	public TargetConditionIndividualCoverage getConditionResult(){
		return conditionResult;
	}
	
	public int getRuleResult(){
		return ruleResult;
	}

	public TargetConditionCombinationalCoverage getTargetConditionCombinationalCoverage(){
		if ( (targetResult==TargetConditionIndividualCoverage.TRUE || targetResult==TargetConditionIndividualCoverage.EMPTY) &&
				(conditionResult==TargetConditionIndividualCoverage.TRUE || conditionResult==TargetConditionIndividualCoverage.EMPTY) )
			return TargetConditionCombinationalCoverage.BOTHTRUE;
		else 
		if (targetResult==TargetConditionIndividualCoverage.FALSE)
			return TargetConditionCombinationalCoverage.FALSETARGET;
		else 
		if (conditionResult==TargetConditionIndividualCoverage.FALSE)
			return TargetConditionCombinationalCoverage.FALSECONDITION;
		else
		if	(targetResult==TargetConditionIndividualCoverage.ERROR)
			return TargetConditionCombinationalCoverage.ERRORTARGET;			
		else
//		if	(conditionResult==TargetConditionIndividualCoverage.ERROR)
				return TargetConditionCombinationalCoverage.ERRORCONDITION;
	}

	public RuleDecisionCoverage getRuleDecisionCoverage(){
		TargetConditionCombinationalCoverage combCoverage = getTargetConditionCombinationalCoverage();
		if (combCoverage==TargetConditionCombinationalCoverage.BOTHTRUE)
			return RuleDecisionCoverage.EFFECT;
		else
		if (combCoverage==TargetConditionCombinationalCoverage.FALSETARGET || combCoverage==TargetConditionCombinationalCoverage.FALSECONDITION)
			return RuleDecisionCoverage.NA;
		else
			return RuleDecisionCoverage.INDETERMINATE;			
	}
	
	// 1/13/15 Jimmy
	public int getTargetConditionCoverage() {
		TargetConditionCombinationalCoverage combCoverage = getTargetConditionCombinationalCoverage();
		if (combCoverage==TargetConditionCombinationalCoverage.BOTHTRUE || 
				combCoverage==TargetConditionCombinationalCoverage.FALSECONDITION || 
				combCoverage==TargetConditionCombinationalCoverage.ERRORCONDITION)
			return 2;
		else
		if (combCoverage==TargetConditionCombinationalCoverage.FALSETARGET || combCoverage==TargetConditionCombinationalCoverage.ERRORTARGET)
			return 1;
		return 0;
	}

/*	public static CoverageType getCoverageType(boolean result){
		return result? CoverageType.TRUE: CoverageType.FALSE;
	}
*/
}
