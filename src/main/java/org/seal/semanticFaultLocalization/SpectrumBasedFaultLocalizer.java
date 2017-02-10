package org.seal.semanticFaultLocalization;

import org.seal.semanticCoverage.Coverage;
import org.seal.semanticCoverage.PolicyCoverageFactory;
import org.seal.semanticCoverage.RuleCoverage;
import org.seal.semanticCoverage.TargetCoverage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shuaipeng on 10/11/16.
 */
public class SpectrumBasedFaultLocalizer {
    private int[][] matrix;
    private int[] verdicts;

    public SpectrumBasedFaultLocalizer(List<List<Coverage>> coverageMatrix) {
        int numTests = coverageMatrix.size();
        int numElems = 0;
        for (List<Coverage> row : coverageMatrix)
            numElems = Math.max(numElems, row.size());
        matrix = new int[numTests][numElems];
        for (int i = 0; i < coverageMatrix.size(); i++) {
            List<Coverage> row = coverageMatrix.get(i);
            for (int j = 0; j < row.size(); j++) {
                Coverage coverage = row.get(j);
                if (coverage instanceof TargetCoverage) {
                    if (((TargetCoverage) coverage).getMatchResult() == TargetCoverage.TargetMatchResult.MATCH)
                        matrix[i][j] = 1;
                } else if (coverage instanceof RuleCoverage) {
                    if (((RuleCoverage) coverage).getRuleDecisionCoverage() == RuleCoverage.RuleDecisionCoverage.EFFECT)
                        matrix[i][j] = 1;
                }
            }
        }
//        printMatrix(matrix);
        List<Boolean> results = PolicyCoverageFactory.getResults();
        verdicts = new int[numTests];
        for (int i = 0; i < results.size(); i++)
            verdicts[i] = results.get(i) ? 0 : 1;
    }

