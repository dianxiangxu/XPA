package org.seal.xacml.utils;

import java.io.File;
import java.util.regex.Pattern;

import org.seal.combiningalgorithms.ReadPolicy;
import org.seal.semanticCoverage.PolicyCoverageFactory;
import org.w3c.dom.Node;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.DOMHelper;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.balana.ctx.xacml3.XACML3EvaluationCtx;

public class XACMLElementUtil {
	private static Pattern policyPattern = Pattern.compile("(?:\\w+:)*Policy");
    private static Pattern policySetPattern = Pattern.compile("(?:\\w+:)*PolicySet");
    private static Pattern rulePattern = Pattern.compile("(?:\\w+:)*Rule");
    
    public static boolean isRule(Node node){
    	String name = DOMHelper.getLocalName(node);
 	    if (rulePattern.matcher(name).matches()) {
 	    	return true;
 	    } else{
 	    	return false;
 	    }
    }
    
    public static boolean isPolicy(Node node){
    	String name = DOMHelper.getLocalName(node);
 	    if (policyPattern.matcher(name).matches()) {
 	    	return true;
 	    } else{
 	    	return false;
 	    }
    }
    
    public static boolean isPolicySet(Node node){
    	String name = DOMHelper.getLocalName(node);
 	    if (policySetPattern.matcher(name).matches()) {
 	    	return true;
 	    } else{
 	    	return false;
 	    }
    }
    
    public static String getPolicyName(String path){
		String[] tokens = path.substring(0, path.length() - 4).split(File.separator);
		return tokens[tokens.length-1];
	}
	
	public static String getPolicyName(File file){
		return getPolicyName(file.toString());
	}
	
	public static int evaluateRequestForPolicy(AbstractPolicy policy, String request) throws ParsingException{
		PolicyCoverageFactory.init(policy);
		PolicyCoverageFactory.newRow();
		
		RequestCtxFactory rc = new RequestCtxFactory();
		AbstractRequestCtx ar = rc.getRequestCtx(request);
		XACML3EvaluationCtx ec = new XACML3EvaluationCtx(new RequestCtx(ar.getAttributesSet(),ar.getDocumentRoot()), ReadPolicy.getPDPconfig());
		return policy.evaluate(ec).getDecision();
	}
    
    
}
