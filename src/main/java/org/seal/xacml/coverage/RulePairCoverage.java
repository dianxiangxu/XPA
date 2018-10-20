package org.seal.xacml.coverage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.seal.xacml.NameDirectory;
import org.seal.xacml.RequestGeneratorBase;
import org.seal.xacml.helpers.Z3StrExpressionHelper;
import org.seal.xacml.utils.XACMLElementUtil;
import org.seal.xacml.utils.RequestBuilder;
import org.seal.xacml.utils.XMLUtil;
import org.seal.xacml.utils.Z3StrUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.ParsingException;
import org.wso2.balana.PolicyMetaData;
import org.wso2.balana.Rule;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.xacml3.Target;
import org.xml.sax.SAXException;

/**
 * Created by roshanshrestha on 2/10/17.
 */
public class RulePairCoverage extends RequestGeneratorBase {
	
	public RulePairCoverage(String policyFilePath) throws ParsingException, IOException, SAXException, ParserConfigurationException{
		init(policyFilePath);
	}
	
	public List<String> generateTests(boolean permitDeny) throws ParsingException, IOException, SAXException, ParserConfigurationException{
	    StringBuilder preExpression = new StringBuilder();
        traverse( doc.getDocumentElement(), preExpression, permitDeny);
        return getRequests();
    }
	
   private void traverse(Element node, StringBuilder preExpression, boolean permitDeny) throws ParsingException, IOException {
	    boolean isPolicySet = XACMLElementUtil.isPolicySet(node);
		if (XACMLElementUtil.isPolicy(node) || isPolicySet) {
		    Node targetNode = XMLUtil.findInChildNodes(node, NameDirectory.TARGET);
		    Target target;
			if (targetNode != null) {
		        target = Target.getInstance(targetNode, policyMetaData);
		        if(target.getAnyOfSelections().size()>0){
		        	preExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
		        }
		    }
			if(isPolicySet) {
                NodeList children = node.getChildNodes();
		        for (int i = 0; i < children.getLength(); i++) {
		            Node child = children.item(i);
		            StringBuilder preExpressionCurrent = new StringBuilder(preExpression.toString());
		            if (child instanceof Element && XMLUtil.isTraversableElement(child)) {
		            	if(isPolicySet) {
		            		traverse((Element) child, preExpressionCurrent, permitDeny);
		            	} 
		            }
		        }
			} else {
        		List<Node> rules = new ArrayList<Node>();
        		NodeList childs = node.getChildNodes();
        		for(int k = 0; k < childs.getLength(); k++) {
        			Node c = childs.item(k);
        			if(XACMLElementUtil.isRule(c)){
        				rules.add(c);
        			}
        		}
        		StringBuilder preExpressionCurrent = new StringBuilder(preExpression.toString());
		           
        		coverRulePair(rules,preExpressionCurrent, permitDeny);
//        		for(String expression:ruleExpressions) {
//        			boolean sat = Z3StrUtil.processExpression(expression, z3ExpressionHelper);
//        			if (sat == true) {
//        			    addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
//        			}
//        		}
        	}
		}	
    }

	
	public void coverRulePair(List<Node> rules, StringBuilder preExpression,boolean permitDeny ) throws ParsingException,IOException{
		int N = rules.size();
		int n = N - 1;
		
		for(int i = 0; i < n;i++) {
			for(int j = i+1; j < N; j++) {
				StringBuilder falsifyOtherRules = new StringBuilder();
				Node n1 = rules.get(i);
				Node n2 = rules.get(j);
				if(permitDeny) {
					Rule r1 = Rule.getInstance(n1, policyMetaData, null);
					Rule r2 = Rule.getInstance(n2, policyMetaData, null);
					if(r1.getEffect()==r2.getEffect()) {
						continue;
					}
				}
			    StringBuilder pairExpressions = new StringBuilder(preExpression);
			    pairExpressions.append(getRuleExpression(n1)).append(System.lineSeparator());
			    pairExpressions.append(getRuleExpression(n2)).append(System.lineSeparator());
				for(int k = 0 ; k < N; k++){
			    	if(k!=i && k!=j) {
			    		Rule rule = Rule.getInstance(rules.get(k), policyMetaData, null);
			    		falsifyOtherRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
			    	}
			    }
				pairExpressions.append(falsifyOtherRules);
				boolean sat = Z3StrUtil.processExpression(pairExpressions.toString(), z3ExpressionHelper);
    			if (sat == true) {
    			    addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
    			}
				
			}
		}
		
	   	}
	
	private String getRuleExpression(Node node) throws ParsingException{
		Target target = XMLUtil.getTarget(node, policyMetaData);
		Condition condition = XMLUtil.getCondition(node, policyMetaData);
	
	    StringBuffer ruleExpression = new StringBuffer();
	    if(target != null){
		    	ruleExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
		}
	    if(condition != null){
	    		ruleExpression.append(z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator());
	    }
	    return ruleExpression.toString();
	}
	
}
