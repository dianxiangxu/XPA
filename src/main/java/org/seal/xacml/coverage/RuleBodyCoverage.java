package org.seal.xacml.coverage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.seal.xacml.Attr;
import org.seal.xacml.NameDirectory;
import org.seal.xacml.RequestGeneratorBase;
import org.seal.xacml.components.CombiningAlgorithmURI;
import org.seal.xacml.policyUtils.PolicyLoader;
import org.seal.xacml.utils.RequestBuilder;
import org.seal.xacml.utils.XACMLElementUtil;
import org.seal.xacml.utils.XMLUtil;
import org.seal.xacml.utils.Z3StrUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.ParsingException;
import org.wso2.balana.Policy;
import org.wso2.balana.Rule;
import org.wso2.balana.cond.Apply;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.cond.Expression;
import org.wso2.balana.xacml3.Target;
import org.xml.sax.SAXException;

/**
 * Created by roshanshrestha on 2/10/17.
 */
public class RuleBodyCoverage extends RequestGeneratorBase {
	private boolean conditionFlag;
	private boolean targetFlag;
	private Map<String,List<Attr>> ruleAttrMap;
	private boolean dominantIndeterminateFlag;
	private List<Rule> dominantRules;
	
	
	public RuleBodyCoverage(String policyFilePath) throws ParsingException, IOException, SAXException, ParserConfigurationException{
		init(policyFilePath);
		ruleAttrMap = new HashMap<String,List<Attr>>();
	}
	
	public List<String> generateRequests() throws ParsingException, IOException, SAXException, ParserConfigurationException{
		conditionFlag = targetFlag = true;
        StringBuilder preExpression = new StringBuilder();
        
        traverse( doc.getDocumentElement(), preExpression,null,false);
        return getRequests();
    }
	
	public List<String> generateRequestsForTruthValues(boolean targetFlag, boolean conditionFlag, boolean falsifyPostRules) throws ParsingException, IOException, SAXException, ParserConfigurationException{
		this.targetFlag = targetFlag;
		this.conditionFlag = conditionFlag;
        StringBuilder preExpression = new StringBuilder();
        List<String> paths = new ArrayList<String>();
        traverse( doc.getDocumentElement(), preExpression,null, falsifyPostRules);
        return getRequests();
    }
    
   private void traverse(Element node, StringBuilder preExpression, List<Rule> previousRules, boolean falsifyPostRules) throws ParsingException, IOException {
	    if (XACMLElementUtil.isRule(node)) {
	    	boolean dominantIndeterminateFlag=false;
	    	List<Rule> dominantRules = new ArrayList<Rule>();
	    	
	    	String expression;
	    	String expressionErrorCondition=null;
	    	Condition condition = XMLUtil.getCondition(node, policyMetaData);
		    
	    	if(falsifyPostRules){
	    		expression = getRuleExpressionForTruthValuesWithPostRules(node,preExpression,previousRules);
	    		if(condition!=null && !conditionFlag) {
	    			expressionErrorCondition = getRuleTargetExpression(node, preExpression, previousRules);
	    		}
	    	} else{
	    		expression = getRuleCoverageExpression(node,preExpression,previousRules);
	    	}
	    	if(expression!=null) {
				boolean sat = Z3StrUtil.processExpression(expression, z3ExpressionHelper);
				if (sat == true) {
					String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
				    addRequest(req);
				}
	    	}
			if(condition!=null&&!conditionFlag) {
				String req = IndCondition(condition, expressionErrorCondition);
				if(req !=null) {
					addRequest(req);
				}
//				sat = Z3StrUtil.processExpression(expressionErrorCondition, z3ExpressionHelper);
//				if (sat == true) {
//					String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
//				    addRequest(req);
//				}
			}
		    previousRules.add(Rule.getInstance(node, policyMetaData, null));
		    return;
		}
		
		if (XACMLElementUtil.isPolicy(node) || XACMLElementUtil.isPolicySet(node)) {
		    Node targetNode = XMLUtil.findInChildNodes(node, NameDirectory.TARGET);
		    Target target;
		    
			if (targetNode != null) {
		        target = Target.getInstance(targetNode, policyMetaData);
		        if(target.getAnyOfSelections().size()>0){
		        	preExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
		        }
		    }
	        NodeList children = node.getChildNodes();
	        previousRules = null;
	        if(XACMLElementUtil.isPolicy(node)){
	        	previousRules = new ArrayList<Rule>();
	        	Policy p = Policy.getInstance(node);
	        	String ca = p.getCombiningAlg().getIdentifier().toString();
	        	if(ca.equals(CombiningAlgorithmURI.map.get("PO")) || ca.equals(CombiningAlgorithmURI.map.get("OPO")) ||ca.equals(CombiningAlgorithmURI.map.get("DUP"))) {
	        		falsifyRulesFlag = 1;
	        	} 
	        	else if(ca.equals(CombiningAlgorithmURI.map.get("DO")) || ca.equals(CombiningAlgorithmURI.map.get("ODO")) ||ca.equals(CombiningAlgorithmURI.map.get("PUD"))) {
	        		falsifyRulesFlag = 2;
	        	} else {
	        		falsifyRulesFlag = 0;
	        	}
	        		
	        	
	        }
	        for (int i = 0; i < children.getLength(); i++) {
	            Node child = children.item(i);
	            StringBuilder preExpressionCurrent = new StringBuilder(preExpression.toString());
	            if (child instanceof Element && XMLUtil.isTraversableElement(child)) {
	            	traverse((Element) child, preExpressionCurrent, previousRules,falsifyPostRules);
	            }
	        }
		}	
    }

