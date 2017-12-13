package org.seal.xacml.semanticFaultLocalization;

/**
 * Created by shuaipeng on 10/14/16.
 */
class PolicyElementCoefficient implements Comparable<PolicyElementCoefficient> {
    private double suspiciousScore;
    private int index;
    private int rank;

    PolicyElementCoefficient(double suspiciousScore, int index) {
    	this.suspiciousScore = suspiciousScore;
        this.index = index;
    }

    private static boolean approximateEqual(double a, double b) {
        return Math.abs(a - b) < 0.0000000001;
    }

    @Override
    public int compareTo(PolicyElementCoefficient other) {
        return Double.compare(other.suspiciousScore, this.suspiciousScore);
    }

    boolean approximateEqual(PolicyElementCoefficient other) {
        return approximateEqual(suspiciousScore, other.suspiciousScore);
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
    
    public double getSuspiciousScore(){
    	return suspiciousScore;
    }
}
