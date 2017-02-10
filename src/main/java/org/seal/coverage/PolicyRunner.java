
package org.seal.coverage;

import org.seal.combiningalgorithms.ReadPolicy;
import org.seal.combiningalgorithms.loadPolicy;
import org.wso2.balana.ParsingException;
import org.wso2.balana.Policy;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.balana.ctx.xacml3.XACML3EvaluationCtx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

// a number of tests (queries) for the same policy
// the tests are assumed under the same folder of the policy under test
public class PolicyRunner {
 
	public static String testExecutionResult = "";
	private static String BalanaPolicyLocation = "resources";
//	private static String BalanaPolicyLocation = "//Users//dianxiangxu//Documents//JavaProjects//WSO2-XACML//XPA//resources";
	
	//private PDP pdp;			// created from the policy under test
	
	private Policy policy;
	private String orginalPolicyFolder;
	
	public PolicyRunner(String policyFilePath) throws Exception{
		loadPolicy loadpolicy = new loadPolicy();
		// Test	
			//System.out.println(policyFilePath);
		this.policy = loadpolicy.getPolicy(policyFilePath);
		if (this.policy == null)
			throw new Exception("load policy failed");
		
	}
	

    public int runTestWithoutOracle(String testID, String request){
		PolicyCoverageFactory.currentTestID = testID;
    	PolicyCoverageFactory.currentTestOracle = -1;
    	return getResponse(request);
    }

    public boolean runTest(String testID, String request, String oracleString){
		PolicyCoverageFactory.currentTestID = testID;
    	int oracle = balanaFinalDecision(oracleString);
    	PolicyCoverageFactory.currentTestOracle = oracle;
    	int response = getResponse(request);
    	boolean verdict = (response==oracle);
    	// 11/1/15 modify literal form.
    	testExecutionResult = (verdict ? "Pass" : "Fail") + "(" + oracle + "/" + response + ")";
    	return verdict;
    }

    public boolean runTestFromFile(String testID, String requestFile, String oracleString){
		PolicyCoverageFactory.currentTestID = testID;
    	int oracle = balanaFinalDecision(oracleString);
    	PolicyCoverageFactory.currentTestOracle = oracle;
    	if (new File(requestFile).exists())
    		return getResponse(readTextFile(requestFile)) == oracle;
    	else
    		return getResponse(readTextFile(orginalPolicyFolder+File.separator+requestFile)) == oracle;
    }

    public int getResponse(String request){
    	return PolicyEvaluate(this.policy, request);
    }
        

    
	public int balanaFinalDecision(String decisionString){
		if (decisionString.equalsIgnoreCase("Permit"))
			return AbstractResult.DECISION_PERMIT;
		else
		if (decisionString.equalsIgnoreCase("Deny"))
			return AbstractResult.DECISION_DENY;
		else
		if (decisionString.equalsIgnoreCase("NA") || decisionString.equalsIgnoreCase("NotApplicable")) // new pattern 11/13/14
			return AbstractResult.DECISION_NOT_APPLICABLE;	
		else
		if (decisionString.equalsIgnoreCase("INDETERMINATE"))
			return AbstractResult.DECISION_INDETERMINATE;	
		else
		if (decisionString.equalsIgnoreCase("IP"))
			return AbstractResult.DECISION_INDETERMINATE_PERMIT;	
		else
		if (decisionString.equalsIgnoreCase("ID"))
			return AbstractResult.DECISION_INDETERMINATE_DENY;	
		else
		if (decisionString.equalsIgnoreCase("IDP"))
			return AbstractResult.DECISION_INDETERMINATE_DENY_OR_PERMIT;	
		return AbstractResult.DECISION_INDETERMINATE;	
	}

	/*
	public int balanaFinalDecision(String decisionString){
		if (decisionString.equalsIgnoreCase("Permit"))
			return AbstractResult.DECISION_PERMIT;
		else
		if (decisionString.equalsIgnoreCase("Deny"))
			return AbstractResult.DECISION_DENY;
		else
		if (decisionString.equalsIgnoreCase("NA"))
			return AbstractResult.DECISION_NOT_APPLICABLE;	
		else
		if (decisionString.equalsIgnoreCase("INDETERMINATE"))
			return AbstractResult.DECISION_INDETERMINATE;	
		else
		if (decisionString.equalsIgnoreCase("IP"))
			return AbstractResult.DECISION_INDETERMINATE;	
		else
		if (decisionString.equalsIgnoreCase("ID"))
			return AbstractResult.DECISION_INDETERMINATE;	
		else
		if (decisionString.equalsIgnoreCase("IDP"))
			return AbstractResult.DECISION_INDETERMINATE;	
		return AbstractResult.DECISION_INDETERMINATE;	
	}
*/
	
 	public static String readTextFile(String fileName){
		return readTextFile(new File(fileName));
	}

	public static String readTextFile(File file){
		String text = "";
		if (file==null || !file.exists())
			return text;
		Scanner in = null; 
		try {
			in = new Scanner(new FileReader(file));
			while (in.hasNextLine())
				text += in.nextLine()+"\n";
		} catch (IOException ioe){
		}
		finally {
			if (in!=null)
				in.close();
		}
		return text;
	}


	public static void copyFile (File fromFile, File toFile) throws IOException {
		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = from.read(buffer)) != -1)
		    	to.write(buffer, 0, bytesRead);
		} 
		catch (IOException ioe){
			throw ioe;
		}
		finally {
			try {
				if (from != null)
					from.close();
				if (to != null)
					to.close();
			}
			catch (IOException ioe){
				throw ioe;
			}
		}
	}
	     
	public static void saveStringToTextFile(String fileString, String fileName) {
		File file = new File(fileName);
		saveStringToTextFile(fileString, file);
	}

	public static void saveStringToTextFile(String fileString, File file) {
		PrintWriter out = null;
		if (file.exists()) {
			file.delete();
		}
		try {
			out = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		out.print(fileString);
		out.close();
	}
	
	public int PolicyEvaluate(Policy policy, String request) {
		RequestCtxFactory rc = new RequestCtxFactory();
		AbstractRequestCtx ar = null;
		try {
			ar = rc.getRequestCtx(request);
		} catch (ParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XACML3EvaluationCtx ec;

		ec = new XACML3EvaluationCtx(new RequestCtx(ar.getAttributesSet(),
				ar.getDocumentRoot()), ReadPolicy.getPDPconfig());
//System.out.println("Policy: "+policy);
//System.out.println("Request: "+request);
		return policy.evaluate(ec).getDecision();
	}

 
}