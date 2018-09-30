package org.seal.xacml.mutation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.seal.xacml.NameDirectory;
import org.seal.xacml.RequestGeneratorBase;
import org.seal.xacml.TaggedRequest;
import org.seal.xacml.components.CombiningAlgorithmURI;
import org.seal.xacml.coverage.RuleBodyCoverage;
import org.seal.xacml.policyUtils.PolicyLoader;
import org.seal.xacml.semanticMutation.Mutant;
import org.seal.xacml.semanticMutation.Mutator;
import org.seal.xacml.utils.ExceptionUtil;
import org.seal.xacml.utils.RequestBuilder;
import org.seal.xacml.utils.XACMLElementUtil;
import org.seal.xacml.utils.XMLUtil;
import org.seal.xacml.utils.Z3StrUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.wso2.balana.Policy;
import org.wso2.balana.PolicyMetaData;
import org.wso2.balana.Rule;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.xacml3.AllOfSelection;
import org.wso2.balana.xacml3.Target;
import org.xml.sax.SAXException;

public class PNOMutationBasedTestGenerator extends RequestGeneratorBase {
	private AbstractPolicy policy;
	private String currentMutationMethod;
	private boolean trueRuleFlag;
	private boolean falseRuleFlag;
	public PNOMutationBasedTestGenerator(String policyFilePath) throws ParsingException, IOException, SAXException, ParserConfigurationException{
		init(policyFilePath);
		this.policy = PolicyLoader.loadPolicy(new File(policyFilePath));
	}	   
	
	public List<TaggedRequest> generateRequests(List<String> mutationMethods) throws IOException, ParserConfigurationException, ParsingException, SAXException, InvocationTargetException, IllegalAccessException, NoSuchMethodException{
		Mutator mutator = new Mutator(new Mutant(policy, XACMLElementUtil.getPolicyName(policyFilePath)));
		boolean flagLargeSpace = false;
		Class<? extends PNOMutationBasedTestGenerator> cls = this.getClass();
        trueRuleFlag = false;
        falseRuleFlag = false;
        Class[] noParams = {};
        Class[] params = {AbstractPolicy.class};
        
        List<TaggedRequest> taggedRequests = new ArrayList<TaggedRequest>();
		for(String meth:mutationMethods) {
			List<String> methods = new ArrayList<String>();
			methods.add(meth);
 			currentMutationMethod = meth;
			System.out.println(currentMutationMethod);
			String tag = MutationMethodAbbrDirectory.getAbbr(currentMutationMethod);
			setRequests(new ArrayList<String>());
			String methodName = "generate" + tag + "Requests";
			Method method = cls.getDeclaredMethod(methodName, noParams);
			
			
			List<String> requests = (List<String>)method.invoke(this, null);
			
			for(int i = 0; i< requests.size();i++){
					taggedRequests.add(new TaggedRequest(tag,requests.get(i)));
				
			}
			
				
			
		}
		
		return taggedRequests;
	}

	private boolean doRequestPropagatesMutationFault(String request, AbstractPolicy aPolicy, List<Mutant> mutants) throws ParsingException,NoSuchMethodException,InvocationTargetException,IllegalAccessException,ParserConfigurationException,IOException,SAXException{
		String req = request.replaceAll(System.lineSeparator(), " ").trim(); 
		if(req.isEmpty()){
			return false;
		}
		AbstractPolicy p = aPolicy;
		boolean returnFlag = false;
		List<Mutant> tMutants = new ArrayList<Mutant>();
		
		for(Mutant mutant:mutants){
			AbstractPolicy mutantPolicy = mutant.getPolicy();
			int pRes = XACMLElementUtil.evaluateRequestForPolicy(p, req);
			int mRes = XACMLElementUtil.evaluateRequestForPolicy(mutantPolicy, req);
			if (pRes != mRes){
				tMutants.add(mutant);
				returnFlag= true;
			}
		}
		mutants.removeAll(tMutants);
		return returnFlag;
	}
	
