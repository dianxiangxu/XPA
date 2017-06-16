package org.seal.xacml.coverage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.seal.policyUtils.PolicyLoader;
import org.seal.xacml.NameDirectory;
import org.seal.xacml.helpers.Z3StrExpressionHelper;
import org.seal.xacml.utils.PolicyElementUtil;
import org.seal.xacml.utils.RequestBuilder;
import org.seal.xacml.utils.XMLUtil;
import org.seal.xacml.utils.Z3StrUtil;
import org.w3c.dom.Document;
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
public class RuleCoverage {
	private static String policyFilePath;
	private static Document doc;
	private static PolicyMetaData policyMetaData;
	private static List<String> requests;
	private static Z3StrExpressionHelper z3ExpressionHelper;
	
	public static List<String> generateRequests(String policyFilePath) throws ParsingException, IOException, SAXException, ParserConfigurationException{
        init(policyFilePath);
        StringBuilder preExpression = new StringBuilder();
        List<String> paths = new ArrayList<String>();
        traverse( doc.getDocumentElement(), paths, preExpression,null);
        return requests;
    }
    
    private static void init(String path) throws IOException, SAXException, ParserConfigurationException, ParsingException{
		policyFilePath = path;
		doc = PolicyLoader.getDocument(new FileInputStream(policyFilePath));
	    policyMetaData = PolicyLoader.loadPolicy(doc).getMetaData();
		requests = new ArrayList<String>();
		z3ExpressionHelper = new Z3StrExpressionHelper();
    }
   
	private static void traverse(Element node, List<String> paths, StringBuilder preExpression, List<Rule> previousRules) throws ParsingException, IOException {
	    if (PolicyElementUtil.isRule(node)) {
	        String expresion = getRuleCoverageExpression(node,paths,preExpression,previousRules);
			boolean sat = Z3StrUtil.processExpression(expresion, z3ExpressionHelper);
			if (sat == true) {
			    requests.add(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
			}
		    previousRules.add(Rule.getInstance(node, policyMetaData, null));
		    return;
		}
		
		if (PolicyElementUtil.isPolicy(node) || PolicyElementUtil.isPolicySet(node)) {
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
	        if(PolicyElementUtil.isPolicy(node)){
	        	previousRules = new ArrayList<Rule>();
	        }
	        for (int i = 0; i < children.getLength(); i++) {
	            Node child = children.item(i);
	            StringBuilder preExpressionCurrent = new StringBuilder(preExpression.toString());
	            if (child instanceof Element && XMLUtil.isTraversableElement(child)) {
	            	traverse((Element) child, paths, preExpressionCurrent, previousRules);
	            }
	        }
	        if(paths.size()>0){
	        	paths.remove(paths.size() - 1);
	        }
		}	
    }

	public static String getRuleCoverageExpression (Element node, List<String> paths, StringBuilder preExpression, List<Rule> previousRules) throws ParsingException{
		Node targetNode = XMLUtil.findInChildNodes(node, NameDirectory.TARGET);
		Target target = null;
	    Condition condition = null;
	    
		if (!XMLUtil.isEmptyNode(targetNode)) {
		    target = Target.getInstance(targetNode, policyMetaData);
		    paths.add(target.encode());
		}
		Node conditionNode = XMLUtil.findInChildNodes(node, NameDirectory.CONDITION);
		    if (!XMLUtil.isEmptyNode(conditionNode)) {
	        condition = Condition.getInstance(conditionNode, policyMetaData, null);
	        paths.add(condition.encode());
	    }
	    if (!XMLUtil.isEmptyNode(targetNode)) {
	    	paths.remove(paths.size() - 1);
	    }
	    if (!XMLUtil.isEmptyNode(conditionNode)) {
	        paths.remove(paths.size() - 1);
	    }
	    StringBuffer ruleExpression = new StringBuffer();
	    ruleExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
	    ruleExpression.append(z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator());
	    
	    StringBuffer falsifyPreviousRules = new StringBuffer();
	    for(Rule rule:previousRules){
			falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
		}                
	    return preExpression.toString()+ruleExpression+falsifyPreviousRules;
	}
}
