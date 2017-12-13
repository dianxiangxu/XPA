package org.seal.xacml.semanticRepair;

import org.apache.commons.io.IOUtils;
import org.seal.xacml.policyUtils.PolicyLoader;
import org.seal.xacml.policyUtils.XpathSolver;
import org.seal.xacml.semanticCoverage.PolicyCoverageFactory;
import org.seal.xacml.semanticCoverage.TestSuite;
import org.seal.xacml.semanticFaultLocalization.SpectrumBasedDiagnosisResults;
import org.seal.xacml.semanticFaultLocalization.SpectrumBasedFaultLocalizer;
import org.seal.xacml.semanticMutation.Mutant;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
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

    List<Boolean> getTestResult() {
        if (testResult == null) {
            testResult = testSuite.runTests(mutant);
        }
        return testResult;
    }

    /**
     * make sure to run tests before calling this method
     */
    List<Integer> getSuspicionRank() throws IOException, SAXException, ParserConfigurationException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
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

    /**
     * The faulty policy is at the root of the tree, and of course is promising. We assume that the faults are independent,
     * and that if the failed tests of a mutant is the subset of the failed tests of its parent, the mutant is promising.
     *
     * @return
     * @throws Exception
     */
    boolean isPromising() {
        return parent == null || failedTestsIsSubset();
    }

    private boolean failedTestsIsSubset() {
        List<Boolean> result = getTestResult();
        List<Boolean> parentResult = parent.getTestResult();
        for (int i = 0; i < result.size(); i++)
            if (!result.get(i) && parentResult.get(i))
                return false;
        return true;
    }

    private List<Integer> getRandomSuspicionRank(Mutant mutant) throws ParserConfigurationException, SAXException, IOException {
        InputStream stream = IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset());
        Document doc = PolicyLoader.getDocument(stream);
        List<String> xpathList = XpathSolver.getEntryListRelativeXPath(doc);
        List<Integer> randomSuspicionRank = new ArrayList<>();
        for (int i = 0; i < xpathList.size(); i++) {
            randomSuspicionRank.add(i);
        }
        Collections.shuffle(randomSuspicionRank);
        return randomSuspicionRank;
    }
}
