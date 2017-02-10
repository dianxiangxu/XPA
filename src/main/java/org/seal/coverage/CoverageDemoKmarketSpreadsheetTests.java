package org.seal.coverage;

import org.seal.faultlocalization.SpectrumBasedDiagnosisResults;
import org.seal.faultlocalization.SpectrumBasedFaultLocalizer;

public class CoverageDemoKmarketSpreadsheetTests {
	public static void main(String[] args) throws Exception{
		//PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite("tests//kmarket-tests.xls","tests//kmarket-blue-policy.xml");
		String policyFileToRepair = "Experiments//conference3//mutants//conference3_ANR4.xml";
		String testSuiteSpreadSheetFile = "Experiments//conference3//test_suites//conference3_MCDCCoverage_NoError//conference3_MCDCCoverage_NoError.xls";
		PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(testSuiteSpreadSheetFile,
				policyFileToRepair);
		testSuite.runAllTests();
		System.out.println("\n");
//		testSuite.generateJUnitFile("src", 
//				"org.seal.coverage", "KmarketGeneratedTests");
		PolicyCoverageFactory.writeCoverageToSpreadSheet("Experiments//conference3//coverage.xls");
		for (SpectrumBasedDiagnosisResults results: SpectrumBasedFaultLocalizer.applyAllFaultLocalizers()){
			results.printCoefficients();
		}
	}
}
