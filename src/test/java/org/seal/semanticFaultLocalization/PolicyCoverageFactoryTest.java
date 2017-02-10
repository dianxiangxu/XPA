package org.seal.semanticFaultLocalization;

import org.junit.Assert;
import org.junit.Test;
import org.seal.semanticCoverage.*;
import org.seal.policyUtils.PolicyLoader;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This class tests if the semanticCoverage information collected by {@link PolicyCoverageFactory} is correct.
 * Created by shuaipeng on 10/14/16.
 */
public class PolicyCoverageFactoryTest {
    @Test
    public void coverageMatrixTest() throws ParserConfigurationException, ParsingException, SAXException, IOException {
        String testsCSVfileName = "org/seal/policies/conference3/test_suites/conference3_MCDCCoverage/conference3_MCDCCoverage.csv";
        File testsCSVfile = new File(faultLocalizationTest.class.getClassLoader().getResource(testsCSVfileName).getFile());
        TestSuite testSuite = TestSuite.loadTestSuite(testsCSVfile);
        String fileName = "org/seal/policies/conference3/conference3.xml";
        File file = new File(getClass().getClassLoader().getResource(fileName).getFile());
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        List<Boolean> results = testSuite.runTests(policy);
        for (boolean result : results)
            Assert.assertTrue(result);
        List<List<Coverage>> coverageMatrix = PolicyCoverageFactory.getCoverageMatrix();
        // convert the semanticCoverage matrix to a matrix of integer to ease testing
        int numTests = coverageMatrix.size();
        int numElems = 0;
        for (List<Coverage> row : coverageMatrix)
            numElems = Math.max(numElems, row.size());
        int[][] matrix = new int[numTests][numElems];
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
        int[][] expectedMatrix = new int[][]{{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
        Assert.assertEquals(matrix.length, expectedMatrix.length);
        for (int i = 0; i < matrix.length; i++)
            Assert.assertArrayEquals(matrix[i], expectedMatrix[i]);
    }

}
