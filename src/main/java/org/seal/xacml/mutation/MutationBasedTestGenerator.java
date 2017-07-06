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
import org.seal.xacml.RequestGeneratorBase;
import org.seal.xacml.TaggedRequest;
import org.seal.xacml.coverage.RuleCoverage;
import org.seal.xacml.utils.FileIOUtil;
import org.seal.xacml.utils.RequestBuilder;
import org.seal.xacml.utils.XACMLElementUtil;
import org.seal.xacml.utils.Z3StrUtil;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.wso2.balana.Rule;
import org.wso2.balana.combine.CombiningAlgorithm;
import org.wso2.balana.combine.xacml3.DenyOverridesRuleAlg;
import org.wso2.balana.combine.xacml3.DenyUnlessPermitRuleAlg;
import org.wso2.balana.combine.xacml3.PermitOverridesRuleAlg;
import org.wso2.balana.combine.xacml3.PermitUnlessDenyRuleAlg;
import org.wso2.balana.xacml3.AnyOfSelection;
import org.wso2.balana.xacml3.Target;
import org.xml.sax.SAXException;

public class MutationBasedTestGenerator extends RequestGeneratorBase {
	private AbstractPolicy policy;
	public MutationBasedTestGenerator(String policyFilePath) throws ParsingException, IOException, SAXException, ParserConfigurationException{
		init(policyFilePath);
		this.policy = PolicyLoader.loadPolicy(new File(policyFilePath));
	}	   
	
	public List<TaggedRequest> generateRequests(List<String> mutationMethods) throws IOException, ParserConfigurationException, ParsingException, SAXException, InvocationTargetException, IllegalAccessException, NoSuchMethodException{
		Mutator mutator = new Mutator(new Mutant(policy, XACMLElementUtil.getPolicyName(policyFilePath)));
        Map<String,List<Mutant>> mutantsMap = mutator.generateMutantsCategorizedByMethods(mutationMethods);
        Class<? extends MutationBasedTestGenerator> cls = this.getClass();
        Class[] params = {AbstractPolicy.class};
        Class[] noParams = {};
        
        List<TaggedRequest> taggedRequests = new ArrayList<TaggedRequest>();
		for(Map.Entry<String, List<Mutant>> e:mutantsMap.entrySet()){
			List<Mutant> mutants = (List<Mutant>)e.getValue();
			String mutationMethod = e.getKey().toString();
			String tag = MutationMethodAbbrDirectory.getAbbr(mutationMethod);
			String methodName = "generate" + tag + "Requests";
			Method method = cls.getDeclaredMethod(methodName, noParams);
			List<String> requests = (List<String>)method.invoke(this, null);
			int j = 0;
			for(Mutant mutant:mutants){
				for(int i = j; i< requests.size();i++){
					String mutantForPropagationForMutant = MutationMethodForPropagationForMutantDirectory.getMutationMethod(mutationMethod);
					if(!mutantForPropagationForMutant.equalsIgnoreCase("SELF")){
						Class klass = Mutator.class;
						Method m = klass.getDeclaredMethod(mutantForPropagationForMutant, params);
						mutant = (Mutant) m.invoke(new Mutator(mutant), mutant.getPolicy());
					}
					File f = new File(mutant.getName());//
					FileIOUtil.writeFile(f, mutant.encode());//
					AbstractPolicy p;
					String mutantForPropagationForPolicy = MutationMethodForPropagationForPolicyDirectory.getMutationMethod(mutationMethod);
					
					if(mutantForPropagationForPolicy.equalsIgnoreCase("SELF")){
						p = policy;
					} else{
						Class klass = Mutator.class;
						Method m = klass.getDeclaredMethod(mutantForPropagationForPolicy, params);
						Mutant mut = (Mutant) m.invoke(new Mutator(new Mutant(policy,"")), policy);
						p = mut.getPolicy();
					}
					if(doRequestPropagatesMutationFault(requests.get(i), p, mutant)){
						File r = new File(tag+(i+1));//
						FileIOUtil.writeFile(r, requests.get(i));//
						taggedRequests.add(new TaggedRequest(tag,requests.get(i)));
						j = i+1;
						break;
					}
				}
			}
		}
		return taggedRequests;
	}

	private boolean doRequestPropagatesMutationFault(String request, AbstractPolicy policy, Mutant mutant) throws ParsingException{
		String req = request.replaceAll(System.lineSeparator(), " ").trim(); 
		if(req.isEmpty()){
			return false;
		}
		AbstractPolicy mutantPolicy = mutant.getPolicy();
		int pRes = XACMLElementUtil.evaluateRequestForPolicy(policy, req);
		int mRes = XACMLElementUtil.evaluateRequestForPolicy(mutantPolicy, req);
		if (pRes == mRes){
			return false;
		} else {
			return true;
		}
	}
	
	public List<String> generatePTTRequests() throws IOException{
		if(!policy.isTargetEmpty()){
			Target policyTarget = (Target)policy.getTarget();
			List<AnyOfSelection> anyOf = policyTarget.getAnyOfSelections();
			if(anyOf.size() != 0){
				String expression = z3ExpressionHelper.getFalseTargetExpression(policyTarget).toString();
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
	}
	
	public List<String> generatePTFRequests() throws IOException{
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
	}
	
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
}
