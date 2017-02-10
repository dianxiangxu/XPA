package org.seal.semanticCoverage;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seal.policyUtils.XpathSolver;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.AbstractTarget;
import org.wso2.balana.MatchResult;
import org.wso2.balana.Rule;
import org.wso2.balana.XACMLConstants;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.ctx.ResultFactory;
import org.wso2.balana.ctx.xacml2.Result;
import org.wso2.balana.PolicySet;

public privileged aspect SematicPolicyTracer {
	private static Log logger = LogFactory.getLog(SematicPolicyTracer.class);

	pointcut ruleEvaluationPointcut(Rule rule, EvaluationCtx context): call(AbstractResult Rule.evaluate(*)) && target(rule) && args(context);

	// replace the evaluate method in the Rule class to record the result of
	// each rule evaluation
	AbstractResult around(Rule rule, EvaluationCtx context): ruleEvaluationPointcut(rule, context) {
		// If the Target is null then it's supposed to inherit from the
		// parent policy, so we skip the matching step assuming we wouldn't
		// be here unless the parent matched
		String xPath = XpathSolver.buildRuleXpath(rule);
		MatchResult match = null;
		logger.debug("evaluating rule " + rule.getId().toString());

		// start of changes
		// xacmlVersion, processObligations, processAdvices have been changed to
		// public
		AbstractTarget target = rule.getTarget();
		int effectAttr = rule.getEffect();
		Condition condition = rule.getCondition();
		// end of changes

		if (target != null) {

			match = target.match(context);
			int result = match.getResult();

			// if the target didn't match, then this Rule doesn't apply
			if (result == MatchResult.NO_MATCH) {
				// start of change
				Coverage ruleCoverage = new RuleCoverage(
						RuleCoverage.IntermediateCoverage.FALSE,
						RuleCoverage.IntermediateCoverage.NOTEVALUATED,
						Result.DECISION_NOT_APPLICABLE, rule.getId().toString());
				PolicyCoverageFactory.addCoverage(ruleCoverage, xPath);

				// end of change
				return ResultFactory.getFactory().getResult(
						Result.DECISION_NOT_APPLICABLE, context);
			}

			// if the target was indeterminate, we can't go on
			if (result == MatchResult.INDETERMINATE) {
				int xacmlVersion = rule.xacmlVersion;
				// defines extended indeterminate results with XACML 3.0
				if (xacmlVersion == XACMLConstants.XACML_VERSION_3_0) {
					if (effectAttr == AbstractResult.DECISION_PERMIT) {
						// start of change
						Coverage ruleCoverage = new RuleCoverage(
								RuleCoverage.IntermediateCoverage.ERROR,
								RuleCoverage.IntermediateCoverage.NOTEVALUATED,
								Result.DECISION_INDETERMINATE_PERMIT, rule.getId().toString());
						PolicyCoverageFactory.addCoverage(ruleCoverage, xPath);
						// end of change

						return ResultFactory.getFactory().getResult(
								Result.DECISION_INDETERMINATE_PERMIT,
								match.getStatus(), context);
					} else {
						// start of change
						Coverage ruleCoverage = new RuleCoverage(
								RuleCoverage.IntermediateCoverage.ERROR,
								RuleCoverage.IntermediateCoverage.NOTEVALUATED,
								Result.DECISION_INDETERMINATE_DENY, rule.getId().toString());
						PolicyCoverageFactory.addCoverage(ruleCoverage, xPath);
						// end of change
						return ResultFactory.getFactory().getResult(
								Result.DECISION_INDETERMINATE_DENY,
								match.getStatus(), context);
					}
				}
				// start of change
				Coverage ruleCoverage = new RuleCoverage(
						RuleCoverage.IntermediateCoverage.ERROR,
						RuleCoverage.IntermediateCoverage.NOTEVALUATED,
						Result.DECISION_INDETERMINATE, rule.getId().toString());
				PolicyCoverageFactory.addCoverage(ruleCoverage, xPath);
				// end of change
				return ResultFactory.getFactory().getResult(
						Result.DECISION_INDETERMINATE, match.getStatus(),
						context);
			}
		}

		// if there's no condition, then we just return the effect
		if (condition == null) {
			// start of change
			Coverage ruleCoverage = new RuleCoverage(
					RuleCoverage.IntermediateCoverage.TRUE,
					RuleCoverage.IntermediateCoverage.EMPTY, effectAttr, rule.getId().toString());
			PolicyCoverageFactory.addCoverage(ruleCoverage, xPath);
			// end of change
			// if any obligations or advices are defined, evaluates them and
			// return
			return ResultFactory.getFactory().getResult(effectAttr, rule.processObligations(context), rule.processAdvices(context), context);

		}

		// otherwise we evaluate the condition
		EvaluationResult result = condition.evaluate(context);
		// System.out.println(result.toString());

		if (result.indeterminate()) {

			// defines extended indeterminate results with XACML 3.0
			int xacmlVersion = rule.xacmlVersion;

			if (xacmlVersion == XACMLConstants.XACML_VERSION_3_0) {
				if (effectAttr == AbstractResult.DECISION_PERMIT) {
					// start of change
					Coverage ruleCoverage = new RuleCoverage(
							RuleCoverage.IntermediateCoverage.TRUE,
							RuleCoverage.IntermediateCoverage.ERROR,
							Result.DECISION_INDETERMINATE_PERMIT, rule.getId().toString());
					PolicyCoverageFactory.addCoverage(ruleCoverage, xPath);
					// end of change
					return ResultFactory.getFactory().getResult(
							Result.DECISION_INDETERMINATE_PERMIT,
							result.getStatus(), context);
				} else {
					// start of change
					Coverage ruleCoverage = new RuleCoverage(
							RuleCoverage.IntermediateCoverage.TRUE,
							RuleCoverage.IntermediateCoverage.ERROR,
							Result.DECISION_INDETERMINATE_DENY, rule.getId().toString());
					PolicyCoverageFactory.addCoverage(ruleCoverage, xPath);
					// end of change
					return ResultFactory.getFactory().getResult(
							Result.DECISION_INDETERMINATE_DENY,
							result.getStatus(), context);
				}
			}
			// start of change
			Coverage ruleCoverage = new RuleCoverage(
					RuleCoverage.IntermediateCoverage.TRUE,
					RuleCoverage.IntermediateCoverage.ERROR,
					Result.DECISION_INDETERMINATE, rule.getId().toString());
			PolicyCoverageFactory.addCoverage(ruleCoverage, xPath);
			// end of change

			// if it was INDETERMINATE, then that's what we return
			return ResultFactory.getFactory().getResult(
					Result.DECISION_INDETERMINATE, result.getStatus(), context);
		} else {
			// otherwise we return the effect on true, and NA on false
			BooleanAttribute bool = (BooleanAttribute) (result
					.getAttributeValue());
			if (bool.getValue()) {
				// start of change
				Coverage ruleCoverage = new RuleCoverage(
						RuleCoverage.IntermediateCoverage.TRUE,
						RuleCoverage.IntermediateCoverage.TRUE, effectAttr, rule.getId().toString());
				PolicyCoverageFactory.addCoverage(ruleCoverage, xPath);
				// end of change
				// if any obligations or advices are defined, evaluates them and
				// return
				return ResultFactory.getFactory().getResult(effectAttr, rule.processObligations(context), rule.processAdvices(context), context);
			} else {
				// start of change
				Coverage ruleCoverage = new RuleCoverage(
						RuleCoverage.IntermediateCoverage.TRUE,
						RuleCoverage.IntermediateCoverage.FALSE,
						Result.DECISION_NOT_APPLICABLE, rule.getId().toString());
				PolicyCoverageFactory.addCoverage(ruleCoverage, xPath);
				// end of change
				return ResultFactory.getFactory().getResult(
						Result.DECISION_NOT_APPLICABLE, context);
			}
		}

	}

	// record the entry of policy evaluation
	before(AbstractPolicy policy, EvaluationCtx context): target(policy) && call(* AbstractPolicy.evaluate(*)) && args(context) {
		logger.debug("enter Policy ID: " + policy.getId());
    	AbstractTarget policyTarget = policy.getTarget();
    	int result = MatchResult.MATCH; // assume that there is no policy target (considered a match)
        if (policyTarget != null) {
        	MatchResult matchResult = policyTarget.match(context);
            result = matchResult.getResult();
        }
        String xpath = XpathSolver.buildTargetXpath(policy);
        PolicyCoverageFactory.addCoverage(new TargetCoverage(result), xpath);
	}

	// record the result of policy evaluation
	after(AbstractPolicy policy) returning(AbstractResult result): target(policy) && call(* AbstractPolicy.evaluate(*))  {
		logger.debug("leave Policy ID:" + policy.getId());
	}

	pointcut runNewTest(AbstractPolicy policy, String request,
			String oracleString): call(boolean TestSuite.runTest(AbstractPolicy, String, String)) && args(policy, request, oracleString);

	before(AbstractPolicy policy, String request, String oracleString) : runNewTest(policy, request, oracleString) {
		logger.debug("start running a test on " + policy.getId());
		PolicyCoverageFactory.newRow();
	}

	pointcut runNewTestSuite(TestSuite testSuite, AbstractPolicy policy): call(List<Boolean> TestSuite.runTests(AbstractPolicy)) && target(testSuite) && args(policy);

	before(TestSuite testSuite, AbstractPolicy policy): runNewTestSuite(testSuite, policy) {
		logger.debug("start running test suite on " + policy.getId());
		PolicyCoverageFactory.init(policy);
	}
	
	after(TestSuite testSuite, AbstractPolicy policy) returning(List<Boolean> results): runNewTestSuite(testSuite, policy) {
		logger.debug("finished running test suite on " + policy.getId());
		PolicyCoverageFactory.setResults(results);
	}
	
	pointcut encodePolicySet(PolicySet policySet, StringBuilder builder): call(void AbstractPolicy.encode(StringBuilder)) && target(policySet) && args(builder);

	void around(PolicySet policySet, StringBuilder builder): encodePolicySet(policySet, builder) {
        String xacmlVersionId = policySet.getMetaData().getXACMLIdentifier();
        // add xacml version ID
        builder.append("<PolicySet xmlns=\"" + xacmlVersionId + "\""  + " PolicySetId=\"").
                append(policySet.getId().toString()).append("\" PolicyCombiningAlgId=\"").
                append(policySet.getCombiningAlg().getIdentifier().toString()).append("\">\n");

        String description = policySet.getDescription();
        if (description != null){
            builder.append("<Description>").append(description).append("</Description>\n");
        }
        
        String version = policySet.getDefaultVersion();
        if (version != null){
            builder.append("<PolicySetDefaults><XPathVersion>").append(version).
                    append("</XPathVersion></PolicySetDefaults>\n");
        }
        policySet.getTarget().encode(builder);
        policySet.encodeCommonElements(builder);
        builder.append("</PolicySet>\n");
	}
}
