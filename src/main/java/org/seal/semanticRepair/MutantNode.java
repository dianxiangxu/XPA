package org.seal.semanticRepair;

import org.seal.semanticCoverage.PolicyCoverageFactory;
import org.seal.semanticCoverage.TestSuite;
import org.seal.semanticFaultLocalization.SpectrumBasedDiagnosisResults;
import org.seal.semanticFaultLocalization.SpectrumBasedFaultLocalizer;
import org.seal.semanticMutation.Mutant;

import java.util.List;

/**
 * Created by speng on 3/15/17.
 */

public class MutantNode implements Comparable<MutantNode> {
    private Mutant mutant;
    private TestSuite testSuite;
    private String scoringMethod;
    private MutantNode parent;
    private int totalRank;
    private int layer;
    private List<Boolean> testResult;

    MutantNode(MutantNode parent, Mutant mutant, TestSuite testSuite, String scoringMethod, int rank, int layer) {
        this.setMutant(mutant);
        this.testSuite = testSuite;
        this.scoringMethod = scoringMethod;
        this.parent = parent;
        if (parent != null)
            this.totalRank = parent.getTotalRank() + rank;
        else
            this.totalRank = rank;
        this.layer = layer;
    }

    int getLayer() {
        return layer;
    }

    Mutant getMutant() {
        return mutant;
    }

    void setMutant(Mutant mutant) {
        this.mutant = mutant;
    }

    List<Boolean> getTestResult() throws Exception {
        if (testResult == null)
            testResult = testSuite.runTests(mutant);
        return testResult;
    }

    /**
     * make sure to run tests before calling this method
     */
    List<Integer> getSuspicionRank() throws Exception {
        if (scoringMethod.equals("random")) {
            return getRandomSuspicionRank(mutant);
        }
        SpectrumBasedFaultLocalizer faultLocalizer = new SpectrumBasedFaultLocalizer(PolicyCoverageFactory.getCoverageMatrix(), PolicyCoverageFactory.getResults());
        SpectrumBasedDiagnosisResults diagnosisResults = new SpectrumBasedDiagnosisResults(faultLocalizer.applyFaultLocalizeMethod(scoringMethod));
        return diagnosisResults.getIndexRankedBySuspicion();
    }


    @Override
    public int compareTo(MutantNode other) {
        return this.getTotalRank() - other.getTotalRank();
    }

    @Override
    public boolean equals(Object obj) {
        MutantNode other = (MutantNode) obj;
        return this.getTotalRank() == other.getTotalRank();
    }

    int getTotalRank() {
        return totalRank;
    }

    boolean isPromising() throws Exception {
        if (parent == null)
            return true;
        return failedTestsIsSubset();
    }

    private boolean failedTestsIsSubset() throws Exception {
        List<Boolean> result = getTestResult();
        List<Boolean> parentResult = parent.getTestResult();
        for (int i = 0; i < result.size(); i++)
            if (!result.get(i) && parentResult.get(i))
                return false;
        return true;
    }

    private List<Integer> getRandomSuspicionRank(Mutant mutant) {
        //TODO how to get number of policy elements?
        return null;
    }
}
