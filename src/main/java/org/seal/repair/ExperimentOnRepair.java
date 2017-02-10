/**
 * 
 */
package org.seal.repair;

import org.seal.mutation.PolicyMutator;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.seal.mutation.PolicyMutant;

import com.opencsv.CSVWriter;

/**
 * @author speng
 * 
 */
public class ExperimentOnRepair {
	private String policyFilePath;
	private String testSuiteSpreadSheetFile;
	private List<PolicyMutant> mutantList;
	private static List<Integer> numTriesList;
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String[] policy = { "conference3", "fedora-rule3", "itrust3",
				"kmarket-blue-policy", "obligation3", "pluto3" };
		String[] testsuite = { "Basic", "Exclusive", "Pair", "PDpair",
				"DecisionCoverage", "RuleLevel", "MCDCCoverage" };
		int policyNumber = 0;
		int testsuiteNumber = 6;

		String testSuiteSpreadSheetFile = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + "test_suites"
				+ File.separator + policy[policyNumber] + "_"
				+ testsuite[testsuiteNumber] + File.separator
				+ policy[policyNumber] + "_" + testsuite[testsuiteNumber]
				+ ".xls";
		String policyMutantSpreadsheetFile = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + "mutants"
				+ File.separator + policy[policyNumber] + "_mutants.xls";
		String experimentResultFileName = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + "repair"
				+ File.separator + policy[policyNumber] + "_"
				+ testsuite[testsuiteNumber] + "_repair.csv";
		ExperimentOnRepair experiment = new ExperimentOnRepair(policyMutantSpreadsheetFile,
				testSuiteSpreadSheetFile);
		List<List<String>> repairMethodPairList = PolicyRepairer.getRepairMethodPairList();
		CSVWriter writer = new CSVWriter(new FileWriter(experimentResultFileName), ',');
		writeCSVTitleRow(writer, experiment.mutantList);
		for(List<String> repairMethodPair: repairMethodPairList) {
			long startTime = System.currentTimeMillis();
			List<PolicyMutant> correctedPolicyList = experiment.startExperiment(repairMethodPair.get(0), 
					repairMethodPair.get(1));
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			System.out.printf("running time: " + duration + " milliseconds\n");
			writeCSVResultRow(writer, repairMethodPair, correctedPolicyList,
					numTriesList, duration);
		}
		writer.close();
	}

		private static void writeCSVTitleRow(CSVWriter writer, List<PolicyMutant> mutantList) {
			String[] titles = new String[mutantList.size() + 5];
			titles[0] = "repair method";
			titles[1] = "fault localizer";
			titles[2] = "time spent(ms)";
			titles[3] = "repaired/total";
			titles[4] = "total tries";
			int index = 5;
			for (PolicyMutant mutant: mutantList) {
				titles[index] = mutant.getNumber();
				index ++;
			}
			writer.writeNext(titles);
		}

	private static void writeCSVResultRow(CSVWriter writer, List<String> repairMethodPair,
			List<PolicyMutant> correctedPolicyList, List<Integer> numTriesList,
			long duration) {
		String[] entry = new String[correctedPolicyList.size() + 5];
		entry[0] = repairMethodPair.get(0);
		entry[1] = repairMethodPair.get(1);
		entry[2] = Long.toString(duration);
		int repaired = 0;
		for (PolicyMutant mutant: correctedPolicyList)
			if (mutant != null)
				repaired ++;
		entry[3] = repaired + "/" + correctedPolicyList.size();
		int numTriesTotal = 0;
		for (int numTries: numTriesList)
			numTriesTotal += numTries;
		entry[4] = Integer.toString(numTriesTotal);
		int index = 5;
		for (int numTries: numTriesList) {
			entry[index] = Integer.toString(numTries);
			index ++;
		}
		writer.writeNext(entry);
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public ExperimentOnRepair(String policyFilePath,
			String testSuiteSpreadSheetFile) throws Exception {
		this.policyFilePath = policyFilePath;
		this.testSuiteSpreadSheetFile = testSuiteSpreadSheetFile;
		this.mutantList = createSelectedMutants();
	}

	public List<PolicyMutant> startExperiment(String repairMethod, String faultLocalizeMethod) throws Exception {
		PolicyRepairer repairer = new PolicyRepairer(testSuiteSpreadSheetFile);
		List<PolicyMutant> correctedPolicyList = new ArrayList<PolicyMutant>();
		numTriesList = new ArrayList<Integer>();
		PolicyMutant correctedPolicy;
		for (PolicyMutant mutant : this.mutantList) {
			System.out.println("bugPosition:\t" + Arrays.toString(mutant.getFaultLocation()));
			switch (repairMethod) {
			case "repairRandomOrder":
				correctedPolicy = repairer.repairRandomOrder(mutant);
				break;
			case "repairOneByOne":
				correctedPolicy = repairer.repairOneByOne(mutant);
				break;
			case "repairSmartly":
				correctedPolicy = repairer.repairSmartly(mutant, faultLocalizeMethod);
				break;
			default:
				throw new IllegalArgumentException("wrong  repairMethod" + repairMethod);
			}			
			correctedPolicyList.add(correctedPolicy);
			numTriesList.add(repairer.getNumTriesBeforSucceed());
//			Test.showRepairResult(correctedPolicy, mutant.getMutantFilePath());
//			System.out.println("==========");
		}
		return correctedPolicyList;
	}
	
	
	private List<PolicyMutant> createSelectedMutants() throws Exception {
		//comment out some mutants that cannot be repaired
		PolicyMutator policyMutator = new PolicyMutator(this.policyFilePath);
		policyMutator.createPolicyTargetTrueMutants();
		policyMutator.createPolicyTargetFalseMutants();
		policyMutator.createCombiningAlgorithmMutants();
		policyMutator.createRuleEffectFlippingMutants();
		policyMutator.createRemoveRuleMutants();
		policyMutator.createAddNewRuleMutants();
		//policyMutator.createRuleTargetTrueMutants();
		policyMutator.createRuleTargetFalseMutants();
		policyMutator.createRuleConditionTrueMutants();
		policyMutator.createRuleConditionFalseMutants();
		policyMutator.createFirstPermitRuleMutants();
		policyMutator.createFirstDenyRuleMutants();
		policyMutator.createRuleTypeReplacedMutants();
		policyMutator.createFlipComparisonFunctionMutants();
		policyMutator.createAddNotFunctionMutants();
		policyMutator.createRemoveNotFunctionMutants();
//		policyMutator.createRemoveParallelTargetElementMutants();
		policyMutator.createRemoveParallelConditionElementMutants();
		return policyMutator.getMutantList();
	}
	
	
}
