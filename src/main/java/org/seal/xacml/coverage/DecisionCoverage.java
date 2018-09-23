package org.seal.xacml.coverage;
import static org.seal.xacml.policyUtils.XpathSolver.policyPattern;
import static org.seal.xacml.policyUtils.XpathSolver.policysetPattern;
import static org.seal.xacml.policyUtils.XpathSolver.rulePattern;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.DOMHelper;
import org.wso2.balana.ParsingException;
import org.wso2.balana.Policy;
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

public class DecisionCoverage extends RequestGeneratorBase{
	private AbstractPolicy policy;
	private boolean error;
	private boolean[][][] currentPolicyRulesCoverage;
	private List<Rule> currentPolicyRules;
	private int currentPolicyRuleIndex;
	private String currentPolicyCA;
	
	public DecisionCoverage(String policyFilePath,boolean error) throws ParsingException, IOException, SAXException, ParserConfigurationException{
		init(policyFilePath);
		this.policy = PolicyLoader.loadPolicy(new File(policyFilePath));
		this.error = error;
	}
	
	private void traverse(Element node, StringBuilder preExpression,List<Rule> previousRules) throws IOException, ParsingException {
		String name = DOMHelper.getLocalName(node);
		Target target = null;
		Condition condition = null;


	    List<Attr> curRuleAttr = new ArrayList<Attr>();

		if (rulePattern.matcher(name).matches()) {
		    Rule r = Rule.getInstance(node, policyMetaData, null);
		    Node targetNode = findInChildNodes(node, NameDirectory.TARGET);
		    if (!XMLUtil.isEmptyNode(targetNode)) {
		        target = Target.getInstance(targetNode, policyMetaData);
		        z3ExpressionHelper.getTrueTargetExpression(target,curRuleAttr);
		    }
		    
		    Node conditionNode = findInChildNodes(node, NameDirectory.CONDITION);
		    if (!XMLUtil.isEmptyNode(conditionNode)) {
		        condition = Condition.getInstance(conditionNode, policyMetaData, null);
		        z3ExpressionHelper.getTrueConditionExpression(condition,curRuleAttr);
		    }
		    
		    if((target == null)&& (condition == null)) {
    		    return;
			}
		    
		    if(target==null) {
		    	currentPolicyRulesCoverage[currentPolicyRuleIndex][0][0]= true;
		    	currentPolicyRulesCoverage[currentPolicyRuleIndex][0][1]= true;
		    	currentPolicyRulesCoverage[currentPolicyRuleIndex][0][2]= true;
		    }
		    
		    if(condition==null) {
		    	currentPolicyRulesCoverage[currentPolicyRuleIndex][1][0]= true;
		    	currentPolicyRulesCoverage[currentPolicyRuleIndex][1][1]= true;
		    	currentPolicyRulesCoverage[currentPolicyRuleIndex][1][2]= true;
		    }
		    
		    StringBuffer falsifyPreviousRules = new StringBuffer();
		    for(Rule rule:previousRules){
		    	if(shouldFalisfy(rule.getEffect())) {
		    		falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule)+ System.lineSeparator());
			 	}
		    }
		    
			boolean sat;
			if(!isTrueTargetCovered() || !isTrueConditionCovered()) {
				StringBuffer ruleExpression = new StringBuffer();
				ruleExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
				ruleExpression.append(z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator());
				String expresion = preExpression.toString() + ruleExpression + System.lineSeparator() + falsifyPreviousRules;
				sat = Z3StrUtil.processExpression(expresion, z3ExpressionHelper);
				if (sat){
					String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
					addRequest(req);
					updateCoverage(req);
				}
			}
			
