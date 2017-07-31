package org.seal.xacml.mutation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.seal.combiningalgorithms.MyAttr;
import org.seal.coverage.PolicySpreadSheetTestRecord;
import org.seal.gui.TestPanel;
import org.seal.policyUtils.PolicyLoader;
import org.seal.semanticMutation.Mutant;
import org.seal.semanticMutation.Mutator;
import org.seal.xacml.NameDirectory;
import org.seal.xacml.RequestGeneratorBase;
import org.seal.xacml.TaggedRequest;
import org.seal.xacml.coverage.RuleCoverage;
import org.seal.xacml.utils.FileIOUtil;
import org.seal.xacml.utils.RequestBuilder;
import org.seal.xacml.utils.XACMLElementUtil;
import org.seal.xacml.utils.XMLUtil;
import org.seal.xacml.utils.Z3StrUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.wso2.balana.Rule;
import org.wso2.balana.combine.CombiningAlgorithm;
import org.wso2.balana.combine.xacml3.DenyOverridesRuleAlg;
import org.wso2.balana.combine.xacml3.DenyUnlessPermitRuleAlg;
import org.wso2.balana.combine.xacml3.PermitOverridesRuleAlg;
import org.wso2.balana.combine.xacml3.PermitUnlessDenyRuleAlg;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.xacml3.AnyOfSelection;
import org.wso2.balana.xacml3.Target;
import org.xml.sax.SAXException;

public class MutationBasedTestGenerator extends RequestGeneratorBase {
	private AbstractPolicy policy;
	private String currentMutationMethod;
	public MutationBasedTestGenerator(String policyFilePath) throws ParsingException, IOException, SAXException, ParserConfigurationException{
		init(policyFilePath);
		this.policy = PolicyLoader.loadPolicy(new File(policyFilePath));
	}	   
	
	public List<TaggedRequest> generateRequests(List<String> mutationMethods) throws IOException, ParserConfigurationException, ParsingException, SAXException, InvocationTargetException, IllegalAccessException, NoSuchMethodException{
		Mutator mutator = new Mutator(new Mutant(policy, XACMLElementUtil.getPolicyName(policyFilePath)));
        Map<String,List<Mutant>> mutantsMap = mutator.generateMutantsCategorizedByMethods(mutationMethods);
        Class<? extends MutationBasedTestGenerator> cls = this.getClass();
      
        Class[] noParams = {};
        Class[] params = {AbstractPolicy.class};
        List<TaggedRequest> taggedRequests = new ArrayList<TaggedRequest>();
		for(Map.Entry<String, List<Mutant>> e:mutantsMap.entrySet()){
			List<Mutant> mutants = (List<Mutant>)e.getValue();
			currentMutationMethod = e.getKey().toString();
			String tag = MutationMethodAbbrDirectory.getAbbr(currentMutationMethod);
			String methodName = "generate" + tag + "Requests";
			Method method = cls.getDeclaredMethod(methodName, noParams);
			List<String> requests = (List<String>)method.invoke(this, null);
			int j = 0;
			for(Mutant mutant:mutants){
				File f = new File(mutant.getName());//
				FileIOUtil.writeFile(f, mutant.encode());//
			}
			for(int i = 0; i< requests.size();i++){
				AbstractPolicy p;
				String mutantForPropagationForPolicy = MutationMethodForPropagationForPolicyDirectory.getMutationMethod(currentMutationMethod);
				
				if(mutantForPropagationForPolicy.equalsIgnoreCase("SELF")){
					p = policy;
				} else{
					Class klass = Mutator.class;
					Method m = klass.getDeclaredMethod(mutantForPropagationForPolicy, params);
					Mutant mut = (Mutant) m.invoke(new Mutator(new Mutant(policy,"")), policy);
					p = mut.getPolicy();
				}
				if(doRequestPropagatesMutationFault(requests.get(i), p, mutants)){
					File r = new File(tag+(i+1));//
					FileIOUtil.writeFile(r, requests.get(i));//
					taggedRequests.add(new TaggedRequest(tag,requests.get(i)));
					j = i+1;
				}
			}
			
		}
		return taggedRequests;
	}

