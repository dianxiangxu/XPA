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

public class MCDC2 extends RequestGeneratorBase{
	private AbstractPolicy policy;
	private boolean error;
	private boolean[][][] currentPolicyRulesCoverage;
	private List<Rule> currentPolicyRules;
	private int currentPolicyRuleIndex;
	private String currentPolicyCA;
	private Map<String,List<Attr>> ruleAttrMap;
	private List<String> covered;
	private List<String> falseTargetRequests;

	
	public MCDC2(String policyFilePath,boolean error) throws ParsingException, IOException, SAXException, ParserConfigurationException{
		init(policyFilePath);
		this.policy = PolicyLoader.loadPolicy(new File(policyFilePath));
		this.error = error;
		ruleAttrMap = new HashMap<String,List<Attr>>();
		covered = new ArrayList<String>();
		this.falseTargetRequests = new ArrayList<String>();

	}
	
	private void traverse(Element node, StringBuilder preExpression,List<Rule> previousRules) throws IOException, ParsingException, ParserConfigurationException, SAXException {
		String name = DOMHelper.getLocalName(node);
		Target target = null;
		Condition condition = null;
	    List<Attr> curRuleAttr = new ArrayList<Attr>();

		if (rulePattern.matcher(name).matches()) {
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
		    
		    StringBuffer falsifyPreviousRules = new StringBuffer();
		   		    Rule lastRule = null;
		    for(Rule rule:previousRules){
		    	falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule)+ System.lineSeparator());
		   }

		    Rule r = Rule.getInstance(node, policyMetaData, null);
		    ruleAttrMap.put(r.getId().toString(),curRuleAttr);

		    		    
		    boolean f = false;

	    	if(node.getNextSibling().getNextSibling()==null) {
			    	StringBuffer b = new StringBuffer(falsifyPreviousRules.toString());

			    	if(target!=null) {
			    		b.append(z3ExpressionHelper.getFalseTargetExpression(target) + System.lineSeparator());
			    		
			    	}
			    	boolean sat = Z3StrUtil.processExpression(preExpression.toString() + System.lineSeparator() + b.toString(), z3ExpressionHelper);
	    			if (sat == true) {
	    				 String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
	    					for(int i = 0; i < currentPolicyRules.size(); i++) {
								Target t = (Target)currentPolicyRules.get(i).getTarget();
								if(t != null) {
									int res = XACMLElementUtil.TargetEvaluate(t, req);
									if(res==1) {
										currentPolicyRulesCoverage[i][0][1]	= true;
									} else {
										f = true;
									}
								}
							}
	    					if(f) {
	    						for(String re:falseTargetRequests) {
	    							addRequest(re);
	    						}
	    					} else {
	    						addRequest(req);
	    					}
	    	    }
			}
	    	 if((target == null)&& (condition == null)) {
	    			return;
			}
	    	     
			StringBuffer ruleTargetExpression = new StringBuffer();
			StringBuffer ruleNotConditionExpression = new StringBuffer();
			StringBuffer errorConditionExpression = new StringBuffer();
			boolean sat = false;
			ruleTargetExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
			if(target!=null){
				String falseExpression = preExpression.toString()+z3ExpressionHelper.getFalseTargetExpression(target) + System.lineSeparator() + falsifyPreviousRules;
				if(condition!=null) {
				    
					sat = Z3StrUtil.processExpression(falseExpression , z3ExpressionHelper);
				    if (sat) {
				    	String req =     RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
				    	falseTargetRequests.add(req);
				    }
				}else if ( node.getNextSibling().getNextSibling()==null) {
					StringBuffer b = new StringBuffer(falsifyPreviousRules);
					b.append(z3ExpressionHelper.getFalseTargetExpression(target) + System.lineSeparator());
		    		
			    	sat = Z3StrUtil.processExpression(preExpression.toString() + System.lineSeparator() + b.toString(), z3ExpressionHelper);
	    			if (sat == true) {
	    				String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
	    				if(f) {
	    					addRequest(req);
	    				} else {
	    					falseTargetRequests.add(req);
		    			}
					}
				}
				
				ruleNotConditionExpression.append(z3ExpressionHelper.getTrueTargetExpression(target));
				errorConditionExpression.append(z3ExpressionHelper.getTrueTargetExpression(target));
			}
			StringBuffer ruleExpression = new StringBuffer();
			ruleExpression.append(ruleTargetExpression);
			ruleExpression.append(z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator());
			ruleNotConditionExpression.append(z3ExpressionHelper.getFalseConditionExpression(condition));
			String expresion = preExpression.toString() + ruleExpression + System.lineSeparator() + falsifyPreviousRules;
			String notExpresion = preExpression.toString() +  ruleNotConditionExpression + System.lineSeparator() + falsifyPreviousRules;
			