	private List<Mutant> notCoveredMutants(List<TaggedRequest> tRequests, List<Mutant> muts) throws ParsingException,NoSuchMethodException,InvocationTargetException,IllegalAccessException,ParserConfigurationException,IOException,SAXException{

		List<Mutant> covered = new ArrayList<Mutant>();
		for(Mutant mut:muts) {
			for(int i = tRequests.size()-1;i >=0;i--) {
				TaggedRequest tr = tRequests.get(i);
				int mRes = XACMLElementUtil.evaluateRequestForPolicy(mut.getPolicy(), tr.getBody());
				int pRes = XACMLElementUtil.evaluateRequestForPolicy(policy, tr.getBody());
				if (pRes != mRes){
					covered.add(mut);
					break;
				}	
				
			}
		}
		muts.removeAll(covered);
		return muts;
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
	
	public List<String> generateCRERequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		currentMutationMethod =  "createRuleEffectFlippingMutants";
		if(trueRuleFlag) {
			return new ArrayList<String>();
		} else {
			trueRuleFlag = true;
		}
		RuleBodyCoverage coverage = new RuleBodyCoverage(policyFilePath);
		return coverage.generateRequestsForTruthValues(true,true,true);
	}
	
	public List<String> generateRTTRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		RuleBodyCoverage coverage = new RuleBodyCoverage(policyFilePath);
		return coverage.generateRequestsForTruthValues(false,true,true);
	}
	
	public List<String> generateRTFRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		return generateCRERequests();
//		RuleCoverage coverage = new RuleCoverage(policyFilePath);
//		return coverage.generateRequestsForTruthValues(true,true,true);
	}
	
	public List<String> generateRCTRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		RuleBodyCoverage coverage = new RuleBodyCoverage(policyFilePath);
		return coverage.generateRequestsForTruthValues(true,false,true);
	}
	
	public List<String> generateRCFRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		return generateCRERequests();
//		RuleCoverage coverage = new RuleCoverage(policyFilePath);
//		return coverage.generateRequestsForTruthValues(true,true,true);
	}
	
	public List<String> generateANFRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		return generateCRERequests();
//		RuleCoverage coverage = new RuleCoverage(policyFilePath);
//		return coverage.generateRequestsForTruthValues(true,true,true);
	}
	
	public List<String> generateRNFRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		return generateCRERequests();
//		RuleCoverage coverage = new RuleCoverage(policyFilePath);
//		return coverage.generateRequestsForTruthValues(true,true,true);
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
		return generateCRERequests();
