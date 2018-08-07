package org.seal.xacml.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.seal.xacml.NameDirectory;
import org.seal.xacml.combiningalgorithms.ReadPolicy;
import org.seal.xacml.semanticCoverage.PolicyCoverageFactory;
import org.w3c.dom.Node;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.Balana;
import org.wso2.balana.DOMHelper;
import org.wso2.balana.MatchResult;
import org.wso2.balana.ParsingException;
import org.wso2.balana.PolicyMetaData;
import org.wso2.balana.PolicyTreeElement;
import org.wso2.balana.Rule;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.combine.CombinerElement;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.balana.ctx.xacml3.XACML3EvaluationCtx;
import org.wso2.balana.xacml3.Target;

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
	
	public static List<Rule> getRuleFromPolicy(AbstractPolicy policy) {
		List<CombinerElement> childElements = policy.getChildElements();
		List<Rule> Elements = new ArrayList<Rule>();
		for (CombinerElement element : childElements) {
			PolicyTreeElement tree1 = element.getElement();
			Rule rule = null;
			if (tree1 instanceof Rule) {
				rule = (Rule) tree1;
				Elements.add(rule);
			}
		}
		return Elements;
	}
	
	public static boolean isDefaultRule(Node node, PolicyMetaData policyMetaData) throws ParsingException{
		Node targetNode = XMLUtil.findInChildNodes(node, NameDirectory.TARGET);
		Node conditionNode = XMLUtil.findInChildNodes(node, NameDirectory.CONDITION);
	    Target target = null;
	    Condition condition = null;
	   
	    if (!XMLUtil.isEmptyNode(targetNode)) {
		    target = Target.getInstance(targetNode, policyMetaData);
		}
		
		if (!XMLUtil.isEmptyNode(conditionNode)) {
	        condition = Condition.getInstance(conditionNode, policyMetaData, null);
	    }
		if(target==null && condition == null){
			return true;
		} else {
			return false;
		}
	}
	
	public static int TargetEvaluate(Target target, String request) {
		// 0 = match, 1 = no match, 2 = ind
		MatchResult match = null;

		XACML3EvaluationCtx ec;
		ec = getEvaluationCtx(request);
		
		match = target.match(ec);
		//System.err.println("Target match result: " + match.getResult());
		return match.getResult();

	}

	
	private static XACML3EvaluationCtx getEvaluationCtx(String request) {
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
				ar.getDocumentRoot()), Balana.getInstance().getPdpConfig());
		return ec;
	}
	
	public static int ConditionEvaluate(Condition condition, String request) {
		XACML3EvaluationCtx ec;
		ec = getEvaluationCtx(request);
		EvaluationResult result = condition.evaluate(ec);
		if (result.indeterminate()) {
			return 2;
		} else {
			BooleanAttribute bool = (BooleanAttribute) (result
					.getAttributeValue());
			if (bool.getValue()) {
				// if any obligations or advices are defined, evaluates them and
				// return
				return 0;
			} else {
				return 1;
			}
		}

	}

}