	private boolean doRequestPropagatesMutationFault(String request, AbstractPolicy policy, List<Mutant> mutants) throws ParsingException,NoSuchMethodException,InvocationTargetException,IllegalAccessException,ParserConfigurationException,IOException,SAXException{
		String req = request.replaceAll(System.lineSeparator(), " ").trim(); 
		if(req.isEmpty()){
			return false;
		}
		for(Mutant mutant:mutants){
			String mutantForPropagationForMutant = MutationMethodForPropagationForMutantDirectory.getMutationMethod(currentMutationMethod);
			Class[] params = {AbstractPolicy.class};
			if(!mutantForPropagationForMutant.equalsIgnoreCase("SELF")){
				Class klass = Mutator.class;
				Method m = klass.getDeclaredMethod(mutantForPropagationForMutant, params);
				mutant = (Mutant) m.invoke(new Mutator(mutant), mutant.getPolicy());
			}
		
			AbstractPolicy mutantPolicy = mutant.getPolicy();
			int pRes = XACMLElementUtil.evaluateRequestForPolicy(policy, req);
			int mRes = XACMLElementUtil.evaluateRequestForPolicy(mutantPolicy, req);
			if (pRes != mRes){
				return true;
			}
		}
		return false;
	}
	
	
	public List<String> generatePTTRequests() throws IOException, ParsingException{
		
		traverseForPTA(doc.getDocumentElement(), new StringBuilder(),false);
		return getRequests();
	}
	
	public List<String> generatePTFRequests() throws IOException, ParsingException{
		
		traverseForPTA(doc.getDocumentElement(), new StringBuilder(),true);
		return getRequests();
	}
	
	private void traverseForPTA(Element node, StringBuilder preExpression,boolean targetTrue) throws ParsingException,IOException{
		boolean isPolicySet = XACMLElementUtil.isPolicySet(node);
		if ( isPolicySet || XACMLElementUtil.isPolicy(node)) {
			Node targetNode = XMLUtil.findInChildNodes(node, NameDirectory.TARGET);
		    if(targetNode!=null){
		    	Target target = Target.getInstance(targetNode, policyMetaData);
		    	StringBuffer targetExpression = null;
		    	if(targetTrue){
		    		targetExpression = z3ExpressionHelper.getTrueTargetExpression(target);
		    	} else{
		    		targetExpression = z3ExpressionHelper.getFalseTargetExpression(target);
		    	}
		    	StringBuffer trueTargetExpression = new StringBuffer();
		    	traversePostTargetPTAExpression(node,trueTargetExpression);
		    	String expression = preExpression + targetExpression.toString() + System.lineSeparator() + trueTargetExpression.toString();
		    	boolean sat = Z3StrUtil.processExpression(expression, z3ExpressionHelper);
	    		if (sat == true) {
	    			addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
	    		}
		    }
		    if (isPolicySet) {
		    	NodeList lst = node.getChildNodes();
				for(int i = 0; i < lst.getLength();i++){
					StringBuilder curPreExpression = new StringBuilder();
					curPreExpression.append(preExpression);
					
					Node child = lst.item(i);
					if(child instanceof Element && XMLUtil.isTraversableElement(child) ){
						if(XACMLElementUtil.isPolicy(child) ||XACMLElementUtil.isPolicySet(child)){
							Node targetN = XMLUtil.findInChildNodes(node, NameDirectory.TARGET);
						    if(targetN !=null){
						    	Target targetT = Target.getInstance(targetN, policyMetaData);
						    	curPreExpression.append(z3ExpressionHelper.getTrueTargetExpression(targetT));
						    }
							traverseForPTA((Element)child,curPreExpression,targetTrue);
						}
					}
				}
		    }
		}
	
	}
	
