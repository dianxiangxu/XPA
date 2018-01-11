package org.seal.xacml.semanticCoverage;

/**
 * Created by shuaipeng on 9/14/16.
 */
public class RuleCoverage extends Coverage {
    private CombinedCoverage combined;
    private RuleDecisionCoverage ruleDecisionCoverage;
    private String ruleId;

    public RuleCoverage(IntermediateCoverage targetResult,
                        IntermediateCoverage conditionResult, int ruleResult, String ruleId) {
        this(targetResult, conditionResult, ruleResult);
        this.ruleId = ruleId;
    }

    private RuleCoverage(IntermediateCoverage targetResult,
                        IntermediateCoverage conditionResult, int ruleResult) {
        // get combine semanticCoverage
        if ((targetResult == IntermediateCoverage.TRUE || targetResult == IntermediateCoverage.EMPTY)
                && (conditionResult == IntermediateCoverage.TRUE || conditionResult == IntermediateCoverage.EMPTY))
            combined = CombinedCoverage.BOTH_TRUE;
        else if (targetResult == IntermediateCoverage.FALSE)
            combined = CombinedCoverage.FALSE_TARGET;
        else if (conditionResult == IntermediateCoverage.FALSE)
            combined = CombinedCoverage.FALSE_CONDITION;
        else if (targetResult == IntermediateCoverage.ERROR)
            combined = CombinedCoverage.ERROR_TARGET;
        else
            combined = CombinedCoverage.ERROR_CONDITION;
        // get rule decision semanticCoverage
        if (combined == CombinedCoverage.BOTH_TRUE)
            ruleDecisionCoverage = RuleDecisionCoverage.EFFECT;
        else if (combined == CombinedCoverage.FALSE_TARGET
                || combined == CombinedCoverage.FALSE_CONDITION)
            ruleDecisionCoverage = RuleDecisionCoverage.NA;
        else
            ruleDecisionCoverage = RuleDecisionCoverage.INDETERMINATE;
    }

    public CombinedCoverage getCombinedCoverage() {
        return combined;
    }

    public RuleDecisionCoverage getRuleDecisionCoverage() {
        return ruleDecisionCoverage;
    }

    public String getRuleId() {
        return ruleId;
    }

    public enum IntermediateCoverage {
        TRUE, FALSE, ERROR, EMPTY, NOT_EVALUATED
    }

    public enum CombinedCoverage {
        BOTH_TRUE, FALSE_TARGET, FALSE_CONDITION, ERROR_TARGET, ERROR_CONDITION
    }

    public enum RuleDecisionCoverage {
        EFFECT, NA, INDETERMINATE
    }

}
