package org.seal.mutation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import org.seal.coverage.PolicySpreadSheetTestRecord;
import org.seal.coverage.PolicySpreadSheetTestSuite;
import org.seal.faultlocalization.SpectrumBasedDiagnosisResults;
import org.seal.faultlocalization.SpectrumBasedFaultLocalizer;

public class PolicyMutant {
	private String number;
	private String mutantFilePath;
	// change to the following if multiple faults need to be considered
	// int[] faultLocations 
	private int[] faultLocation;

	private String testResult;

	public PolicyMutant(String number, String fileName, int bugPosition){
		this.number = number;
		this.mutantFilePath = fileName;
		this.faultLocation = new int[] {bugPosition};
	}
	
	public PolicyMutant(String number, String fileName, int[] bugPositions){
		this.number = number;
		this.mutantFilePath = fileName;
		this.faultLocation = bugPositions;
	}
	public ArrayList<SpectrumBasedDiagnosisResults> run(ArrayList<PolicySpreadSheetTestRecord> testCases) throws Exception{
		// Test
			//System.out.println(mutantFilePath);
		if (!new PolicySpreadSheetTestSuite(testCases, mutantFilePath).runAllTests()) // not all tests passed (i.e., at least one test failed, otherwise fault localization is meaningless)
		    return SpectrumBasedFaultLocalizer.applyAllFaultLocalizersToPolicyMutant(faultLocation);
		else
			return null;
	}

	public String getNumber(){
		return number;
	}

	public String getMutantFilePath(){
		return mutantFilePath;
	}
	
	public String getMutantFilePath(String directory){
		if (directory.equals(""))
			return mutantFilePath;
		else
			return directory+File.separator+mutantFilePath;
	}
	
	public void removeDirectoryFromFilePath(){
		mutantFilePath = new File(mutantFilePath).getName();
	}
	
	public int[] getFaultLocation(){
		return faultLocation;
	}

	public String getTestResult(){
		return testResult;
	}
	
	public void setTestResult(String result){
		this.testResult = result;
	}
	
	public Vector<Object> getMutantVector(){
		Vector<Object> vector = new Vector<Object>();
		vector.add("");		// sequence number
		vector.add(number);	// mutant name
		vector.add(mutantFilePath);
		vector.add(Arrays.toString(faultLocation));
		vector.add(testResult);	
//		vector.add(mutantString);
		return vector;
	}
	
	public void clear() {
		new File(mutantFilePath).delete();
	}
}
