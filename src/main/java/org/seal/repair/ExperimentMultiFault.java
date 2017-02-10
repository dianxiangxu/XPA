package org.seal.repair;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import org.seal.faultlocalization.FaultLocalizationExperiment;
import org.seal.mutation.PolicyMutant;
import org.seal.mutation.PolicyMutator;
import org.wso2.balana.Rule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.opencsv.CSVWriter;

public class ExperimentMultiFault {

	public static void main(String[] args) throws Exception {
		String[] policy = { "conference3", "fedora-rule3", "itrust3",
				"kmarket-blue-policy", "obligation3", "pluto3", "itrust3-5", "itrust3-10", "itrust3-20", "itrust3-40"};
		String[] testsuite = { "Basic", "Exclusive", "Pair", "PDpair",
				"DecisionCoverage", "RuleLevel", "MCDCCoverage" };
		int policyNumber = 6;
		int testsuiteNumber = 6;

		String testSuiteSpreadSheetFile = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + "test_suites"
				+ File.separator + policy[policyNumber] + "_"
				+ testsuite[testsuiteNumber] + File.separator
				+ policy[policyNumber] + "_" + testsuite[testsuiteNumber]
				+ ".xls";
		String policyMutantSpreadsheetFil = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + "mutants"
				+ File.separator + policy[policyNumber] + "_mutants.xls";
		String timingFileName = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + "repair"
				+ File.separator + policy[policyNumber] + "_"
				+ testsuite[testsuiteNumber] + "_multiFault_repair_timing.csv";
		String repairResultFileName = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + "repair"
				+ File.separator + policy[policyNumber] + "_"
				+ testsuite[testsuiteNumber] + "_multiFault_repairedFile.csv";
		String policyFile = "Experiments" + File.separator
				+ policy[policyNumber] + File.separator + policy[policyNumber]
				+ ".xml";
		List<String> faultLocalizeMethods = new ArrayList<String>();
//		faultLocalizeMethods.add("jaccard");
//		faultLocalizeMethods.add("euclid");
//		faultLocalizeMethods.add("ochiai");
//		faultLocalizeMethods.add("ochiai2");
//		faultLocalizeMethods.add("hamann");
//		faultLocalizeMethods.add("simpleMatching");
//		faultLocalizeMethods.add("anderberg");
//		faultLocalizeMethods.add("goodman");
//		faultLocalizeMethods.add("sorensenDice");
//		faultLocalizeMethods.add("rogersTanimoto");
		
		
//		faultLocalizeMethods.add("cbi");
		faultLocalizeMethods.add("naish2");
//		faultLocalizeMethods.add("sokal");
//		faultLocalizeMethods.add("tarantula");
		faultLocalizeMethods.add("random");
		
		int numFaults = 1;
		// multiple faults
		List<String> createMutantMethods = new ArrayList<String>();
//		createMutantMethods.add("createPolicyTargetTrueMutants");// PTT
//		createMutantMethods.add("createPolicyTargetFalseMutants");// PTF
		// //comment out because cannot localize

		// createMutantMethods.add("createRemoveRuleMutants");//RER
		// createMutantMethods.add("createAddNewRuleMutants");//ANR
//		createMutantMethods.add("createRuleTargetTrueMutants");// RTT  //cannot repair???
//		createMutantMethods.add("createRuleTargetFalseMutants");// RTF
//		createMutantMethods.add("createRuleConditionTrueMutants");// RCT
//		createMutantMethods.add("createRuleConditionFalseMutants");// RCF
//		createMutantMethods.add("createFirstPermitRuleMutants");//FPR
//		createMutantMethods.add("createFirstDenyRuleMutants");//FDR
		// createMutantMethods.add("createRuleTypeReplacedMutants");//RTR

//		createMutantMethods.add("createRemoveParallelTargetElementMutants");// RPTE
		// createMutantMethods.add("createRemoveParallelConditionElementMutants");//RPCE

		//definitely can be repaired
		createMutantMethods.add("createCombiningAlgorithmMutants");//CRC
		createMutantMethods.add("createRuleEffectFlippingMutants");// CRE
		createMutantMethods.add("createAddNotFunctionMutants");// ANF
		createMutantMethods.add("createRemoveNotFunctionMutants");// RNF
		createMutantMethods.add("createFlipComparisonFunctionMutants");// FCF
		createMutantMethods.add("createChangeComparisonFunctionMutants");// CCF

		long createMutantStart = System.currentTimeMillis();
		List<PolicyMutant> mutantList = FaultLocalizationExperiment
				.createMultiFaultMutants(policyFile, numFaults,
						createMutantMethods);
		 String MutantsCSVFileName = FaultLocalizationExperiment.createMutantsCSVFile(mutantList);
		long createMutantEnd = System.currentTimeMillis();
		System.out.println("it took " + (createMutantEnd - createMutantStart)/1000 + " seconds to create mutants");
		
//		numFaults = 2;
//		String number = "MUTANT CRE1 MUTANT CCF2_1";//cannot rpair, takes 40 minutes for each run of start() 
//		String filename = "Experiments//kmarket-blue-policy//mutants//mutants//kmarket-blue-policy_CRE1_CCF2_1.xml";
//		int[] bugPositions = new int[] {1, 2};
//		int numFaults = 1;
//		String number = "MUTANT CRE5_CCF3_2";
//		String filename = "Experiments//conference3//mutants//mutants//conference3_CRE5_CCF3_2.xml";
//		int[] bugPositions = new int[] {5, 3};
//		int numFaults = 2;
//		String number = "MUTANT RTF4_RCF2";//cannot rpair, takes 40 minutes for each run of start() 
//		String filename = "Experiments//conference3//mutants//mutants//conference3_RTF4_RCF2.xml";
//		int[] bugPositions = new int[] {4, 2};
//		int numFaults = 2;
//		String number = "MUTANT CRE5_RTF3"; 
//		String filename = "Experiments//conference3//mutants//mutants//conference3_CRE5_RTF3.xml";
//		int[] bugPositions = new int[] {5, 3};
//		String number = "MUTANT CCF12_3"; 
//		String filename = "Experiments//itrust3-5//mutants//itrust3-5_CCF12_3.xml";
//		int[] bugPositions = new int[] {12};
//		PolicyMutant policyToRepair = new PolicyMutant(number, filename, bugPositions);
//		List<PolicyMutant> mutantList = new ArrayList<PolicyMutant>();
//		mutantList.add(policyToRepair);
		
		FileUtils.forceMkdir(new File(FilenameUtils.getPath(timingFileName)));
		FileUtils.forceMkdir(new File(FilenameUtils.getPath(repairResultFileName)));
		CSVWriter timingWriter = new CSVWriter(new FileWriter(timingFileName), ',');
		CSVWriter repairResultWriter = new CSVWriter(new FileWriter(repairResultFileName), ',');
		writeCSVTitleRow(timingWriter, faultLocalizeMethods);
		writeCSVTitleRow(repairResultWriter, faultLocalizeMethods);
		
		long startTime = System.currentTimeMillis();
		int outerCnt = 0;
		long seed = 1;
		Random rn = new Random();
		rn.setSeed(seed);
		for (PolicyMutant mutant: mutantList) {
			double probability = 0.1;
			if (rn.nextDouble() > probability) {
				outerCnt ++;
				continue;
			}
			List<String> durationList = new ArrayList<String>();
			List<String> repairedFileList = new ArrayList<String>();
			int innerCnt = 0;
			for (String faultLocalizeMethod: faultLocalizeMethods) {
				innerCnt++;
				long start = System.currentTimeMillis();
				String repaired = start(mutant, testSuiteSpreadSheetFile, faultLocalizeMethod, numFaults);
				long end = System.currentTimeMillis();
				System.out.println("mutant: " + mutant.getNumber() + ", faultlocalizer: " + faultLocalizeMethod);
				long currentTime = System.currentTimeMillis();
				long elapsedSeconds = (currentTime - startTime)/1000;
				long[] time = timeUtile(elapsedSeconds);
				System.out.println("elapsed time: " + time[0] + " hours, " + time[1] + " minutes, " + time[2] + " seconds");
				double ratio = ((double)innerCnt + outerCnt*faultLocalizeMethods.size())/faultLocalizeMethods.size()/mutantList.size();
				System.out.println("finished " + ratio*100 + "%");
				long estimatedRemainingSeconds =  (long) (elapsedSeconds*(1/ratio - 1));
				time = timeUtile(estimatedRemainingSeconds);
				System.out.println("estimated remaining time: " + time[0] + " hours, " + time[1] + " minutes, " + time[2] + " seconds");
				durationList.add(Long.toString(end - start));
				if (repaired != null) {
					repairedFileList.add(repaired);
				} else {
					repairedFileList.add("cannot repair");
				}
				String mutantsFolder = "Experiments" + File.separator
						+ policy[policyNumber] + File.separator + "mutants";
				for (int i = 0; i < numFaults; i++)
					mutantsFolder += File.separator + "mutants";
				FileUtils.deleteQuietly(new File(mutantsFolder));
			}
			writeCSVResultRow(timingWriter, mutant.getNumber(), durationList);
			writeCSVResultRow(repairResultWriter, mutant.getNumber(), repairedFileList);
			outerCnt++;
		}
		timingWriter.close();
		repairResultWriter.close();
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
			List<String> durationList)  {
		
		String[] entry = new String[durationList.size() + 1];
		entry[0] = mutantNumber;
		int index = 1;
		for (String duration: durationList) {
			entry[index] = duration;
			index ++;
		}
		writer.writeNext(entry);
		writer.flushQuietly();
	}

