package org.seal.coverage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.seal.faultlocalization.TestCellResult;

public class PolicySpreadSheetTestSuite {

	public static final String TEST_KEYWORD = "Test";

	private ArrayList<PolicySpreadSheetTestRecord> policyTestSuite;
	private String policyUnderTest;

	public PolicySpreadSheetTestSuite(
			ArrayList<PolicySpreadSheetTestRecord> testRecords,
			String policyUnderTest) {
		this.policyTestSuite = testRecords;
		this.policyUnderTest = policyUnderTest;
	}

	public PolicySpreadSheetTestSuite(String testSuiteSpreadSheetFile,
			String policyUnderTest) throws Exception {
		
		this(readTestSuite(testSuiteSpreadSheetFile), policyUnderTest);
		//System.out.println(testSuiteSpreadSheetFile);
	}
	
	// 2/5/14J
	public ArrayList<PolicySpreadSheetTestRecord> getTestRecord() {
		return policyTestSuite;
	}

	public static ArrayList<PolicySpreadSheetTestRecord> readTestSuite(
			String testSuiteSpreadSheetFile) throws Exception {
		ArrayList<PolicySpreadSheetTestRecord> testSuite = new ArrayList<PolicySpreadSheetTestRecord>();
		FileInputStream inputStream = new FileInputStream(
				testSuiteSpreadSheetFile);
		HSSFWorkbook workBook = new HSSFWorkbook(inputStream);
		// System.out.println(testSuiteSpreadSheetFile + "&&&&&");
		Sheet sheet = workBook.getSheetAt(0);
		for (Row row : sheet) {
			loadTestRow(testSuite,
					new File(testSuiteSpreadSheetFile).getParentFile(), row);
		}
		return testSuite;
	}

	private static void loadTestRow(
			ArrayList<PolicySpreadSheetTestRecord> testSuite,
			File testSuiteFolder, Row row) {
		if (row.getCell(0) == null || row.getCell(1) == null)
			return;
		String keyword = row.getCell(0).toString().trim();
		if (keyword.equals("")
				|| keyword.startsWith("//")
				|| !keyword.substring(0, TEST_KEYWORD.length())
						.equalsIgnoreCase(TEST_KEYWORD))
			return;
		String requestFileName = row.getCell(1) != null ? row.getCell(1)
				.toString() : "";
		String oracle = row.getCell(2) != null ? row.getCell(2).toString() : "";
		String requestString = "";
		if (!requestFileName.equals("")) {
			// Requires spreadsheet be in the same folder as the request files.
			requestString = PolicyRunner.readTextFile(testSuiteFolder
					.getAbsolutePath() + File.separator + requestFileName);
		}
		testSuite.add(new PolicySpreadSheetTestRecord(keyword, requestFileName,
				requestString, oracle));
	}

