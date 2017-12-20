package org.seal.xacml.coverage;
import static org.seal.xacml.policyUtils.XpathSolver.policyPattern;
import static org.seal.xacml.policyUtils.XpathSolver.policysetPattern;
import static org.seal.xacml.policyUtils.XpathSolver.rulePattern;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.seal.xacml.Attr;
import org.seal.xacml.NameDirectory;
import org.seal.xacml.RequestGeneratorBase;
import org.seal.xacml.policyUtils.PolicyLoader;
import org.seal.xacml.utils.RequestBuilder;
import org.seal.xacml.utils.XMLUtil;
import org.seal.xacml.utils.Z3StrUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.DOMHelper;
import org.wso2.balana.ParsingException;
import org.wso2.balana.Rule;
import org.wso2.balana.TargetMatch;
import org.wso2.balana.attr.xacml3.AttributeDesignator;
import org.wso2.balana.cond.Apply;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.cond.Expression;
import org.wso2.balana.xacml3.AllOfSelection;
import org.wso2.balana.xacml3.AnyOfSelection;
import org.wso2.balana.xacml3.Target;
import org.xml.sax.SAXException;

public class MCDC extends RequestGeneratorBase{
	private AbstractPolicy policy;
	private boolean error;
	
	public MCDC(String policyFilePath,boolean error) throws ParsingException, IOException, SAXException, ParserConfigurationException{
		init(policyFilePath);
		this.policy = PolicyLoader.loadPolicy(new File(policyFilePath));
		this.error = error;
	}
	
	private void traverse(Element node, StringBuilder preExpression,List<Rule> previousRules) throws IOException, ParsingException {
		String name = DOMHelper.getLocalName(node);
		Target target = null;
		Condition condition = null;
		if (rulePattern.matcher(name).matches()) {
		    Node targetNode = findInChildNodes(node, NameDirectory.TARGET);
		    if (!XMLUtil.isEmptyNode(targetNode)) {
		        target = Target.getInstance(targetNode, policyMetaData);
		    }
		    Node conditionNode = findInChildNodes(node, NameDirectory.CONDITION);
		    if (!XMLUtil.isEmptyNode(conditionNode)) {
		        condition = Condition.getInstance(conditionNode, policyMetaData, null);
		    }
		    if(condition==null && target==null){
		    	return;
		    }
		    
		    StringBuffer falsifyPreviousRules = new StringBuffer();
		    for(Rule rule:previousRules){
		    	falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule)+ System.lineSeparator());
		    }
			StringBuffer ruleTargetExpression = new StringBuffer();
			StringBuffer ruleNotConditionExpression = new StringBuffer();
			StringBuffer errorConditionExpression = new StringBuffer();
			boolean sat = false;
			ruleTargetExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
			if(target!=null){
				z3ExpressionHelper.getTrueTargetExpression(target);
				String falseExpression = preExpression.toString()+z3ExpressionHelper.getFalseTargetExpression(target) + System.lineSeparator() + falsifyPreviousRules;
				sat = Z3StrUtil.processExpression(falseExpression, z3ExpressionHelper);
			    	if (sat) {
					    addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
					}
			    if(error){
			    	IndTarget(target,preExpression.toString() + falsifyPreviousRules);
			    }
				ruleNotConditionExpression.append(z3ExpressionHelper.getTrueTargetExpression(target));
				errorConditionExpression.append(z3ExpressionHelper.getTrueTargetExpression(target));
			}
			StringBuffer ruleExpression = new StringBuffer();
			ruleExpression.append(ruleTargetExpression);
			ruleExpression.append(z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator());
			ruleNotConditionExpression.append(z3ExpressionHelper.getFalseConditionExpression(condition));
			//String notExpresion = preExpression.toString() +  ruleNotConditionExpression + System.lineSeparator() + falsifyPreviousRules;
			List<String> ruleExprs = getMCDCExpressions(preExpression.toString() + ruleExpression.toString());
			List<String> expressions = new ArrayList<String>();
			for(String e:ruleExprs) {
				String expresion = (e + falsifyPreviousRules);
				sat = Z3StrUtil.processExpression(expresion, z3ExpressionHelper);
				if (sat){
					addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
				}
			}
			
