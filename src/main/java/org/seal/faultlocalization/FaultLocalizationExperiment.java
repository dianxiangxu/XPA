package org.seal.faultlocalization;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.seal.coverage.PolicySpreadSheetTestRecord;
import org.seal.coverage.PolicySpreadSheetTestSuite;
import org.seal.mutation.PolicyMutant;
import org.seal.mutation.PolicyMutator;
import org.seal.mutation.PolicySpreadSheetMutantSuite;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;


public class FaultLocalizationExperiment {
	private List<PolicyMutant> policyMutants; 
	
	public FaultLocalizationExperiment(String testSuiteSpreadSheetFile, String MutantsCSVFileName
			, String experimentResultFileNam) throws Exception {

		policyMutants = readMutantsCSVFile(MutantsCSVFileName);
		this.runExperiment(testSuiteSpreadSheetFile, experimentResultFileNam);
	}
	
	public static List<PolicyMutant> createMultiFaultMutants(String policyFile, int numFaults,
			List<String> createMutantMethods) throws Exception {
		List<List<PolicyMutant>> mutantLists = new ArrayList<List<PolicyMutant>>();
		PolicyMutant baseMutant = new PolicyMutant("", policyFile, new int[] {});
		mutantLists.add(new ArrayList<PolicyMutant>());
		mutantLists.get(0).add(baseMutant);
		for (int i = 1; i <= numFaults; i++) {
			mutantLists.add(new ArrayList<PolicyMutant>());
			for (PolicyMutant mutant : mutantLists.get(i - 1)) {
				PolicyMutator mutator = new PolicyMutator(mutant);
				mutator.createSelectedMutants(createMutantMethods);
				mutantLists.get(i).addAll(mutator.getMutantList());
			}
		}
		return mutantLists.get(numFaults);
	}
	
	public static String createMutantsCSVFile(List<PolicyMutant> mutantList) throws IOException {
		if (mutantList == null || mutantList.size() == 0)
			return "";
		String directoryName = new File(mutantList.get(0).getMutantFilePath()).getParent();
		String mutantsCSVFileName = directoryName + File.separator + "mutants.csv";
		CSVWriter writer = new CSVWriter(new FileWriter(mutantsCSVFileName), ',');
		for (PolicyMutant mutant: mutantList) {
			String[] entry = new String[3];
			entry[0] = mutant.getNumber();
			entry[1] = new File(mutant.getMutantFilePath()).getName();
			entry[2] = Arrays.toString(mutant.getFaultLocation());
			writer.writeNext(entry);
		}
		writer.close();
		return mutantsCSVFileName;
	}
	
