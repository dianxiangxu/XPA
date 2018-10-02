package org.seal.xacml.coverage;

import static org.seal.xacml.policyUtils.XpathSolver.policyPattern;
import static org.seal.xacml.policyUtils.XpathSolver.policysetPattern;
import static org.seal.xacml.policyUtils.XpathSolver.rulePattern;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.seal.mcdc.MCDCConditionSet;
import org.seal.mcdc.MCDCGen;
import org.seal.xacml.NameDirectory;
import org.seal.xacml.components.CombiningAlgorithmURI;
import org.seal.xacml.utils.RequestBuilder;
import org.seal.xacml.utils.XACMLElementUtil;
import org.seal.xacml.utils.XMLUtil;
import org.seal.xacml.utils.Z3StrUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.balana.DOMHelper;
import org.wso2.balana.ParsingException;
import org.wso2.balana.Rule;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.xacml3.Target;
import org.xml.sax.SAXException;

public class MCDC extends DecisionCoverage{
	private List<String> covered;
	private MCDCCoverageRecord [][][] mcdcCoverage;
	private  MCDCPolicyTargetCoverageRecord [] mcdcCoveragePT;
	private int targetCoverageIndex;
	private HashMap mcdcName;
	public MCDC(String policyFilePath,boolean error) throws ParsingException, IOException, SAXException, ParserConfigurationException{ 
		super(policyFilePath,error);
		mcdcName = new HashMap<>();
		covered = new ArrayList<String>();
		List<Rule> rules = XACMLElementUtil.getRuleFromPolicy(this.policy);
		
		mcdcCoverage = new MCDCCoverageRecord[rules.size()][2][];
		Node node = doc.getDocumentElement();
		udpatePolicyMeta(node);
		
		Node targetNode = findInChildNodes(node, NameDirectory.TARGET);
	    if(targetNode!=null) {
	    	Target target = (Target) Target.getInstance(targetNode, policyMetaData);
	    	String targetExp = z3ExpressionHelper.getTrueTargetExpression(target).toString();
	    	if(targetExp.indexOf("and")>=0||targetExp.indexOf("or")>=0) {
		    	MCDCGen mcdcGen = new MCDCGen(targetExp,mcdcName);
		    	if(mcdcGen.isMCDCFeasible()) {
		    		MCDCConditionSet mcdcSet = mcdcGen.getMCDCConditionSet();
			    	List<String> positives = mcdcGen.getPositiveCases();
		    		mcdcCoveragePT= new MCDCPolicyTargetCoverageRecord[positives.size()];
		    		for(int j = 0; j < positives.size(); j++) {
		    			String exp = positives.get(j);
		    			if(mcdcGen.isOrWithSameAttributeExp()) {
		    				exp = processConstraintSolverIssue(exp);
		    			}
		    			mcdcCoveragePT[j] = new MCDCPolicyTargetCoverageRecord(exp,false);
		    			
		    		}
		    	}
	    	}
	    }
		
		for(int i = 0; i< rules.size();i++) {
			
			RuleBody rb = new RuleBody(rules.get(i),null,null);
			if(rb.getTarget()!=null) {
				String tExp = rb.getRuleTargetExpression();
		    	if(tExp.indexOf("and") >= 0 || tExp.indexOf("or")>=0) {
					MCDCGen mcdcGenT = new MCDCGen(tExp,mcdcName);
			    	
			    	if(mcdcGenT.isMCDCFeasible()) {
			    		int count = mcdcGenT.getConditionSet().size();
				    	
			    		mcdcCoverage[i][0]= new MCDCCoverageRecord[count];
			    		List<String> mcdcSet = mcdcGenT.getConditionSet();
			    		for(int j = 0; j < count; j++) {
			    			String c = mcdcSet.get(j);
			    			String[] tokens = c.split("\\s+");
			    			List<String> lst = new ArrayList<String>();
			    			Map<String,String> map = new HashMap<String,String>();
			    			for(String k:tokens) {
			    				if(k.length()>=4) {
			    					String key = k.trim();
			    					if(key.charAt(0)=='!') {
			    						key = key.substring(1);
			    					}
			    					lst.add(mcdcGenT.getValue(key));
			    				}
			    			}
			    			mcdcCoverage[i][0][j] = new MCDCCoverageRecord(c,lst,false);
			    		}
			    	}
		    	}
			}
			
			if(rb.getCondition()!=null) {
				String cExp = rb.getRuleConditionExpression();
				if(cExp.indexOf("and") >= 0 || cExp.indexOf("or")>=0) {
					MCDCGen mcdcGenC = new MCDCGen(cExp,mcdcName);
			    	if(mcdcGenC.isMCDCFeasible()) {
			    		int count = mcdcGenC.getConditionSet().size();
				    	
			    		mcdcCoverage[i][1]= new MCDCCoverageRecord[count];
			    		List<String> mcdcSet = mcdcGenC.getConditionSet();
			    		for(int j = 0; j < count; j++) {
			    			String c = mcdcSet.get(j);
			    			String[] tokens = c.split("\\s+");
			    			List<String> lst = new ArrayList<String>();
			    			Map<String,String> map = new HashMap<String,String>();
			    			for(String k:tokens) {
			    				if(k.length()>=4) {
			    					String key = k.trim();
			    					if(key.charAt(0)=='!') {
			    						key = key.substring(1);
			    					}
			    					lst.add(mcdcGenC.getValue(key));
			    				}
			    			}
			    			mcdcCoverage[i][1][j] = new MCDCCoverageRecord(c,lst,false);
			    		}
			    	}
				}
			}
		}
	}
	
