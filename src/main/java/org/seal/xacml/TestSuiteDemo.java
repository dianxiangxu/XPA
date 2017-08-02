package org.seal.xacml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.seal.xacml.utils.FileIOUtil;
import org.seal.xacml.utils.XACMLElementUtil;
import org.seal.xacml.utils.PropertiesLoader;
import org.seal.xacml.xpa.utils.TestUtil;

public class TestSuiteDemo {

	public static final String TEST_KEYWORD = "Test";

	private List<TestRecord> testRecords;
	private String policyFilePath;
	private String type;

	public TestSuiteDemo(List<TestRecord> requestRecords, String policyFilePath, String type) {
		this.testRecords = requestRecords;
		this.policyFilePath = policyFilePath;
		this.type = type;
	}
	
	public TestSuiteDemo(String policyFilePath,List<String> requests, String type) {
		this.testRecords = new ArrayList<TestRecord>();
		for(int i = 0; i < requests.size();i++){
			testRecords.add(new TestRecord(requests.get(i),"",TestUtil.getName(i)));
		}
		this.policyFilePath = policyFilePath;
		this.type = type;
	}
	
	public TestSuiteDemo(String policyFilePath, String type, List<TaggedRequest> requests) {
		this.testRecords = new ArrayList<TestRecord>();
		for(int i = 0; i < requests.size();i++){
			testRecords.add(new TestRecord(requests.get(i).getBody(),"",TestUtil.getName(i) + "-" + requests.get(i).getTitle()));
		}
		this.policyFilePath = policyFilePath;
		this.type = type;
	}

	public TestSuiteDemo(String testSuiteMetaFilePath, String policyFilePath) throws Exception {
		this.policyFilePath = policyFilePath;
		this.testRecords = readTestSuite(testSuiteMetaFilePath);
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
	
	public List<TestRecord> getTestRecords() {
		return testRecords;
	}
	

	public int getNumberOfTests() {
		return testRecords.size();
	}


	private static void loadTestRow(List<TestRecord> records, File testSuiteFolder, Row row) throws IOException {
		if (row.getCell(0) == null || row.getCell(1) == null){
			return;
		}
		String keyword = row.getCell(0).toString().trim();
		if (Pattern.matches("[a-zA-Z]+",keyword)){
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

	public void writeMetaFile(String fileName) {
		HSSFWorkbook workBook = new HSSFWorkbook();
		workBook.createSheet("test suite");
		Sheet sheet = workBook.getSheetAt(0);
		writeTitleRow(sheet, 0);
		int rowIndex = 1;
		for (TestRecord test : testRecords) {
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
		titleCells[0].setCellValue("SNo");
		titleCells[1].setCellValue("Request Name");
		titleCells[2].setCellValue("Expected response");
	}

	private static void writeTestRow(Sheet sheet, int rowIndex,
			TestRecord test) {
		Row testRow = sheet.createRow(rowIndex);
		Cell[] testCells = new Cell[3];
		for (int i = 0; i < testCells.length; i++)
			testCells[i] = testRow.createCell(i);
		testCells[0].setCellValue(rowIndex);
		testCells[1].setCellValue(TestUtil.getName(rowIndex-1));
		testCells[2].setCellValue(test.getOracle());
	}

	public TestRecord findPolicyTest(String decision) {
		for (TestRecord test : testRecords) {
			if (test.getOracle().equalsIgnoreCase(decision))
				return test;
		}
		return null;
	}
	
	public void save() throws IOException{
		TestUtil.setUpDefaultTestSuiteDirectory(policyFilePath,type);
		String testSuiteDirectory = TestUtil.getDefaultTestSuiteDirectoryPath(policyFilePath, type);	
		String metaFilePath = testSuiteDirectory + File.separator + TestUtil.getTestSuiteMetaFileName(XACMLElementUtil.getPolicyName(policyFilePath), type);
		
		for(int i = 0; i < testRecords.size();i++){
			String fileNamePrefix = PropertiesLoader.getProperties("config").getProperty("requestFileNamePrefix");
			String index = String.valueOf(i+1);
			String extension = PropertiesLoader.getProperties("config").getProperty("testRequestFileExtension");
			String filePath =  testSuiteDirectory + File.separator + testRecords.get(i).getName() + "." + extension;
			FileIOUtil.writeFile(filePath, testRecords.get(i).getRequest());
		}
		writeMetaFile(metaFilePath);
	}
}