	ExperimentMultiFault(PolicyMutant policyToRepair,
			String faultLocalizeMethod, String testSuiteFile) throws Exception {
	}
	
	static String start(PolicyMutant policyToRepair, String testSuiteFile, String faultLocalizeMethod, int maxSearchLayer) throws Exception {
		Queue<MutantNode> queue = new PriorityQueue<MutantNode>();
		queue.add(new MutantNode(null, policyToRepair, testSuiteFile, faultLocalizeMethod, 0, 0));
		MutantNode node = null;
		boolean foundRepair = false;
		while (!queue.isEmpty()) {
			node = queue.poll();
//			System.out.println("queue size: " + queue.size());
//			System.out.println();
			List<Boolean> testResults = node.getTestResult();
			if (PolicyRepairer.booleanListAnd(testResults)) {
				foundRepair = true;
				break;//found a repair
			}
			if (!node.isPromising()) {
				continue;
			}
			if (node.getLayer() + 1 > maxSearchLayer)
				continue;
			List<Integer> suspicionRank = node.getSuspicionRank();
			PolicyMutator mutator = new PolicyMutator(node.getMutant());
			int rank = 1;
			for (int bugPosition : suspicionRank) {
				List<PolicyMutant> mutantList = generateMutants(mutator, bugPosition);
				for (PolicyMutant mutant: mutantList) {
					queue.add(new MutantNode(node, mutant, testSuiteFile, faultLocalizeMethod, rank, node.getLayer() + 1));
				}
				rank ++;
			}
		}
		String res = new String(node.getMutant().getNumber());
		for(MutantNode mutantNode: queue)
			mutantNode.clear();
		queue.clear();
		if(foundRepair)
			return res;
		return null;
	}
	