	protected void traverse(Element node, StringBuilder preExpression,List<Rule> previousRules) throws IOException, ParsingException, ParserConfigurationException, SAXException {
		String name = DOMHelper.getLocalName(node);
		
	    if (rulePattern.matcher(name).matches()) {
	    	
	    	if(mcdcCoveragePT!=null) {
	    		if(targetCoverageIndex < mcdcCoveragePT.length) {
	    			preExpression = new StringBuilder(mcdcCoveragePT[targetCoverageIndex].getC());
	    			mcdcCoveragePT[targetCoverageIndex].setCovered(true);
	    			targetCoverageIndex++;
	    		}
	    	}
	    	RuleBody rb = new RuleBody(node,previousRules,preExpression);
			
	    	if((rb.getTarget() == null) && (rb.getCondition() == null)) {
    		    return;
			}
	    	
	    	boolean targetMCDCFeasible = false;
	    	boolean sat;
			if(rb.getTarget()!=null) {
		    	String tExp = rb.getRuleTargetExpression();
		    	MCDCGen mcdcGenT = new MCDCGen(tExp,mcdcName);
		    	if(mcdcGenT.isMCDCFeasible()) {
		    		
		    		List<String> cases = mcdcGenT.getCases();
		    		List<String> conditions = mcdcGenT.getConditionSet();
		    		for(int i = 0; i < cases.size();i++) {
		    			if(!isCovered(conditions.get(i), true)) {
		    			
			    			String expression = rb.getReachabilityExp();
			    			if(mcdcGenT.isOrWithSameAttributeExp()) {
			    				expression += processConstraintSolverIssue(cases.get(i));
			    			} else {
			    				if(cases.size()==4 && mcdcGenT.isAndOr()) {
			    					
			    						expression += processConstraintSolverIssueAndOr(cases.get(i));
			    					
			    				} else {
			    						expression += cases.get(i);
			    				}
			    			}
			    			
			    			if(rb.getCondition()!=null) {
			    				expression += rb.getRuleConditionExpression();
			    			}
			    			sat = Z3StrUtil.processExpression(expression, z3ExpressionHelper);
							if (sat){
								targetMCDCFeasible = true;
								String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
								addRequest(req);
								updateCoverage(req);
								updateMCDCCoverage(conditions.get(i), req, 0);
							}
		    			}
		    		}
		   		
		    	}
			}
	    	
	    	if(rb.getCondition()!=null) {
		    	String cExp = rb.getRuleConditionExpression();
		    	if(cExp.indexOf("and")>=0 || cExp.indexOf("or")>=0) {
			    	MCDCGen mcdcGenC = new MCDCGen(cExp,mcdcName);
			    	if(mcdcGenC.isMCDCFeasible()) {
			    		List<String> cases;
			    		List<String> conditionsC;
			    		if(targetMCDCFeasible) {
			    			conditionsC = mcdcGenC.getMCDCConditionSet().getNegativeConditions();
			    			cases = mcdcGenC.getNegativeCases();
			    		} else {
				    		conditionsC = mcdcGenC.getConditionSet();
				    		cases = mcdcGenC.getCases();
			    		}
			    		
			    		for(int i = 0; i < cases.size();i++) {
			    			if(!isCovered(conditionsC.get(i), true)) {

				    			String expression = rb.getReachabilityExp();
				    			if(rb.getTarget()!=null) {
				    				expression += rb.getRuleTargetExpression();
				    			}
				    			
				    			if(mcdcGenC.isOrWithSameAttributeExp()) {
				    				expression += processConstraintSolverIssue(cases.get(i));
				    			} else {
				    				expression += cases.get(i);
				    			}
				    			//expression += cases.get(i);
				    			sat = Z3StrUtil.processExpression(expression, z3ExpressionHelper);
								if (sat){
									String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
									addRequest(req);
									updateCoverage(req);
									updateMCDCCoverage(conditionsC.get(i), req, 1);
	
								}
			    			}
			    		}
			    	}
		    	}
	    	}
	    	
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
			Node targetNode = findInChildNodes(node, NameDirectory.TARGET);
		    if(targetNode!=null) {
		    	Target target = (Target) Target.getInstance(targetNode, policyMetaData);
		    	String targetExp = z3ExpressionHelper.getTrueTargetExpression(target).toString();
		    	if(targetExp.indexOf("and")>=0||targetExp.indexOf("or")>=0) {
			    	MCDCGen mcdcGen = new MCDCGen(targetExp,mcdcName);
			    	if(mcdcGen.isMCDCFeasible() && !mcdcGen.isOrWithSameAttributeExp()) {
			    		List<String> cases = mcdcGen.getNegativeCases();
			    		for(int i = 0; i < cases.size();i++) {
			    			String expression = preExpression.toString() + System.lineSeparator() ;
			    			expression += cases.get(i);
			    			
							boolean sat = Z3StrUtil.processExpression(expression, z3ExpressionHelper);
							
							if (sat){
								String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
								addRequest(req);
								updateCoverage(req);
								falsePolicyTarget = true;
	
							}
			    		}
			    	}
		    	}
		    }
			
		    coverPolicyTarget(name, node, previousRules, preExpression);
		    
		    if(mcdcCoveragePT!=null) {
		    	for(MCDCPolicyTargetCoverageRecord r:mcdcCoveragePT) {
		    		if(!r.isCovered()) {
		    			String expression = preExpression.toString() + System.lineSeparator() ;
		    			expression += r.getC();
						boolean sat = Z3StrUtil.processExpression(expression, z3ExpressionHelper);
						
						if (sat){
							String req = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
							addRequest(req);
							updateCoverage(req);
						}
		    		}
		    	}
		    }
		}
    }
	