	private void traversePostTargetPTAExpression(Element node, StringBuffer exp) throws ParsingException{
		NodeList lst = node.getChildNodes();
		boolean flagStop = false;
		for(int i = 0; i < lst.getLength();i++){
			Node child = lst.item(i);
			if(child instanceof Element && XMLUtil.isTraversableElement(child)){
				if(XACMLElementUtil.isRule(child)){
					exp.append(getRuleExpression((Element)child));
					flagStop = true;
				} else if(XACMLElementUtil.isPolicy(child) || XACMLElementUtil.isPolicySet(child)){
					Node targetNode = XMLUtil.findInChildNodes(child, NameDirectory.TARGET);
				    if(targetNode!=null){
				    	Target target = Target.getInstance(targetNode, policyMetaData);
				    	exp.append(z3ExpressionHelper.getTrueTargetExpression(target));
				    }
				    traversePostTargetPTAExpression((Element)child, exp);
				    flagStop = true;
				}
			}
			if(flagStop){
				return;
			}
		}
	}
	
	public StringBuffer getRuleExpression(Element node) throws ParsingException{
	    Target target = XMLUtil.getTarget(node, policyMetaData);
	    Condition condition = XMLUtil.getCondition(node, policyMetaData);
	    StringBuffer ruleExpression = new StringBuffer();
	    ruleExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
		ruleExpression.append(z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator());
	    return ruleExpression;
	}
	
	/*public List<String> generatePTFRequests() throws IOException{
		if(!policy.isTargetEmpty()){
			Target policyTarget = (Target)policy.getTarget();
			List<AnyOfSelection> anyOf = policyTarget.getAnyOfSelections();
			if(anyOf.size() != 0){
				String expression = z3ExpressionHelper.getTrueTargetExpression(policyTarget).toString();
				boolean sat = Z3StrUtil.processExpression(expression, z3ExpressionHelper);
				String request = RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList());
				List<String> requests = new ArrayList<String>();
				requests.add(request);
				if (sat == true) {
				    setRequests(requests);
				} else{
					setRequests(null);
				}
			}
		}
		return getRequests();
	}*/
	
