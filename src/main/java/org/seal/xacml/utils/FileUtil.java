package org.seal.xacml.utils;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class FileUtil {
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
