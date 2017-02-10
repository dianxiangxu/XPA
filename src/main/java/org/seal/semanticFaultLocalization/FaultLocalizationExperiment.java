package org.seal.semanticFaultLocalization;

import com.opencsv.CSVWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.seal.semanticCoverage.*;
import org.seal.semanticMutation.Mutant;
import org.seal.policyUtils.PolicyLoader;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shuaipeng on 10/11/16.
 */
public class FaultLocalizationExperiment {
    //TODO write a program to modify the given requests, generating test suites
    public static void main(String[] args) throws IOException {
        try {
            runTest();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (ParsingException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
//        String mutantsCSVfileName = "experiments/conference3/mutants/mutants.csv";
        String mutantsCSVfileName = "experiments/HL7/mutants/manual/mutants.csv";
        File mutantsCSVfile = new File(mutantsCSVfileName);
//        String faultLocalizeResultsFile = "experiments/conference3/fault-localization/conference3_faultLocalization.csv";
        String faultLocalizeResultsFile = "experiments/HL7/fault-localization/HL7_faultLocalization.csv";
        FileUtils.forceMkdir(new File(FilenameUtils.getPath(faultLocalizeResultsFile)));
        CSVWriter writer = new CSVWriter(new FileWriter(faultLocalizeResultsFile), ',');
        List<String> faultLocalizeMethods = Arrays.asList("jaccard","tarantula","ochiai","ochiai2","cbi","hamann",
                "simpleMatching","sokal","naish2","goodman","sorensenDice","anderberg","euclid","rogersTanimoto");
        writeCSVTitleRow(writer, faultLocalizeMethods);

        try {
            List<Mutant> mutants = PolicyLoader.readMutantsCSVFile(mutantsCSVfile);
//            File testsCSVfile = new File("experiments/HL7/test_suites/manual/HL7.csv");
            File testsCSVfile = new File("experiments/HL7/test_suites/mcdc/HL7.csv");
            TestSuite testSuite = TestSuite.loadTestSuite(testsCSVfile);
            for (Mutant mutant: mutants) {
                System.out.println(mutant.getName());
                List<Boolean> results = testSuite.runTests(mutant);
                if (booleanListAnd(results))
                    continue;
                List<List<Coverage>> coverageMatrix = PolicyCoverageFactory.getCoverageMatrix();
                SpectrumBasedFaultLocalizer faultLocalizer = new SpectrumBasedFaultLocalizer(coverageMatrix);
                List<String> aveNumElemToInspcetList = new ArrayList<>();
                for (String faultLocalizeMethod: faultLocalizeMethods) {
                    SpectrumBasedDiagnosisResults diagnosisResults = new SpectrumBasedDiagnosisResults(
                            faultLocalizer.applyFaultLocalizeMethod(faultLocalizeMethod));
                    List<Integer> faultLocations = mutant.getFaultLocations();
                    aveNumElemToInspcetList.add(Double.toString(diagnosisResults.getAverageNumberOfElementsToInspect(faultLocations)));
                }
                String mutantName = mutant.getName();
                writeCSVResultRow(writer, mutantName, aveNumElemToInspcetList);
//                SpectrumBasedDiagnosisResults diagnosisResults = new SpectrumBasedDiagnosisResults(faultLocalizer.naish2());
//                System.out.println(diagnosisResults.getIndexRankedBySuspicion());
//                List<Integer> faultLocations = ((Mutant)mutant).getFaultLocations();
//                System.out.println(faultLocations.toString());
//                System.out.println(diagnosisResults.getAverageNumberOfElementsToInspect(faultLocations));
            }
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }


    static boolean booleanListAnd(List<Boolean> booleanList) {
        for (boolean b: booleanList)
            if (!b)
                return false;
        return true;
    }

    static void writeCSVTitleRow(CSVWriter writer, List<String> faultLocalizeMethods) {
        String[] titles = new String[faultLocalizeMethods.size() + 1];
        titles[0] = "mutant";
        int index = 1;
        for (String faultLocalizeMethod : faultLocalizeMethods) {
            titles[index] = faultLocalizeMethod;
            index++;
        }
        writer.writeNext(titles);
    }

    static void writeCSVResultRow(CSVWriter writer, String mutantName, List<String> columnValue) {
        String[] entry = new String[columnValue.size() + 1];
        entry[0] = mutantName;
        int index = 1;
        for (String duration: columnValue) {
            entry[index] = duration;
            index ++;
        }
        writer.writeNext(entry);
        writer.flushQuietly();
    }

    private static void runTest() throws ParserConfigurationException, ParsingException, SAXException, IOException {
//        File csvFile = new File("experiments/conference3/test_suites/conference3_MCDCCoverage/conference3_MCDCCoverage.csv");
//        File csvFile = new File("experiments/HL7/test_suites/manual/HL7.csv");
        File csvFile = new File("experiments/HL7/test_suites/mcdc/HL7.csv");
        TestSuite testSuite = TestSuite.loadTestSuite(csvFile);
//        File file = new File("/media/shuaipeng/data/XPA/xpa/src/main/resources/org/seal/policies/conference3.xml");
        File file = new File("/media/shuaipeng/data/XPA/xpa/src/test/resources/org/seal/policies/HL7/HL7.xml");
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        List<Boolean> results = testSuite.runTests(policy);
        System.out.println(results.toString());
        List<List<Coverage>> coverageMatrix = PolicyCoverageFactory.getCoverageMatrix();
        System.out.println("there are " + coverageMatrix.size() + " tests");
        for (List<Coverage> row : coverageMatrix)
            System.out.println(row.size());
        for (int i = 0; i < coverageMatrix.size(); i++) {
            System.out.println("test No. " + i);
            List<Coverage> row = coverageMatrix.get(i);
            for (Coverage coverage : row) {
                if (coverage instanceof RuleCoverage) {
                    System.out.println("RuleCoverage");
                    RuleCoverage ruleCoverage = (RuleCoverage) coverage;
                    System.out.println(ruleCoverage.getRuleId());
                    System.out.println(ruleCoverage.getCombinedCoverage());
                    System.out.println(ruleCoverage.getRuleDecisionCoverage());
                } else if (coverage instanceof TargetCoverage) {
                    System.out.println("TargetCoverage");
                    TargetCoverage targetCoverage = (TargetCoverage) coverage;
                    System.out.println(targetCoverage.getMatchResult());
                }
            }
        }

        SpectrumBasedFaultLocalizer faultLocalizer = new SpectrumBasedFaultLocalizer(PolicyCoverageFactory.getCoverageMatrix());

    }
}