	private boolean isCovered(String c,boolean targetFlag) {
		boolean flag = false;
		int selector = 0;
		if(!targetFlag) {
			selector = 1;
		}
		String[] tokens = c.trim().split("\\s+");
		for(int i = currentPolicyRuleIndex; i < currentPolicyRules.size();i++) {
			MCDCCoverageRecord[] records = mcdcCoverage[i][selector];
			if(records==null) {
				continue;
			}
			for(MCDCCoverageRecord record:records) {
				String e = record.getRecord();
				int count = 0;
				for(String k:tokens) {
					if(k.length()>=4) {
						String key = k.trim();
						if(e.indexOf(k)<0) {
							break;
						}
						count++;
					}
				}
				
				String[] tkns = e.split("\\s+");
				for(String t:tkns) {
					if(t.trim().length()>=4) {
						count--;
					}
				}
				
				if(count == 0) {
					if(record.isCovered()) {
						flag = true;
						break;
					}
				}
					
			}
			if(flag) {
				break;
			}
		}
		return flag;
	}
	
	
	private boolean updateMCDCCoverage(String exp,String req, int selector) {
		boolean flag = false;
		
		coverCurrentRule(exp,selector);
		MCDCGen mcdcGen = new MCDCGen(null, mcdcName);
		for(int i = currentPolicyRuleIndex+1; i < currentPolicyRules.size();i++) {
			if(doesFurtherEvaluatesRule(i, req)) {
				MCDCCoverageRecord[] records = mcdcCoverage[i][selector];
				if(records==null) {
					continue;
				}
				for(MCDCCoverageRecord record:records) {
					String e = record.getRecord();
					String[] tokens = e.trim().split("\\s+");
					
					int count = 0;
					int countFound = 0;
					List<String> notTokens = new ArrayList<String>();
					for(String token:tokens) {
						String k = token.trim();
						if(k.length()>=4) {
							String key = k.trim();
							if(exp.indexOf(key)>=0){
								int offset = exp.indexOf(key);
								if(offset > 0) {
									if(!(exp.charAt(offset-1)=='!')) {
										count++;
										countFound++;
										continue;	
									}
								} else {
								count++;
								countFound++;
								continue;
								}
							}
							if(exp.indexOf(key)<0 && key.charAt(0)== '!') {
								notTokens.add(key);
							} 
							count++;

						}
					}
					
					
					
					if(countFound == count) {
						record.setCovered(true);
						flag = true;
						break;
						
					} else {
						int diff = count - countFound;
						String[] tkns = exp.split("\\s+");
						int diffCount = 0;
						for(String t:tkns) {
							String k = t.trim();
							if(k.length()>=4 && k.charAt(0)=='!') {
								if(notTokens.contains(k)) {
									continue;
								}
								String[] value = mcdcGen.getValue(k.substring(1)).split("\\s+");
								for(String nt:notTokens) {
									String[] v = mcdcGen.getValue(nt.substring(1)).split("\\s+");
									if(v[1].equals(value[1])) {
										diffCount++;
									}
								}
							}
						}
						if(diffCount==diff) {
							record.setCovered(true);
							flag = true;
						}
					}
						
				}
				
			} else {
				break;
			}
		}
		return flag;
	}
	