			if(condition!=null){
				/*sat = Z3StrUtil.processExpression(notExpresion, z3ExpressionHelper);
			    if (sat) {
			    	addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
			    }*/
			    if(error){
			    	IndCondition(condition, preExpression.toString() + System.lineSeparator() +ruleTargetExpression + System.lineSeparator() + falsifyPreviousRules);
			    }
			}
			previousRules.add(Rule.getInstance(node, policyMetaData, null));
			return;
		}
		if (policyPattern.matcher(name).matches() || policysetPattern.matcher(name).matches()) {
		    Node targetNode = findInChildNodes(node, NameDirectory.TARGET);
		    if (targetNode != null) {
	            target = Target.getInstance(targetNode, policyMetaData);
	            List<AnyOfSelection> anyOfSelections = target.getAnyOfSelections();
	            if(anyOfSelections.size() > 0){
	            	StringBuffer expresion = z3ExpressionHelper.getFalseTargetExpression(target);
	            	expresion.append(preExpression);
	            	List<String> tExprs = getMCDCExpressions(expresion.toString());
	    			List<String> expressions = new ArrayList<String>();
	    			boolean sat = false; 
	    			for(String e:tExprs) {
	    				sat = Z3StrUtil.processExpression(e, z3ExpressionHelper);
	    				if (sat){
	    					addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
	    				}
	    			}
	            	if(error){
	            		IndTarget(target,preExpression.toString());
	            	}
	            	preExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
	            }
		    }
		    NodeList children = node.getChildNodes();
		    previousRules = null;
		    if(policyPattern.matcher(name).matches()){
		    	previousRules = new ArrayList<Rule>();
		    }
		    for (int i = 0; i < children.getLength(); i++) {
		        Node child = children.item(i);
		        StringBuilder preExpressionCurrent = new StringBuilder(preExpression.toString());
		        if (child instanceof Element && isTraversableElement(child)) {
		        	traverse((Element) child, preExpressionCurrent,previousRules);
		        }
		    }
		}
    }
	
	public List<String> getMCDCExpressions(String expression){
		
		List<String> aExprs = new ArrayList<String>();
		List<Integer> lIndex = new ArrayList<Integer>();
		List<Integer> rIndex = new ArrayList<Integer>();
		
		int l=0;
		int s = 0;
		
		for(int i = 0; i < expression.length(); i++) {
			if(expression.charAt(i)=='(') {
				l = i;
				s = 1;
			} else if(expression.charAt(i)=='>' || expression.charAt(i)=='<' || expression.charAt(i)=='=') {
				if(s == 1) {
					s = 2;
				}
			} else if(expression.charAt(i)==')'){
				if(s == 2) {
					s = 0;
					String aExpr = expression.substring(l, i+1);
					lIndex.add(l);
					rIndex.add(i+1);
					aExprs.add(aExpr);
				}
			}
		}
		List<String> aNExprs = new ArrayList<String>();
		for(String e:aExprs) {
			aNExprs.add("(not " + e + ")");
		}
		List<String> expressions = new ArrayList<String>();
		expressions.add(expression);
		for(int i = 0; i < aExprs.size();i++) {
			String expr = expression.substring(0,(int)lIndex.get(i)) + aNExprs.get(i).toString() + expression.substring((int)rIndex.get(i));
			expressions.add(expr);
		}
		String nExpression = expression.substring(0);
		List<Integer> nlIndex = new ArrayList<Integer>();
		List<Integer> nrIndex = new ArrayList<Integer>();
		int offset = 0;
		for(int i = 0; i < aExprs.size();i++) {
			 int lF = lIndex.get(i)+offset;
			 nExpression = nExpression.substring(0,lF) + aNExprs.get(i).toString() + nExpression.substring((int)rIndex.get(i)+offset);
			 nlIndex.add(lF);
			 nrIndex.add(lF + aNExprs.get(i).toString().length());
			 offset += 6;
				
		}
		expressions.add(nExpression);
		for(int i = 0; i < aExprs.size();i++) {
			String expr = nExpression.substring(0,(int)nlIndex.get(i))  + aExprs.get(i).toString() +" "+ nExpression.substring((int)nrIndex.get(i));
			expressions.add(expr);
		}
		return expressions;
		
	}
	
	

	public List<String> generateTests(){
		StringBuilder preExpression = new StringBuilder();
	    try{
	    	traverse( doc.getDocumentElement(), preExpression,null);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		return getRequests();
	}
	 
	
	public String getTargetAttribute(Target target, ArrayList<Attr> collector) {
		StringBuffer sb = new StringBuffer();
		if (target != null) {
			for (AnyOfSelection anyofselection : target.getAnyOfSelections()) {
				StringBuilder orBuilder = new StringBuilder();
				for (AllOfSelection allof : anyofselection.getAllOfSelections()) {
					StringBuilder allBuilder = new StringBuilder();
					for (TargetMatch match : allof.getMatches()) {
						if (match.getEval() instanceof AttributeDesignator) {
							AttributeDesignator attribute = (AttributeDesignator) match.getEval();
							allBuilder.append(" (" + z3ExpressionHelper.getOperator(match.getMatchFunction().encode()) + " " + z3ExpressionHelper.getName(attribute)+ " ");
							if (attribute.getType().toString().contains("string")) {
								String value = match.getAttrValue().encode();
								value = value.replaceAll("\n", "");
								value = value.trim();
								allBuilder.append("\"" + value + "\")");
							}
							if (attribute.getType().toString().contains("integer")) {
								String value = match.getAttrValue().encode();
								value = value.replaceAll("\n", "");
								value.trim();
								value = value.trim();
								allBuilder.append(value + ")");
							}
							z3ExpressionHelper.getType(attribute);
						}
					}
					allBuilder.insert(0, " (and");
					allBuilder.append(")");
					orBuilder.append(allBuilder);
				}
				orBuilder.insert(0, " (or ");
				orBuilder.append(")");
				sb.append(orBuilder);
			}
			sb.insert(0, "(and ");
			sb.append(")");
			return sb.toString();
		}
		return "";
	}
    
    private Attr invalidAttr() {
		Attr myattr = new Attr(randomAttribute(), randomAttribute(),"http://www.w3.org/2001/XMLSchema#string");
		myattr.addValue("Indeterminate");
		return myattr;
	}

    public boolean isDefaultRule(Rule rule) {
		if (rule.getCondition() == null && rule.getTarget() == null) {
			return true;
		} else {
			return false;
		}
	}
   
    private boolean IndTarget(Target target,String prefix) throws IOException{
		StringBuffer sb = new StringBuffer();
		ArrayList<Attr> temp = new ArrayList<Attr>();
		if (policy.getCombiningAlg().getIdentifier().toString().equals("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable")) {

			sb.append(True_Target(target, temp) + "\n");
			Attr unique = getDifferentAttribute(z3ExpressionHelper.getAttributeList(), temp);
			if (unique == null) {
				return false;
			}
			temp.add(invalidAttr());
			mergeAttribute(temp, z3ExpressionHelper.getAttributeList());
			temp.remove(unique);
			sb.append(prefix);
		} else {
			sb.append(True_Target(target, temp) + "\n");
			temp.add(invalidAttr());
			mergeAttribute(temp, z3ExpressionHelper.getAttributeList());
			temp.remove(0);
			sb.append(prefix);
		}
		boolean sat = Z3StrUtil.processExpression(sb.toString(), z3ExpressionHelper);
		if (sat) {
			String request = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
			addRequest(request);
			return true;
		}
		return false;
	}

    private  boolean IndCondition(Condition condition,String prefix) throws IOException{
		StringBuffer sb = new StringBuffer();
		ArrayList<Attr> temp = new ArrayList<Attr>();
		if (policy.getCombiningAlg().getIdentifier().toString().equals("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable")) {
			sb.append(True_Condition(condition, temp) + "\n");
			Attr unique = getDifferentAttribute(z3ExpressionHelper.getAttributeList(), temp);
			if (unique == null) {
				return false;
			}
			temp.add(invalidAttr());
			mergeAttribute(temp, z3ExpressionHelper.getAttributeList());
			temp.remove(unique);
			sb.append(prefix);
		} else {
			sb.append(True_Condition(condition, temp) + "\n");
			temp.add(invalidAttr());
			mergeAttribute(temp, z3ExpressionHelper.getAttributeList());
			temp.remove(0);
			sb.append(prefix);
		}
		boolean sat = Z3StrUtil.processExpression(sb.toString(), z3ExpressionHelper);
		if (sat) {
			String request = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
			addRequest(request);
			return true;
		}
		return false;
	}
  
    public StringBuffer True_Target(Target target, ArrayList<Attr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getTargetAttribute(target, collector));
		sb.append("\n");
		return sb;
	}
   
    public StringBuffer False_Condition(Condition condition, ArrayList<Attr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getConditionAttribute(condition, collector));
		String[] lines = sb.toString().split("\n");
		StringBuffer output = new StringBuffer();
		for (String s : lines) {
			if (s.isEmpty()) {
				continue;
			} else {
				StringBuffer subsb = new StringBuffer();
				subsb.append("(not ");
				subsb.append(s);
				subsb.append(")");
				output.append(subsb);
			}
		}
		return output;
	}

    public StringBuffer True_Condition(Condition condition, ArrayList<Attr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getConditionAttribute(condition, collector));
		sb.append("\n");
		return sb;
	}
    
    private String randomAttribute() {
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}
	
	private String getConditionAttribute(Condition condition, ArrayList<Attr> collector) {
		if (condition != null) {
			Expression expression = condition.getExpression();
			StringBuffer sb = new StringBuffer();
			if (expression instanceof Apply) {
				Apply apply = (Apply) expression;
				sb = z3ExpressionHelper.ApplyStatements(apply, "", sb);
			}
			return sb.toString();
		}
		return "";
	}

	private Attr getDifferentAttribute(List<Attr> globle,List<Attr> local) {
		out: for (Attr l : local) {
			for (Attr g : globle) {
				if (g.getName().equals(l.getName())) {
					continue out;
				}
			}
			return l;
		}
		return null;
	}
	
	public void mergeAttribute(List<Attr> Globalattributes,List<Attr> Localattributes) {
		for (Attr localmyattr : Localattributes) {
			boolean found = false;
			for (Attr globalmyattr : Globalattributes) {
				if (localmyattr.getName().equals(globalmyattr.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				Globalattributes.add(localmyattr);
			}
		}
	}
	
	
	private Node findInChildNodes(Node parent, String localName) {
        List<Node> childNodes = XMLUtil.getChildNodeList(parent);
        for (Node child : childNodes) {
            if (localName.equals(child.getLocalName())) {
                return child;
            }
        }
        return null;
    }

	private boolean isTraversableElement(Node e){
		if(rulePattern.matcher(e.getLocalName()).matches()||policysetPattern.matcher(e.getLocalName()).matches() || policyPattern.matcher(e.getLocalName()).matches()){
			return true;
		} else{
			return false;
		}
	}
}