	public List<String> generateCRERequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		RuleCoverage coverage = new RuleCoverage(policyFilePath);
		return coverage.generateRequests();
	}
	
	public List<String> generateRTTRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		RuleCoverage coverage = new RuleCoverage(policyFilePath);
		return coverage.generateRequestsForTruthValues(false,true,true);
	}
	
	public List<String> generateRTFRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		RuleCoverage coverage = new RuleCoverage(policyFilePath);
		return coverage.generateRequestsForTruthValues(true,true,true);
	}
	
	public List<String> generateRCTRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		RuleCoverage coverage = new RuleCoverage(policyFilePath);
		return coverage.generateRequestsForTruthValues(true,false,true);
	}
	
	public List<String> generateRCFRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		RuleCoverage coverage = new RuleCoverage(policyFilePath);
		return coverage.generateRequestsForTruthValues(true,true,true);
	}
	
	public List<String> generateANFRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		RuleCoverage coverage = new RuleCoverage(policyFilePath);
		return coverage.generateRequestsForTruthValues(true,true,true);
	}
	
	public List<String> generateRNFRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		RuleCoverage coverage = new RuleCoverage(policyFilePath);
		return coverage.generateRequestsForTruthValues(true,true,true);
	}
	
	public List<String> generateFPRRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException, InvalidMutationMethodException {
		traverseForPermitOrDeny(doc.getDocumentElement(), new StringBuilder(),currentMutationMethod);
		return getRequests();
	}
	
	public List<String> generateFDRRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException, InvalidMutationMethodException {
		traverseForPermitOrDeny(doc.getDocumentElement(), new StringBuilder(),currentMutationMethod);
		return getRequests();
	}
	
	public List<String> generateRERRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		RuleCoverage coverage = new RuleCoverage(policyFilePath);
		return coverage.generateRequestsForTruthValues(true,true,true);
	}
	
	public List<String> generateRPTERequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		traverseForRPTE(doc.getDocumentElement(),new StringBuilder(),true,true, new ArrayList<Rule>());
		List<String> reqs = getRequests();
		return reqs;
	}
	
	private void traverseForRPTE(Element node, StringBuilder preExpression,boolean notForRule,boolean falsifyPostRules,  List<Rule> previousRules) throws IOException, ParsingException, ParserConfigurationException, SAXException{
		boolean isPolicy = XACMLElementUtil.isPolicy(node);
		if ( isPolicy || XACMLElementUtil.isPolicySet(node)) {
		    Node targetNode = XMLUtil.findInChildNodes(node, NameDirectory.TARGET);
		    if (isPolicy) {
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
			            			if(allOfCount > 1){
			            				for(int i = 0; i < childrenAllOf.size(); i++){
			            					Node childAllOf = childrenAllOf.get(i);
			            					if(childAllOf.getLocalName() !=null && childAllOf.getLocalName().equals("AllOf")){
			            						previousRules = new ArrayList<Rule>();
			            						StringBuilder currentPreExpression = new StringBuilder(preExpression.toString());
			            						child.removeChild(childAllOf);
			            						Node nextChild = childAllOf.getNextSibling();
			            						Target target = Target.getInstance(targetNode, policyMetaData);
			            						currentPreExpression.append(z3ExpressionHelper.getFalseTargetExpression(target));
			            						List<Node> childrenAllOfCopy = new ArrayList<Node>();
			            						childrenAllOfCopy.addAll(XMLUtil.getChildNodeList(child));
			            						
			            						for(Node c:childrenAllOfCopy){
			            							child.removeChild(c);
			            						}
			            						child.appendChild(childAllOf);
			            						Target targetTrue = Target.getInstance(targetNode, policyMetaData);
			            						currentPreExpression.append(System.lineSeparator()).append(z3ExpressionHelper.getTrueTargetExpression(targetTrue));
			            						child.removeChild(childAllOf);
			            						for(Node c:childrenAllOfCopy){ 
			            							child.appendChild(c);
			            						}
			            						child.insertBefore(childAllOf, nextChild);
			            						NodeList childs = node.getChildNodes();
			            						boolean flag = true;
			            						if(!XACMLElementUtil.isRule(node) && XMLUtil.isTraversableElement(node)){
			            							for (int j = 0; j < childs.getLength() && flag; j++) {
				            				            Node childCur = childs.item(j);
				            				            if (childCur instanceof Element && XMLUtil.isTraversableElement(childCur) && flag ) {
				            				            	traverseForRPTE((Element)childCur, currentPreExpression,true,false,new ArrayList<Rule>());
				            				            	flag = false;
				            				            
					            				            for (int k = 0; k < childs.getLength(); k++) {
						            				            Node childC = childs.item(k);
						            				            if (childC instanceof Element && XMLUtil.isTraversableElement(childC) && XACMLElementUtil.isRule(childC)) {
						            				            	traverseForRPTE((Element)childC, currentPreExpression,false,true,previousRules);
						            				            }
						            				        }
				            				            }
				            				        }
			            						}
			            					}
			            				}
			            			}
			            		}
			            	}
			            }
		            }
		        }
		    }
		} else if (XACMLElementUtil.isRule(node)) {
	    	List<String> expressions;
	    	if(falsifyPostRules){
	    		expressions = getRuleExpressionForTruthValuesWithPostRules(node,preExpression,previousRules,notForRule);
	    	} else{
	    		expressions = getRuleCoverageExpression(node,preExpression,previousRules);
	    	}
	    	for(String expression:expressions){
	    		boolean sat = Z3StrUtil.processExpression(expression, z3ExpressionHelper);
	    		if (sat == true) {
	    			addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
	    		}
	    	}
		    previousRules.add(Rule.getInstance(node, policyMetaData, null));
		    return;
		}
	}
	public List<String> getRuleCoverageExpression (Element node, StringBuilder preExpression, List<Rule> previousRules) throws IOException, ParsingException, ParserConfigurationException, SAXException{
		return getRuleExpressionForTruthValues(node, preExpression, previousRules,false);
	}