	public String getRuleCoverageExpression (Element node, StringBuilder preExpression, List<Rule> previousRules) throws ParsingException{
		return getRuleExpressionForTruthValues(node, preExpression, previousRules);
	}
	
	public String getRuleExpressionForTruthValues(Element node, StringBuilder preExpression, List<Rule> previousRules) throws ParsingException{
	    Target target = XMLUtil.getTarget(node, policyMetaData);
	    Condition condition = XMLUtil.getCondition(node, policyMetaData);
	    StringBuffer ruleExpression = new StringBuffer();
	    Rule r = Rule.getInstance(node, policyMetaData, null);
	    List<Attr> curRuleAttr = new ArrayList<Attr>();

	    if(target != null){
		    if(targetFlag){
		    	
		    	ruleExpression.append(z3ExpressionHelper.getTrueTargetExpression(target,curRuleAttr) + System.lineSeparator());
		    }else{
		    	ruleExpression.append(z3ExpressionHelper.getFalseTargetExpression(target,curRuleAttr) + System.lineSeparator());
	
		    }
	    }
	    if(condition != null){
	    	if(conditionFlag){
	    		ruleExpression.append(z3ExpressionHelper.getTrueConditionExpression(condition,curRuleAttr) + System.lineSeparator());
	    	} else{
	    		ruleExpression.append(z3ExpressionHelper.getFalseConditionExpression(condition,curRuleAttr) + System.lineSeparator());
	    	}
	    
	    }
	    if(condition==null && target==null) {
	    	return null;
	    }
		ruleAttrMap.put(r.getId().toString(),curRuleAttr);
		dominantIndeterminateFlag = false;
		dominantRules = new ArrayList<Rule>();
	    
	    StringBuffer falsifyPreviousRules = new StringBuffer();
	    for(Rule rule:previousRules){
//	    	if((rule.getEffect()==0 && falsifyRulesFlag == 2) || (rule.getEffect()==1 && falsifyRulesFlag == 1)) {
//	    		continue;
//	    	}
	    	if(falsifyRulesFlag==0) {
	    		falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());

	    	} else {
	    		boolean flag = hasCommonAttribute(ruleAttrMap.get(rule.getId().toString()),ruleAttrMap.get(r.getId().toString()));
		    	
	    		if(flag){
	    			falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());

	    		}
	    	}
//	    	if(trueRuleExpr.contains(ruleExpression)) {
//				falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
//
//	    	}
		}                
	    return preExpression.toString() + ruleExpression + ruleReachability(r, previousRules) + System.lineSeparator() + propagation(r, getPostRules(node));