			List<String> mcdcExps = getMCDCExpression(node,false);
			if(mcdcExps.size()<=1) {
				sat = Z3StrUtil.processExpression(expresion, z3ExpressionHelper);
				if (sat){
					addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
				}
			} else {
				for(String exp:mcdcExps) {
					String e = exp.replaceAll(System.lineSeparator(), "");
					String expression = e + System.lineSeparator() + z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator();
					expression += preExpression.toString() + System.lineSeparator() +  falsifyPreviousRules;
					sat = Z3StrUtil.processExpression(expression, z3ExpressionHelper);
					
					if (sat){
						addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
						
					}
				}
			}
			if(target!=null) {
						
				if(error){
					if(!currentPolicyRulesCoverage[currentPolicyRuleIndex][0][2]) {
						IndTarget(target, preExpression.toString() + System.lineSeparator() + falsifyPreviousRules + System.lineSeparator() );
						String req = getRequests().get(getRequests().size()-1);
						for(int i = currentPolicyRuleIndex; i < currentPolicyRules.size(); i++) {
							Target t = (Target)currentPolicyRules.get(i).getTarget();
							if(t != null) {
								int res = XACMLElementUtil.TargetEvaluate(t, req);
								if(res==2) {
									currentPolicyRulesCoverage[i][0][2]	= true;
								}
							}
						}
					}
				}
			}
			if(condition!=null){
				if(target!=null) {
					sat = Z3StrUtil.processExpression(notExpresion, z3ExpressionHelper);
					if (sat) {
				    	String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
				    	if(!currentPolicyRulesCoverage[currentPolicyRuleIndex][1][1]) {
				    		addRequest(req);
				    	}
				    	for(int i = currentPolicyRuleIndex; i < currentPolicyRules.size(); i++) {
							Target t = (Target)currentPolicyRules.get(i).getTarget();
							if(t != null) {
								int res = XACMLElementUtil.TargetEvaluate(t, req);
								if(res==0) {
									Condition c = (Condition)currentPolicyRules.get(i).getCondition();
									if(c!=null) {
									int resC = XACMLElementUtil.ConditionEvaluate(c, req);
									if(resC==1) {
										
										currentPolicyRulesCoverage[i][1][1]	= true;
									}
									}
								}
							}
						}
					}
				} else if ( node.getNextSibling().getNextSibling()==null) {
					sat = Z3StrUtil.processExpression(notExpresion, z3ExpressionHelper);
					if (sat) {
				    	String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
				    	if(!currentPolicyRulesCoverage[currentPolicyRuleIndex][1][1]) {
				    		addRequest(req);
				    	}
					}
				}
				if(error){
					if(!currentPolicyRulesCoverage[currentPolicyRuleIndex][1][2]) {
				
						String pref = preExpression.toString() + System.lineSeparator();
						if(target!=null) {
							pref += z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator();
						}
						IndCondition(condition, pref + falsifyPreviousRules + System.lineSeparator() );
						String req = getRequests().get(getRequests().size()-1);
						for(int i = currentPolicyRuleIndex; i < currentPolicyRules.size(); i++) {
							Condition c = (Condition)currentPolicyRules.get(i).getCondition();
							Target t = (Target)currentPolicyRules.get(i).getTarget();

							if(c != null) {
								int res = XACMLElementUtil.ConditionEvaluate(c, req);
								if (t !=null) {
									int resT = XACMLElementUtil.TargetEvaluate(t, req);
									if(resT==0) {
									if(res==2) {
										currentPolicyRulesCoverage[i][1][2]	= true;
									}
									}
								} else {
									if(res==2) {
										currentPolicyRulesCoverage[i][1][2]	= true;
									}
								}
							}
						}
					}
				}
			}
			previousRules.add(Rule.getInstance(node, policyMetaData, null));
			currentPolicyRuleIndex++;
			return;
		}
		if (policyPattern.matcher(name).matches() || policysetPattern.matcher(name).matches()) {
		    Node targetNode = findInChildNodes(node, NameDirectory.TARGET);
		    if (targetNode != null) {
	            target = Target.getInstance(targetNode, policyMetaData);
	            if(target.getAnyOfSelections().size()>0){
	            	StringBuffer expresion = z3ExpressionHelper.getFalseTargetExpression(target);
	            	expresion.append(preExpression);
	            	boolean sat = false;
	            	
	            	sat = Z3StrUtil.processExpression(expresion.toString(), z3ExpressionHelper);
	            	if (sat){
    					addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
    				}
	            	List<String> mcdcExps = getMCDCExpression(node,true);
	    			if(mcdcExps.size()<=1) {
	    				sat = Z3StrUtil.processExpression(expresion.toString(), z3ExpressionHelper);
	    				if (sat){
	    					addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
	    				}
	    			} else {
	    				for(String exp:mcdcExps) {
	    					if(exp.equals("R")) {
	    						continue;
	    					}
	    					String e = exp.replaceAll(System.lineSeparator(), "");
	    					String expression = e + System.lineSeparator() + z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator();
	    					expression += preExpression.toString() + System.lineSeparator() ;
	    					sat = Z3StrUtil.processExpression(expression, z3ExpressionHelper);
	    					
	    					if (sat){
	    						addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
	    						
	    					}
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
	 
	public List<String> getMCDCExpressions(String expression, boolean coveredFlag){
		
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
			if(coveredFlag) {
				String cExpr = expression.substring(0,(int)lIndex.get(i)) +  expression.substring((int)rIndex.get(i));
				boolean cvrd = false;
				for(String c:covered) {
					if(c.equals(cExpr)) {
						cvrd = true;
						break;
					}
				}
				if(!cvrd) {
				covered.add(cExpr);
				expressions.add(expr);
				}
			} else {
				expressions.add(expr);
			}
		}
		/*
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
		}*/
		for(String expr:expressions) {
			expr = expr.replaceAll(System.lineSeparator(), "");
			
		}
		return expressions;
		
	}

	public List<String> getMCDCExpression(Element node,boolean policyTargetFlag) throws IOException, ParsingException, ParserConfigurationException, SAXException{
		Node targetNode = XMLUtil.findInChildNodes(node, NameDirectory.TARGET);
		List<String> expressions = new ArrayList<String>();
	    
	    if (targetNode != null) {
	        List<Node> children = XMLUtil.getChildNodeList(targetNode);
	        int allOfCount = 0;
	        if(children.size()>1){
	            for (Node child : children) {
	            	if(child!=null && child.getLocalName() !=null && child.getLocalName().equals("AnyOf")){
	            		List<Node> childrenAllOf = XMLUtil.getChildNodeList(child);
	            		if(childrenAllOf.size() > 2){
	            			for(Node childAllOf:childrenAllOf){
	            				if(childAllOf.getLocalName() !=null && childAllOf.getLocalName().equals("AllOf")){
	            					allOfCount++;
	            				}
	            			}
	            			if(allOfCount > 0){
	            				if(allOfCount>1) {
	            					for(int i = 0; i < childrenAllOf.size(); i++){
		            					Node childAllOf = childrenAllOf.get(i);
		            					if(childAllOf.getLocalName() !=null && childAllOf.getLocalName().equals("AllOf")){
		            						child.removeChild(childAllOf);
		            						
		            					}
	            					}
	            					List<List<String>> allOfExpressions = new ArrayList<List<String>>();
	            					for(int i = 0; i < childrenAllOf.size(); i++){
		            					Node childAllOf = childrenAllOf.get(i);
		            					if(childAllOf.getLocalName() !=null && childAllOf.getLocalName().equals("AllOf")){
		            						child.appendChild(childAllOf);
		            						Target t = Target.getInstance(targetNode, policyMetaData);
		            						String tExpression = z3ExpressionHelper.getTrueTargetExpression(t).toString();
		            						allOfExpressions.add(getMCDCExpressions(tExpression, false));	
		            						child.removeChild(childAllOf);
		            					}
	            					}
	            					for(int i = 0; i < childrenAllOf.size(); i++){
		            					Node childAllOf = childrenAllOf.get(i);
		            					if(childAllOf.getLocalName() !=null && childAllOf.getLocalName().equals("AllOf")){
		            						child.appendChild(childAllOf);
		            						
		            					}
	            					}
	            					if(allOfExpressions.get(0).size()==2) {
	            						List<String> dAttrs = getDistinctAttrs(allOfExpressions);
	            						String e2 = null;
	            						String e3 = null;
	            						if(dAttrs.size()==1) {
	            							e2 = allOfExpressions.get(0).get(0); 
		            						e3 = allOfExpressions.get(1).get(0); 
		            					    
	            						}else {
	            							e2 = "(or " + allOfExpressions.get(0).get(0) + " " + allOfExpressions.get(1).get(1) +")";
		            						e3 = "(or " + allOfExpressions.get(0).get(1) + " " + allOfExpressions.get(1).get(0) +")";
		            					    	
	            						}
	            						if(policyTargetFlag) {	
	            							expressions.add("R");
	            						} else {
	            							expressions.add(e2);
	            					            							
	            						}
	            					    expressions.add(e3);
	            					} else {
	            						for(int i = 0; i < allOfExpressions.size();i++) {
	            									for(int j = 0; j< allOfExpressions.size();j++) {
	            										if(j!=i) {
	            											
	            	            							for(int k = 0 ; k< allOfExpressions.get(j).size()-1;k++ ) {
	            	            								String e = "(and " + allOfExpressions.get(i).get(1) + " " + allOfExpressions.get(j).get(k) +")";
	            			            						expressions.add(e);
	            	            							}
	            										}
	            									}
	            						}
	            							
	            					}
        						}else {
        							Target target = Target.getInstance(targetNode, policyMetaData);
        							String expression = z3ExpressionHelper.getTrueTargetExpression(target).toString();
        							if(target.getAnyOfSelections().get(0).getAllOfSelections().get(0).getMatches().size() > 1) {
        								expressions = getMCDCExpressions(expression,true);
        							} else
        							{
        								expressions.add(expression);
        							}
        						}
	            				
	            			}
	            		}
	            	}
	            }
	        }
	    } 
		

		return expressions;
	}

//	public List<String> getRuleMCDCExpression(Element node) throws IOException, ParsingException, ParserConfigurationException, SAXException{
//		List<Rule> postRules = new ArrayList<Rule>();
//		Node sibling = null;
//		Node n = node;
//		Node targetNode = XMLUtil.findInChildNodes(node, NameDirectory.TARGET);
//		Condition condition = XMLUtil.getCondition(node, policyMetaData);
//	    
//	    boolean flag = false;
//	    List<StringBuffer> ruleExpressions = new ArrayList<StringBuffer>();
//	    
//	    if (targetNode != null) {
//	        List<Node> children = XMLUtil.getChildNodeList(targetNode);
//	        int allOfCount = 0;
//	        if(children.size()>1){
//	            for (Node child : children) {
//	            	if(child!=null && child.getLocalName() !=null && child.getLocalName().equals("AnyOf")){
//	            		List<Node> childrenAllOf = XMLUtil.getChildNodeList(child);
//	            		if(childrenAllOf.size() > 2){
//	            			for(Node childAllOf:childrenAllOf){
//	            				if(childAllOf.getLocalName() !=null && childAllOf.getLocalName().equals("AllOf")){
//	            					allOfCount++;
//	            				}
//	            			}
//	            			if(allOfCount > 0){
//	            				for(int i = 0; i < childrenAllOf.size(); i++){
//	            					Node childAllOf = childrenAllOf.get(i);
//	            					if(childAllOf.getLocalName() !=null && childAllOf.getLocalName().equals("AllOf")){
//	            						child.removeChild(childAllOf);
//	            						Node nextChild = childAllOf.getNextSibling();
//	            						StringBuilder currentPreExpression = new StringBuilder();
//	            						if(allOfCount>1) {
//	            							Target target = Target.getInstance(targetNode, policyMetaData);
//	                						
//	            							currentPreExpression.append(z3ExpressionHelper.getFalseTargetExpression(target) +  System.lineSeparator());
//	            						}
//	            						StringBuilder currentPreExpressionMRoot = new StringBuilder(currentPreExpression.toString());
//	            						List<Node> childrenAllOfCopy = new ArrayList<Node>();
//	            						childrenAllOfCopy.addAll(XMLUtil.getChildNodeList(child));
//	            						
//	            						for(Node c:childrenAllOfCopy){
//	            							child.removeChild(c);
//	            						}
//	            						
//	            						child.appendChild(childAllOf);
//	            						Target targetTrue = Target.getInstance(targetNode, policyMetaData);
//	            						currentPreExpression.append(System.lineSeparator()).append(z3ExpressionHelper.getTrueTargetExpression(targetTrue));
//	            						List<Node> childrenMatchOf = XMLUtil.getChildNodeList(childAllOf);
//		        	            		
//	            						if( AllOfSelection.getInstance(childAllOf,policyMetaData).getMatches().size() > 1) {
//		            						for(int si = 0; si < childrenMatchOf.size(); si++){
//		            							Node childMatchOf = childrenMatchOf.get(si);
//		    	            					
//		    	            					if(childMatchOf.getLocalName() !=null && childMatchOf.getLocalName().equals("Match")){
//		    	            						childAllOf.removeChild(childMatchOf);
//		    	            						Node nextChildMatchOf = childMatchOf.getNextSibling();
//		    	            						Target targetM = Target.getInstance(targetNode, policyMetaData);
//		    	            						StringBuilder currentPreExpressionM = new StringBuilder(currentPreExpressionMRoot.toString());
//		    	            						currentPreExpressionM.append(z3ExpressionHelper.getTrueTargetExpression(targetM)+  System.lineSeparator());
//		    	            						List<Node> childrenMatchOfCopyM = new ArrayList<Node>();
//		    	            						childrenMatchOfCopyM.addAll(XMLUtil.getChildNodeList(childAllOf));
//		    	            						
//		    	            						for(Node c:childrenMatchOfCopyM){
//		    	            							childAllOf.removeChild(c);
//		    	            						}
//		    	            						childAllOf.appendChild(childMatchOf);
//		    	            						Target targetTrueM = Target.getInstance(targetNode, policyMetaData);
//		    	            						currentPreExpressionM.append(System.lineSeparator()).append(z3ExpressionHelper.getFalseTargetExpression(targetTrueM)+  System.lineSeparator());
//		    	            						childAllOf.removeChild(childMatchOf);
//		    	            						for(Node c:childrenMatchOfCopyM){ 
//		    	            							childAllOf.appendChild(c);
//		    	            						}
//		    	            						childAllOf.insertBefore(childMatchOf, nextChildMatchOf);
//		    	            						ruleExpressions.add(new StringBuffer(currentPreExpressionM));
//		    	            						
//		    	            					}
//		    	            					
//		    	            				}		
//		            					}
//	            						child.removeChild(childAllOf);
//	            						for(Node c:childrenAllOfCopy){ 
//	            							child.appendChild(c);
//	            						}
//	            						child.insertBefore(childAllOf, nextChild);
//	            						if(allOfCount > 1) {
//	            							ruleExpressions.add(new StringBuffer(currentPreExpression));
//	            						}
//	            						flag = true;
//	            					}
//	            				}
//	            			}
//	            		}
//	            	}
//	            }
//	        }
//	    } 
//		
//		if(!flag){
//			return new ArrayList<String>();
//		}
//
//		if(condition != null){
//			for(StringBuffer ruleExpression:ruleExpressions){
//				ruleExpression.append(z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator());
//			}
//		}
//	   
//		while(true){
//			sibling = n.getNextSibling();
//			if(sibling == null){
//				break;
//			} else{
//				if(sibling.getNodeType() == Node.ELEMENT_NODE){
//					if(XACMLElementUtil.isRule(sibling)){
//						postRules.add(Rule.getInstance(sibling, policyMetaData, null));
//					}
//				} 
//			}
//			n = sibling;
//		}
//		
//	    List<String> lst = new ArrayList<String>();
//	    for(StringBuffer expression:ruleExpressions){
//	    	lst.add(expression.toString());
//	    }
//		return lst;
//	}

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
   
    private boolean IndTarget(Target target,String prefix) throws IOException{
		StringBuffer sb = new StringBuffer();
		ArrayList<Attr> temp = new ArrayList<Attr>();
		if (policy.getCombiningAlg().getIdentifier().toString().equals("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable")) {

			sb.append(False_Target(target, temp) + "\n");
			Attr unique = getDifferentAttribute(z3ExpressionHelper.getAttributeList(), temp);
			if (unique == null) {
				if(this.currentPolicyRuleIndex!=0)
				return false;
			}
			temp.add(invalidAttr());
			mergeAttribute(z3ExpressionHelper.getAttributeList(),temp);
			temp.remove(unique);
			sb.append(prefix);
		} else {
			sb.append(False_Target(target, temp) + "\n");
			temp.add(invalidAttr());
			mergeAttribute(z3ExpressionHelper.getAttributeList(),temp);
			temp.remove(0);
			sb.append(prefix);
		}
		boolean sat = Z3StrUtil.processExpression(sb.toString(), z3ExpressionHelper);
		if (sat) {
			List<Attr> gAttrs = z3ExpressionHelper.getAttributeList();
			List<Attr> attrs = getTargetAttrList(target);
			String request = RequestBuilder.buildIDRequest(gAttrs,attrs);
			int res = XACMLElementUtil.TargetEvaluate(target, request);
			if(res==2) {
				addRequest(request);
				
			
			return true;
			}
			return true;
		}
		return false;
	}

    private  boolean IndCondition(Condition condition,String prefix) throws IOException{
		StringBuffer sb = new StringBuffer();
		ArrayList<Attr> temp = new ArrayList<Attr>();
		if (policy.getCombiningAlg().getIdentifier().toString().equals("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable")) {
			sb.append(True_Condition(condition, temp) + System.lineSeparator());
			Attr unique = getDifferentAttribute(z3ExpressionHelper.getAttributeList(), temp);
			if (unique == null) {
				return false;
			}
			//temp.add(invalidAttr());
			//mergeAttribute(z3ExpressionHelper.getAttributeList(),temp);
			//temp.remove(unique);
			sb.append(prefix);
		} else {
			//True_Condition(condition, temp);
			sb.append(True_Condition(condition, temp) + System.lineSeparator());

			//temp.add(invalidAttr());
			//mergeAttribute(z3ExpressionHelper.getAttributeList(),temp);
			//temp.remove(0);
			sb.append(prefix);
		}
		boolean sat = Z3StrUtil.processExpression(sb.toString(), z3ExpressionHelper);
		if (sat) {
			//List<Attr> lst = z3ExpressionHelper.getAttributeList();
			//lst.removeAll(temp);
			//String request = RequestBuilder.buildRequest(lst);
			List<Attr> gAttrs = z3ExpressionHelper.getAttributeList();
			//List<Attr> attrs = getTargetAttrList(target);
			String request = RequestBuilder.buildIDRequest(gAttrs,temp);
			
			int res = XACMLElementUtil.ConditionEvaluate(condition, request);
			if(res==2) {
				addRequest(request);
				
			
			return true;
			}
			return true;
		}
		return false;
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
				sb = z3ExpressionHelper.ApplyStatements(apply, "", sb, collector);
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
	
	private List<String> getDistinctAttrs(List<List<String>> allOf){
		List<String> lst = new ArrayList<String>();
		for(List<String> l: allOf ) {
			for(String line:l) {
				line = line.replaceAll(System.lineSeparator(), "");
				String[] tokens = line.split(" ");
				for(int i = 0;i < tokens.length;i++) {
					String t = tokens[i];
					if(t.indexOf("=")>0) {
						if(!lst.contains(tokens[i+1])) {
							lst.add(tokens[i+1]);
						}
					}
				}
			}
		}
		return lst;
	}
}
