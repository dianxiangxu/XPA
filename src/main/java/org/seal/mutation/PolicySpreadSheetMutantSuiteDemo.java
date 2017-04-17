// 10/20/14 Jimmy

package org.seal.mutation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.seal.coverage.PolicySpreadSheetTestSuite;
import org.seal.faultlocalization.TestCellResult;
import org.seal.policyUtils.PolicyLoader;
import org.seal.semanticCoverage.TestSuite;
import org.seal.semanticMutation.Mutant;
import org.seal.xpa.util.MutantUtil;
import org.wso2.balana.AbstractPolicy;

public class PolicySpreadSheetMutantSuiteDemo {

	public static final String MUTANT_KEYWORD = "MUTANT";

	private List<Mutant> policyMutantSuite;
	private String mutantsDirectory;
	
	private String policy;
	
	public PolicySpreadSheetMutantSuiteDemo(String directory,
			List<Mutant> mutantRecords, String policy) {
		this.mutantsDirectory = directory;
		this.policyMutantSuite = mutantRecords;
		this.policy = policy;
	}
	
	public PolicySpreadSheetMutantSuiteDemo(String mutantSuiteSpreadSheetFile, String policy) throws Exception {
		this(new File(mutantSuiteSpreadSheetFile).getParent(), readMutantSuite(mutantSuiteSpreadSheetFile), policy);
		//System.out.println(mutantSuiteSpreadSheetFile);
	}

	public static ArrayList<Mutant> readMutantSuite(
			String mutantSuiteSpreadSheetFile) throws IOException {
		ArrayList<Mutant> mutantSuite = new ArrayList<Mutant>();
		FileInputStream inputStream = new FileInputStream(
				mutantSuiteSpreadSheetFile);
		HSSFWorkbook workBook = new HSSFWorkbook(inputStream);
		// System.out.println(testSuiteSpreadSheetFile + "&&&&&");
		Sheet sheet = workBook.getSheetAt(0);
		for (Row row : sheet) {
			loadMutantRow(mutantSuite, new File(mutantSuiteSpreadSheetFile).getParentFile(), row);
		}
		return mutantSuite;
	}

	private static void loadMutantRow(ArrayList<Mutant> mutantSuite, File mutantFolder, Row row) {
		if (row.getCell(0) == null || row.getCell(1) == null)
			return;
		String keyword = row.getCell(0).toString().trim();
		if (keyword.equals("")
				|| keyword.startsWith("//"))
				//|| !keyword.substring(0, MUTANT_KEYWORD.length())
					//	.equalsIgnoreCase(MUTANT_KEYWORD))
			return;
		String mutantFileName = row.getCell(1) != null ? row.getCell(1).toString() : "";
		// use absolute path. 11/17/14
		String absoluteMutantFileName = mutantFolder.getAbsolutePath() + File.separator + mutantFileName;
		List<Integer> bugPositions = fromString(row.getCell(2).toString());
		AbstractPolicy policy = null;
		try{
			policy = PolicyLoader.loadPolicy(new File(absoluteMutantFileName));
		}
		
		catch(Exception e){
			e.printStackTrace();
		}
		mutantSuite.add(new Mutant( policy, bugPositions,mutantFileName));
	}
	