//	    return preExpression.toString()+ruleExpression+falsifyPreviousRules;
	}
	
	public String propagation(Rule r, List<Rule> rules)   throws ParsingException{
		 StringBuffer falsifyPostRules = new StringBuffer();
		 for(Rule rule:rules){
			 List<Attr> curRuleAttr = new ArrayList<Attr>();
			 	    	
			 z3ExpressionHelper.getTrueTargetExpression((Target)rule.getTarget(),curRuleAttr);
			 z3ExpressionHelper.getTrueConditionExpression((Condition)rule.getCondition(),curRuleAttr);
			
			 
			if(currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("FA"))) {
				if(r.getEffect()== rule.getEffect()) {
					falsifyPostRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
				}
	    	} else if(rule.getEffect()==0) {
	    		if(currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("DUP"))){
	    			falsifyPostRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
	    		} else if(currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("PO")) || currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("OPO"))) {
	    			if(hasCommonAttribute(curRuleAttr,ruleAttrMap.get(r.getId().toString()))) {
	    				falsifyPostRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
	    		    	
	    			} else {
	    				dominantRules.add(rule);
	    				dominantIndeterminateFlag = true;
	    			}
	    		}
	    	} else if(rule.getEffect()==1) {
	    		if(currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("PUD"))){
	    			falsifyPostRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
	    		} else if(currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("DO")) || currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("ODO"))) {
	    			if(hasCommonAttribute(curRuleAttr,ruleAttrMap.get(r.getId().toString()))) {
	    				falsifyPostRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
	    		    	
	    			} else {
	    				dominantRules.add(rule);
	    				dominantIndeterminateFlag = true;
	    			}
	    		}
	    	}	
		 }
		 
		 if(dominantIndeterminateFlag) {
			 List<Rule> allRules = XACMLElementUtil.getRuleFromPolicy(PolicyLoader.loadPolicy(doc));
			 if(r.getEffect() == 0 && currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("DO"))) {
				 for(Rule rule:allRules) {
					 if(rule.getEffect()==0 && !rule.getId().equals(r.getId())) {
						falsifyPostRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
			    	 }
				 }
			 }
			 
			 if(r.getEffect() == 1 && currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("PO"))) {
				 for(Rule rule:allRules) {
					 if(rule.getEffect()==1) {
						falsifyPostRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
			    	 }
				 }
			 }
		 }
		 return falsifyPostRules.toString();
	}
	
	public String ruleReachability(Rule r, List<Rule> previousRules) {
		 StringBuffer falsifyPreviousRules = new StringBuffer();
		 for(Rule rule:previousRules){
			if(currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("FA"))) {
	    		falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
	    	} else if(rule.getEffect()==0) {
	    		if(currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("DUP"))){
	    			falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
	    		} else if(currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("PO")) || currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("OPO"))) {
	    			if(hasCommonAttribute(ruleAttrMap.get(rule.getId().toString()),ruleAttrMap.get(r.getId().toString()))) {
	    				falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
	    		    	
	    			} else {
	    				dominantRules.add(rule);
	    				dominantIndeterminateFlag = true;
	    			}
	    		}
	    	} else if(rule.getEffect()==1) {
	    		if(currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("PUD"))){
	    			falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
	    		} else if(currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("DO")) || currentPolicyRCA.equals(CombiningAlgorithmURI.map.get("ODO"))) {
	    			if(hasCommonAttribute(ruleAttrMap.get(rule.getId().toString()),ruleAttrMap.get(r.getId().toString()))) {
	    				falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
	    		    	
	    			} else {
	    				dominantRules.add(rule);
	    				dominantIndeterminateFlag = true;
	    			}
	    		}
	    	}	
		 }
		 return falsifyPreviousRules.toString();
	}
	
	public String getRuleTargetExpression(Element node, StringBuilder preExpression, List<Rule> previousRules) throws ParsingException{
	    Target target = XMLUtil.getTarget(node, policyMetaData);
	    Condition condition = XMLUtil.getCondition(node, policyMetaData);
	    StringBuffer ruleExpression = new StringBuffer();
	    Rule r = Rule.getInstance(node, policyMetaData, null);
	    List<Attr> curRuleAttr = new ArrayList<Attr>();

	    if(target != null){
		    if(targetFlag){
		    	
		    	ruleExpression.append(z3ExpressionHelper.getTrueTargetExpression(target,curRuleAttr) + System.lineSeparator());
		    }else{
		    	ruleExpression.append(z3ExpressionHelper.getFalseTargetExpression(target,curRuleAttr) + System.lineSeparator());
	
		    }
	    }
	    
		ruleAttrMap.put(r.getId().toString(),curRuleAttr);

	    
	    StringBuffer falsifyPreviousRules = new StringBuffer();
	    for(Rule rule:previousRules){
	    	if(falsifyRulesFlag==0) {
	    		falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());

	    	} else {
	    		if(ruleAttrMap.get(rule.getId().toString()).containsAll(ruleAttrMap.get(r.getId().toString()))){
				falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());

	    	}
	    	}

		}                
	    return preExpression.toString()+ruleExpression+falsifyPreviousRules;
	}
	
	
	public boolean hasCommonAttribute(List<Attr> l1,List<Attr> l2) {
		List<Attr> s1 = new ArrayList<Attr>();
		s1.addAll(l1);
		s1.retainAll(l2);
		if(s1.size()>0) {
			return true;
		}
		return false;
		
	}
	public String getRuleExpressionForTruthValuesWithPostRules(Element node, StringBuilder preExpression, List<Rule> previousRules) throws ParsingException{
		Rule r = Rule.getInstance(node, policyMetaData, null);
    	
		String expression =  getRuleExpressionForTruthValues(node, preExpression, previousRules);
		List<Rule> postRules = new ArrayList<Rule>();
		Node sibling = null;
		Node n = node;
		while(true){
			sibling = n.getNextSibling();
			if(sibling == null){
				break;
			} else{
				if(sibling.getNodeType() == Node.ELEMENT_NODE){
					if(XACMLElementUtil.isRule(sibling)){
						postRules.add(Rule.getInstance(sibling, policyMetaData, null));
					}
				} 
			}
			n = sibling;
		}
		StringBuffer falsifyPostRules = new StringBuffer();
	    for(Rule rule:postRules){
//	    	if((rule.getEffect()==0 && falsifyRulesFlag == 2) || (rule.getEffect()==1 && falsifyRulesFlag == 1)) {
//	    		continue;
//	    	}
	    	/*if(ruleAttrMap.get(rule.getId().toString()).containsAll(ruleAttrMap.get(r.getId().toString()))){
	    		falsifyPostRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());

	    	}*/

	    	//falsifyPostRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
		}
		
		return expression + falsifyPostRules;
	}
	
	public List<Rule> getPostRules(Node node) throws ParsingException{
		List<Rule> postRules = new ArrayList<Rule>();
		Node sibling = null;
		Node n = node;
		while(true){
			sibling = n.getNextSibling();
			if(sibling == null){
				break;
			} else{
				if(sibling.getNodeType() == Node.ELEMENT_NODE){
					if(XACMLElementUtil.isRule(sibling)){
						postRules.add(Rule.getInstance(sibling, policyMetaData, null));
					}
				} 
			}
			n = sibling;
		}
		return postRules;
	}
	protected  String IndCondition(Condition condition,String prefix) throws IOException{
		StringBuffer sb = new StringBuffer();
		ArrayList<Attr> temp = new ArrayList<Attr>();
		sb.append(True_Condition(condition, temp) + System.lineSeparator());
		sb.append(prefix);
		boolean sat = Z3StrUtil.processExpression(sb.toString(), z3ExpressionHelper);
		if (sat) {
			List<Attr> gAttrs = z3ExpressionHelper.getAttributeList();
			String request = RequestBuilder.buildIDRequest(gAttrs,temp);
			return request;
		}
		return null;
	}
	
	 public StringBuffer True_Condition(Condition condition, ArrayList<Attr> collector) {
			StringBuffer sb = new StringBuffer();
			sb.append(getConditionAttribute(condition, collector));
			sb.append("\n");
			return sb;
		}
	 
	 protected String getConditionAttribute(Condition condition, ArrayList<Attr> collector) {
			if (condition != null) {
				Expression expression = condition.getExpression();
				StringBuffer sb = new StringBuffer();
				if (expression instanceof Apply) {
					Apply apply = (Apply) expression;
					sb = z3ExpressionHelper.ApplyStatements(apply, "", sb,collector);
				}
				return sb.toString();
			}
			return "";
		}
	
}
