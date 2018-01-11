package org.seal.xacml.coverage;

import java.util.List;

import org.seal.xacml.semanticCoverage.Coverage;
import org.seal.xacml.semanticCoverage.RuleCoverage.CombinedCoverage;

public class CoverageHelper {
	public static float getRuleCoverage(List<List<Coverage>> coverageMatrix) {
		int elementsCount = coverageMatrix.get(0).size();
		int coveredCount = 0;
		int rulesCount = 0;
		boolean ruleFlag;
		for(int i = 0; i < elementsCount;i++) {
			ruleFlag = false;
			for(List<Coverage> testCoverage:coverageMatrix) {
				Coverage coverage = testCoverage.get(i);
				if(coverage instanceof org.seal.xacml.semanticCoverage.RuleCoverage) {
					ruleFlag = true;
					if(((org.seal.xacml.semanticCoverage.RuleCoverage) coverage).getCombinedCoverage() == CombinedCoverage.BOTH_TRUE) {
						coveredCount++;
						break;
					}
				}
			}
			if(ruleFlag) {
				rulesCount++;
			}
		}
		float coverage = 0;
		if(rulesCount > 0) {
			coverage =  coveredCount / (float)rulesCount;
		} else {
			coverage = 0;
		}
		return coverage * 100;
	}
}
