package org.seal.xacml.utils;

import java.io.File;
import java.util.regex.Pattern;

import org.w3c.dom.Node;
import org.wso2.balana.DOMHelper;

public class PolicyElementUtil {
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
    
    
}
