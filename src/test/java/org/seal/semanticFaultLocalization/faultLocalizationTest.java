package org.seal.semanticFaultLocalization;

import com.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.seal.semanticCoverage.Coverage;
import org.seal.semanticCoverage.PolicyCoverageFactory;
import org.seal.semanticCoverage.TestSuite;
import org.seal.semanticMutation.Mutant;
import org.seal.policyUtils.PolicyLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A rather comprehensive test. Run test suite against a set of mutants; for each mutant, use a set of fault localizer
 * methods to get diagnosis results; compare the diagnosis results with expected results.
 * Created by shuaipeng on 10/27/16.
 */
public class faultLocalizationTest {

    @Test
    public void faultLocalizationTestConference3() throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String mutantsCSVfileName = "org/seal/policies/conference3/mutants/mutants.csv";
        File mutantsCSVfile = new File(faultLocalizationTest.class.getClassLoader().getResource(mutantsCSVfileName).getFile());
        List<String> faultLocalizeMethods = Arrays.asList("jaccard", "tarantula", "ochiai", "ochiai2", "cbi", "hamann",
                "simpleMatching", "sokal", "naish2", "goodman", "sorensenDice", "anderberg", "euclid", "rogersTanimoto");
        List<Mutant> mutants = PolicyLoader.readMutantsCSVFile(mutantsCSVfile);
        String testsCSVfileName = "org/seal/policies/conference3/test_suites/conference3_MCDCCoverage/conference3_MCDCCoverage.csv";
        File testsCSVfile = new File(faultLocalizationTest.class.getClassLoader().getResource(testsCSVfileName).getFile());
        TestSuite testSuite = TestSuite.loadTestSuite(testsCSVfile);
        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter, ',');
        FaultLocalizationExperiment.writeCSVTitleRow(csvWriter, faultLocalizeMethods);
        for (Mutant mutant : mutants) {
            List<Boolean> results = testSuite.runTests(mutant);
            if (FaultLocalizationExperiment.booleanListAnd(results))
                continue;
            List<List<Coverage>> coverageMatrix = PolicyCoverageFactory.getCoverageMatrix();
            SpectrumBasedFaultLocalizer faultLocalizer = new SpectrumBasedFaultLocalizer(coverageMatrix);
            List<String> aveNumElemToInspcetList = new ArrayList<>();
            for (String faultLocalizeMethod : faultLocalizeMethods) {
                SpectrumBasedDiagnosisResults diagnosisResults = new SpectrumBasedDiagnosisResults(
                        faultLocalizer.applyFaultLocalizeMethod(faultLocalizeMethod));
                List<Integer> faultLocations = mutant.getFaultLocations();
                aveNumElemToInspcetList.add(Double.toString(diagnosisResults.getAverageNumberOfElementsToInspect(faultLocations)));
            }
            String mutantName = mutant.getName();
            FaultLocalizationExperiment.writeCSVResultRow(csvWriter, mutantName, aveNumElemToInspcetList);
        }
        String resultCSV = stringWriter.toString();
        csvWriter.close();
        String expectedResultCSVFileName = "org/seal/policies/conference3/fault-localization/conference3_MCDCCoverage_fault-localiazation-benchmark.csv";
        InputStream inputStream = faultLocalizationTest.class.getClassLoader().getResourceAsStream(expectedResultCSVFileName);
        String expectedCSV = IOUtils.toString(inputStream, Charset.defaultCharset());
        Assert.assertEquals(expectedCSV, resultCSV);
    }

}
