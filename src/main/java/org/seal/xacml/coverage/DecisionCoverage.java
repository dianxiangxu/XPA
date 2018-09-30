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
	protected AbstractPolicy policy;
	protected boolean error;
	protected boolean[][][] currentPolicyRulesCoverage;
	protected List<Rule> currentPolicyRules;
	protected int currentPolicyRuleIndex;
	protected String currentPolicyCA;
	protected boolean falsePolicyTarget;
	
	public DecisionCoverage(String policyFilePath,boolean error) throws ParsingException, IOException, SAXException, ParserConfigurationException{
		init(policyFilePath);
		this.policy = PolicyLoader.loadPolicy(new File(policyFilePath));
		this.error = error;
	}
	
	protected void traverse(Element node, StringBuilder preExpression,List<Rule> previousRules) throws IOException, ParsingException, SAXException, ParserConfigurationException {
		String name = DOMHelper.getLocalName(node);
		
	    if (rulePattern.matcher(name).matches()) {
	    	RuleBody rb = new RuleBody(node,previousRules,preExpression);
			
	    	if((rb.getTarget() == null)&& (rb.getCondition() == null)) {
    		    return;
			}
		    
			boolean sat;
			if(!isTrueTargetCovered() || !isTrueConditionCovered()) {
				String expresion = rb.getRuleCoverageExpression();
				sat = Z3StrUtil.processExpression(expresion, z3ExpressionHelper);
				if (sat){
					String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
					addRequest(req);
					updateCoverage(req);
				}
			}
			
			coverErrorNFalse(rb);
			
			
			previousRules.add(Rule.getInstance(node, policyMetaData, null));
		    
			currentPolicyRuleIndex++;
			return;
		}
		if (policyPattern.matcher(name).matches() || policysetPattern.matcher(name).matches()) {
			if(policyPattern.matcher(name).matches()){
		    	udpatePolicyMeta(node);
		    }
		    coverPolicyTarget(name, node, previousRules, preExpression);
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
	
	protected void coverPolicyTarget(String name, Node node, List<Rule> previousRules, StringBuilder preExpression) throws ParsingException, IOException, SAXException , ParserConfigurationException{
		Node targetNode = findInChildNodes(node, NameDirectory.TARGET);
	    previousRules = null;
	    if(policyPattern.matcher(name).matches()){
	    	previousRules = new ArrayList<Rule>();
	    	//udpatePolicyMeta(node);
	    }
	    
	    Target target = null;
	    if (targetNode != null) {
            target = Target.getInstance(targetNode, policyMetaData);
            if(target.getAnyOfSelections().size()>0){
	            	if(!falsePolicyTarget) {
	            	StringBuffer expresion = z3ExpressionHelper.getFalseTargetExpression(target);
	            	expresion.append(preExpression);
	            	boolean sat = Z3StrUtil.processExpression(expresion.toString(), z3ExpressionHelper);
	            	if (sat){
	            		addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
	            	}
            	}
            	if(error){
            		addRequest(IndTarget(target,preExpression.toString()));
            	}
            	preExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
            }
	    }
	    
	    NodeList children = node.getChildNodes();
	    
	    for (int i = 0; i < children.getLength(); i++) {
	        Node child = children.item(i);
	        StringBuilder preExpressionCurrent = new StringBuilder(preExpression.toString());
	        if (child instanceof Element && isTraversableElement(child)) {
	        	traverse((Element) child, preExpressionCurrent,previousRules);
	        }
	    }
	}
	
	protected void coverErrorNFalse(RuleBody rb) throws IOException {
		boolean sat = false;
		if(rb.getTarget()!=null){
			if(!isFalseTargetCovered()) {
				String falseTargetExpression = rb.getReachabilityExp() + rb.getFalseRuleTargetExpression() + rb.getFalseRuleConditionExpression();
				sat = Z3StrUtil.processExpression(falseTargetExpression, z3ExpressionHelper);
				if(sat) {
					String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
					addRequest(req);
					updateCoverage(req);
				}
			}
			
			if(error && !isErrorTargetCovered()){
				String req = IndTarget(rb.getTarget(), rb.getReachabilityExp() );
				if(req !=null) {
					if(isReachable(req)) {
						addRequest(req);
						updateCoverage(req);
					}
				}
			}
		}
		
		if(rb.getCondition()!=null){
			String targetExp = "";
			if(rb.getTarget()!=null) {
				targetExp = rb.getRuleTargetExpression();
			}
			if(!isFalseConditionCovered()) {
				
				String falseConditionExpression = rb.getReachabilityExp() + targetExp + rb.getFalseRuleConditionExpression();
				sat = Z3StrUtil.processExpression(falseConditionExpression, z3ExpressionHelper);
				if(sat) {
					String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
					addRequest(req);
					updateCoverage(req);
				}
			}
			
			if(error && !isErrorConditionCovered()){
				String req = IndCondition(rb.getCondition(), rb.getReachabilityExp() + targetExp );
				if(req !=null) {
					if(isReachable(req)) {
						addRequest(req);
						updateCoverage(req);
					}
				}
			}
		}
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
		if(currentPolicyCA.equals(CombiningAlgorithmURI.map.get("FA"))) {
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
    
    protected Attr invalidAttr() {
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
   
    protected String IndTarget(Target target,String prefix) throws IOException{
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
    
    protected String randomAttribute() {
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
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

	protected Attr getDifferentAttribute(List<Attr> globle,List<Attr> local) {
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
	
	protected Node findInChildNodes(Node parent, String localName) {
        List<Node> childNodes = XMLUtil.getChildNodeList(parent);
        for (Node child : childNodes) {
            if (localName.equals(child.getLocalName())) {
                return child;
            }
        }
        return null;
    }

	protected boolean isTraversableElement(Node e){
		if(rulePattern.matcher(e.getLocalName()).matches()||policysetPattern.matcher(e.getLocalName()).matches() || policyPattern.matcher(e.getLocalName()).matches()){
			return true;
		} else{
			return false;
		}
	}
	
	protected void udpatePolicyMeta(Node node) throws ParsingException {
		Policy p = Policy.getInstance(node);
		currentPolicyRules = XACMLElementUtil.getRuleFromPolicy(p);
    	currentPolicyRulesCoverage = new boolean[currentPolicyRules.size()][2][3];
    	currentPolicyRuleIndex = 0;
    	currentPolicyCA = p.getCombiningAlg().getIdentifier().toString();
	}
	
	protected class RuleBody{
		private Target target;
		
		private Condition condition;
		private String reachabilityExp;
		
		public RuleBody(Node node, List<Rule> previousRules, StringBuilder preExpression) throws ParsingException {
			List<Attr> curRuleAttr = new ArrayList<Attr>();
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
		    
		    updateMeta(preExpression, previousRules);
		    
		   
		    
		}
		
		public RuleBody(Rule rule, List<Rule> previousRules, StringBuilder preExpression) throws ParsingException {
		    target = (Target)rule.getTarget();
		    condition = (Condition)rule.getCondition();
		    
		    updateMeta(preExpression, previousRules);
		    
		   
		    
		}
		protected void updateMeta(StringBuilder preExpression, List<Rule> previousRules) {
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
			    if(preExpression !=null) {
			    	falsifyPreviousRules.append(preExpression + System.lineSeparator());
			    }
			    if(previousRules !=null) {
				    for(Rule rule:previousRules){
				    	if(shouldFalisfy(rule.getEffect())) {
				    		falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule)+ System.lineSeparator());
					 	}
				    }
			    }
			    reachabilityExp = falsifyPreviousRules.toString();
		}
		public Target getTarget() {
			return target;
		}

		public void setTarget(Target target) {
			this.target = target;
		}

		public Condition getCondition() {
			return condition;
		}

		public void setCondition(Condition condition) {
			this.condition = condition;
		}

		public String getReachabilityExp() {
			return reachabilityExp;
		}

		public void setReachabilityExp(String reachabilityExp) {
			this.reachabilityExp = reachabilityExp;
		}
		
		public String getRuleExpression() {
			StringBuffer ruleExpression = new StringBuffer();
			ruleExpression.append(getRuleTargetExpression());
			ruleExpression.append(getRuleConditionExpression());
			return ruleExpression.toString();
		}
		
		public String getRuleTargetExpression() {
			StringBuffer ruleTExpression = new StringBuffer();
			ruleTExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
			return ruleTExpression.toString();
		}
		
		public String getRuleConditionExpression() {
			StringBuffer ruleCExpression = new StringBuffer();
			ruleCExpression.append(z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator());
			return ruleCExpression.toString();
		}
		
		public String getFalseRuleTargetExpression() {
			StringBuffer ruleTExpression = new StringBuffer();
			ruleTExpression.append(z3ExpressionHelper.getFalseTargetExpression(target) + System.lineSeparator());
			return ruleTExpression.toString();
		}
		
		public String getFalseRuleConditionExpression() {
			StringBuffer ruleCExpression = new StringBuffer();
			ruleCExpression.append(z3ExpressionHelper.getFalseConditionExpression(condition) + System.lineSeparator());
			return ruleCExpression.toString();
		}
		
		public String getRuleCoverageExpression() {
			StringBuffer ruleExpression = new StringBuffer();
			ruleExpression.append(reachabilityExp);
			ruleExpression.append(getRuleExpression());
			return ruleExpression.toString();
		}
	}
}