	private void coverCurrentRule(String exp,int selector) {
		String[] tokens = exp.trim().split("\\s+");
		
		MCDCCoverageRecord[] records = mcdcCoverage[currentPolicyRuleIndex][selector];
		for(MCDCCoverageRecord record:records) {
				
			String e = record.getRecord();
			int count = 0;
			for(String k:tokens) {
				if(k.length()>=4) {
					String key = k.trim();
					if(e.indexOf(k)<0) {
						break;
					}
					count++;
				}
			}
			String[] tkns = e.split("\\s+");
			for(String t:tkns) {
				if(t.trim().length()>=4) {
					count--;
				}
			}
			if(count == 0) {
				record.setCovered(true);
				return;
			}
		}	
	}
	
	private boolean doesFurtherEvaluatesRule(int i, String req) {
		boolean flag = true;
		Target t = (Target)currentPolicyRules.get(i-1).getTarget();
		int resT = 0;
		if (t !=null) {
			resT = XACMLElementUtil.TargetEvaluate(t, req);
		}
		Condition c = (Condition)currentPolicyRules.get(i-1).getCondition();
		int resC = 0;
		if(c != null && resT==0) {
			resC = XACMLElementUtil.ConditionEvaluate(c, req);
		}
		
		if(resT == 0 && resC==0) {
			if(currentPolicyCA.equals(CombiningAlgorithmURI.map.get("PO")) || currentPolicyCA.equals(CombiningAlgorithmURI.map.get("OPO")) ||currentPolicyCA.equals(CombiningAlgorithmURI.map.get("DUP"))) {
				if(currentPolicyRules.get(currentPolicyRuleIndex).getEffect() == 0) {
					flag = false;
				}
			} 
	    	else if(currentPolicyCA.equals(CombiningAlgorithmURI.map.get("DO")) || currentPolicyCA.equals(CombiningAlgorithmURI.map.get("ODO")) ||currentPolicyCA.equals(CombiningAlgorithmURI.map.get("PUD"))) {
	    		if(currentPolicyRules.get(currentPolicyRuleIndex).getEffect() == 1) {
	    			flag = false;
				}	
	    	} else {
	    		flag = false;
	    	}
		}
		
		if((resT == 2 || resC==2) && currentPolicyCA.equals(CombiningAlgorithmURI.map.get("FA")) ) {
			flag = false;
		}
		return flag;

	}
	
