// 10/20/14 Jimmy

package org.seal.xacml.mutation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.seal.coverage.PolicySpreadSheetTestSuite;
import org.seal.xacml.components.CombiningAlgorithmURI;
import org.seal.xacml.policyUtils.PolicyLoader;
import org.seal.xacml.semanticCoverage.TestSuite;
import org.seal.xacml.semanticMutation.Mutant;
import org.seal.xacml.utils.MutantUtil;
import org.seal.xacml.xpa.Experiment;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.xml.sax.SAXException;

public class PolicySpreadSheetMutantSuite {

	public static final String MUTANT_KEYWORD = "MUTANT";

	private List<Mutant> policyMutantSuite;
	private String mutantsDirectory;
	
	public String getMutantsDirectory() {
		return mutantsDirectory;
	}

	private String policy;
	
	public PolicySpreadSheetMutantSuite(String directory,
			List<Mutant> mutantRecords, String policy) {
		this.mutantsDirectory = directory;
		this.policyMutantSuite = mutantRecords;
		this.policy = policy;
	}
	
	public PolicySpreadSheetMutantSuite(String mutantSuiteSpreadSheetFile, String policy) throws Exception {
		this(new File(mutantSuiteSpreadSheetFile).getParent(), readMutantSuite(mutantSuiteSpreadSheetFile), policy);
		//System.out.println(mutantSuiteSpreadSheetFile);
	}

	public static ArrayList<Mutant> readMutantSuite(
			String mutantSuiteSpreadSheetFile) throws IOException {
		ArrayList<Mutant> mutantSuite = new ArrayList<Mutant>();
		FileInputStream inputStream = new FileInputStream(
				mutantSuiteSpreadSheetFile);
		HSSFWorkbook workBook= new HSSFWorkbook(inputStream);
		// System.out.println(testSuiteSpreadSheetFile + "&&&&&");
		Sheet sheet= workBook.getSheetAt(0);
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
			//policy = PolicyLoader.loadPolicy(new File(absoluteMutantFileName));
		}
		