	private static List<PolicyMutant> generateMutants(PolicyMutator mutator, int bugPosition) throws Exception {
		if (bugPosition == -1) {
			List<PolicyMutant> mutantList = mutator.createCombiningAlgorithmMutants();
			return mutantList;
		}
		if (bugPosition == 0) {
			List<PolicyMutant> mutantList = generateMutantsPolicyTarget(mutator);
			return mutantList;
		}
		List<Rule> ruleList = mutator.getRuleList();
		Rule myrule = ruleList.get(bugPosition - 1);
		int ruleIndex = bugPosition;
		List<PolicyMutant> mutantList = new ArrayList<PolicyMutant>();
		//CRE
		mutantList.addAll(mutator.createRuleEffectFlippingMutants(myrule, ruleIndex));
		//ANF
		mutantList.addAll(mutator.createAddNotFunctionMutants(myrule, ruleIndex));
		//RNF
		mutantList.addAll(mutator.createRemoveNotFunctionMutants(myrule,  ruleIndex));
		//FCF
		mutantList.addAll(mutator.createFlipComparisonFunctionMutants(myrule, ruleIndex));
		//CCF
		mutantList.addAll(mutator.createChangeComparisonFunctionMutants(myrule, ruleIndex));

//		//ANR
//		mutantList = mutator.createAddNewRuleMutants(myrule, ruleIndex);
//		//RTT
//		mutantList.addAll(mutator.createRuleTargetTrueMutants(myrule, ruleIndex));
//		//RTF
//		mutantList.addAll(mutator.createRuleTargetFalseMutants(myrule, ruleIndex));
//		//RCT
//		mutantList.addAll(mutator.createRuleConditionTrueMutants(myrule, ruleIndex));
//		//RCF
//		mutantList.addAll(mutator.createRuleConditionFalseMutants(myrule, ruleIndex));
//		//RPTE
//		mutantList.addAll(mutator.createRemoveParallelTargetElementMutants(myrule, ruleIndex));

		return mutantList;
	}
	
	private static List<PolicyMutant> generateMutantsPolicyTarget(PolicyMutator mutator) {
		List<PolicyMutant> mutantList = new ArrayList<PolicyMutant>();
		//create mutant methods who's bugPosition == 0
		//CRC
		mutantList.addAll(mutator.createCombiningAlgorithmMutants());

//		// PTT
//		mutantList.addAll(mutator.createPolicyTargetTrueMutants());
//		// PTF
//		mutantList.addAll(mutator.createPolicyTargetTrueMutants());
//		//FPR
//		mutantList.addAll(mutator.createFirstPermitRuleMutants());
//		//FDR
//		mutantList.addAll(mutator.createFirstDenyRuleMutants());
		return mutantList;
	}
	
	static private long[] timeUtile(long totalSeconds) {
		long hours = totalSeconds / 60 / 60;
		long minutes = totalSeconds / 60 - 60 * hours;
		long seconds = totalSeconds % 60;
		return new long[] {hours, minutes, seconds};
	}
	
}