public List<String> getRuleExpressionForTruthValues(Element node, StringBuilder preExpression, List<Rule> previousRules,boolean notForRule) throws IOException, ParsingException, ParserConfigurationException, SAXException{
    Target target = XMLUtil.getTarget(node, policyMetaData);
    Condition condition = XMLUtil.getCondition(node, policyMetaData);
    List<StringBuffer> ruleExpressions = new ArrayList<StringBuffer>();
    if(target != null){
    	StringBuffer ruleExpression = new StringBuffer();
    	if(notForRule){
	    	ruleExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
	    	ruleExpressions.add(ruleExpression);
    	}else{
    		List<String> exps = z3ExpressionHelper.getRPTEExpression(target,policyMetaData);
    		for(String exp:exps){
    			ruleExpressions.add(new StringBuffer(exp + System.lineSeparator()));
    		}
    	}
	    
    }
    if(condition != null){
    	if(ruleExpressions.size() == 0){
    		ruleExpressions.add( new StringBuffer(z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator()));
    	} else{
    		for(StringBuffer ruleExpression:ruleExpressions){
    			ruleExpression.append(z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator());
    		}
    	}
    	
    }
    StringBuffer falsifyPreviousRules = new StringBuffer();
    for(Rule rule:previousRules){
		falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
	}
    List<String> expressions = new ArrayList<String>();
    for(StringBuffer ruleExpression:ruleExpressions){
    	expressions.add(preExpression.toString()+ruleExpression+falsifyPreviousRules);
    }
    return expressions;
}