		catch(Exception e){
			e.printStackTrace();
		}
		mutantSuite.add(new Mutant( policy, bugPositions,mutantFileName.split("\\.")[0]));
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
		HSSFWorkbook workBook= new HSSFWorkbook();
		workBook.createSheet("mutation list");
		Sheet sheet= workBook.getSheetAt(0);
		Row row = sheet.createRow(0);
		for (int mutantIndex =0; mutantIndex<mutantList.size(); mutantIndex++){
			row = sheet.createRow(mutantIndex+1);
			writeMutantRow(mutantIndex+1,row, mutantList.get(mutantIndex)); 
		}
		try {
			FileOutputStream out= new FileOutputStream(this.mutantsDirectory + File.separator + mutantSpreadSheetFileName);
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

	/*public int updateMutantTestResult(Vector<Vector<Object>> data, TestSuite testSuite) throws ParsingException{
		int mutantIndex=0;
		int killedCount= 0;
		for (Mutant mutant : policyMutantSuite) {
			Vector<Object> vector = data.get(mutantIndex);
			//vector.set(vector.size()-1, TestSuite.runTests(mutant.getTestResult());
			
			// TO BE DONE
			boolean result= testSuite.isKilled(mutant.getPolicy());
			String col = null;
			if(result) {
				col = "Killed";
				killedCount++;
			} else {
				col = "Live";
			}
			vector.set(vector.size()-1, col);
			mutantIndex++;
		}
		return killedCount;
	}*/
//	
//	public int updateMutantTestResult(Vector<Vector<Object>> data, TestSuite testSuite, File mutantFolder){
//		int mutantIndex=0;
//		int killedCount= 0;
//		for (Mutant mutant : policyMutantSuite) {
//			Vector<Object> vector = data.get(mutantIndex);
//			//vector.set(vector.size()-1, TestSuite.runTests(mutant.getTestResult());
//			
//			// TO BE DONE
//			String absoluteMutantFileName = mutantFolder.getAbsolutePath() + File.separator + mutant.getName() + ".xml";
//			AbstractPolicy policy = null;
//			try{
//				policy = PolicyLoader.loadPolicy(new File(absoluteMutantFileName));
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//			mutant.setPolicy(policy);
//			List<Boolean> results = testSuite.runTests(mutant);
//			mutant.setPolicy(null);
//			int countFailed = 0;
//			for(Boolean b:results) {
//				if(!b) {
//					countFailed++;
//				}
//			}
//			String col = "Live (0 test failed)";
//			if(countFailed > 0) {
//				killedCount++;
//				if(countFailed == 1) {
//					col = "Killed (1 test failed)";
//				} else {
//					col = "Killed (" + countFailed + " tests failed)";
//				}
//			}
//			vector.set(vector.size()-1, col);
//			mutantIndex++;
//			if(mutantIndex%100==0) {
//				System.out.println("------"+mutantIndex+"-kc-"+killedCount);
//			}
//		}
//		return killedCount;
//	}
	
	public int updateMutantTestResult(Vector<Vector<Object>> data, TestSuite testSuite, File mutantFolder) throws SAXException, IOException, ParsingException, ParserConfigurationException{
		int mutantIndex=0;
		int killedCount= 0;
		
		
		
		int creCount= 0;
		int rttCount= 0;
		int rtfCount= 0;
		int rctCount= 0;
		int rcfCount= 0;
		int anfCount= 0;
		int rnfCount= 0;
		int rerCount= 0;
		int pttCount= 0;
		int ptfCount= 0;
		int fprCount= 0;
		int fdrCount= 0;
		int crcCount= 0;
		int rpteCount= 0;
		
		
		int creKCount= 0;
		int rttKCount= 0;
		int rtfKCount= 0;
		int rctKCount= 0;
		int rcfKCount= 0;
		int anfKCount= 0;
		int rnfKCount= 0;
		int rerKCount= 0;
		int pttKCount= 0;
		int ptfKCount= 0;
		int fprKCount= 0;
		int fdrKCount= 0;
		int crcKCount= 0;
		int rpteKCount= 0;
		
		String killedCRCs="";
		String notKilledCRCs="";
		
		
		for (Mutant mutant : policyMutantSuite) {
			Vector<Object> vector = data.get(mutantIndex);
			//vector.set(vector.size()-1, TestSuite.runTests(mutant.getTestResult());
			
			
			// TO BE DONE
			String absoluteMutantFileName = mutantFolder.getAbsolutePath() + File.separator + mutant.getName() + ".xml";
			AbstractPolicy policy = null;
			try{
				policy = PolicyLoader.loadPolicy(new File(absoluteMutantFileName));
			}catch(Exception e){
				e.printStackTrace();
			}
			mutant.setPolicy(policy);
			List<Boolean> results = testSuite.runTests(mutant);
			mutant.setPolicy(null);
			int countFailed = 0;
			for(Boolean b:results) {
				if(!b) {
					countFailed++;
				}
			}
			
			if(mutant.getName().indexOf("CRE")>0) {
				creCount++;
				if(countFailed > 0) {
					creKCount++;
				}
			}
			
			if(mutant.getName().indexOf("RTT")>0) {
				rttCount++;
				if(countFailed > 0) {
					rttKCount++;
				}
			}
			
			if(mutant.getName().indexOf("RTF")>0) {
				rtfCount++;
				if(countFailed > 0) {
					rtfKCount++;
				}
			}
			
			if(mutant.getName().indexOf("RCT")>0) {
				rctCount++;
				if(countFailed > 0) {
					rctKCount++;
				}
			}
			
			if(mutant.getName().indexOf("RCF")>0) {
				rcfCount++;
				if(countFailed > 0) {
					rcfKCount++;
				}
			}
			
			if(mutant.getName().indexOf("ANF")>0) {
				anfCount++;
				if(countFailed > 0) {
					anfKCount++;
				}
			}
			
			if(mutant.getName().indexOf("RNF")>0) {
				rnfCount++;
				if(countFailed > 0) {
					rnfKCount++;
				}
			}
			
			if(mutant.getName().indexOf("RER")>0) {
				rerCount++;
				if(countFailed > 0) {
					rerKCount++;
				}
			}
			
			if(mutant.getName().indexOf("PTT")>0) {
				pttCount++;
				if(countFailed > 0) {
					pttKCount++;
				}
			}
			
			if(mutant.getName().indexOf("PTF")>0) {
				ptfCount++;
				if(countFailed > 0) {
					ptfKCount++;
				}
			}
			
			if(mutant.getName().indexOf("FPR")>0) {
				fprCount++;
				if(countFailed > 0) {
					fprKCount++;
				}
			}
			
			if(mutant.getName().indexOf("FDR")>0) {
				fdrCount++;
				if(countFailed > 0) {
					fdrKCount++;
				}
			}
			
			if(mutant.getName().indexOf("CRC")>0) {
				crcCount++;
				if(countFailed > 0) {
					crcKCount++;
					killedCRCs += "("+ mutant.getName().substring(mutant.getName().indexOf("CRC"));  
					if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("PO"))) {
						killedCRCs += ":{" + Experiment.currentPolicyCA + "->" + "PO}  ";
					}
					if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("DO"))) {
						killedCRCs += ":{" +  Experiment.currentPolicyCA + "->" + "DO}  ";
					}
					if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("OPO"))) {
						killedCRCs += ":{" + Experiment.currentPolicyCA + "->" + "OPO}  ";
					}
					if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("ODO"))) {
						killedCRCs += ":{" +  Experiment.currentPolicyCA + "->" + "ODO}  ";
					}
					if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("FA"))) {
						killedCRCs += ":{" + Experiment.currentPolicyCA + "->" + "FA}  ";
					}
					if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("PUD"))) {
						killedCRCs += ":{" + Experiment.currentPolicyCA + "->" + "PUD}  ";
					}
					if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("DUP"))) {
						killedCRCs += ":{" + Experiment.currentPolicyCA + "->" + "DUP}  ";
					}
					killedCRCs += ")  ";
				} else {
					notKilledCRCs +=  "("+ mutant.getName().substring(mutant.getName().indexOf("CRC"));  
					if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("PO"))) {
						notKilledCRCs +=":{" + Experiment.currentPolicyCA + "->" + "PO}  ";
					}
					if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("DO"))) {
						notKilledCRCs +=":{" + Experiment.currentPolicyCA + "->" + "DO}  ";
					}
					if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("OPO"))) {
						notKilledCRCs +=":{" + Experiment.currentPolicyCA + "->" + "OPO}  ";
					}
					if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("ODO"))) {
						notKilledCRCs +=":{" + Experiment.currentPolicyCA + "->" + "ODO}  ";
					}
					if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("FA"))) {
						notKilledCRCs +=":{" + Experiment.currentPolicyCA + "->" + "FA}  ";
					}
					if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("PUD"))) {
						notKilledCRCs += ":{" + Experiment.currentPolicyCA + "->" + "PUD}  ";
					}
					if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("DUP"))) {
						notKilledCRCs += ":{" + Experiment.currentPolicyCA + "->" + "DUP}  ";
					}
					notKilledCRCs += ")  ";
				}
			}
			
			if(mutant.getName().indexOf("RPTE")>0) {
				rpteCount++;
				if(countFailed > 0) {
					rpteKCount++;
				}
			}
			
			
			String col = "Live (0 test failed)";
			if(countFailed > 0) {
				killedCount++;
				if(countFailed == 1) {
					col = "Killed (1 test failed)";
				} else {
					col = "Killed (" + countFailed + " tests failed)";
				}
			}
			
			vector.set(vector.size()-1, col);
			mutantIndex++;
			if(mutantIndex%100==0) {
				System.out.println("------"+mutantIndex+"-kc-"+killedCount);
			}
		}
		boolean skipDetailflag = false; 
		String line = ","; if(creCount > 0){ if(creCount== creKCount) {line += "Y"; skipDetailflag = true;} else { if(creKCount>0){line+="B";}else{line+="N"; skipDetailflag = true; skipDetailflag = true;}} if(!skipDetailflag) { line += "; k=" +String.valueOf(creKCount); line += "; t=" +String.valueOf(creCount);}}
		
		line += ","; skipDetailflag = false; if(rttCount > 0){ if(rttCount== rttKCount) {line += "Y"; skipDetailflag = true;} else { if(rttKCount>0){line+="B";}else{line+="N"; skipDetailflag = true;}} if(!skipDetailflag) { line += "; k=" +String.valueOf(rttKCount); line += "; t=" +String.valueOf(rttCount);}}
		line += ","; skipDetailflag = false; if(rtfCount > 0){ if(rtfCount== rtfKCount) {line += "Y"; skipDetailflag = true;} else { if(rtfKCount>0){line+="B";}else{line+="N"; skipDetailflag = true;}} if(!skipDetailflag) { line += "; k=" +String.valueOf(rtfKCount); line += "; t=" +String.valueOf(rtfCount);}}
		line += ","; skipDetailflag = false; if(rctCount > 0){ if(rctCount== rctKCount) {line += "Y"; skipDetailflag = true;} else { if(rctKCount>0){line+="B";}else{line+="N"; skipDetailflag = true;}} if(!skipDetailflag) { line += "; k=" +String.valueOf(rctKCount); line += "; t=" +String.valueOf(rctCount);}}
		line += ","; skipDetailflag = false; if(rcfCount > 0){ if(rcfCount== rcfKCount) {line += "Y"; skipDetailflag = true;} else { if(rcfKCount>0){line+="B";}else{line+="N"; skipDetailflag = true;}} if(!skipDetailflag) { line += "; k=" +String.valueOf(rcfKCount); line += "; t=" +String.valueOf(rcfCount);}}
		line += ","; skipDetailflag = false; if(anfCount > 0){ if(anfCount== anfKCount) {line += "Y"; skipDetailflag = true;} else { if(anfKCount>0){line+="B";}else{line+="N"; skipDetailflag = true;}} if(!skipDetailflag) { line += "; k=" +String.valueOf(anfKCount); line += "; t=" +String.valueOf(anfCount);}}
		line += ","; skipDetailflag = false; if(rnfCount > 0){ if(rnfCount== rnfKCount) {line += "Y"; skipDetailflag = true;} else { if(rnfKCount>0){line+="B";}else{line+="N"; skipDetailflag = true;}} if(!skipDetailflag) { line += "; k=" +String.valueOf(rnfKCount); line += "; t=" +String.valueOf(rnfCount);}}
		line += ","; skipDetailflag = false; if(rerCount > 0){ if(rerCount== rerKCount) {line += "Y"; skipDetailflag = true;} else { if(rerKCount>0){line+="B";}else{line+="N"; skipDetailflag = true;}} if(!skipDetailflag) { line += "; k=" +String.valueOf(rerKCount); line += "; t=" +String.valueOf(rerCount);}}
	
		line += ","; skipDetailflag = false; if(pttCount > 0){ if(pttCount== pttKCount) {line += "Y"; skipDetailflag = true;} else { if(pttKCount>0){line+="B";}else{line+="N"; skipDetailflag = true;}} if(!skipDetailflag) { line += "; k=" +String.valueOf(pttKCount); line += "; t=" +String.valueOf(pttCount);}}
		line += ","; skipDetailflag = false; if(ptfCount > 0){ if(ptfCount== ptfKCount) {line += "Y"; skipDetailflag = true;} else { if(ptfKCount>0){line+="B";}else{line+="N"; skipDetailflag = true;}} if(!skipDetailflag) { line += "; k=" +String.valueOf(ptfKCount); line += "; t=" +String.valueOf(ptfCount);}}
		line += ","; skipDetailflag = false; if(fprCount > 0){ if(fprCount== fprKCount) {line += "Y"; skipDetailflag = true;} else { if(fprKCount>0){line+="B";}else{line+="N"; skipDetailflag = true;}} if(!skipDetailflag) { line += "; k=" +String.valueOf(fprKCount); line += "; t=" +String.valueOf(fprCount);}}
		line += ","; skipDetailflag = false; if(fdrCount > 0){ if(fdrCount== fdrKCount) {line += "Y"; skipDetailflag = true;} else { if(fdrKCount>0){line+="B";}else{line+="N"; skipDetailflag = true;}} if(!skipDetailflag) { line += "; k=" +String.valueOf(fdrKCount); line += "; t=" +String.valueOf(fdrCount);}}
		line += ","; skipDetailflag = false; if(crcCount > 0){ if(crcCount== crcKCount) {line += "Y"; skipDetailflag = true;} else { if(crcKCount>0){line+="B";}else{line+="N"; skipDetailflag = true;}} if(!skipDetailflag) { line += "; k=" +String.valueOf(crcKCount); line += "; t=" +String.valueOf(crcCount);}}
		line += ","; skipDetailflag = false; if(rpteCount > 0){ if(rpteCount== rpteKCount) {line += "Y"; skipDetailflag = true;} else { if(rpteKCount>0){line+="B";}else{line+="N"; skipDetailflag = true;}} if(!skipDetailflag) { line += "; k=" +String.valueOf(rpteKCount); line += "; t=" +String.valueOf(rpteCount) ;}} line +=  ",k=" + killedCRCs + ",nk=" + notKilledCRCs +  System.lineSeparator();
		try {
			File file = new File("/home/roshan/Projects/XPA/Experiments/results.csv");
			FileWriter fr = new FileWriter(file, true);
			BufferedWriter br = new BufferedWriter(fr);
			br.write(line);
			br.close();
			fr.close();
		}catch(Exception e) {
			
		}
		boolean allKilled = false;
		
		//System.out.print(arg0);
		return killedCount;
	}
	
	public void runAndWriteDetectionInfoToExcelFile(String fileName, String testSuiteSpreadSheet) throws Exception {
		PolicySpreadSheetTestSuite tests = null;
		try {
			tests =	new PolicySpreadSheetTestSuite(testSuiteSpreadSheet, policy);
			//writeDetectionInfoToExcelFile(fileName, tests);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeDetectionInfoToExcelFile(String fileName, TestSuite suite, File mutantFolder) throws Exception {
		//System.gc();
		HSSFWorkbook workBook= new HSSFWorkbook();
		workBook.createSheet("fault-detection-info");
		Sheet sheet= workBook.getSheetAt(0);
		int numTests = suite.getSize();
		// An integer array to store counts of how many mutants that a test can detect,
		// plus the last cell is the count of detection for the whole test suite. 
		int[] detectionCount= new int[numTests+1];
		// initialize detection count
		for (int i = 0; i < numTests+1; i++) {
			detectionCount[i] = 0;
		}
		
		// 01/27/15 if #col > 255, print a brief version of the result, because of the limit of columns.
		if (numTests+3 > 255) {
			writeSimpleDetectionTitleRow(sheet, 0, numTests);
			int rowIndex = 1;
			for(int i = 0; i< policyMutantSuite.size();i++){
				Mutant m = policyMutantSuite.get(i);
				String absoluteMutantFileName = mutantFolder.getAbsolutePath() + File.separator + m.getName() + ".xml";
				AbstractPolicy policy = null;
				try{
					policy = PolicyLoader.loadPolicy(new File(absoluteMutantFileName));
				}catch(Exception e){
					e.printStackTrace();
				}
				m.setPolicy(policy);
			//for (Mutant mutant : policyMutantSuite) {
				writeSimpleDetectionInfoRow(sheet, rowIndex++, m, suite, detectionCount);
				m.setPolicy(null);
			}
			writeSimpleDetectionCountRow(sheet, rowIndex++, detectionCount);
			writeSimpleDetectionRateRow(sheet, rowIndex++, detectionCount, policyMutantSuite.size());
		} else {
			// Else (#col <= 255) write the full detail of test results.
			// write title row. specify number of columns by number of tests.
			if (numTests!=0)
				writeDetectionTitleRow(sheet, 0, numTests);
			
			int rowIndex = 1;
			for(int i = 0; i< policyMutantSuite.size();i++){
			//for (Mutant mutant : policyMutantSuite) {
				Mutant m = policyMutantSuite.get(i);
				String absoluteMutantFileName = mutantFolder.getAbsolutePath() + File.separator + m.getName() + ".xml";
				AbstractPolicy policy = null;
				try{
					policy = PolicyLoader.loadPolicy(new File(absoluteMutantFileName));
				}catch(Exception e){
					e.printStackTrace();
				}
				m.setPolicy(policy);
				String name = m.getName();
				writeDetectionInfoRow(sheet, rowIndex++, m, suite, detectionCount,i+1,name);
				m.setPolicy(null);
			}
			
			// 10/26/14: Add statistics: a row of fault detection count/rate for each single test.
			// and the detection count/rate for the whole test suite.
			writeDetectionCountRow(sheet, rowIndex++, detectionCount);
			writeDetectionRateRow(sheet, rowIndex++, detectionCount, policyMutantSuite.size());
		}
			
		try {
			FileOutputStream out= new FileOutputStream(fileName);
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
			Mutant mutant, TestSuite suite, int[] detectionCount) throws Exception {
		Row mutantRow = sheet.createRow(rowIndex);
		Cell[] mutantCells = new Cell[3];
		boolean killed = false; // fault detected?
		
		for (int i = 0; i < 3; i++) {
			mutantCells[i] = mutantRow.createCell(i);
		}
		//mutantCells[0].setCellValue(mutant.getNumber());
		mutantCells[1].setCellValue(Arrays.toString(mutant.getFaultLocations().toArray()));
		//PolicySpreadSheetTestSuite mutantTestSuite = 
		//		new PolicySpreadSheetTestSuite(tests.getTestRecord(), mutant.getMutantFilePath(mutantsDirectory));
		//boolean[] testResult= mutantTestSuite.runAllTestsOnMutant();
		//TestCellResult[] rowResult;	
		//rowResult= mutantTestSuite.runAllTestsOnMutant();
		List<Boolean> results = suite.runTests(mutant);
		for (int j = 0; j < results.size(); j++) {
			if(results.get(j)) {
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
		
		if (killed)
			detectionCount[suite.getSize()]++;
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
				titleCells[i].setCellValue("Name"); // header for mutant name
			if (i==1)
				titleCells[i].setCellValue("status");
			if (i>=2 && i<=numTests+1) {
				titleCells[i].setCellValue("Test" + (i-1));
			}
//			if (i==numTests+1) {
//				titleCells[i].setCellValue("Status");
//			}			
		}
	}
	
	private void writeDetectionInfoRow(Sheet sheet, int rowIndex,
			Mutant mutant, TestSuite suite, int[] detectionCount, int number, String name) throws Exception {
		int offset= 2;
		// initialize
		Row mutantRow = sheet.createRow(rowIndex);
		Cell[] mutantCells = new Cell[suite.getSize()+offset];
		boolean killed = false; // fault detected?
		// create cells.
		for (int i = 0; i < suite.getSize()+offset; i++) {
			mutantCells[i] = mutantRow.createCell(i);
		}
		// set values.
		mutantCells[0].setCellValue(name);
		//mutantCells[1].setCellValue(Arrays.toString(mutant.getFaultLocations().toArray()));
		//PolicySpreadSheetTestSuite mutantTestSuite = 
		//		new PolicySpreadSheetTestSuite(tests.getTestRecord(), mutant.getMutantFilePath(mutantsDirectory));
		
		// 11/1/15 fix test result return
		//boolean[] testResult= mutantTestSuite.runAllTestsOnMutant();
		//TestCellResult[] rowResult;	
		//rowResult= mutantTestSuite.runAllTestsOnMutant();
		List<Boolean> results = suite.runTests(mutant);
		for (int j = 0; j<results.size();j++) {
			//if(rowResult[j].getVerdict()) {
			Boolean b = results.get(j);
			if(b) {
				
			//mutantCells[j+2].setCellValue(rowResult[j].getLiteralDetail());
				mutantCells[j+2].setCellValue("");
				
			} else {
				// just count
				//mutantCells[j+2].setCellValue(rowResult[j].getLiteralDetail());
			mutantCells[j+2].setCellValue("X");
				detectionCount[j]++;
				killed = true;
			}
		}
		//mutantCells[tests.getNumberOfTests()+2].setCellValue(killed ? "Yes" : "No");
		//mutant.setTestResult(killed ? "Yes" : "No");
		if (killed) {
			detectionCount[suite.getSize()]++;
			mutantCells[1].setCellValue("killed");
		} else {
			mutantCells[1].setCellValue("live");
		}
	}
	
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
		
		PolicySpreadSheetMutantSuite mutantSuite = 
				new PolicySpreadSheetMutantSuite("Experiments//" + policy + "//mutation//" + policy + "_mutants.xls",
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