	public static List<Integer> fromString(String str) {
		String[] strs = str.replace("[", "").replace("]", "").split(",");
		List<Integer> results = new ArrayList<Integer>();
		for (int i = 0; i < strs.length; i++) {
			results.add( Integer.parseInt(strs[i].trim()));
		}
		return results;
	}
	public void writePolicyMutantsSpreadSheet(List<Mutant> mutantList, String mutantSpreadSheetFileName){
		HSSFWorkbook workBook = new HSSFWorkbook();
		workBook.createSheet("mutation list");
		Sheet sheet = workBook.getSheetAt(0);
		Row row = sheet.createRow(0);
		for (int mutantIndex =0; mutantIndex<mutantList.size(); mutantIndex++){
			row = sheet.createRow(mutantIndex+1);
			writeMutantRow(mutantIndex+1,row, mutantList.get(mutantIndex)); 
		}
		try {
			FileOutputStream out = new FileOutputStream(this.mutantsDirectory + File.separator + mutantSpreadSheetFileName);
			workBook.write(out);
			out.close();
		}
		catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

	private void writeMutantRow(int number,Row row, Mutant mutant){
		Cell idCell = row.createCell(0);
		idCell.setCellValue(number);
		Cell pathCell = row.createCell(1);
		pathCell.setCellValue(MutantUtil.getMutantFileName(mutant));
		Cell faultLocationCell = row.createCell(2);
		faultLocationCell.setCellValue(Arrays.toString(mutant.getFaultLocations().toArray()));
	}

	public Vector<Vector<Object>> getMutantData() {
		int index = 1;
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		for (Mutant mutant : policyMutantSuite) {
			Vector<Object> vector = MutantUtil.getVector(mutant, policy, ".xml");
			vector.set(0, index + "");
			data.add(vector);
			index++;
		}
		return data;
	}

	public void updateMutantTestResult(Vector<Vector<Object>> data, TestSuite testSuite){
		int mutantIndex=0;
		for (Mutant mutant : policyMutantSuite) {
			Vector<Object> vector = data.get(mutantIndex);
			//vector.set(vector.size()-1, TestSuite.runTests(mutant.getTestResult());
			
			// TO BE DONE
			vector.set(vector.size()-1, testSuite.runTests(mutant));
			mutantIndex++;
		}
	}
	
	public void runAndWriteDetectionInfoToExcelFile(String fileName, String testSuiteSpreadSheet) throws Exception {
		PolicySpreadSheetTestSuite tests = null;
		try {
			tests =	new PolicySpreadSheetTestSuite(testSuiteSpreadSheet, policy);
			runAndWriteDetectionInfoToExcelFile(fileName, tests);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void runAndWriteDetectionInfoToExcelFile(String fileName, PolicySpreadSheetTestSuite tests) throws Exception {
		//System.gc();
		HSSFWorkbook workBook = new HSSFWorkbook();
		workBook.createSheet("fault-detection-info");
		Sheet sheet = workBook.getSheetAt(0);
		int numTests = tests.getNumberOfTests();
		// An integer array to store counts of how many mutants that a test can detect,
		// plus the last cell is the count of detection for the whole test suite. 
		int[] detectionCount = new int[numTests+1];
		// initialize detection count
		for (int i = 0; i < numTests+1; i++) {
			detectionCount[i] = 0;
		}
		
		// 01/27/15 if #col > 255, print a brief version of the result, because of the limit of columns.
		if (numTests+3 > 255) {
			writeSimpleDetectionTitleRow(sheet, 0, numTests);
			int rowIndex = 1;
			for (Mutant mutant : policyMutantSuite) {
				writeSimpleDetectionInfoRow(sheet, rowIndex++, mutant, tests, detectionCount);
			}
			writeSimpleDetectionCountRow(sheet, rowIndex++, detectionCount);
			writeSimpleDetectionRateRow(sheet, rowIndex++, detectionCount, policyMutantSuite.size());
		} else {
			// Else (#col <= 255) write the full detail of test results.
			// write title row. specify number of columns by number of tests.
			if (numTests!=0)
				writeDetectionTitleRow(sheet, 0, numTests);
			
			int rowIndex = 1;
		
			for (Mutant mutant : policyMutantSuite) {
			// TO BE DONE
				//writeDetectionInfoRow(sheet, rowIndex++, mutant, tests, detectionCount);
			}
			
			// 10/26/14: Add statistics: a row of fault detection count/rate for each single test.
			// and the detection count/rate for the whole test suite.
			writeDetectionCountRow(sheet, rowIndex++, detectionCount);
			writeDetectionRateRow(sheet, rowIndex++, detectionCount, policyMutantSuite.size());
		}
			
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			workBook.write(out);
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	// 01/27/15 simple result for large sheet. - title row
	private void writeSimpleDetectionTitleRow(Sheet sheet, int rowIndex, int numTests) {
		Row titleRow = sheet.createRow(rowIndex);
		Cell[] titleCells = new Cell[numTests+3]; // name, bugPosition, detected?.
		for (int i = 0; i < 3; i++) {
			titleCells[i] = titleRow.createCell(i);
			if (i==0)
				titleCells[i].setCellValue(""); // mutant name
			if (i==1)
				titleCells[i].setCellValue("Bug Position");
			if (i==2) {
				titleCells[i].setCellValue("Detected?");
			}			
		}
	}
	// 01/27/15 simple result for large sheet. - detect info
	private void writeSimpleDetectionInfoRow(Sheet sheet, int rowIndex, 
			Mutant mutant, PolicySpreadSheetTestSuite tests, int[] detectionCount) throws Exception {
		Row mutantRow = sheet.createRow(rowIndex);
		Cell[] mutantCells = new Cell[3];
		boolean killed = false; // fault detected?
		
		for (int i = 0; i < 3; i++) {
			mutantCells[i] = mutantRow.createCell(i);
		}
		//mutantCells[0].setCellValue(mutant.getNumber());
		mutantCells[1].setCellValue(Arrays.toString(mutant.getFaultLocations().toArray()));
		/*PolicySpreadSheetTestSuite mutantTestSuite = 
				new PolicySpreadSheetTestSuite(tests.getTestRecord(), mutant.getMutantFilePath(mutantsDirectory));
		//boolean[] testResult = mutantTestSuite.runAllTestsOnMutant();
		TestCellResult[] rowResult;	
		rowResult = mutantTestSuite.runAllTestsOnMutant();
		for (int j = 0; j < rowResult.length; j++) {
			if(rowResult[j].getVerdict()) {
				// idle
			} else {
				// just count
				detectionCount[j]++;
				killed = true;
				break; // try this...
			}
		}
		mutantCells[2].setCellValue(killed ? "Yes" : "No");
		//mutant.setTestResult(killed ? "Yes" : "No");
			*/	
		
		if (killed)
			detectionCount[tests.getNumberOfTests()]++;
	}
	
	// 01/27/15 simple result for large sheet. - detect count
	private void writeSimpleDetectionCountRow(Sheet sheet, int rowIndex, int[] detectionCount) {
		Row mutantRow = sheet.createRow(rowIndex);
		int arrayLength = detectionCount.length;
		Cell[] mutantCells = new Cell[3];
		for (int i = 0; i < mutantCells.length; i++) {
			mutantCells[i] = mutantRow.createCell(i);
			if (i==0)
				mutantCells[i].setCellValue("Count");
			// leave blank if i=1
			if (i==2) {
				mutantCells[i].setCellValue(detectionCount[arrayLength-1]);
			}
		}
	}
	// 01/27/15 simple result for large sheet. - detect ratio
	private void writeSimpleDetectionRateRow(Sheet sheet, int rowIndex, int[] detectionCount, int numOfMutants) {
		Row mutantRow = sheet.createRow(rowIndex);
		int arrayLength = detectionCount.length;
		Cell[] mutantCells = new Cell[3];
		DecimalFormat fourDForm = new DecimalFormat("#.####"); // Double formatter.
		for (int i = 0; i < mutantCells.length; i++) {
			mutantCells[i] = mutantRow.createCell(i);
			if (i==0)
				mutantCells[i].setCellValue("Detection Rate");
			// leave blank if i=1
			if (i==2) {
				mutantCells[i].setCellValue(fourDForm.format((double)detectionCount[arrayLength-1]/numOfMutants));
				// For convenience
				System.out.println("# of mutants detected = " + detectionCount[arrayLength-1]);
				System.out.println("Overall detection ratio = " + fourDForm.format((double)detectionCount[arrayLength-1]/numOfMutants));
			}
		}
	}

	/**
	 * Normal version title row.
	 * @param sheet
	 * @param rowIndex
	 * @param numTests
	 */
	private void writeDetectionTitleRow(Sheet sheet, int rowIndex, int numTests) {
		Row titleRow = sheet.createRow(rowIndex);
		Cell[] titleCells = new Cell[numTests+3]; // name, bugPosition, test1~n, detected?
		// Optimization may apply.
		for (int i = 0; i < numTests+3; i++) {
			titleCells[i] = titleRow.createCell(i);
			if (i==0)
				titleCells[i].setCellValue(""); // header for mutant name
			if (i==1)
				titleCells[i].setCellValue("Bug Position");
			if (i>=2 && i<=numTests+1) {
				titleCells[i].setCellValue("Test" + (i-1));
			}
			if (i==numTests+2) {
				titleCells[i].setCellValue("Detected?");
			}			
		}
	}
	
	/*private void writeDetectionInfoRow(Sheet sheet, int rowIndex,
			Mutant mutant, PolicySpreadSheetTestSuite tests, int[] detectionCount) throws Exception {

		// initialize
		Row mutantRow = sheet.createRow(rowIndex);
		Cell[] mutantCells = new Cell[tests.getNumberOfTests()+3];
		boolean killed = false; // fault detected?
		// create cells.
		for (int i = 0; i < tests.getNumberOfTests()+3; i++) {
			mutantCells[i] = mutantRow.createCell(i);
		}
		// set values.
		mutantCells[0].setCellValue(mutant.getNumber());
		mutantCells[1].setCellValue(Arrays.toString(mutant.getFaultLocation()));
		PolicySpreadSheetTestSuite mutantTestSuite = 
				new PolicySpreadSheetTestSuite(tests.getTestRecord(), mutant.getMutantFilePath(mutantsDirectory));
		
		// 11/1/15 fix test result return
		//boolean[] testResult = mutantTestSuite.runAllTestsOnMutant();
		TestCellResult[] rowResult;	
		rowResult = mutantTestSuite.runAllTestsOnMutant();
		
		for (int j = 0; j < rowResult.length; j++) {
			if(rowResult[j].getVerdict()) {
				mutantCells[j+2].setCellValue(rowResult[j].getLiteralDetail());
			} else {
				// just count
				mutantCells[j+2].setCellValue(rowResult[j].getLiteralDetail());
				detectionCount[j]++;
				killed = true;
			}
		}
		mutantCells[tests.getNumberOfTests()+2].setCellValue(killed ? "Yes" : "No");
		mutant.setTestResult(killed ? "Yes" : "No");
		if (killed)
			detectionCount[tests.getNumberOfTests()]++;
	}*/
	
	private void writeDetectionCountRow(Sheet sheet, int rowIndex, int[] detectionCount) {
		Row mutantRow = sheet.createRow(rowIndex);
		int arrayLength = detectionCount.length;
		Cell[] mutantCells = new Cell[arrayLength+2];
		for (int i = 0; i < mutantCells.length; i++) {
			mutantCells[i] = mutantRow.createCell(i);
			if (i==0)
				mutantCells[i].setCellValue("Count");
			// leave blank if i=1
			if (i>=2 && i <= arrayLength+1) {
				mutantCells[i].setCellValue(detectionCount[i-2]);
			}
		}
	}
	
	private void writeDetectionRateRow(Sheet sheet, int rowIndex, int[] detectionCount, int numOfMutants) {
		Row mutantRow = sheet.createRow(rowIndex);
		int arrayLength = detectionCount.length;
		Cell[] mutantCells = new Cell[arrayLength+2];
		DecimalFormat fourDForm = new DecimalFormat("#.####"); // Double formatter.
		for (int i = 0; i < mutantCells.length; i++) {
			mutantCells[i] = mutantRow.createCell(i);
			if (i==0)
				mutantCells[i].setCellValue("Detection Rate");
			// leave blank if i=1
			if (i>=2 && i <= arrayLength+1) {
				mutantCells[i].setCellValue(fourDForm.format((double)detectionCount[i-2]/numOfMutants));
			}
		}
		// For convenience
		System.out.println("# of mutants detected = " + detectionCount[arrayLength-1]);
		System.out.println("Overall detection ratio = " + fourDForm.format((double)detectionCount[arrayLength-1]/numOfMutants));
	}

	// Run all tests on all mutations
	public static void main(String[] args) throws Exception{
		
		//String policy = "kmarket-blue-policy";
		String policy = "pluto3";
		//String testSuite = "RuleLevel";
		String testSuite = "ByDenyPermit";
		
		PolicySpreadSheetMutantSuiteDemo mutantSuite = 
				new PolicySpreadSheetMutantSuiteDemo("Experiments//" + policy + "//mutation//" + policy + "_mutants.xls",
						"Experiments//" + policy + "//" + policy + ".xml");
		mutantSuite.runAndWriteDetectionInfoToExcelFile("Experiments//" + policy + "//test_suites//"
				+ policy + "_detection_info_" + testSuite + ".xls", 
				"Experiments//" + policy + "//test_suites//" + (policy+"_"+testSuite + "//")+ policy + "_" + testSuite + ".xls");
		System.out.println("Detection-info-spreadsheet created.");
//		for (PolicyMutant mutant : mutantSuite.policyMutantSuite) {
//			//System.out.println(mutant.getMutantFile());
//			PolicySpreadSheetTestSuite testSuite = 
//					new PolicySpreadSheetTestSuite("Experiments//kmarket-blue-policy//"
//							+ "test_suites//test_suite0//kmarket-tests_old_RuleLevel.xls",
//					mutant.getMutantFile());
//		testSuite.runAllTests();
//		System.out.println("\n");	
//		}
		
	}
	
	public List<Mutant> getMutantList()
	{
		return this.policyMutantSuite;
	}
	
}