//		RuleCoverage coverage = new RuleCoverage(policyFilePath);
//		return coverage.generateRequestsForTruthValues(true,true,true);
	}
	
	public List<String> generateRPTERequests() throws IOException, ParsingException, ParserConfigurationException, SAXException {
		traverseForRPTE(doc.getDocumentElement(),new StringBuilder(),true, new ArrayList<Rule>());
		List<String> reqs = getRequests();
		return reqs;
	}
	
	public List<String> generateCCARequests() throws IOException, ParsingException, ParserConfigurationException, SAXException, InvalidMutationMethodException {
		traverseForCCA(doc.getDocumentElement(),new StringBuilder());
		List<String> reqs = getRequests();
		return reqs;
	}
	
	public List<String> generateRCCFRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException, InvalidMutationMethodException {
		traverseForRCCFRequests(doc.getDocumentElement(),new StringBuilder(), new ArrayList<Rule>());
		
		List<String> reqs = getRequests();
		return reqs;
	}
	
	public List<String> generatePCCFRequests() throws IOException, ParsingException, ParserConfigurationException, SAXException, InvalidMutationMethodException {
		traverseForPCCFRequests(doc.getDocumentElement(),new StringBuilder(), false);
		
		List<String> reqs = getRequests();
		return reqs;
	}
	
	private void traverseForRCCFRequests(Element node, StringBuilder preExpression, List<Rule> previousRules) throws ParsingException, IOException, SAXException,ParserConfigurationException {
		if (XACMLElementUtil.isRule(node)) {
			List<String> expressions = getRCCFExpressionForTruthValuesWithPostRules(node,preExpression,previousRules);
	    	for(String expression: expressions){
				boolean sat = Z3StrUtil.processExpression(expression, z3ExpressionHelper);
				if (sat == true) {
				    addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
				}
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
	        }
	        for (int i = 0; i < children.getLength(); i++) {
	            Node child = children.item(i);
	            StringBuilder preExpressionCurrent = new StringBuilder(preExpression.toString());
	            if (child instanceof Element && XMLUtil.isTraversableElement(child)) {
	            	traverseForRCCFRequests((Element) child, preExpressionCurrent, previousRules);
	            }
	        }
		}
	}
	
	private void traverseForPCCFRequests(Element node, StringBuilder preExpression,boolean processRuleFlag) throws ParsingException, IOException, SAXException,ParserConfigurationException {
		if (XACMLElementUtil.isRule(node) && processRuleFlag) {
			String ruleExpression = preExpression.toString() + System.lineSeparator() + getRuleExpression(node).toString();
			boolean sat = Z3StrUtil.processExpression(ruleExpression, z3ExpressionHelper);
			if (sat == true) {
			    addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
			    return;
			}
    	    return;
		}
		
		if (XACMLElementUtil.isPolicy(node) || XACMLElementUtil.isPolicySet(node)) {
		    Node targetNode = XMLUtil.findInChildNodes(node, NameDirectory.TARGET);
		    Target target = null;
		    List<String> targetExpressions = null;
		    if (targetNode != null) {
		        target = Target.getInstance(targetNode, policyMetaData);
		        if(target.getAnyOfSelections().size()>0){
		        	if(processRuleFlag){
		        		targetExpressions = new ArrayList<String>();
		        		targetExpressions.add(z3ExpressionHelper.getTrueTargetExpression(target).toString());
		        	} else{
		        		targetExpressions = getPCCFExpression(node);
		        	}
		        	NodeList children = node.getChildNodes();
			        
		        	for(String targetExp:targetExpressions){
		        		boolean exitFlag = false;
				        for (int i = 0; i < children.getLength(); i++) {
				            Node child = children.item(i);
				            StringBuilder preExpressionCurrent = new StringBuilder(preExpression.toString());
				            preExpressionCurrent.append(System.lineSeparator()+targetExp);
				            
				            if (child instanceof Element && XMLUtil.isTraversableElement(child)) {
				            	traverseForPCCFRequests((Element) child, preExpressionCurrent,true);
				            	exitFlag = true;
				            }
				            if(exitFlag){
				            	break;
				            }
				        }
			        }
		        }
		    } 
			if(!processRuleFlag){
				NodeList children = node.getChildNodes();
	        
				for (int i = 0; i < children.getLength(); i++) {
					Node child = children.item(i);
					StringBuilder preExpressionCurrent = new StringBuilder(preExpression+System.lineSeparator()+z3ExpressionHelper.getTrueTargetExpression(target));
	            
		            if (child instanceof Element && XMLUtil.isTraversableElement(child)) {
		            	traverseForPCCFRequests((Element) child, preExpressionCurrent,false);
		            }
				}
			}
		}
	}
	
	public List<String> getRCCFExpressionForTruthValuesWithPostRules(Element node, StringBuilder preExpression, List<Rule> previousRules) throws ParsingException, SAXException, IOException,ParserConfigurationException  {
		
	    Target target = XMLUtil.getTarget(node, policyMetaData);
	    Condition condition = XMLUtil.getCondition(node, policyMetaData);
	    StringBuffer targetExpression = new StringBuffer();
	    StringBuffer conditionExpression = new StringBuffer();
	    
	    if(target != null){
	    	targetExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
	    }
	    if(condition != null){
	    	conditionExpression.append(z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator());
	    }
	    List<StringBuilder> expressions = new ArrayList<StringBuilder>();
	    if(target!=null ){
	    	
	    	if(target.getAnyOfSelections().size()>0){
	    		StringBuilder targetEncode = new StringBuilder();
	    		target.encode(targetEncode);
	    	List<Target> targets = getTargetsCCF(XMLUtil.loadXMLDocumentFromString(targetEncode.toString()).getDocumentElement(),policyMetaData);
	    	if(!targets.isEmpty()){
	    		String conditionStr = "";
	    		if(condition!=null){
	    			conditionStr=(z3ExpressionHelper.getTrueConditionExpression(condition) + System.lineSeparator());
	    		}
	    		for(Target t:targets){
	    			StringBuilder sb =new StringBuilder(z3ExpressionHelper.getTrueTargetExpression(t) + System.lineSeparator());
	    			sb.append(conditionStr);
	    			expressions.add(new StringBuilder(preExpression + System.lineSeparator() + sb.toString()));
	    		}
	    	}
	    	}
	    }
	    
	    if(condition!=null ){
	    	List<Condition> conditions = getConditionsCCF(XMLUtil.loadXMLDocumentFromString(condition.encode()).getDocumentElement(),policyMetaData);
	    	if(!conditions.isEmpty()){
	    		String targetStr = "";
	    		if(target!=null){
	    			targetStr=(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
	    		}
	    		for(Condition c:conditions){
	    			StringBuilder sb =new StringBuilder(z3ExpressionHelper.getTrueConditionExpression(c) + System.lineSeparator());
	    			sb.append(targetStr);
	    			expressions.add(new StringBuilder(preExpression + System.lineSeparator() + sb.toString()));
	    		}
	    	}
	    }
	    StringBuffer falsifyPreviousRules = new StringBuffer();
	    for(Rule rule:previousRules){
			falsifyPreviousRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
		}                
	    
		
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
	    	falsifyPostRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
		}
	    List<String> finalExpressions = new ArrayList<String>();
	    for(StringBuilder sb:expressions){
	    	sb.append(falsifyPreviousRules + System.lineSeparator() + falsifyPostRules);
	    	finalExpressions.add(sb.toString());
	    }
		
	    return finalExpressions;
	}
	
	
	public List<String> getPCCFExpression(Element node) throws ParsingException, SAXException, IOException,ParserConfigurationException  {
	    Target target = XMLUtil.getTarget(node, policyMetaData);
	    
	    List<String> expressions = new ArrayList<String>();
	    if(target!=null ){
	    	if(target.getAnyOfSelections().size()>0){
	    		StringBuilder targetEncode = new StringBuilder();
	    		target.encode(targetEncode);
		    	List<Target> targets = getTargetsCCF(XMLUtil.loadXMLDocumentFromString(targetEncode.toString()).getDocumentElement(),policyMetaData);
		    	if(!targets.isEmpty()){
		    		for(Target t:targets){
		    			StringBuilder sb =new StringBuilder(z3ExpressionHelper.getTrueTargetExpression(t) + System.lineSeparator());
		    			expressions.add(sb.toString());
		    		}
		    	}
	    	}
	    }
		
	    return expressions;
	}
	
	private static List<Target> getTargetsCCF(Node node,PolicyMetaData policyMetaData) throws ParsingException,ParserConfigurationException,IOException,SAXException{
		Node targetNode = node;
	    List<Target> targets = new ArrayList<Target>();
	    if (!XMLUtil.isEmptyNode(targetNode)) {
	    	String targetString = XMLUtil.nodeToString(targetNode);
	    	for(String key:ComparisionFunctionsMap.functionListMap.keySet()){
	    		int index = targetString.indexOf(key);
	    		if(index > -1){
	    			List<String> replacements = ComparisionFunctionsMap.functionListMap.get(key);
	    			for(String rep:replacements){
	    			String newTargetStr = new String(targetString);
	    			newTargetStr = newTargetStr.replaceAll(key, rep);
	    			Node tNode = (Node)XMLUtil.loadXMLDocumentFromString(newTargetStr).getDocumentElement();
	    		    targets.add(Target.getInstance(tNode, policyMetaData));
	    			}
	    		}
	    	}
		    
		}
	    return targets;
	}
	
	private static List<Condition> getConditionsCCF(Node node,PolicyMetaData policyMetaData) throws ParsingException,ParserConfigurationException,IOException,SAXException{
		Node conditionNode = node;
	    List<Condition> conditions = new ArrayList<Condition>();
	    if (!XMLUtil.isEmptyNode(conditionNode)) {
	    	String conditionString = XMLUtil.nodeToString(conditionNode);
	    	for(String key:ComparisionFunctionsMap.functionListMap.keySet()){
	    		int index = conditionString.indexOf(key);
	    		if(index > -1){
	    			List<String> replacements = ComparisionFunctionsMap.functionListMap.get(key);
	    			for(String rep:replacements){
	    			String newConditionStr = new String(conditionString);
	    			newConditionStr = newConditionStr.replaceAll(key, rep);
	    			Node cNode = (Node)XMLUtil.loadXMLDocumentFromString(newConditionStr).getDocumentElement();
	    		    conditions.add(Condition.getInstance(cNode, policyMetaData,null));
	    			}
	    		}
	    	}
		    
		}
	    return conditions;
	}
	
	private void traverseForRPTE(Element node, StringBuilder preExpression,boolean notForRule,List<Rule> previousRules) throws IOException, ParsingException, ParserConfigurationException, SAXException{
		boolean isPolicy = XACMLElementUtil.isPolicy(node);
		if ( isPolicy || XACMLElementUtil.isPolicySet(node)) {
		    Node targetNode = XMLUtil.findInChildNodes(node, NameDirectory.TARGET);
		    if (isPolicy) {
		    	boolean noParallelTarget = false;
	            
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
				            				            	traverseForRPTE((Element)childCur, currentPreExpression,true,new ArrayList<Rule>());
				            				            	flag = false;
				            				            
					            				            for (int k = 0; k < childs.getLength(); k++) {
						            				            Node childC = childs.item(k);
						            				            if (childC instanceof Element && XMLUtil.isTraversableElement(childC) && XACMLElementUtil.isRule(childC)) {
						            				            	traverseForRPTE((Element)childC, currentPreExpression,false,previousRules);
						            				            }
						            				        }
				            				            }
				            				        }
			            						}
			            						
			            					}
			            				}
			            			}else {
			    			      		noParallelTarget = true;
			    			      	}
			            		}else {
						      		noParallelTarget = true;
						      	}
			            	}else {
					      		noParallelTarget = true;
					      	}
			            }
		            }else {
			      		noParallelTarget = true;
			      	}
		        }else {
		      		noParallelTarget = true;
		      	}
		    	if(noParallelTarget){
			      	NodeList childs = node.getChildNodes();
			      	StringBuilder currentPreExpression = new StringBuilder(preExpression.toString());
			      	Target target = Target.getInstance(targetNode, policyMetaData);
					currentPreExpression.append(z3ExpressionHelper.getTrueTargetExpression(target));
					
					if(XMLUtil.isTraversableElement(node)){
						for (int j = 0; j < childs.getLength(); j++) {
				            Node childC = childs.item(j);
		                    if (childC instanceof Element && XMLUtil.isTraversableElement(childC) && XACMLElementUtil.isRule(childC)){
				            	traverseForRPTE((Element)childC, currentPreExpression,false,previousRules);
				            }
					        
				        }
					}
		      	}
		    } else{
		    	boolean noParallelTarget = false;
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
			            						if(XMLUtil.isTraversableElement(node)){
			            							for (int j = 0; j < childs.getLength(); j++) {
				            				            Node childC = childs.item(j);
		            				                    if (childC instanceof Element && XMLUtil.isTraversableElement(childC) && (XACMLElementUtil.isPolicy(childC)||XACMLElementUtil.isPolicySet(childC))) {
				            				            	traverseForRPTE((Element)childC, currentPreExpression,true,new ArrayList<Rule>());
				            				            }
					            				        
				            				        }
			            						}
			            	
			            					}
			            				}
			            			}
			            			else {
			        		      		noParallelTarget = true;
			        		      	}
			            		}else {
			    		      		noParallelTarget = true;
			    		      	}
			            	}
			            	else {
					      		noParallelTarget = true;
					      	}
			            }
		            }else {
			      		noParallelTarget = true;
			      	}
		      	} else {
		      		noParallelTarget = true;
		      	}
		      	if(noParallelTarget){
			      	NodeList childs = node.getChildNodes();
			      	StringBuilder currentPreExpression = new StringBuilder(preExpression.toString());
			      	Target target = Target.getInstance(targetNode, policyMetaData);
					currentPreExpression.append(z3ExpressionHelper.getTrueTargetExpression(target));
					
					if(XMLUtil.isTraversableElement(node)){
						for (int j = 0; j < childs.getLength(); j++) {
				            Node childC = childs.item(j);
		                    if (childC instanceof Element && XMLUtil.isTraversableElement(childC) && (XACMLElementUtil.isPolicy(childC)||XACMLElementUtil.isPolicySet(childC))) {
				            	traverseForRPTE((Element)childC, currentPreExpression,true,new ArrayList<Rule>());
				            }
				        }
					}
		      	}
			}
		} else if (XACMLElementUtil.isRule(node)) {
	    	List<String> expressions;
	    	expressions = getRuleExpressionForTruthValuesWithPostRules(node,preExpression,previousRules,notForRule);
	    	
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
	


public List<String> getRuleExpressionForTruthValues(Element node, StringBuilder preExpression, List<Rule> previousRules) throws IOException, ParsingException, ParserConfigurationException, SAXException{
    Target target = XMLUtil.getTarget(node, policyMetaData);
    Condition condition = XMLUtil.getCondition(node, policyMetaData);
    List<StringBuffer> ruleExpressions = new ArrayList<StringBuffer>();
    if(target != null){
    	StringBuffer ruleExpression = new StringBuffer();
    		ruleExpression.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
	    	ruleExpressions.add(ruleExpression);
    	
	    
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
    StringBuffer falsifyPostRules = new StringBuffer();
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
    for(Rule rule:postRules){
    	falsifyPostRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
	}
    for(StringBuffer ruleExpression:ruleExpressions){
    	expressions.add(preExpression.toString()+ruleExpression+falsifyPreviousRules+ System.lineSeparator()+falsifyPostRules);
    }
    
    return expressions;
}

public List<String> getRuleExpressionForTruthValuesWithPostRules(Element node, StringBuilder preExpression, List<Rule> previousRules,boolean notForRule) throws IOException, ParsingException, ParserConfigurationException, SAXException{
	if(notForRule){
		return getRuleExpressionForTruthValues(node,preExpression,previousRules);
	}
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
            			if(allOfCount > 0){
            				for(int i = 0; i < childrenAllOf.size(); i++){
            					Node childAllOf = childrenAllOf.get(i);
            					if(childAllOf.getLocalName() !=null && childAllOf.getLocalName().equals("AllOf")){
            						child.removeChild(childAllOf);
            						Node nextChild = childAllOf.getNextSibling();
            						StringBuilder currentPreExpression = new StringBuilder(preExpression.toString());
            						if(allOfCount>1) {
            							Target target = Target.getInstance(targetNode, policyMetaData);
                						
            							currentPreExpression.append(z3ExpressionHelper.getFalseTargetExpression(target) +  System.lineSeparator());
            						}
            						StringBuilder currentPreExpressionMRoot = new StringBuilder(currentPreExpression.toString());
            						List<Node> childrenAllOfCopy = new ArrayList<Node>();
            						childrenAllOfCopy.addAll(XMLUtil.getChildNodeList(child));
            						
            						for(Node c:childrenAllOfCopy){
            							child.removeChild(c);
            						}
            						
            						child.appendChild(childAllOf);
            						Target targetTrue = Target.getInstance(targetNode, policyMetaData);
            						currentPreExpression.append(System.lineSeparator()).append(z3ExpressionHelper.getTrueTargetExpression(targetTrue));
            						List<Node> childrenMatchOf = XMLUtil.getChildNodeList(childAllOf);
	        	            		
            						if( AllOfSelection.getInstance(childAllOf,policyMetaData).getMatches().size() > 1) {
	            						for(int si = 0; si < childrenMatchOf.size(); si++){
	            							Node childMatchOf = childrenMatchOf.get(si);
	    	            					
	    	            					if(childMatchOf.getLocalName() !=null && childMatchOf.getLocalName().equals("Match")){
	    	            						childAllOf.removeChild(childMatchOf);
	    	            						Node nextChildMatchOf = childMatchOf.getNextSibling();
	    	            						Target targetM = Target.getInstance(targetNode, policyMetaData);
	    	            						StringBuilder currentPreExpressionM = new StringBuilder(currentPreExpressionMRoot.toString());
	    	            						currentPreExpressionM.append(z3ExpressionHelper.getTrueTargetExpression(targetM)+  System.lineSeparator());
	    	            						List<Node> childrenMatchOfCopyM = new ArrayList<Node>();
	    	            						childrenMatchOfCopyM.addAll(XMLUtil.getChildNodeList(childAllOf));
	    	            						
	    	            						for(Node c:childrenMatchOfCopyM){
	    	            							childAllOf.removeChild(c);
	    	            						}
	    	            						childAllOf.appendChild(childMatchOf);
	    	            						Target targetTrueM = Target.getInstance(targetNode, policyMetaData);
	    	            						currentPreExpressionM.append(System.lineSeparator()).append(z3ExpressionHelper.getFalseTargetExpression(targetTrueM)+  System.lineSeparator());
	    	            						childAllOf.removeChild(childMatchOf);
	    	            						for(Node c:childrenMatchOfCopyM){ 
	    	            							childAllOf.appendChild(c);
	    	            						}
	    	            						childAllOf.insertBefore(childMatchOf, nextChildMatchOf);
	    	            						ruleExpressions.add(new StringBuffer(currentPreExpressionM));
	    	            						
	    	            					}
	    	            					
	    	            				}		
	            					}
            						child.removeChild(childAllOf);
            						for(Node c:childrenAllOfCopy){ 
            							child.appendChild(c);
            						}
            						child.insertBefore(childAllOf, nextChild);
            						if(allOfCount > 1) {
            							ruleExpressions.add(new StringBuffer(currentPreExpression));
            						}
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
//    for(Rule rule:postRules){
//    	falsifyPostRules.append(z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule) + System.lineSeparator());
//	}
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
		        		String finalExpression = preExpression + expression.toString();
		        		boolean sat = Z3StrUtil.processExpression(finalExpression, z3ExpressionHelper);
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
	        		StringBuilder preExpressionCurrent = new StringBuilder(preExpression.toString());
	    	    	
	        		if (child instanceof Element && XMLUtil.isTraversableElement(child)) {
	        			traverseForPermitOrDeny((Element)child, preExpressionCurrent, mutationMethod);
	        		}
	        	}
	        }
	        
		}	
    }
	
	private void traverseForCCA(Element node, StringBuilder preExpression) throws ParsingException, IOException, InvalidMutationMethodException {
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
	       if(isPolicy){
	    	    Policy p = Policy.getInstance(node);
	        	String ca = p.getCombiningAlg().getIdentifier().toString();
	        	if(ca.equals(CombiningAlgorithmURI.map.equals("PO")) || ca.equals(CombiningAlgorithmURI.map.equals("OPO")) ||ca.equals(CombiningAlgorithmURI.map.equals("DUP"))) {
	        		falsifyRulesFlag = 1;
	        	} else if(ca.equals(CombiningAlgorithmURI.map.equals("DO")) || ca.equals(CombiningAlgorithmURI.map.equals("ODO")) ||ca.equals(CombiningAlgorithmURI.map.equals("PUD"))) {
	        		falsifyRulesFlag = 2;
	        	} else {
	        		falsifyRulesFlag = 0;
	        	}
		        
	        	List<EffectTCMap> lst = new ArrayList<EffectTCMap>();
	        	String errExpression = "";
	        	for (int i = 0; i < children.getLength(); i++) {
	        		Node child = children.item(i);
	        		if(XACMLElementUtil.isRule(child)){
	        			Rule rule = Rule.getInstance(child, policyMetaData, null);
	        			String expression = z3ExpressionHelper.getTrueTargetTrueConditionExpression(rule).append(System.lineSeparator()).toString();
	        			if((rule.getEffect()==0 && falsifyRulesFlag == 2) || (rule.getEffect()==1 && falsifyRulesFlag == 1)) {
	        				errExpression += z3ExpressionHelper.getTrueTargetTrueConditionExpression(rule).append(System.lineSeparator()).toString();
	        	    	} else {
	        	    		errExpression += z3ExpressionHelper.getFalseTargetFalseConditionExpression(rule).append(System.lineSeparator()).toString();
	        	    	}
	        			int effect = rule.getEffect();	
	        			EffectTCMap entry = new EffectTCMap(effect,expression);
	        			lst.add(entry);
	        		}
	        	}
	        	int count = lst.size() - 1;
	        	boolean flag = false;
	        	for(int i = 0; i < count; i++){
	        		EffectTCMap entryI = lst.get(i);
	        		
	        		for(int j = i+1; j <= count; j++){
	        			EffectTCMap entryJ = lst.get(j);
	        			if(entryI.getEffect()==entryJ.getEffect()){
	        				continue;
	        			} else {
	        				String exp = preExpression.toString() + System.lineSeparator() + entryI.getExpression() + System.lineSeparator() + entryJ.getExpression();
	        				boolean sat = Z3StrUtil.processExpression(exp, z3ExpressionHelper);
			    			if (sat == true) {
			    			    addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
			    			    flag = true;
			    			    break;
			    			} 
	        			} 
		        	}
	        		if(flag){
	        			break;
	        		}
	        	}
	        	
				String errExp = preExpression.toString() + errExpression;
				boolean sat = Z3StrUtil.processExpression(errExp, z3ExpressionHelper);
    			if (sat == true) {
    			    addRequest(RequestBuilder.buildAllIDRequest(z3ExpressionHelper.getAttributeList()));
    			} 
	
	         }else{
	        	boolean foundFlag = false;
	        	List<Node> nodeLst = new ArrayList<Node>();
	        	List<String> targetExpLst = new ArrayList<String>();
	        	
	        	for (int i = 0; i < children.getLength(); i++) {
	        		Node child = children.item(i);
	        		
	        		if (child instanceof Element && (XACMLElementUtil.isPolicy(child) || XACMLElementUtil.isPolicySet(child)) ){
	        			nodeLst.add(child);
	        			Node tNode = XMLUtil.findInChildNodes(child, NameDirectory.TARGET);
		    		    if(tNode!=null){
		    		    	Target t = Target.getInstance(tNode, policyMetaData);
		    		        if(t.getAnyOfSelections().size()>0){
		    		        	targetExpLst.add(z3ExpressionHelper.getTrueTargetExpression(t).toString());
		    		        }else{
			    		    	targetExpLst.add(""); 
			    		    }
		    		    } else{
		    		    	targetExpLst.add(""); 
		    		    }
	        		}
	        	} 	
	        	int count = nodeLst.size() -1;
	        	for (int i = 0; i < count; i++) {
	        		String tExp1 = targetExpLst.get(i);
	        		for(int j = i+1; j < count; j++){
	        			String tExp2 = targetExpLst.get(j);
	        			String decisionExp = preExpression.toString() + System.lineSeparator() + tExp1 + System.lineSeparator() + tExp2;
	        			boolean sat = Z3StrUtil.processExpression(decisionExp, z3ExpressionHelper);
	        			if (sat == true) {
	        			    List<EffectTCMap> lst1 = getEffectTCMapForPolicyRules(nodeLst.get(i),new StringBuilder());
	        			    List<EffectTCMap> lst2 = getEffectTCMapForPolicyRules(nodeLst.get(j),new StringBuilder());
	        			    for(int k = 0; k < lst1.size();k++){
	        			    	EffectTCMap m1 = lst1.get(k);
	        			    	for(int l = 0; l < lst1.size();l++){
	        			    		EffectTCMap m2 = lst1.get(l);
	        			    		if(m1.getEffect()!=m2.getEffect()){
	        			    			String reqExp = decisionExp + System.lineSeparator() + m1.getExpression() + System.lineSeparator() + m2.getExpression();
	        			    			sat = Z3StrUtil.processExpression(reqExp, z3ExpressionHelper);
	        			    			if (sat == true) {
	        			    			    addRequest(RequestBuilder.buildRequest(z3ExpressionHelper.getAttributeList()));
	        			    			    foundFlag = true;
	        			    			    break;
	        			    			}
	        			    		}
		        			    }	
	        			    	if(foundFlag){
	        			    		break;
	        			    	}
	        			    }
	        			}
	        	    	if(foundFlag){
    			    		break;
    			    	}
    				}
	            	if(foundFlag){
			    		break;
			    	}
				}
	        	for (int i = 0; i < children.getLength(); i++) {
	        		Node child = children.item(i);
	        		StringBuilder preExpressionCurrent = new StringBuilder(preExpression.toString());
	    	    	
	        		if (child instanceof Element && XMLUtil.isTraversableElement(child)) {
	        			traverseForCCA((Element)child, preExpressionCurrent);
	        		}
	        	}
	        }
		}	
    }
	
	private List<EffectTCMap> getEffectTCMapForPolicyRules(Node node, StringBuilder preExp) throws ParsingException{
		boolean isPolicy = XACMLElementUtil.isPolicy(node);
		List<EffectTCMap> lst = new ArrayList<EffectTCMap>();
		if ( isPolicy || XACMLElementUtil.isPolicySet(node)) {
			NodeList children = node.getChildNodes();
	    	if(isPolicy){
	        	for (int i = 0; i < children.getLength(); i++) {
	        		Node child = children.item(i);
	        		if(XACMLElementUtil.isRule(child)){
	        			Rule rule = Rule.getInstance(child, policyMetaData, null);
	        			String expression = preExp.toString() + System.lineSeparator() + z3ExpressionHelper.getTrueTargetTrueConditionExpression(rule).append(System.lineSeparator()).toString();
	        			int effect = rule.getEffect();	
	        			EffectTCMap entry = new EffectTCMap(effect,expression);
	        			lst.add(entry);
	        		}
	        	}
	        } else{
	        	for (int i = 0; i < children.getLength(); i++) {
	        		Node child = children.item(i);
	        		Node targetNode = XMLUtil.findInChildNodes(child, NameDirectory.TARGET);
	    		    Target target;
	    		    StringBuilder targetExp = new StringBuilder();
	    			if (targetNode != null) {
	    		        target = Target.getInstance(targetNode, policyMetaData);
	    		        if(target.getAnyOfSelections().size()>0){
	    		        	targetExp.append(z3ExpressionHelper.getTrueTargetExpression(target) + System.lineSeparator());
	    		        }
	    		    } 
	        		StringBuilder curPreExp = new StringBuilder(preExp.append(System.lineSeparator()).append(targetExp));
	        		if(XACMLElementUtil.isPolicy(child)|| XACMLElementUtil.isPolicySet(child)){
	        			lst.addAll(getEffectTCMapForPolicyRules(child, curPreExp));
	        		}
	        	}
	        }
		}
		return lst;
	}
	
	class EffectTCMap{
		int effect;
		String expression;
		public EffectTCMap(int e,String exp){
			this.effect = e;
			this.expression = exp;
		}
		
		public int getEffect(){
			return effect;
		}
		
		public String getExpression(){
			return expression;
		}
	}

}
