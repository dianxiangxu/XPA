package org.seal.xacml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.seal.xacml.utils.FileIOUtil;
import org.seal.xacml.utils.FileUtil;
import org.seal.xacml.utils.PropertiesLoader;

public class TestSuiteDemo {

	public static final String TEST_KEYWORD = "Test";

	private List<TestRecord> testRecords;
	private String policyFilePath;

	public TestSuiteDemo(List<TestRecord> requestRecords, String policyFile) {
		this.testRecords = requestRecords;
		this.policyFilePath = policyFile;
	}

	public List<TestRecord> getTestRecords() {
		return testRecords;
	}

	public static List<TestRecord> readTestSuite(String testSuiteMetaFilePath) throws Exception {
		List<TestRecord> records = new ArrayList<TestRecord>();
		FileInputStream inputStream = new FileInputStream(testSuiteMetaFilePath);
		HSSFWorkbook workBook = new HSSFWorkbook(inputStream);
		Sheet sheet = workBook.getSheetAt(0);
		for (Row row : sheet) {
			loadTestRow(records,new File(testSuiteMetaFilePath).getParentFile(), row);
		}
		return records;
	}

	private static void loadTestRow(List<TestRecord> records, File testSuiteFolder, Row row) throws IOException {
		if (row.getCell(0) == null || row.getCell(1) == null){
			return;
		}
		String keyword = row.getCell(0).toString().trim();
		if (keyword.equals("") || keyword.startsWith("//") || !keyword.substring(0, TEST_KEYWORD.length()).equalsIgnoreCase(TEST_KEYWORD)){
			return;
		}
		String name = row.getCell(1) != null ? row.getCell(1).toString() : "";
		String oracle = row.getCell(2) != null ? row.getCell(2).toString() : "";
		String requestString = "";
		if (!name.equals("")) {
			String extension = PropertiesLoader.getProperties("config").getProperty("testRequestFileExtension");
			String filePath = testSuiteFolder.getAbsolutePath() + File.separator + name + "."+ extension;
			requestString = FileIOUtil.readFile(filePath);
		}
		records.add(new TestRecord(requestString, oracle, name));
	}

	/*public void writeMetaFile(String fileName) {
		HSSFWorkbook workBook = new HSSFWorkbook();
		workBook.createSheet("test suite");
		Sheet sheet = workBook.getSheetAt(0);
		writeTitleRow(sheet, 0);
		int rowIndex = 1;
		for (RequestRecord test : testRecords) {
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
			RequestRecord test) {
		Row testRow = sheet.createRow(rowIndex);
		Cell[] testCells = new Cell[3];
		for (int i = 0; i < testCells.length; i++)
			testCells[i] = testRow.createCell(i);
		testCells[0].setCellValue(test.getNumber());
		testCells[1].setCellValue(test.getRequestFile());
		testCells[2].setCellValue(test.getOracle());
	}

	public RequestRecord findPolicyTest(String decision) {
		for (RequestRecord test : testRecords) {
			if (test.getOracle().equalsIgnoreCase(decision))
				return test;
		}
		return null;
	}

	public Vector<Vector<Object>> getTestData() {
		int index = 1;
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		for (RequestRecord test : testRecords) {
			Vector<Object> vector = test.getTestVector();
			vector.set(0, index + "");
			data.add(vector);
			index++;
		}
		return data;
	}

	public int getNumberOfTests() {
		return testRecords.size();
	}
	*/
	public void save() throws IOException{
		FileUtil.setUpDefaultTestSuiteDirectory(policyFilePath,NameDirectory.RULE_COVERAGE);
		String testSuiteDirectory = FileUtil.getDefaultTestSuiteDirectoryPath(policyFilePath, NameDirectory.RULE_COVERAGE);	
		
		for(int i = 1; i <= testRecords.size();i++){
			String fileNamePrefix = PropertiesLoader.getProperties("config").getProperty("requestFileNamePrefix");
			String index = String.valueOf(i);
			String extension = PropertiesLoader.getProperties("config").getProperty("testRequestFileExtension");
			String filePath =  testSuiteDirectory + File.separator + fileNamePrefix + index + "." + extension;
			FileIOUtil.writeFile(filePath, testRecords.get(i).getRequest());
		}
	}

}
