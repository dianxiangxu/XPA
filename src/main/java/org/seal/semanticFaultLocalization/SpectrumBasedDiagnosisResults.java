package org.seal.semanticFaultLocalization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by shuaipeng on 10/14/16.
 */
public class SpectrumBasedDiagnosisResults {
    private List<PolicyElementCoefficient> coefficientList = new ArrayList<>();

    SpectrumBasedDiagnosisResults(double[] coefficients) {
        for (int i = 0; i < coefficients.length; i++) {
            coefficientList.add(new PolicyElementCoefficient(coefficients[i], i));
        }
        Collections.sort(coefficientList);
        rankSuspicion(coefficientList);
    }

    /**
     * set rank for each element in ruleCoefficients
     * note that if two element in ruleCoefficients have almost the same coefficient,
     * set their rank as the same. For example, [0.9, 0.9, 0.8, 0.7, 0.7] will have rank
     * [2, 2, 3, 5, 5]. The rank is not [1, 1, 3, 4, 4] because we want to know worst
     * case performance.
     */
    private static void rankSuspicion(List<PolicyElementCoefficient> ruleCoefficients) {
        //worst case ranking
        int size = ruleCoefficients.size();
        ruleCoefficients.get(size - 1).setRank(size);
        for (int index = size - 2; index >= 0; index--) {
            if (ruleCoefficients.get(index).approximateEqual(ruleCoefficients.get(index + 1)))
                ruleCoefficients.get(index).setRank(ruleCoefficients.get(index + 1).getRank());
            else
                ruleCoefficients.get(index).setRank(index + 1);
        }
    }

    List<Integer> getIndexRankedBySuspicion() {
        List<Integer> indexRankedBySuspicion = new ArrayList<>();
        for (PolicyElementCoefficient coefficient : coefficientList)
            indexRankedBySuspicion.add(coefficient.getIndex());
        return Collections.unmodifiableList(indexRankedBySuspicion);
    }

    /**
     * @param bugPosition
     */
    private int getNumberOfElementsToInspect(int bugPosition) {
        if (coefficientList.size() == 0)
            throw new RuntimeException("coefficientList is empty");
        for (PolicyElementCoefficient coefficient : coefficientList) {
            if (coefficient.getIndex() == bugPosition) {
                return coefficient.getRank();
            }
        }
        throw new RuntimeException("bugPosition " + bugPosition + " is not in coefficientList");
    }

    double getAverageNumberOfElementsToInspect(List<Integer> bugPositions) {
        List<Double> scores = new ArrayList<>();
        for (int bugPosition : bugPositions) {
            scores.add((double) getNumberOfElementsToInspect(bugPosition));
        }
        double average = 0;
        for (double score : scores)
            average += score;
        average /= scores.size();
        return average;
    }
}