public List<String> getRuleExpressionForTruthValuesWithPostRules(Element node, StringBuilder preExpression, List<Rule> previousRules,boolean notForRule) throws IOException, ParsingException, ParserConfigurationException, SAXException{
	List<Rule> postRules = new ArrayList<Rule>();
	Node sibling = null;
	Node n = node;
	Node targetNode = XMLUtil.findInChildNodes(node, NameDirectory.TARGET);
	Condition condition = XMLUtil.getCondition(node, policyMetaData);
    
    boolean flag = false;
    List<StringBuffer> ruleExpressions = new ArrayList<StringBuffer>();
    
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
            			if(allOfCount > 1){
            				for(int i = 0; i < childrenAllOf.size(); i++){
            					Node childAllOf = childrenAllOf.get(i);
            					if(childAllOf.getLocalName() !=null && childAllOf.getLocalName().equals("AllOf")){
            						child.removeChild(childAllOf);
            						Node nextChild = childAllOf.getNextSibling();
            						Target target = Target.getInstance(targetNode, policyMetaData);
            						StringBuilder currentPreExpression = new StringBuilder(preExpression.toString());
            						currentPreExpression.append(z3ExpressionHelper.getFalseTargetExpression(target));
            						List<Node> childrenAllOfCopy = new ArrayList<Node>();
            						childrenAllOfCopy.addAll(XMLUtil.getChildNodeList(child));
            						
            						for(Node c:childrenAllOfCopy){
            							child.removeChild(c);
            						}
            						child.appendChild(childAllOf);
            						Target targetTrue = Target.getInstance(targetNode, policyMetaData);
            						currentPreExpression.append(System.lineSeparator()).append(z3ExpressionHelper.getTrueTargetExpression(targetTrue));
            						child.removeChild(childAllOf);
            						for(Node c:childrenAllOfCopy){ 
            							child.appendChild(c);
            						}
            						child.insertBefore(childAllOf, nextChild);
            						ruleExpressions.add(new StringBuffer(currentPreExpression));
            						flag = true;
            					}
            				}
            			}
            		}
            	}
            }
        }
    } 
	
	if(!flag){
		return new ArrayList<String>();
	}

	if(condition != null){
		for(StringBuffer ruleExpression:ruleExpressions){
			ruleExpression.append(z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator());
		}
	}
   
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
	StringBuffer falsifyPreviousRules = new StringBuffer();
    for(Rule rule:previousRules){
		falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
	}
    StringBuffer falsifyPostRules = new StringBuffer();
    for(Rule rule:postRules){
    	falsifyPostRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
	}
    List<String> lst = new ArrayList<String>();
    for(StringBuffer expression:ruleExpressions){
    	lst.add(expression.append(falsifyPreviousRules).append(falsifyPostRules).toString());
    }
	return lst;
}


	
	private void traverseForPermitOrDeny(Element node, StringBuilder preExpression, String mutationMethod) throws ParsingException, IOException, InvalidMutationMethodException {
	    boolean isPolicy = XACMLElementUtil.isPolicy(node);
		if ( isPolicy || XACMLElementUtil.isPolicySet(node)) {
		    Node targetNode = XMLUtil.findInChildNodes(node, NameDirectory.TARGET);
		    Target target;
			if (targetNode != null) {
		        target = Target.getInstance(targetNode, policyMetaData);
		        if(target.getAnyOfSelections().size()>0){
		        	preExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
		        }
		    }
	        NodeList children = node.getChildNodes();
	    	StringBuilder preExpressionCurrent = new StringBuilder(preExpression.toString());
	    	boolean isRule;
    		
	        if(isPolicy){
	        	int effectA, effectB;
	            switch (mutationMethod) {
	                case "createFirstDenyRuleMutants":
	                    effectA = AbstractResult.DECISION_PERMIT;
	                    effectB = AbstractResult.DECISION_DENY;
	                    break;
	                case "createFirstPermitRuleMutants":
	                    effectA = AbstractResult.DECISION_DENY;
	                    effectB = AbstractResult.DECISION_PERMIT;
	                    break;
	                default:
	                	throw new InvalidMutationMethodException("Invalid mutation method");
	            }
	            int state = 0; //  0:starting 1:effectA found 2:effectB found
	        	int j = 0;
	        	boolean continueFlag;
	            while(true){
	            	StringBuilder expression = new StringBuilder();
		            continueFlag = false;
	            	state = 0;
	            	for (int i = 0; i < j; i++) {
	            		Node child = children.item(i);
	            		if(XACMLElementUtil.isRule(child)){
		        			Rule rule = Rule.getInstance(child, policyMetaData, null);
		        			if(XACMLElementUtil.isRule(child)){
		        				expression.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule).append(System.lineSeparator()));
		        			}
	            		}
	            	}
		            for (int i = j; i < children.getLength(); i++) {
		        		Node child = children.item(i);
		        		if(XACMLElementUtil.isRule(child)){
		        			Rule rule = Rule.getInstance(child, policyMetaData, null);
		        			if(rule.getEffect() == effectA && state == 0){
		        				expression.append(z3ExpressionHelper.getTrueTargetTrueConditionExpression(rule)).append(System.lineSeparator());
		        				state = 1;
		        				if(j==0){
		        					j = i;
		        				}
		        			} else if(rule.getEffect() == effectB && state == 1){
		        				expression.append(z3ExpressionHelper.getTrueTargetTrueConditionExpression(rule)).append(System.lineSeparator());
		        				state = 2;
		        			}
		        			else{
		        				expression.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule).append(System.lineSeparator()));
		        			}
		        		}
		        	}
		        	if(state == 2){
		        		boolean sat = Z3StrUtil.processExpression(preExpression + expression.toString(), z3ExpressionHelper);
		    			if (sat == true) {
		    			    addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
		    			    break;
		    			} else{
		    				continueFlag = true;
		    			}
		        	}
		        	if(!continueFlag){
		        		break;
		        	}
		        	j++;
	        	}
	        }else{
	        	for (int i = 0; i < children.getLength(); i++) {
	        		Node child = children.item(i);
	        		if (child instanceof Element && XMLUtil.isTraversableElement(child)) {
	        			traverseForPermitOrDeny((Element)child, preExpressionCurrent, mutationMethod);
	        		}
	        	}
	        }
	        
		}	
    }

}
