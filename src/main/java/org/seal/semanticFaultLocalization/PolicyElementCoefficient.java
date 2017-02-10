package org.seal.semanticFaultLocalization;

/**
 * Created by shuaipeng on 10/14/16.
 */
class PolicyElementCoefficient implements Comparable<PolicyElementCoefficient> {
    private double coefficient;
    private int index;
    private int rank;

    PolicyElementCoefficient(double coefficient, int index) {
        this.coefficient = coefficient;
        this.index = index;
    }

    private static boolean approximateEqual(double a, double b) {
        return Math.abs(a - b) < 0.0000000001;
    }

    @Override
    public int compareTo(PolicyElementCoefficient other) {
        return Double.compare(other.coefficient, this.coefficient);
    }

    boolean approximateEqual(PolicyElementCoefficient other) {
        return approximateEqual(coefficient, other.coefficient);
    }

    int getIndex() {
        return index;
    }

    int getRank() {
        return rank;
    }

    void setRank(int rank) {
        this.rank = rank;
    }
}