	private List<PolicyMutant> readMutantsCSVFile(String mutantsCSVFileName) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(mutantsCSVFileName));
		List<String[]> entries = reader.readAll();
		reader.close();
		List<PolicyMutant> mutantList = new ArrayList<PolicyMutant>();
		for (String[] entry: entries) {
			String mutantNumber = entry[0];
			String fileName = new File(mutantsCSVFileName).getParent() + File.separator + entry[1];
			int[] bugPositions = PolicySpreadSheetMutantSuite.fromString(entry[2]);
			mutantList.add(new PolicyMutant(mutantNumber, fileName, bugPositions));
		}
		return mutantList;
	}
	
	private void runExperiment (String testSuiteSpreadSheetFile,  String experimentResultFileName) throws Exception {

		ArrayList<PolicySpreadSheetTestRecord> testSuite = PolicySpreadSheetTestSuite.readTestSuite(testSuiteSpreadSheetFile);
		CSVWriter writer = new CSVWriter(new FileWriter(experimentResultFileName), ',');
		ArrayList<SpectrumBasedDiagnosisResults> spectrumBasedDiagnosisResults = null;
		int validResults = 0;
		int index = 0;
		do {
			spectrumBasedDiagnosisResults = policyMutants.get(index).run(testSuite);
			index++;
		} while (spectrumBasedDiagnosisResults == null);
		int numFaultlocalizers = spectrumBasedDiagnosisResults.size();
		double[] totals = new double[numFaultlocalizers];
		//writing title row
		writeCSVTitleRow(writer, spectrumBasedDiagnosisResults);
		//writing each result row
		for (int i = 0; i < policyMutants.size(); i++) {
			PolicyMutant mutant = policyMutants.get(i);
			System.out.println(mutant.getNumber());
			if (mutant.getNumber().trim().equals("MUTANT RTT1_1"))
				System.out.println();
			spectrumBasedDiagnosisResults = mutant.run(testSuite);
			if (spectrumBasedDiagnosisResults!=null) {
				validResults++;
				writeCSVResultRow(writer, spectrumBasedDiagnosisResults, mutant, totals);
			}
		}
		//writing average row
		writeCSVAverageRow(writer, totals, validResults);
		writer.close();
	}

	private static void writeCSVTitleRow(CSVWriter writer, List<SpectrumBasedDiagnosisResults> spectrumBasedDiagnosisResults) {
		int numFaultlocalizers = spectrumBasedDiagnosisResults.size();
		String[] titles = new String[numFaultlocalizers + 1];
		titles[0] = "mutant number";
		for (int i = 0; i < spectrumBasedDiagnosisResults.size(); i++)
			titles[i + 1] = spectrumBasedDiagnosisResults.get(i).getMethodName();
		writer.writeNext(titles);
	}
	
	private static void writeCSVResultRow(CSVWriter writer, List<SpectrumBasedDiagnosisResults> spectrumBasedDiagnosisResults, PolicyMutant mutant, double[] totals) {
		int numFaultlocalizers = spectrumBasedDiagnosisResults.size();
		String[] entry = new String[numFaultlocalizers + 1];
		entry[0] = mutant.getNumber();
		for (int i = 0; i < spectrumBasedDiagnosisResults.size(); i++) {
			double score = spectrumBasedDiagnosisResults.get(i).getScore();
			entry[i + 1] = String.format("%f", score);
			totals[i] += score;
		}
		writer.writeNext(entry);
	}
	
	private static void writeCSVAverageRow(CSVWriter writer, double[] totals, int validResults) {
		String[] entries = new String[totals.length + 1];
		entries[0] = "average";
		for(int i = 0; i < totals.length; i++) {
			double average = totals[i]/validResults;
			entries[i + 1] = String.format("%.3f", average);
		}
		writer.writeNext(entries);
	}
	

	
	public static void main(String[] args) throws Exception{
		
		// Exclusive - kmarket-blue-policy 
//		new FaultLocalizationExperiment("Experiments//kmarket-blue-policy//test_suites//kmarket-blue-policy_Exclusive//kmarket-blue-policy_Exclusive.xls",
//										"Experiments//kmarket-blue-policy//mutants//kmarket-blue-policy_mutants.xls",
//										"Experiments//kmarket-blue-policy//fault-localization//Exclusive_experiment.xls");
		// Basic - conference3
//		new FaultLocalizationExperiment("Experiments//conference3//test_suites//conference3_Basic//conference3_Basic.xls",
//				"Experiments//conference3//mutants//conference3_mutants.xls",
//				"Experiments//conference3//fault-localization//conference3_Basic_Fault-localiazation.xls");
		
		// General
		String[] policy = { "conference3", "fedora-rule3", "itrust3",
				"kmarket-blue-policy", "obligation3", "pluto3", "itrust3-5",
				"itrust3-10", "itrust3-20" };

		String[] testsuite = {"Basic", "Exclusive", "Pair", "PDpair", "DecisionCoverage", "RuleLevel", "MCDCCoverage"};
		int policyNumber = 0;
		int testsuiteNumber = 6;
		
		String testSuiteSpreadSheetFile = "Experiments//" + policy[policyNumber]+ "//test_suites//" + policy[policyNumber] + "_" + testsuite[testsuiteNumber] + "//" + policy[policyNumber] + "_" + testsuite[testsuiteNumber] + ".xls";
		String policyMutantSpreadsheetFil = "Experiments//" + policy[policyNumber] + "//mutants//" + policy[policyNumber] + "_mutants.xls";
		String experimentResultFileName = "Experiments//" + policy[policyNumber] + "//fault-localization//" + policy[policyNumber] + "_" + testsuite[testsuiteNumber] + "_fault-localiazation.csv";
//		new FaultLocalizationExperiment(testSuiteSpreadSheetFile, policyMutantSpreadsheetFil, experimentResultFileNam);
		
		//multiple faults
		List<String> createMutantMethods = new ArrayList<String>();
		createMutantMethods.add("createPolicyTargetTrueMutants");//PTT
		createMutantMethods.add("createPolicyTargetFalseMutants");//PTF
//		createMutantMethods.add("createCombiningAlgorithmMutants");//CRC //comment out because cannot localize
		createMutantMethods.add("createRuleEffectFlippingMutants");//CRE
//		createMutantMethods.add("createRemoveRuleMutants");//RER
//		createMutantMethods.add("createAddNewRuleMutants");//ANR
		createMutantMethods.add("createRuleTargetTrueMutants");//RTT
		createMutantMethods.add("createRuleTargetFalseMutants");//RTF
		createMutantMethods.add("createRuleConditionTrueMutants");//RCT
		createMutantMethods.add("createRuleConditionFalseMutants");//RCF
//		createMutantMethods.add("createFirstPermitRuleMutants");//FPR
//		createMutantMethods.add("createFirstDenyRuleMutants");//FDR
//		createMutantMethods.add("createRuleTypeReplacedMutants");//RTR
		createMutantMethods.add("createAddNotFunctionMutants");//ANF
		createMutantMethods.add("createRemoveNotFunctionMutants");//RNF
//		createMutantMethods.add("createRemoveParallelTargetElementMutants");//RPTE
//		createMutantMethods.add("createRemoveParallelConditionElementMutants");//RPCE

		String policyFile = "Experiments" + File.separator + policy[policyNumber] + File.separator + policy[policyNumber] + ".xml";
		int numFaults = 1;
		List<PolicyMutant> mutantList = createMultiFaultMutants(policyFile, numFaults, createMutantMethods);	
		String MutantsCSVFileName = createMutantsCSVFile(mutantList);

		new FaultLocalizationExperiment( testSuiteSpreadSheetFile,  MutantsCSVFileName
				, experimentResultFileName);
	}
	

}
