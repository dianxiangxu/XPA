package org.seal.xacml.xpa.utils;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.seal.xacml.NameDirectory;
import org.seal.xacml.TestRecord;
import org.seal.xacml.utils.ExceptionUtil;

import org.seal.xacml.utils.PolicyElementUtil;
import org.seal.xacml.utils.PropertiesLoader;

public class TestUtil {
	private static Properties config;
	static {
		config = PropertiesLoader.getProperties("config");
	}
	public static Vector<Vector<Object>> getTestRecordsVector(List<TestRecord> records){
		Vector<Vector<Object>> recordsVector = new Vector<Vector<Object>>(); 
		for(int i = 0; i< records.size();i++){
			recordsVector.add(getTestRecordVector(records.get(i),i+1));
		}
		return recordsVector;
	}
	public static Vector<Object> getTestRecordVector(TestRecord record,int index){
		Vector<Object> vector = new Vector<Object>();
		vector.add(index);		// sequence number
		vector.add(record.getName());	// test name
		vector.add(getTestFileName(record));
		vector.add(record.getOracle());
		vector.add("");	// actual response
		vector.add("");	// verdict
		vector.add(record.getRequest());
		return vector;
	}
	
	public static String getTestFileName(TestRecord record){
		return record.getName() + "." + config.getProperty("testRequestFileExtension");
	}
	
	public static String getTestFileName(int index){
		return getName(index) + "." + config.getProperty("testRequestFileExtension");
	}
	
	public static String getTestSuiteMetaFileName(String policyName, String coverageName){
		String delimeter = config.getProperty("fileNameDelimeter");
		String testSuiteMetaFileName = policyName + delimeter + coverageName + delimeter + config.getProperty("testSuiteMetaFileName");
		String testSuiteMetaFileExtension = config.getProperty("testSuiteMetaFileExtension");
		return testSuiteMetaFileName + "." + testSuiteMetaFileExtension;
	}
	
	public static String getTestSuiteMetaFilePath(String policyFilePath, String coverageName){
		String delimeter = config.getProperty("fileNameDelimeter");
		String testSuiteMetaFileName = PolicyElementUtil.getPolicyName(policyFilePath) + delimeter + coverageName + delimeter + config.getProperty("testSuiteMetaFileName");
		String testSuiteMetaFileExtension = config.getProperty("testSuiteMetaFileExtension");
		String name = testSuiteMetaFileName + "." + testSuiteMetaFileExtension;
		return TestUtil.getDefaultTestSuiteDirectoryPath(policyFilePath, NameDirectory.RULE_COVERAGE) + File.separator + name;
	}
	
	public static String getName(int index){
		return config.getProperty("requestFileNamePrefix") + (index+1);
	}
	
	public static TestRecord getTestRecord(Vector<Object> v){
		v.set(3, v.get(4));
		String rec = String.valueOf(v.get(2));
		TestRecord record = new TestRecord(v.get(6).toString(), v.get(3).toString(),v.get(1).toString());
		return record;
	}
	
	public static void setUpDefaultTestSuiteDirectory(String policyFilePath, String coverageName){
		File testSuite = new File(getDefaultTestSuiteDirectoryPath(policyFilePath,coverageName));
		if (!testSuite.exists() || !testSuite.isDirectory()) {
			testSuite.mkdirs();
        } else{
        	try{
        		FileUtils.cleanDirectory(testSuite); 
        	}catch(Exception e){
        		ExceptionUtil.handleInDefaultLevel(e);
        	}
        }
	}
	
	public static String getDefaultTestSuiteDirectoryPath(String policyFilePath,String coverageName){
		File f = new File(policyFilePath);
		return f.getParent()+ File.separator + f.getName().split("\\.")[0] + PropertiesLoader.getProperties("config").getProperty("testSuitesBaseFolderNameSuffix") + File.separator + coverageName + PropertiesLoader.getProperties("config").getProperty("testSuiteFolderNameSuffix");
	}
}
