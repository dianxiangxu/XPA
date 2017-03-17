package org.seal.semanticRepair;

import com.opencsv.CSVWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.seal.policyUtils.PolicyLoader;
import org.seal.semanticCoverage.TestSuite;
import org.seal.semanticMutation.Mutant;
import org.seal.semanticMutation.Mutator;
import org.wso2.balana.AbstractPolicy;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shuaipeng on 3/17/17.
 */
public class ExperimentOnRepair {
    public static void main(String[] args) {

    }

    private static List<Mutant> createMultiFaultMutants(AbstractPolicy policy, int numFaults,
                                                        List<String> mutationMethods) throws IOException, SAXException, ParserConfigurationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<List<Mutant>> mutantLists = new ArrayList<List<Mutant>>();
        Mutant baseMutant = new Mutant(policy, "");
        mutantLists.add(new ArrayList<Mutant>());
        mutantLists.get(0).add(baseMutant);
        for (int i = 1; i <= numFaults; i++) {
            mutantLists.add(new ArrayList<Mutant>());
            for (Mutant mutant : mutantLists.get(i - 1)) {
                Mutator mutator = new Mutator(mutant);
                mutantLists.get(i).addAll(mutator.generateSelectedMutants(mutationMethods));
            }
        }
        return mutantLists.get(numFaults);
    }

    private static void writeCSVTitleRow(CSVWriter writer,
                                         List<String> faultLocalizeMethods) {
        String[] titles = new String[faultLocalizeMethods.size() + 1];
        titles[0] = "mutant";
        int index = 1;
        for (String faultLocalizeMethod : faultLocalizeMethods) {
            titles[index] = faultLocalizeMethod;
            index++;
        }
        writer.writeNext(titles);
    }

    private static void writeCSVResultRow(CSVWriter writer, String mutantNumber,
                                          List<String> durationList) {
        String[] entry = new String[durationList.size() + 1];
        entry[0] = mutantNumber;
        int index = 1;
        for (String duration : durationList) {
            entry[index] = duration;
            index++;
        }
        writer.writeNext(entry);
        writer.flushQuietly();
    }

    public void startExperiment() throws Exception {
        File policyFile = new File("src/test/resources/org/seal/policies/HL7/HL7.xml");
        AbstractPolicy policy = PolicyLoader.loadPolicy(policyFile);
        File testsCSVfile = new File("src/test/resources/org/seal/policies/HL7/test_suites/manual/HL7.csv");
        TestSuite testSuite = TestSuite.loadTestSuite(testsCSVfile);
        List<String> scoringMethods = new ArrayList<>();
        scoringMethods.add("naish2");
        scoringMethods.add("random");
        int numFaults = 2;
        List<String> mutationMethods = new ArrayList<>();
        //definitely can be repaired
        mutationMethods.add("createCombiningAlgorithmMutants");//CRC
        mutationMethods.add("createRuleEffectFlippingMutants");// CRE
        mutationMethods.add("createAddNotFunctionMutants");// ANF
        mutationMethods.add("createRemoveNotFunctionMutants");// RNF
        mutationMethods.add("createFlipComparisonFunctionMutants");// FCF
        mutationMethods.add("createChangeComparisonFunctionMutants");// CCF
        List<Mutant> faultyPolicies = createMultiFaultMutants(policy, numFaults, mutationMethods);
        String timingFileName = "experiments/HL7/repair/HL7_multiFault_repair_timing.csv";
        String repairResultFileName = "experiments/HL7/repair/HL7_multiFault_repairedFile.csv";
        FileUtils.forceMkdir(new File(FilenameUtils.getPath(timingFileName)));
        FileUtils.forceMkdir(new File(FilenameUtils.getPath(repairResultFileName)));
        CSVWriter timingWriter = new CSVWriter(new FileWriter(timingFileName), ',');
        CSVWriter repairResultWriter = new CSVWriter(new FileWriter(repairResultFileName), ',');
        writeCSVTitleRow(timingWriter, scoringMethods);
        writeCSVTitleRow(repairResultWriter, scoringMethods);
        Repairer repairer = new Repairer();
        for (Mutant faultyPolicy : faultyPolicies) {
            List<String> durationList = new ArrayList<>();
            List<String> repairedFileList = new ArrayList<>();
            for (String scoringMethod : scoringMethods) {
                String repaired = repairer.repair(faultyPolicy, testSuite, scoringMethod, numFaults);
                repairedFileList.add(repaired);
                //TODO get time takes to repair a faulty policy, add to durationList
            }
            writeCSVResultRow(timingWriter, faultyPolicy.getName(), durationList);
            writeCSVResultRow(repairResultWriter, faultyPolicy.getName(), repairedFileList);
        }
        timingWriter.close();
        repairResultWriter.close();
    }

}