    /**
     * for debugging
     *
     * @param matrix semanticCoverage matrix
     */
    private static void printMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++)
            System.out.println(i + ": " + Arrays.toString(matrix[i]));
    }

    public double[] applyFaultLocalizeMethod(String faultLocalizeMethod)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<?> cls = this.getClass();
        Method method = cls.getDeclaredMethod(faultLocalizeMethod);
        return (double[]) method.invoke(this);
    }


    private int apq(int p, int q, int j) {
        int sum = 0;
        for (int testIndex = 0; testIndex < matrix.length; testIndex++) {
            if (p == 0) {
                if (matrix[testIndex][j] == p && verdicts[testIndex] == q)
                    sum = sum + 1; // Target&&Condition both Not Evaluated (Score=1).
            } else {
                if (verdicts[testIndex] == q) {
                    if (matrix[testIndex][j] == 1)
                        sum += 1;
                    else if (matrix[testIndex][j] == 2)
                        sum += 2;
                    else if (matrix[testIndex][j] == 0)
                        sum += 0;
                }
            }
        }
        return sum;
    }

    double[] cbi() {
        int numElems = matrix[0].length;
        double[] coefficients = new double[numElems];
        for (int j = 0; j < matrix[0].length; j++) {
            double a11 = apq(1, 1, j);
            double a01 = apq(0, 1, j);
            double a10 = apq(1, 0, j);
            double a00 = apq(0, 0, j);
            coefficients[j] = a11 + a10 != 0 ? a11 / (a11 + a10) - (a11 + a01) / (a11 + a10 + a01 + a00) : 0;
        }
        return coefficients;
    }

    double[] hamann() {
        int numElems = matrix[0].length;
        double[] coefficients = new double[numElems];
        for (int j = 0; j < matrix[0].length; j++) {
            double a11 = apq(1, 1, j);
            double a01 = apq(0, 1, j);
            double a10 = apq(1, 0, j);
            double a00 = apq(0, 0, j);
            double d = a11 + a10 + a01 + a00;
            coefficients[j] = d != 0 ? (a11 + a00 - a01 - a10) / d : 0;
        }
        return coefficients;
    }

    double[] simpleMatching() {
        int numElems = matrix[0].length;
        double[] coefficients = new double[numElems];
        for (int j = 0; j < matrix[0].length; j++) {
            double a11 = apq(1, 1, j);
            double a01 = apq(0, 1, j);
            double a10 = apq(1, 0, j);
            double a00 = apq(0, 0, j);
            double d = a11 + a10 + a01 + a00;
            coefficients[j] = d != 0 ? (a11 + a00) / d : 0;
        }
        return coefficients;
    }

    double[] sokal() {
        int numElems = matrix[0].length;
        double[] coefficients = new double[numElems];
        for (int j = 0; j < matrix[0].length; j++) {
            double a11 = apq(1, 1, j);
            double a01 = apq(0, 1, j);
            double a10 = apq(1, 0, j);
            double a00 = apq(0, 0, j);
            double n = 2 * (a11 + a00);
            double d = n + a10 + a01;
            coefficients[j] = d != 0 ? n / d : 0;
        }
        return coefficients;
    }

    double[] rogersTanimoto() {
        int numElems = matrix[0].length;
        double[] coefficients = new double[numElems];
        for (int j = 0; j < matrix[0].length; j++) {
            double a11 = apq(1, 1, j);
            double a01 = apq(0, 1, j);
            double a10 = apq(1, 0, j);
            double a00 = apq(0, 0, j);
            double n = a11 + a00;
            double d = n + 2 * (a10 + a01);
            coefficients[j] = d != 0 ? n / d : 0;
        }
        return coefficients;
    }

    double[] euclid() {
        int numElems = matrix[0].length;
        double[] coefficients = new double[numElems];
        for (int j = 0; j < matrix[0].length; j++) {
            double a11 = apq(1, 1, j);
            double a00 = apq(0, 0, j);
            coefficients[j] = Math.sqrt(a11 + a00);
        }
        return coefficients;
    }

    double[] anderberg() {
        int numElems = matrix[0].length;
        double[] coefficients = new double[numElems];
        for (int j = 0; j < matrix[0].length; j++) {
            double a11 = apq(1, 1, j);
            double a01 = apq(0, 1, j);
            double a10 = apq(1, 0, j);
            coefficients[j] = a11 + 2 * a01 + 2 * a10 != 0 ? a11 / (a11 + 2 * a01 + 2 * a10) : 0;
        }
        return coefficients;
    }

    double[] sorensenDice() {
        int numElems = matrix[0].length;
        double[] coefficients = new double[numElems];
        for (int j = 0; j < matrix[0].length; j++) {
            double a11 = apq(1, 1, j);
            double a01 = apq(0, 1, j);
            double a10 = apq(1, 0, j);
            coefficients[j] = 2 * a11 + a01 + a10 != 0 ? a11 / (2 * a11 + a01 + a10) : 0;
        }
        return coefficients;
    }

    double[] goodman() {
        int numElems = matrix[0].length;
        double[] coefficients = new double[numElems];
        for (int j = 0; j < matrix[0].length; j++) {
            double a11 = apq(1, 1, j);
            double a01 = apq(0, 1, j);
            double a10 = apq(1, 0, j);
            double d = 2 * a11 + a01 + a10;
            coefficients[j] = d != 0 ? ((double) 2 * a11 - a01 - a10) / d : 0;
        }
        return coefficients;
    }

    double[] jaccard() {
        int numElems = matrix[0].length;
        double[] coefficients = new double[numElems];
        for (int j = 0; j < matrix[0].length; j++) {
            double a11 = apq(1, 1, j);
            double a01 = apq(0, 1, j);
            double a10 = apq(1, 0, j);
            coefficients[j] = a11 + a01 + a10 != 0 ? a11 / (a11 + a01 + a10) : 0;
        }
        return coefficients;
    }

    double[] naish2() {
        int numElems = matrix[0].length;
        double[] coefficients = new double[numElems];
        for (int j = 0; j < matrix[0].length; j++) {
            double a11 = apq(1, 1, j);
            double a10 = apq(1, 0, j);
            double a00 = apq(0, 0, j);
            coefficients[j] = a11 - (a10 / (a10 + a00 + 1));
        }
        return coefficients;
    }

    double[] tarantula() {
        int numElems = matrix[0].length;
        double[] coefficients = new double[numElems];
        for (int j = 0; j < matrix[0].length; j++) {
            double a11 = apq(1, 1, j);
            double a01 = apq(0, 1, j);
            double a10 = apq(1, 0, j);
            double a00 = apq(0, 0, j);
            double x = a11 + a01 != 0 ? a11 / (a11 + a01) : 0;
            double y = a10 + a00 != 0 ? a10 / (a10 + a00) : 0;
            coefficients[j] = x + y != 0 ? x / (x + y) : 0;
        }
        return coefficients;
    }

    double[] ochiai() {
        int numElems = matrix[0].length;
        double[] coefficients = new double[numElems];
        for (int j = 0; j < matrix[0].length; j++) {
            double a11 = apq(1, 1, j);
            double a01 = apq(0, 1, j);
            double a10 = apq(1, 0, j);
            double d = Math.sqrt((a11 + a01) * (a11 + a10));
            coefficients[j] = d != 0 ? a11 / d : 0;
        }
        return coefficients;
    }

    double[] ochiai2() {
        int numElems = matrix[0].length;
        double[] coefficients = new double[numElems];
        for (int j = 0; j < matrix[0].length; j++) {
            double a11 = apq(1, 1, j);
            double a01 = apq(0, 1, j);
            double a10 = apq(1, 0, j);
            double a00 = apq(0, 0, j);
            double d = Math.sqrt((a11 + a10) * (a00 + a01) * (a11 + a01) * (a10 + a00));
            coefficients[j] = d != 0 ? (a11 * a00) / d : 0;
        }
        return coefficients;
    }


}
