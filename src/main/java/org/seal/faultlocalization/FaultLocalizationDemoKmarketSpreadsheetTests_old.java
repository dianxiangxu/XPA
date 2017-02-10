package org.seal.faultlocalization;

import java.io.File;

import org.seal.coverage.PolicyCoverageFactory;
import org.seal.coverage.PolicySpreadSheetTestSuite;

public class FaultLocalizationDemoKmarketSpreadsheetTests_old {

	// Ranking report/Coverage Info for a single mutant. 
	public static void main(String[] args) throws Exception{
		
		// Selections
		String[] policy = {"conference3", "fedora-rule3", "itrust3", "kmarket-blue-policy", "obligation3", 
							"pluto3"};
		String[] testsuite = {"Basic", "Exclusive", "Pair", "PDpair", "DecisionCoverage", 
							"RuleLevel", "MCDCCoverage"};
		// Variables
		int policyNumber = 3;
		int testsuiteNumber = 6;
		String mutantID = "CRE2";
		
		String testSuiteSpreadSheetFile = "Experiments" + File.separator 
										+ policy[policyNumber] + File.separator 
										+ "test_suites" + File.separator  
										+ policy[policyNumber] + "_" + testsuite[testsuiteNumber] + File.separator 
										+ policy[policyNumber] + "_" + testsuite[testsuiteNumber] + ".xls";
		String policyUnderTest = "Experiments" + File.separator 
								+ policy[policyNumber] + File.separator 
								+ "mutants" + File.separator 
								+ policy[policyNumber] + "_" + mutantID + ".xml";
		String coverageSpreadsheetName = "Experiments" + File.separator 
										+ policy[policyNumber] + File.separator
										+ "fault-localization" + File.separator 
										+ "coverage_" + testsuite[testsuiteNumber] + "_" + mutantID + ".xls";
		
		PolicySpreadSheetTestSuite testSuite = 
				new PolicySpreadSheetTestSuite(testSuiteSpreadSheetFile, policyUnderTest);
		testSuite.runAllTests();
		System.out.println("\n");
//		testSuite.generateJUnitFile("//Users//dianxiangxu//Documents//JavaProjects//WSO2-XACML//XPA//src", 
		testSuite.generateJUnitFile("src", 
				"org.seal.faultlocalization", "KmarketGeneratedTests");
		
		// Write to coverage spreadsheet
		PolicyCoverageFactory.writeCoverageToSpreadSheet(coverageSpreadsheetName);
		
		for (SpectrumBasedDiagnosisResults results: SpectrumBasedFaultLocalizer.applyAllFaultLocalizers()){
			results.printCoefficients();
		}
	}

}