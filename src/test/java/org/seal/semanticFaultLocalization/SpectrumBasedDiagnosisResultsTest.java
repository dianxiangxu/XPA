package org.seal.semanticFaultLocalization;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by shuaipeng on 10/14/16.
 */
public class SpectrumBasedDiagnosisResultsTest {
    @Test
    public void indexRankedBySuspicionTest() {
        double[] coefficients = new double[]{0.8, 0.7, 0.9, 0.7, 0.6};
        SpectrumBasedDiagnosisResults diagnosisResults = new SpectrumBasedDiagnosisResults(coefficients);
        List<Integer> indexRank = diagnosisResults.getIndexRankedBySuspicion();
        List<Integer> expected = Arrays.asList(2, 0, 1, 3, 4);
        assertTrue(indexRank.equals(expected));
    }
}
