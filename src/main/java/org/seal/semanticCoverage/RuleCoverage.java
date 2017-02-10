package org.seal.semanticCoverage;

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
            combined = CombinedCoverage.BOTHTRUE;
        else if (targetResult == IntermediateCoverage.FALSE)
            combined = CombinedCoverage.FALSETARGET;
        else if (conditionResult == IntermediateCoverage.FALSE)
            combined = CombinedCoverage.FALSECONDITION;
        else if (targetResult == IntermediateCoverage.ERROR)
            combined = CombinedCoverage.ERRORTARGET;
        else
            combined = CombinedCoverage.ERRORCONDITION;
        // get rule decision semanticCoverage
        if (combined == CombinedCoverage.BOTHTRUE)
            ruleDecisionCoverage = RuleDecisionCoverage.EFFECT;
        else if (combined == CombinedCoverage.FALSETARGET
                || combined == CombinedCoverage.FALSECONDITION)
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
        TRUE, FALSE, ERROR, EMPTY, NOTEVALUATED
    }

    enum CombinedCoverage {
        BOTHTRUE, FALSETARGET, FALSECONDITION, ERRORTARGET, ERRORCONDITION
    }

    public enum RuleDecisionCoverage {
        EFFECT, NA, INDETERMINATE
    }

}