			if(target!=null){
				if(!isFalseTargetCovered()) {
					String falseTargetExpression = preExpression.toString()+z3ExpressionHelper.getFalseTargetExpression(target) + System.lineSeparator() + falsifyPreviousRules;
					sat = Z3StrUtil.processExpression(falseTargetExpression, z3ExpressionHelper);
					if(sat) {
						String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
						addRequest(req);
						updateCoverage(req);
					}
				}
				
				if(error && !isErrorTargetCovered()){
						String req = IndTarget(target, preExpression.toString() + System.lineSeparator() + falsifyPreviousRules + System.lineSeparator() );
						if(req !=null) {
							if(isReachable(req)) {
								addRequest(req);
								updateCoverage(req);
							}
							
						}
					
				}
			}
			if(condition!=null){
				String targetExp = "";
				if(target!=null) {
					targetExp = z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator();
				}
				if(!isFalseConditionCovered()) {
					
					String falseConditionExpression = preExpression.toString()+ targetExp +z3ExpressionHelper.getFalseConditionExpression(condition) + System.lineSeparator() + falsifyPreviousRules;
					sat = Z3StrUtil.processExpression(falseConditionExpression, z3ExpressionHelper);
					if(sat) {
						String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
						addRequest(req);
						updateCoverage(req);
					}
				}
				
				if(error && !isErrorConditionCovered()){
						String req = IndCondition(condition, preExpression.toString() + targetExp + System.lineSeparator() + falsifyPreviousRules + System.lineSeparator() );
						if(req !=null) {
							if(isReachable(req)) {
								addRequest(req);
								updateCoverage(req);
							}
						}
					
				}
				
			}
			previousRules.add(r);
			currentPolicyRuleIndex++;
			return;
		}
		if (policyPattern.matcher(name).matches() || policysetPattern.matcher(name).matches()) {
		    Node targetNode = findInChildNodes(node, NameDirectory.TARGET);
		    Policy pol = Policy.getInstance(node);
		    currentPolicyCA = pol.getCombiningAlg().getIdentifier().toString();
        	
		    if (targetNode != null) {
	            target = Target.getInstance(targetNode, policyMetaData);
	            if(target.getAnyOfSelections().size()>0){
	            	StringBuffer expresion = z3ExpressionHelper.getFalseTargetExpression(target);
	            	expresion.append(preExpression);
	            	boolean sat = Z3StrUtil.processExpression(expresion.toString(), z3ExpressionHelper);
	            	if (sat){
	            		addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
	            	}
	            	if(error){
	            		addRequest(IndTarget(target,preExpression.toString()));
	            	}
	            	preExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
	            }
		    }
		    
		    NodeList children = node.getChildNodes();
		    previousRules = null;
		    if(policyPattern.matcher(name).matches()){
		    	previousRules = new ArrayList<Rule>();
		    	Policy p = Policy.getInstance(node);
				currentPolicyRules = XACMLElementUtil.getRuleFromPolicy(p);
		    	currentPolicyRulesCoverage = new boolean[currentPolicyRules.size()][2][3];
		    	currentPolicyRuleIndex = 0;
		    	currentPolicyCA = p.getCombiningAlg().getIdentifier().toString();
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

	public List<String> generateTests(){
		StringBuilder preExpression = new StringBuilder();
	    try{
	    	traverse( doc.getDocumentElement(), preExpression,null);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		return getRequests();
	}
	 
	protected boolean isFalseTargetCovered() {
		return currentPolicyRulesCoverage[currentPolicyRuleIndex][0][1];
	}
	
	protected boolean isTrueTargetCovered() {
		return currentPolicyRulesCoverage[currentPolicyRuleIndex][0][0];
	}
	
	protected boolean isErrorTargetCovered() {
		return currentPolicyRulesCoverage[currentPolicyRuleIndex][0][2];
	}
	
	protected boolean isFalseConditionCovered() {
		return currentPolicyRulesCoverage[currentPolicyRuleIndex][1][1];
	}
	
	protected boolean isTrueConditionCovered() {
		return currentPolicyRulesCoverage[currentPolicyRuleIndex][1][0];
	}
	
	protected boolean isErrorConditionCovered() {
		return currentPolicyRulesCoverage[currentPolicyRuleIndex][1][2];
	}
	
	protected void updateCoverage(String req) {
		for(int i = currentPolicyRuleIndex; i < currentPolicyRules.size(); i++) {
			Target t = (Target)currentPolicyRules.get(i).getTarget();
			int resT = 0;
			if (t !=null) {
				resT = XACMLElementUtil.TargetEvaluate(t, req);
				currentPolicyRulesCoverage[i][0][resT] = true;
			}
			Condition c = (Condition)currentPolicyRules.get(i).getCondition();
			int resC = 0;
			if(c != null && resT==0) {
				resC = XACMLElementUtil.ConditionEvaluate(c, req);
				currentPolicyRulesCoverage[i][1][resC] = true;
			}
			
			if(resT == 0 && resC==0) {
				if(currentPolicyCA.equals(CombiningAlgorithmURI.map.get("PO")) || currentPolicyCA.equals(CombiningAlgorithmURI.map.get("OPO")) ||currentPolicyCA.equals(CombiningAlgorithmURI.map.get("DUP"))) {
					if(currentPolicyRules.get(currentPolicyRuleIndex).getEffect() == 0) {
						break;
					}
				} 
		    	else if(currentPolicyCA.equals(CombiningAlgorithmURI.map.get("DO")) || currentPolicyCA.equals(CombiningAlgorithmURI.map.get("ODO")) ||currentPolicyCA.equals(CombiningAlgorithmURI.map.get("PUD"))) {
		    		if(currentPolicyRules.get(currentPolicyRuleIndex).getEffect() == 1) {
						break;
					}	
		    	} else {
		    		break;
		    	}
			}
			
			if((resT == 2 || resC==2) && currentPolicyCA.equals(CombiningAlgorithmURI.map.get("FA")) ) {
				break;
			}
		}

	}
	
	protected boolean isReachable(String req) {
		boolean flag = true;
		for(int i = 0; i < currentPolicyRuleIndex; i++) {
			Target t = (Target)currentPolicyRules.get(i).getTarget();
			int resT = 0;
			if (t !=null) {
				resT = XACMLElementUtil.TargetEvaluate(t, req);
				currentPolicyRulesCoverage[i][0][resT] = true;
			}
			Condition c = (Condition)currentPolicyRules.get(i).getCondition();
			int resC = 0;
			if(c != null && resT==0) {
				resC = XACMLElementUtil.ConditionEvaluate(c, req);
				currentPolicyRulesCoverage[i][1][resC] = true;
			}
			if(resT == 0 && resC == 0) {
				flag = false;
				break;
			}
			if(resT == 2 || resC == 2) {
				flag = false;
				break;
			}
		}
		return flag;
	}
	
	protected boolean shouldFalisfy(int effect) {
		if(currentPolicyCA.equals(CombiningAlgorithmURI.map.get("PO")) || currentPolicyCA.equals(CombiningAlgorithmURI.map.get("OPO")) ||currentPolicyCA.equals(CombiningAlgorithmURI.map.get("DUP"))) {
			if(effect == 0) {
				return true;
			} else {
				return false;
			}
		} 
    	else if(currentPolicyCA.equals(CombiningAlgorithmURI.map.get("DO")) || currentPolicyCA.equals(CombiningAlgorithmURI.map.get("ODO")) ||currentPolicyCA.equals(CombiningAlgorithmURI.map.get("PUD"))) {
    		if(effect == 1) {
				return true;
			} else {
				return false;
			}
    	} else {
    		return true;
    	}
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
							Attr myattr = new Attr(attribute);
							collector.add(myattr);
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
   
    private String IndTarget(Target target,String prefix) throws IOException{
		StringBuffer sb = new StringBuffer();
		ArrayList<Attr> temp = new ArrayList<Attr>();
		sb.append(False_Target(target, temp) + "\n");
		sb.append(prefix);

		boolean sat = Z3StrUtil.processExpression(sb.toString(), z3ExpressionHelper);
		if (sat) {
			List<Attr> gAttrs = z3ExpressionHelper.getAttributeList();
			String request = RequestBuilder.buildIDRequest(gAttrs,temp);
			return request;
		}
		return null;
	}

//    private  boolean IndCondition(Condition condition,String prefix) throws IOException{
//		StringBuffer sb = new StringBuffer();
//		ArrayList<Attr> temp = new ArrayList<Attr>();
//		if (policy.getCombiningAlg().getIdentifier().toString().equals("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable")) {
//			sb.append(True_Condition(condition, temp) + "\n");
//			Attr unique = getDifferentAttribute(z3ExpressionHelper.getAttributeList(), temp);
//			if (unique == null) {
//				return false;
//			}
//			temp.add(invalidAttr());
//			mergeAttribute(z3ExpressionHelper.getAttributeList(),temp);
//			temp.remove(unique);
//			sb.append(prefix);
//		} else {
//			sb.append(True_Condition(condition, temp) + "\n");
//			temp.add(invalidAttr());
//			mergeAttribute(z3ExpressionHelper.getAttributeList(),temp);
//			temp.remove(0);
//			sb.append(prefix);
//		}
//		boolean sat = Z3StrUtil.processExpression(sb.toString(), z3ExpressionHelper);
//		if (sat) {
//			
//			String request = RequestBuilder.buildIDRequest(z3ExpressionHelper.getAttributeList());
//			int res = XACMLElementUtil.ConditionEvaluate(condition, request);
//			if(res==2) {
//				addRequest(request);
//				
//			
//			return true;
//			}
//			
//		}
//		return false;
//	}
    
    private  String IndCondition(Condition condition,String prefix) throws IOException{
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
    
    public List<Attr> getTargetAttrList(Target target) throws IOException {
    	Z3StrUtil.processExpression(z3ExpressionHelper.getTrueTargetExpression(target).toString(), z3ExpressionHelper);
    	return z3ExpressionHelper.getAttributeList();
	} 
  
    public StringBuffer True_Target(Target target, ArrayList<Attr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getTargetAttribute(target, collector));
		sb.append("\n");
		return sb;
	}
    
    public StringBuffer False_Target(Target target, ArrayList<Attr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append("(not "+getTargetAttribute(target, collector) + ")");
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
				sb = z3ExpressionHelper.ApplyStatements(apply, "", sb,collector);
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