	public void writeToExcelFile(String fileName) {
		HSSFWorkbook workBook = new HSSFWorkbook();
		workBook.createSheet("test suite");
		Sheet sheet = workBook.getSheetAt(0);
		writeTitleRow(sheet, 0);
		int rowIndex = 1;
		for (PolicySpreadSheetTestRecord test : policyTestSuite) {
			writeTestRow(sheet, rowIndex++, test);
		}
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			workBook.write(out);
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static void writeTitleRow(Sheet sheet, int rowIndex) {
		Row titleRow = sheet.createRow(rowIndex);
		Cell[] titleCells = new Cell[3];
		for (int i = 0; i < titleCells.length; i++)
			titleCells[i] = titleRow.createCell(i);
		titleCells[0].setCellValue("");
		titleCells[1].setCellValue("Request file");
		titleCells[2].setCellValue("Expected response");
	}

	private static void writeTestRow(Sheet sheet, int rowIndex,
			PolicySpreadSheetTestRecord test) {
		Row testRow = sheet.createRow(rowIndex);
		Cell[] testCells = new Cell[3];
		for (int i = 0; i < testCells.length; i++)
			testCells[i] = testRow.createCell(i);
		testCells[0].setCellValue(test.getNumber());
		testCells[1].setCellValue(test.getRequestFile());
		testCells[2].setCellValue(test.getOracle());
	}

	public PolicySpreadSheetTestRecord findPolicyTest(String decision) {
		for (PolicySpreadSheetTestRecord test : policyTestSuite) {
			if (test.getOracle().equalsIgnoreCase(decision))
				return test;
		}
		return null;
	}

//	// Jimmy add @: 10/20/14 - inefficient.
//	/**
//	 * Run single test, return true if pass. 
//	 * @param testNumber
//	 * @return
//	 * @throws Exception
//	 */
//	public boolean runSingleTestOnMutant(int testNumber, String mutantPolicy) throws Exception {
//		PolicyRunner policyTester = new PolicyRunner(mutantPolicy);
//		PolicySpreadSheetTestRecord test = policyTestSuite.get(testNumber);
//		//System.out.println(test.getRequest());
//		//System.out.println(test.getRequestFile());
//		PolicyCoverageFactory.currentTestID = test.getNumber();
//		//System.out.print("\n" + test.getNumber());
//		if (test.getOracle().equals("")) {
//			System.err.println(": no test oracle");		
//			policyTester.runTestWithoutOracle(test.getNumber(),
//					test.getRequest());
//		} else {
//			//System.out.println(test.getNumber() + "\t" + test.getRequest() + "\t" + test.getOracle());
//			if (policyTester.runTest(test.getNumber(),
//					test.getRequest(), test.getOracle())) {
//				return true;
//			}
//			else
//				return false;
//		}	
//		return false;
//	}
	
	// 11/1/15 Change return type from boolean[] to TestCellResult[].
	public TestCellResult[] runAllTestsOnMutant() throws Exception {
		PolicyCoverageFactory.init();
		try {
			PolicyRunner policyTester = new PolicyRunner(policyUnderTest);
			
			// test results
			//boolean[] testResult = new boolean[getNumberOfTests()];
			TestCellResult[] rowResult = new TestCellResult[policyTestSuite.size()];
			for (int i = 0; i < rowResult.length; i++) {
				rowResult[i] = new TestCellResult();
			}
			
			for (int i = 0; i < policyTestSuite.size(); i++) {
				PolicyCoverageFactory.currentTestID = policyTestSuite.get(i).getNumber();
				if (policyTestSuite.get(i).getOracle().equals("")) {
					System.out.print(": no test oracle");					
					policyTester.runTestWithoutOracle(policyTestSuite.get(i).getNumber(),
							policyTestSuite.get(i).getRequest());
				} else {
					if (policyTester.runTest(policyTestSuite.get(i).getNumber(),
							policyTestSuite.get(i).getRequest(), policyTestSuite.get(i).getOracle())) {
						rowResult[i].setVerdict(true); // pass
						rowResult[i].setLiteralDetail(PolicyRunner.testExecutionResult);
					}
					else {
						rowResult[i].setVerdict(false); // fail
						rowResult[i].setLiteralDetail(PolicyRunner.testExecutionResult);
					}
				}
			}
			return rowResult;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;		
	}
	
	public boolean runAllTests() throws Exception {
		PolicyCoverageFactory.init();
		boolean allPass = true;
		try {
			PolicyRunner policyTester = new PolicyRunner(policyUnderTest);
			for (PolicySpreadSheetTestRecord test : policyTestSuite) {
				PolicyCoverageFactory.currentTestID = test.getNumber();
//				System.out.print("\n" + test.getNumber());
				if (test.getOracle().equals("")) {
					System.out.print(": no test oracle");
					
					policyTester.runTestWithoutOracle(test.getNumber(),
							test.getRequest());
				} else {
					if (policyTester.runTest(test.getNumber(),
							test.getRequest(), test.getOracle())) {
//						System.out.print(": pass");
					} else {
						allPass = false;
//						System.out.print(": fail");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (allPass)
			System.out.println("All tests pass: "+ policyUnderTest);
		return allPass;
	}

	public Vector<Vector<Object>> getTestData() {
		int index = 1;
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		for (PolicySpreadSheetTestRecord test : policyTestSuite) {
			Vector<Object> vector = test.getTestVector();
			vector.set(0, index + "");
			data.add(vector);
			index++;
		}
		return data;
	}

	public void generateJUnitFile(String projectPath, String packageName,
			String junitFileName) throws Exception {
		StringBuilder stringBuilder = new StringBuilder("package "
				+ packageName + ";" + "\nimport static org.junit.Assert.*;"
				+ "\nimport org.seal.coverage.PolicyRunner;"
				+ "\nimport org.junit.Test;\n");
		stringBuilder.append("\npublic class " + junitFileName + " {");
		stringBuilder.append("\n\tPolicyRunner policyRunner;");
		stringBuilder.append("\n");
		stringBuilder.append("\n\tpublic " + junitFileName + "(){");
		stringBuilder.append("\n\t\ttry{");
		stringBuilder.append("\n\t\t\tpolicyRunner = new PolicyRunner(\""
				+ policyUnderTest + "\");");
		stringBuilder.append("\n\t\t}");
		stringBuilder.append("\n\t\tcatch (Exception e){}");
		stringBuilder.append("\n\t}");
		int index = 1;
		for (PolicySpreadSheetTestRecord test : policyTestSuite) {
			if (!test.getOracle().equals("")) {
				stringBuilder.append("\n\n\t@Test");
				stringBuilder.append("\n\tpublic void test" + (index++)
						+ "()  throws Exception {");
				stringBuilder.append("\n\t\tassertTrue(");
				stringBuilder.append("policyRunner.runTestFromFile(");
				stringBuilder.append("\"" + test.getNumber() + "\", ");
				stringBuilder.append("\"" + test.getRequestFile() + "\", ");
				stringBuilder.append("\"" + test.getOracle() + "\"));");
				stringBuilder.append("\n\t}");
			}
		}
		stringBuilder.append("\n\n}");
		System.out.println(stringBuilder.toString());
		String[] folders = packageName.split("\\.");

		String codeFolder = folders[0];
		for (int i = 1; i < folders.length; i++) {
			codeFolder += File.separator + folders[i];
		}
		PolicyRunner.saveStringToTextFile(stringBuilder.toString(), new File(
				projectPath).getCanonicalFile()
				+ File.separator
				+ codeFolder
				+ File.separator + junitFileName + ".java");
	}

	public int getNumberOfTests() {
		// 10/20/14: Jimmy add this.
		return policyTestSuite.size();
	}

}