	private String processConstraintSolverIssue(String exp) {
		String [] tokens = exp.split("\\s+");
		List<String> bag = new ArrayList<String>();
		for(String t:tokens) {
			if(!bag.contains(t)) {
				bag.add(t);
			} else {
				if(t.length()==5) {
					return exp.replaceAll("\\(and\\s+\\(not\\s+\\(=\\s+" + t.trim() + "(.*?)\\)\\)\\)", "");
				}
			}
		}
		return exp;
	}
	
	private String processConstraintSolverIssueAndOr(String exp){
		String[] tokens = exp.split("not");
		if(tokens.length > 2) {
			return exp;
		}
		String e = exp;
		int l ;
		int r;
		int n = e.indexOf("(not");
		l = n-1;
		while(e.charAt(l)!='(') {
			l --;
		}
		int c = 0;
		r = n;
		while(true) {
			if(e.charAt(r)==')') {
				c++;
			}
			if(c==4) {
				break;
			}
			r++;
		}
		
		String lp = e.substring(0,l);
		String rp = e.substring(r+1);
		String m = lp + rp;
		return m;
	}
	public static void main(String [] args) {
		String str = "(and (or  (and  (not (= kdhgf HR))) (and  (= kdhgf \"IT\"))))\n";
		//String replaced = str.replaceAll("", "");
		String replaced = str.replaceAll("\\(and\\s+\\(not\\s+\\(= " + "kdhgf" , "");
		
		System.out.println(replaced);
		
	}
public  List<String> getMCDCExpressions(String expression, boolean coveredFlag){
		
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
	        List<String> bag = new ArrayList<String>();
	    	for(String exp:expressions) {
				String e = exp.replaceAll(System.lineSeparator(), "");
				bag.add(e);
	    	}
	    	expressions = bag;
	    } 
	    if(!policyTargetFlag) {
		
		    Node conditionNode = XMLUtil.findInChildNodes(node, NameDirectory.CONDITION);
		    
		    if(conditionNode!=null) {
		    	List<String> bag = new ArrayList<String>();
		    	Condition c = Condition.getInstance(conditionNode, policyMetaData, null);
		    	for(String exp:expressions) {
					String expression = exp + System.lineSeparator() + z3ExpressionHelper.getTrueConditionExpression(c) + System.lineSeparator();
					bag.add(expression);
				}
		    	if(bag.size()>0) {
		    		expressions = bag;
		    	}
		    	String cExp = z3ExpressionHelper.getTrueConditionExpression(c).toString();
		    	List<String> cMCDCs = getMCDCExpressions(cExp, false);
		    	if(cMCDCs.size()>2) {
		    		
		    		
		    		for(int i = 0; i < cMCDCs.size()-1;i++) {
		    			String e = cMCDCs.get(i).replaceAll(System.lineSeparator(), "");
		    		    if (targetNode != null) {
		    		    	Target t = Target.getInstance(targetNode, policyMetaData);
		    		    	String expression = e + System.lineSeparator() + z3ExpressionHelper.getTrueTargetExpression(t) + System.lineSeparator();
		    		    	expressions.add(expression);
		    		    } else {
		    		    	expressions.add(e);
		    		    }
		    		}
		    	}
		    }
	    }
		
		return expressions;
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
	
	
	
	private class MCDCCoverageRecord {
		private String record;
		private boolean covered;
		private List<String> lst;
		
		public MCDCCoverageRecord(String r, List<String> l, boolean c) {
			record = r;
			covered = c;
			lst = l;
		}
		public String getRecord() {
			return record;
		}
		public void setRecord(String record) {
			this.record = record;
		}
		public boolean isCovered() {
			return covered;
		}
		public void setCovered(boolean covered) {
			this.covered = covered;
		}
		public List<String> getLst() {
			return lst;
		}
		public void setLst(List<String> lst) {
			this.lst = lst;
		}
	}
	
	private class MCDCPolicyTargetCoverageRecord {
		private String c;
		private boolean covered;
		
		public MCDCPolicyTargetCoverageRecord(String c, boolean covered) {
			this.c = c;
			this.covered = covered;
		}
		public String getC() {
			return c;
		}
		public void setC(String c) {
			this.c = c;
		}
		public boolean isCovered() {
			return covered;
		}
		public void setCovered(boolean covered) {
			this.covered = covered;
		}
		
	}
}
