package org.seal.mutation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.seal.combiningalgorithms.MyAttr;
import org.seal.combiningalgorithms.PolicyX;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.AbstractTarget;
import org.wso2.balana.DOMHelper;
import org.wso2.balana.Policy;
import org.wso2.balana.PolicyTreeElement;
import org.wso2.balana.Rule;
import org.wso2.balana.TargetMatch;
import org.wso2.balana.combine.CombinerElement;
import org.wso2.balana.combine.RuleCombiningAlgorithm;
import org.wso2.balana.combine.xacml2.FirstApplicableRuleAlg;
import org.wso2.balana.combine.xacml3.DenyOverridesRuleAlg;
import org.wso2.balana.combine.xacml3.DenyUnlessPermitRuleAlg;
import org.wso2.balana.combine.xacml3.PermitOverridesRuleAlg;
import org.wso2.balana.combine.xacml3.PermitUnlessDenyRuleAlg;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.xacml3.AllOfSelection;
import org.wso2.balana.xacml3.AnyOfSelection;
import org.wso2.balana.xacml3.Target;


public class PolicyMutator {
    
	private String policyFilePath;
	private Policy policy;
	private PolicyMutant baseMutant;
	private String mutantSpreadSheetFolderName;
	private String mutantFileNameBase;
	private String mutantSpreadSheetNameBase;
	private ArrayList<PolicyMutant> mutantList = new ArrayList<PolicyMutant>();
	private static Pattern ruleIdPattern = Pattern.compile("<Rule\\s+RuleId\\s*=\\s*\"([^\"]+)\"");
	
	// so far only string and integer are considered.					
	String int_function = "urn:oasis:names:tc:xacml:1.0:function:integer-equal";
	String str_function = "urn:oasis:names:tc:xacml:1.0:function:string-equal";
	String int_function_one_and_only = "urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only";
	String str_function_one_and_only = "urn:oasis:names:tc:xacml:1.0:function:string-one-and-only";
	String str_value = "RANDOM$@^$%#&!";
	String str_value1 = "str_A";
	String str_value2 = "str_B";
	String int_value = "-98274365923795632";
	String int_value1 = "123456789";
	String int_value2 = "-987654321";

	public PolicyMutator(PolicyMutant baseMutant) throws Exception {
		this.baseMutant = baseMutant;
		init(baseMutant.getMutantFilePath());
	}

	public PolicyMutator(String policyFilePath) throws Exception {
		this.baseMutant = new PolicyMutant("", policyFilePath, new int[]{});
		init(policyFilePath);
	}

	private void init(String policyFilePath) {
		this.policyFilePath = policyFilePath;
		policy = loadPolicy(policyFilePath);
		File policyFile = new File(policyFilePath);
		File mutantsFolder = new File(policyFile.getParent()+File.separator+"mutants");
		if (!mutantsFolder.exists() || !mutantsFolder.isDirectory()){
			mutantsFolder.mkdir();
		}
		//mutantFileNameBase = policyFile.getParent() +File.separator+"mutants"+File.separator+policyFile.getName();
		String policyPath = policyFile.getAbsolutePath();
		//System.out.println(tempPath);
		int indexOfLastSlash = policyPath.lastIndexOf(File.separator);
//		mutantSpreadSheetFolderName = policyPath.substring(0, indexOfLastSlash);
//		mutantFileNameBase = mutantSpreadSheetFolderName.concat("/mutants").concat(policyPath.substring(indexOfLastSlash));
//		mutantSpreadSheetNameBase = mutantSpreadSheetFolderName.concat(policyPath.substring(indexOfLastSlash));
		//System.out.println(mutantFileNameBase);

		mutantSpreadSheetFolderName = mutantsFolder.getAbsolutePath();
		mutantFileNameBase = mutantSpreadSheetFolderName.concat(policyPath.substring(indexOfLastSlash));
		mutantSpreadSheetNameBase = mutantSpreadSheetFolderName.concat(policyPath.substring(indexOfLastSlash));
	}
	
	public PolicySpreadSheetMutantSuite generateMutants(){
//		try {
//			createOneMutant();
//			//createAllMutants();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		removeDirectoryMutantFileName();
		System.out.println(getMutantsSpreadSheetFileName());
		PolicySpreadSheetMutantSuite.writePolicyMutantsSpreadSheet(mutantList, getMutantsSpreadSheetFileName());
		System.out.println(mutantList.size() + " total mutants");
		System.out.println(policyFilePath);
		System.out.println(mutantSpreadSheetFolderName);
		return new PolicySpreadSheetMutantSuite(mutantSpreadSheetFolderName, mutantList, policyFilePath);
	}

	private void removeDirectoryMutantFileName(){
		for (PolicyMutant policyMutant: mutantList){
			policyMutant.removeDirectoryFromFilePath();
		}
	}
	
	private static int[] appendArray(int[] origArray, int elem) {
		int[] newArray = Arrays.copyOf(origArray, origArray.length + 1);
		newArray[origArray.length] = elem;
		return newArray;
	}
	public void createAllMutants() throws Exception {
		//createTheOriginalPolicy();			// ORG for comparison
		// Policy mutants-------------------------------------
		createPolicyTargetTrueMutants(); // PTT
		createPolicyTargetFalseMutants(); // PTF
		createCombiningAlgorithmMutants(); // CRC
		// Rule mutants---------------------------------------
		createRuleEffectFlippingMutants(); // CRE
		createRemoveRuleMutants();		// RER use i/o
		createAddNewRuleMutants();		// ANR use i/o

		createRuleTargetTrueMutants();	// RTT
		createRuleTargetFalseMutants(); // RTF use i/o : not working for 2.0 (casting abstractTarget to Target)
		
		createRuleConditionTrueMutants();	// RCT
		createRuleConditionFalseMutants(); // RCF use i/o: not working for 2.0 (casting abstractTarget to Target)
		
		createFirstPermitRuleMutants(); // FPR
		createFirstDenyRuleMutants(); // FDR
		
		createRuleTypeReplacedMutants(); // RTR // TODO
		// Function mutants------------------------------------
		//createChangeComparisonFunctionMutants(); // FCF
		//createChangeComparisonFunctionMutants(); // CCF
		createAddNotFunctionMutants(); // ANF
		createRemoveNotFunctionMutants(); // RNF
		
		createRemoveParallelTargetElementMutants(); // RPTE
		createRemoveParallelConditionElementMutants(); // RPCE // TODO
		//=====================================================	
	}

	public void createSelectedMutants(List<String> selectedMutants) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> cls = this.getClass();
		for (String methodName: selectedMutants) {
			Method method = cls.getDeclaredMethod(methodName);
			method.invoke(this);
		}
	}

	public void createOneMutant() {
		createPolicyTargetTrueMutants(); // PTT		
	}


	// Policy Related Mutation Operators----------------------------------------------------------
	// PTT
	/**
	 * Remove the Target of each Policy ensuring that the Policy is applied to all requests.
	 * @return 
	 */
	public List<PolicyMutant> createPolicyTargetTrueMutants() {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		
		int mutantIndex=1;
		int bugPosition = 0;
		if(!policy.isTargetEmpty()) {
			AbstractTarget target = policy.getTarget();
			// Analyze AnyOf... The target might still be empty.
			List<AnyOfSelection> listAnyOf = ((Target)target).getAnyOfSelections();
			//System.out.println("Size = " + listAnyOf.size());
			if (listAnyOf.size()!=0) {
				policy.setTargetEmpty();				
				
				StringBuilder builder = new StringBuilder();
				policy.encode(builder);
				String mutantFileName = getMutantFileName("PTT"+mutantIndex);
				int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
				PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" PTT"+mutantIndex, mutantFileName, bugPositions);
				mutants.add(mutant);
				mutantList.add(mutant);
				saveStringToTextFile(builder.toString(), mutantFileName);
	
				policy.setTarget(target);
			}
		}
		return mutants;
	}
	// PTF
	/**
	 * Set policy target always false.
	 * @return 
	 * @throws Exception
	 */
	public List<PolicyMutant> createPolicyTargetFalseMutants() throws Exception {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		
		int mutantIndex=1;
		int bugPosition = 0;
		// Collect attributes from targets and conditions.
		ArrayList<MyAttr> attr = collectAttributes(policy);
		
		if(attr.size()<1) {
			throw new Exception("No attribute collected");
		} else {
			// build up a false target
			String falseTarget = buildFalseTarget(attr);
			// create a single mutation for the policy.
			StringBuilder builder = new StringBuilder();
			policy.encode(builder);

			// default policy target starting/ending index.
			int policyTargetStartingIndex = builder.indexOf(">\n") + 2;
			int targetEndingIndex = policyTargetStartingIndex;
			// if there exists a target, update the targetEndingIndex; otherwise, use default.
			if(!policy.isTargetEmpty()) {
				targetEndingIndex = builder.indexOf("</Target>", policyTargetStartingIndex) + 9+1; // +1 to include '\n'		
			} 
			// Replace the old target with the false target.
			builder.replace(policyTargetStartingIndex, targetEndingIndex, falseTarget);
			String mutantFileName = getMutantFileName("PTF"+mutantIndex);
			int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
			PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" PTF"+mutantIndex, mutantFileName, bugPositions);
			mutants.add(mutant);
			mutantList.add(mutant);					
			saveStringToTextFile(builder.toString(), mutantFileName);				
		}
		return mutants;
	}
	// CRC
	/**
	 * Replaces the existing rule combining algorithm with another rule combining algorithm. 
	 * The set of considered rule combining algorithms is
	 * {deny-overrides, permit-overrides, first-applicable}.
	 * @return 
	 */
	public List<PolicyMutant> createCombiningAlgorithmMutants(){
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		
		RuleCombiningAlgorithm[] combiningAlgorithms = {new DenyOverridesRuleAlg(), 
				new PermitOverridesRuleAlg(), new DenyUnlessPermitRuleAlg(), 
				new PermitUnlessDenyRuleAlg(), new FirstApplicableRuleAlg()};
		int mutantIndex=1;
		for (RuleCombiningAlgorithm algorithm: combiningAlgorithms){
			if (!policy.getCombiningAlg().getIdentifier().equals(algorithm.getIdentifier())) {
				RuleCombiningAlgorithm originalAlgorithm = (RuleCombiningAlgorithm)policy.getCombiningAlg();
				policy.setCombiningAlg(algorithm);
				StringBuilder builder = new StringBuilder();
				policy.encode(builder);
				String mutantFileName = getMutantFileName("CRC"+mutantIndex);
				int bugPosition = -1;
				int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
				PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" CRC"+mutantIndex, mutantFileName, bugPositions);
				mutants.add(mutant);
				mutantList.add(mutant);
				saveStringToTextFile(builder.toString(), mutantFileName);
				policy.setCombiningAlg(originalAlgorithm);				
				mutantIndex++;
			}
		}
		return mutants;
	}
	
	// Rule Related Mutation Operators------------------------------------------------------------
	// CRE...
	/**
	 * Changes the rule effect by replacing Permit with Deny or Deny with Permit.
	 */
	public void createRuleEffectFlippingMutants(){
		int mutantIndex=1;
		for (CombinerElement rule : policy.getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule myrule = (Rule) tree;
				createRuleEffectFlippingMutants(myrule, mutantIndex);
				mutantIndex++;
			}
		}
	}
	
	public List<PolicyMutant> createRuleEffectFlippingMutants(Rule myrule, int mutantIndex) {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		
		int originalEffect = myrule.getEffect();
		if (originalEffect==AbstractResult.DECISION_DENY)
			myrule.setEffect(AbstractResult.DECISION_PERMIT);
		else
			myrule.setEffect(AbstractResult.DECISION_DENY);
		StringBuilder builder = new StringBuilder();
		policy.encode(builder);
		String mutantFileName = getMutantFileName("CRE"+mutantIndex);
		int bugPosition = mutantIndex;
		int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
		PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" CRE"+mutantIndex, mutantFileName, bugPositions);
		mutantList.add(mutant);
		mutants.add(mutant);
		saveStringToTextFile(builder.toString(), mutantFileName);
		myrule.setEffect(originalEffect);
		return mutants;
	}
	
//	private void createRemoveRuleMutants() throws Exception {
//		//int mutantIndex=1;
//		
//		for (int ruleIndex = 0; ruleIndex<policy.getChildElements().size(); ruleIndex++) {
//			
//			CombinerElement rule = policy.getChildElements().get(ruleIndex);
//			
//			PolicyTreeElement tree = rule.getElement();
//			if (tree instanceof Rule) {
//				
//				policy.getChildElements().remove(rule);
//				Rule myrule = (Rule) tree;
//				String id = myrule.getId().toString();
//				int effect = myrule.getEffect();
//				StringBuilder builder = new StringBuilder();
//				policy.encode(builder);
//				String mutantFileName = getMutantFileName("RER"+ruleIndex);
//				mutantList.add(new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" RER"+ ruleIndex, mutantFileName, ruleIndex));
//				
//				saveStringToTextFile(builder.toString(), mutantFileName);
//				policy.getChildElements().add(ruleIndex, rule);
//			}
//		}
//	}	
//	// unmodifiableCollection
	
	// RER use i/o.
	/**
	 * Chooses one rule and removes it. 
	 * @throws Exception
	 */
	public void createRemoveRuleMutants() throws Exception {
		int mutantIndex=1;
		int maxRules = policy.getChildElements().size();
		for (CombinerElement rule : policy.getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				//policy.getChildElements().remove(rule);
				Rule myrule = (Rule) tree;
				createRemoveRuleMutants(myrule, mutantIndex, maxRules);
				mutantIndex++;
			}
		}
	}
	
	public List<PolicyMutant> createRemoveRuleMutants(Rule myrule, int mutantIndex, int maxRules) throws Exception {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		
		String id = myrule.getId().toString();
		int effect = myrule.getEffect();
		//System.out.println(effect);
		//System.out.println(id);
		// print the original and then modify it in text form.
		StringBuilder builder = new StringBuilder();
		policy.encode(builder);
		String mutantFileName = getMutantFileName("RER"+mutantIndex);
		int bugPosition = maxRules;
		int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
		PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" RER"+mutantIndex, mutantFileName, bugPositions);
		mutantList.add(mutant);
		mutants.add(mutant);
//		policy.getChildElements().add(mutantIndex, rule);
		
		// find the corresponding rule from the string builder and remove it from the policy.
		int ruleStartingIndex = builder.indexOf("<Rule RuleId=\"" + id + "\" Effect=\"" + (effect==0 ? "Permit" : "Deny") + "\"  >"); 
		//System.out.println(ruleStartingIndex);
		int ruleEndingIndex = builder.indexOf("</Rule>", ruleStartingIndex) + 7+1; // +1 to kill the blank line.
		//System.out.println(ruleEndingIndex);
		
		builder.replace(ruleStartingIndex, ruleEndingIndex, "");
		saveStringToTextFile(builder.toString(), mutantFileName);
		return mutants;
	}
	// ANR use i/o, bugposition = ? we let it be the rule based on which we generate mutants.
	// TODO: add more types of new rules.
	/**
	 * Adds a new rule containing a new combination of parameters 
	 * that is not specified in the existing rules of the policy 
	 * (the new rule will have the Permit and Deny effect). 
	 * Append the additional rule right after the rule.
	 */
	public void createAddNewRuleMutants() {
		int ruleIndex = 1;
		for (CombinerElement rule : policy.getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				// More ways of adding a rule.
				// Adding a rule method 1: Adding a mutant rule by flipping the rule effect
				// Adding a rule method 2: Adding a mutant rule by adding the same rule with target-always-true.
				Rule myrule = (Rule) tree;
				createAddNewRuleMutants(myrule, ruleIndex);
				ruleIndex++;
			}
		}
	}
	//changed mutant file name convention
	public List<PolicyMutant> createAddNewRuleMutants(Rule myrule, int ruleIndex) {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		
		int mutantIndex = 1;
		String id = myrule.getId().toString();
		int effect = myrule.getEffect();
		
		StringBuilder builder1 = new StringBuilder();
		StringBuilder builder2 = new StringBuilder();
		policy.encode(builder1);
		policy.encode(builder2);

		// find the corresponding rule from the string builder, create a duplicate rule for each method.
		int ruleStartingIndex = builder1.indexOf("<Rule RuleId=\"" + id + "\" Effect=\"" + (effect==0 ? "Permit" : "Deny") + "\"  >"); 
		int ruleEndingIndex = builder1.indexOf("</Rule>", ruleStartingIndex) + 7+1; // +1 to include '\n'.
		// pull out the rule:
		String theRule = builder1.substring(ruleStartingIndex, ruleEndingIndex);
		String mutantRule1 = "";
		// method 1: append a new rule with negative effect and false target.
		if (effect==0) {
			mutantRule1 = theRule.replace("Effect=\"" + "Permit", "Effect=\"" + "Deny"); 
		} else {
			mutantRule1 = theRule.replace("Effect=\"" + "Deny", "Effect=\"" + "Permit");
		}
		//replace rule id with a random long string to avoid duplicate rule id
		Matcher m = PolicyMutator.ruleIdPattern.matcher(mutantRule1);
		if (m.find()) {
			int ruleIdStart = m.start(1);
			int ruleIdEnd = m.end(1);
			String randStr = UUID.randomUUID().toString();
			mutantRule1 = new StringBuilder(mutantRule1).replace(ruleIdStart,
					ruleIdEnd, randStr).toString();
		}
		// Append the mutant rule to the end of the rule.
		builder1.replace(ruleEndingIndex, ruleEndingIndex, mutantRule1);
		// Save mutation
		String mutantFileName1 = getMutantFileName("ANR"+ruleIndex + "_" + mutantIndex);
		int bugPosition = ruleIndex;
		int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
		PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" ANR"+ruleIndex + "_" + mutantIndex, mutantFileName1, bugPositions);
		mutantList.add(mutant);
		mutants.add(mutant);
		saveStringToTextFile(builder1.toString(), mutantFileName1);
		mutantIndex++;
		// method 2: when there is a target, append a new rule with empty target.
		if(!myrule.isTargetEmpty()){
			String mutantRule2 = "";
			// default target starting/ending index.
			int targetStartingIndex = theRule.indexOf("<Target>");
			int targetEndingIndex = theRule.indexOf("</Target>", targetStartingIndex) + 9+1; // +1 to include '\n'	
			mutantRule2 = theRule.replace(theRule.substring(targetStartingIndex, targetEndingIndex), "");
			// Append the mutant rule to the end of the rule.
			builder2.replace(ruleEndingIndex, ruleEndingIndex, mutantRule2);
			
			String mutantFileName2 = getMutantFileName("ANR"+ruleIndex+"_"+mutantIndex);
			bugPosition = ruleIndex;
			bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
			mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" ANR"+ruleIndex+"_"+mutantIndex, mutantFileName2, bugPositions);
			mutantList.add(mutant);
			mutants.add(mutant);
			saveStringToTextFile(builder2.toString(), mutantFileName2);
			mutantIndex++;
		}
		return mutants;
	}
	// FPR use i/o. create a single mutant, bugposition=0.
	/**
	 * It moves in each policy the rules having a Permit effect before those ones having a Deny effect.
	 * @return 
	 */
	public List<PolicyMutant> createFirstPermitRuleMutants() {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		
//		// Do not create equivalent mutant: when combiningAlgorithm = deny-overrides
//		if (policy.getCombiningAlg().getIdentifier().equals((new DenyOverridesRuleAlg()).getIdentifier()))
//			return;
		if(!(policy.getCombiningAlg() instanceof FirstApplicableRuleAlg))
			return mutants;
		final int MUTANTINDEX=1; // fixed
		
		StringBuilder builder = new StringBuilder();
		policy.encode(builder);
		
		boolean applicable = false;
		
		// d keep tracks of the first deny rule; 
		// p keep tracks of the first permit rule after the deny rule;
		int p = -1, d = -1;	
		
		for (int i = 0; i < policy.getChildElements().size(); i++) {
			CombinerElement rule = policy.getChildElements().get(i);
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule myrule = (Rule) tree;
				String id = myrule.getId().toString();
				int effect = myrule.getEffect();
				if (effect==1) {
					d = i;
					// then find the next permit rule to swap with.
					int currentindex = (p == -1 ? 0 : p); // starting searching index. avoid -1
					while(currentindex<policy.getChildElements().size()) {
						CombinerElement temprule = policy.getChildElements().get(currentindex);
						PolicyTreeElement temptree = temprule.getElement();
						if(temptree instanceof Rule) {
							Rule therule = (Rule) temptree;
							int eff = therule.getEffect();
							if(eff==0){
								p = currentindex;
								break;
							}
						}
						currentindex++;
					}
					if ( p>d && p<policy.getChildElements().size()) {
						// set applicable true.
						applicable = true;
						// swap rule p and rule d.
						// info about the permit rule.
						CombinerElement rulePermit = policy.getChildElements().get(p);
						PolicyTreeElement treePermit = rulePermit.getElement();
						Rule myrulePermit = (Rule) treePermit;
						String idPermit = myrulePermit.getId().toString();
						
						// find the deny rule
						int denyruleStartingIndex = builder.indexOf("<Rule RuleId=\"" + id + "\" Effect=\"" + "Deny" + "\"  >");
						if(denyruleStartingIndex < 0)
							denyruleStartingIndex = builder.indexOf("<Rule Effect=\"" + "Deny" + "\" RuleId=\"" + id + "\"  >");
						int denyruleEndingIndex = builder.indexOf("</Rule>", denyruleStartingIndex) + 7+1; // +1 to kill the blank line.
						// find the permit rule
						int permitruleStartingIndex = builder.indexOf("<Rule RuleId=\"" + idPermit + "\" Effect=\"" + "Permit" + "\"  >");
						if(permitruleStartingIndex < 0)
							permitruleStartingIndex = builder.indexOf("<Rule Effect=\"" + "Permit" + "\" RuleId=\"" + idPermit + "\"  >");
						int permitruleEndingIndex = builder.indexOf("</Rule>", permitruleStartingIndex) + 7+1; // +1 to kill the blank line.
						// copy of permit rule
						String permitrule = builder.substring(permitruleStartingIndex, permitruleEndingIndex);
						// copy of deny rule
						String denyrule = builder.substring(denyruleStartingIndex, denyruleEndingIndex);
						// replace the permit rule with the deny rule
						builder.replace(permitruleStartingIndex, permitruleEndingIndex, denyrule);
						// replace the deny rule with the permit rule
						builder.replace(denyruleStartingIndex, denyruleEndingIndex, permitrule);

						//System.out.println(builder.toString());

						// Important: increment p.
						p++;
					}
				}
			}
		}
		// Fixed 02/17/15: Export exactly 1 mutant, if applicable.
		if (applicable) {
			String mutantFileName = getMutantFileName("FPR"+MUTANTINDEX);
			int bugPosition = 0;
			int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
			PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" FPR"+MUTANTINDEX, mutantFileName, bugPositions);
			mutantList.add(mutant);
			mutants.add(mutant);
			saveStringToTextFile(builder.toString(), mutantFileName);
		}
		return mutants;
	}
	
	// FDR use i/o. create a signle mutant, bugposition=0.
	/**
	 * It moves in each policy the rules having a Deny effect before those ones having a Permit effect.
	 * @return 
	 */
	public List<PolicyMutant> createFirstDenyRuleMutants() {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		
//		// Do not create equivalent mutant: when combiningAlgorithm = permit-overrides
//		if (policy.getCombiningAlg().getIdentifier().equals((new PermitOverridesRuleAlg()).getIdentifier()))
//			return;
		if(!(policy.getCombiningAlg() instanceof FirstApplicableRuleAlg))
			return mutants;
		final int MUTANTINDEX=1; // fixed
		
		StringBuilder builder = new StringBuilder();
		policy.encode(builder);
		
		boolean applicable = false;
		
		// d keep tracks of the first deny rule; 
		// p keep tracks of the first permit rule after the deny rule;
		int p = -1, d = -1;	
		
		for (int i = 0; i < policy.getChildElements().size(); i++) {
			CombinerElement rule = policy.getChildElements().get(i);
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule myrule = (Rule) tree;
				String id = myrule.getId().toString();
				int effect = myrule.getEffect();
				if (effect==0) {
					p = i;
					// then find the next deny rule to swap with.
					int currentindex = (d == -1 ? 0 : d); // starting searching index. avoid -1
					while(currentindex<policy.getChildElements().size()) {
						CombinerElement temprule = policy.getChildElements().get(currentindex);
						PolicyTreeElement temptree = temprule.getElement();
						if(temptree instanceof Rule) {
							Rule therule = (Rule) temptree;
							int eff = therule.getEffect();
							if(eff==1){
								d = currentindex;
								break;
							}
						}
						currentindex++;
					}
					if (d>p && d<policy.getChildElements().size()) {
						// set applicable true.
						applicable = true;
						// swap rule d and rule p.
						// info about the deny rule.
						CombinerElement ruleDeny = policy.getChildElements().get(d);
						PolicyTreeElement treeDeny = ruleDeny.getElement();
						Rule myruleDeny = (Rule) treeDeny;
						String idDeny = myruleDeny.getId().toString();
						
						// find the permit rule
						int permitruleStartingIndex = builder.indexOf("<Rule RuleId=\"" + id + "\" Effect=\"" + "Permit" + "\"  >");
						if(permitruleStartingIndex < 0)
							permitruleStartingIndex = builder.indexOf("<Rule Effect=\"" + "Permit" + "\" RuleId=\"" + id + "\"  >");
						int permitruleEndingIndex = builder.indexOf("</Rule>", permitruleStartingIndex) + 7+1; // +1 to kill the blank line.
						// find the deny rule
						int denyruleStartingIndex = builder.indexOf("<Rule RuleId=\"" + idDeny + "\" Effect=\"" + "Deny" + "\"  >"); 
						if(denyruleStartingIndex < 0)
							denyruleStartingIndex = builder.indexOf("<Rule Effect=\"" + "Deny" + "\" RuleId=\"" + idDeny + "\"  >");
						int denyruleEndingIndex = builder.indexOf("</Rule>", denyruleStartingIndex) + 7+1; // +1 to kill the blank line.
						if (denyruleStartingIndex<0 || denyruleEndingIndex<0 || permitruleStartingIndex<0 || permitruleEndingIndex<0){
							applicable = false;
						} else {
							// copy of deny rule
							String denyrule = builder.substring(denyruleStartingIndex, denyruleEndingIndex);
							// copy of permit rule
							String permitrule = builder.substring(permitruleStartingIndex, permitruleEndingIndex);
							// replace the deny rule with the permit rule
							builder.replace(denyruleStartingIndex, denyruleEndingIndex, permitrule);
							// replace the deny rule with the permit rule
							builder.replace(permitruleStartingIndex, permitruleEndingIndex, denyrule);
	
							//System.out.println(builder.toString());
						}
						// Important: set p = p+1;
						d++;
					}
				}
			}
		}
		// Fixed 02/17/15: Export exactly 1 mutant, if applicable.
		if (applicable) {
			String mutantFileName = getMutantFileName("FDR"+MUTANTINDEX);
			int bugPosition = 0;
			int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
			PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" FDR"+MUTANTINDEX, mutantFileName, bugPositions);
			mutantList.add(mutant);
			mutants.add(mutant);
			saveStringToTextFile(builder.toString(), mutantFileName);
		}
		return mutants;
	}
	
	// RTT
	/**
	 * Remove the Target(if exists) OF EACH RULE ensuring that the Rule is applied to all requests.
	 */
	public void createRuleTargetTrueMutants() {	
		int ruleIndex = 1;	// to indicate bug position.
		for (CombinerElement rule : policy.getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule myrule = (Rule) tree;
				if(!myrule.isTargetEmpty()) {
					createRuleTargetTrueMutants(myrule, ruleIndex);
				}
				ruleIndex++;
			}
		}

	}
	
	public List<PolicyMutant> createRuleTargetTrueMutants(Rule myrule, int ruleIndex) {	
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		int mutantIndex = 1;
		AbstractTarget target = myrule.getTarget();
		// Analyze AnyOf... The target might still be empty.
		//System.out.println(target);
		if(target == null) {
			System.err.println("target is null");
			return mutants;
		}
		List<AnyOfSelection> listAnyOf = ((Target)target).getAnyOfSelections();
		//System.out.println("Size = " + listAnyOf.size());
		if (listAnyOf.size()!=0) {
			myrule.setTargetEmpty();									
			StringBuilder builder = new StringBuilder();
			policy.encode(builder);
			String mutantFileName = getMutantFileName("RTT"+ruleIndex+"_"+mutantIndex);
			int bugPosition = ruleIndex;
			int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
			PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" RTT"+ruleIndex+"_"+mutantIndex, mutantFileName, bugPositions);
			mutantList.add(mutant);
			mutants.add(mutant);
			saveStringToTextFile(builder.toString(), mutantFileName);

			myrule.setTarget(target);
			mutantIndex++;
		}
		return mutants;
	}
	
	// RTF
	/**
	 * Make an always false target whether or not the target exists. 
	*/
	//moved the build false target operation to function createRuleTargetFalseMutants(Rule myrule, int mutantIndex)
	public void createRuleTargetFalseMutants() throws Exception {
		int ruleIndex=1;
		// create mutation for each rule
		for (CombinerElement rule : policy.getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule myrule = (Rule) tree;
				createRuleTargetFalseMutants(myrule, ruleIndex);
				ruleIndex++;
				
			}
		}
	}
	
	public List<PolicyMutant> createRuleTargetFalseMutants(Rule myrule, int ruleIndex) throws Exception {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		
		// Collect attributes from targets and conditions.
		ArrayList<MyAttr> attr = collectAttributes(policy);
		
		if(attr.size()<1) {
			throw new Exception("No attribute collected");
		} 
		// build up a false target
		String falseTarget = buildFalseTarget(attr);
		
		String id = myrule.getId().toString();
		int effect = myrule.getEffect();
		
		StringBuilder builder = new StringBuilder();
		policy.encode(builder);
		
		// find the corresponding rule.
		int ruleStartingIndex = builder.indexOf("<Rule RuleId=\"" + id + "\" Effect=\"" + (effect==0 ? "Permit" : "Deny") + "\"  >");
		// default target starting/ending index.
		int targetStartingIndex = ruleStartingIndex + ("<Rule RuleId=\"" + id + "\" Effect=\"" + (effect==0 ? "Permit" : "Deny") + "\"  >").length()+1; // +1 to include '\n'
		int targetEndingIndex = targetStartingIndex;
		// if there exists a target, update the targetEndingIndex; otherwise, use default.
		if(!myrule.isTargetEmpty()) {
			targetEndingIndex = builder.indexOf("</Target>", targetStartingIndex) + 9+1; // +1 to include '\n'		
		} 
		
		builder.replace(targetStartingIndex, targetEndingIndex, falseTarget);
		String mutantFileName = getMutantFileName("RTF"+ruleIndex);
		int bugPosition = ruleIndex;
		int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
		PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" RTF"+ruleIndex, mutantFileName, bugPositions);
		mutantList.add(mutant);
		mutants.add(mutant);
		saveStringToTextFile(builder.toString(), mutantFileName);
		return mutants;
	}

	// RCT
	/**
	 * Removes the condition(if exists) of each Rule ensuring that the Condition always evaluates to True.
	 */
	public void createRuleConditionTrueMutants() {
		int ruleIndex = 1;	// to indicate bug position.
		for (CombinerElement rule : policy.getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule myrule = (Rule) tree;
				createRuleConditionTrueMutants(myrule, ruleIndex);
				ruleIndex++;
			}
		}
	}
	
	public List<PolicyMutant> createRuleConditionTrueMutants(Rule myrule, int ruleIndex) {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		int mutantIndex = 1;
		if(!myrule.isConditionEmpty()) {
			Condition condition = myrule.getCondition();	
			// Check if Condition is still empty like <Condition/> valid but not properly implemented in balana.
			// Sysmtem.out.println("Condition children size = " + condition.getChildren().size());
			if (condition.getChildren().size() != 0) {
				myrule.setConditionEmpty();				
				StringBuilder builder = new StringBuilder();
				policy.encode(builder);
				String mutantFileName = getMutantFileName("RCT"+ruleIndex+"_"+mutantIndex);
				int bugPosition = ruleIndex;
				int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
				PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" RCT"+ruleIndex+"_"+mutantIndex, mutantFileName, bugPositions);
				mutantList.add(mutant);
				mutants.add(mutant);
				saveStringToTextFile(builder.toString(), mutantFileName);

				myrule.setCondition(condition);
				mutantIndex++;
			}
		}
		return mutants;
	}
	// RCF use i/o  
	/**
	 * Manipulates the Condition values or the Condition functions 
	 * ensuring that the Condition always evaluates to False.
	 * @throws Exception - Unsupported datatype
	 */
	//move build false condition operation to createRuleConditionFalseMutants(Rule myrule, int mutantIndex)
	public void createRuleConditionFalseMutants() throws Exception {
		int ruleIndex=1;

		// Collect attributes from targets and conditions.
		ArrayList<MyAttr> attr = collectAttributes(policy);
		
		if(attr.size()<1) {
			throw new Exception("No attribute collected");
		} else {

			// create mutation for each rule
			for (CombinerElement rule : policy.getChildElements()) {
				PolicyTreeElement tree = rule.getElement();
				if (tree instanceof Rule) {
					Rule myrule = (Rule) tree;
					createRuleConditionFalseMutants(myrule, ruleIndex);
					ruleIndex++;				
				}
			}
		}	
	}
	
	public List<PolicyMutant> createRuleConditionFalseMutants(Rule myrule, int ruleIndex) throws Exception {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		
		// Collect attributes from targets and conditions.
		ArrayList<MyAttr> attr = collectAttributes(policy);
		// build up a false condition
		String falseCondition = buildFalseCondition(attr);
		String id = myrule.getId().toString();
		int effect = myrule.getEffect();
		
		StringBuilder builder = new StringBuilder();
		policy.encode(builder);
		
		// find the corresponding rule.
		int ruleStartingIndex = builder.indexOf("<Rule RuleId=\"" + id + "\" Effect=\"" + (effect==0 ? "Permit" : "Deny") + "\"  >");
		//int ruleEndingIndex = builder.indexOf("</Rule>", ruleStartingIndex) + 7+1; // +1 to include '\n'
		// default target starting/ending index.
		int targetStartingIndex = ruleStartingIndex + ("<Rule RuleId=\"" + id + "\" Effect=\"" + (effect==0 ? "Permit" : "Deny") + "\"  >").length()+1; // +1 to include '\n'
		int targetEndingIndex = targetStartingIndex;
		// if there exists a target, update the targetEndingIndex; otherwise, use default.
		if(!myrule.isTargetEmpty()) {
			targetEndingIndex = builder.indexOf("</Target>", targetStartingIndex) + 9+1; // +1 to include '\n'		
		} 
		// default condition starting/ending index.
		int conditionStartingIndex = targetEndingIndex;
		int conditionEndingIndex = conditionStartingIndex;
		// if there exists a condition, update the conditionEndingIndex; otherwise, use default.
		if (!myrule.isConditionEmpty()) {
			conditionEndingIndex = builder.indexOf("</Condition>", conditionStartingIndex) + "</Condition>".length()+1;// +1 to include '\n'
		}

		builder.replace(conditionStartingIndex, conditionEndingIndex, falseCondition);
		String mutantFileName = getMutantFileName("RCF"+ruleIndex);
		int bugPosition = ruleIndex;
		int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
		PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" RCF"+ruleIndex, mutantFileName, bugPositions);
		mutantList.add(mutant);
		mutants.add(mutant);
		saveStringToTextFile(builder.toString(), mutantFileName);
		return mutants;
	}

	//TODO RTR
	public void createRuleTypeReplacedMutants() throws Exception {
//		int mutantIndex=1;
//		// Collect attributes from targets and conditions.
//		ArrayList<MyAttr> attr = collectAttributes(policy);
//		for (MyAttr attribute : attr) {
////			System.out.println(attribute.getCategory());
////			System.out.println(attribute.getDataType());
////			System.out.println(attribute.getName());
//		}
//		if(attr.size()<1) {
//			throw new Exception("No attribute collected");
//		} else {
//			for (CombinerElement rule : policy.getChildElements()) {
//				PolicyTreeElement tree = rule.getElement();
//				if (tree instanceof Rule) {
//					Rule myrule = (Rule) tree;
//					Target target = (Target) myrule.getTarget();
//					
//					mutantIndex++;
//				}
//			}
//		}	
	}
	
	//-----------------------------Function Related Mutation Operators
	//TODO RUF
	public void createRemoveUniquenessFunctionMutants() {
		
	}
	//TODO AUF
	public void createAddUniquenessFunctionMutants() {
		
	}
	//TODO CNF
	public void createChangeNOFFunctionMutants() {
		
	}
	
	//TODO CLF
	public void createChangeLogicalFunctionMutants() {
		
	}
	
	// ANF
	/**
	 * It adds the Not function as first function of each Condition element.
	 * The number of mutants derived by this class is equal to the number of conditions in the policy.
	 * Applies to string/integer functions only.
	 */
	public void createAddNotFunctionMutants() {
		int ruleIndex = 1;
		for (CombinerElement rule : policy.getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule myrule = (Rule) tree;
				createAddNotFunctionMutants(myrule, ruleIndex);
				ruleIndex++;
			}
		}
	}
	
	public List<PolicyMutant> createAddNotFunctionMutants(Rule myrule, int ruleIndex) {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		int mutantIndex = 1;
		String notFunc = "\t<Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:not\">\n";
		String applyClosure = "\t</Apply>\n";
		String id = myrule.getId().toString();
		int effect = myrule.getEffect();
		// builder
		StringBuilder builder = new StringBuilder();
		policy.encode(builder);
		// find the corresponding rule.
		int ruleStartingIndex = builder.indexOf("<Rule RuleId=\"" + id + "\" Effect=\"" + (effect==0 ? "Permit" : "Deny") + "\"  >");
		// int ruleEndingIndex = builder.indexOf("</Rule>", ruleStartingIndex) + 7+1; // +1 to include '\n'
		// default target starting/ending index.
		int targetStartingIndex = ruleStartingIndex + ("<Rule RuleId=\"" + id + "\" Effect=\"" + (effect==0 ? "Permit" : "Deny") + "\"  >").length()+1; // +1 to include '\n'
		int targetEndingIndex = targetStartingIndex;
		// if there exists a target, update the targetEndingIndex; otherwise, use default.
		if(!myrule.isTargetEmpty()) {
			targetEndingIndex = builder.indexOf("</Target>", targetStartingIndex) + 9+1; // +1 to include '\n'		
		} 
		// default condition starting/ending index.
		int conditionStartingIndex = targetEndingIndex;
		int conditionEndingIndex = conditionStartingIndex;
		// if there exists a condition, update the conditionEndingIndex; otherwise, use default.
		if (!myrule.isConditionEmpty()) {
			conditionEndingIndex = builder.indexOf("</Condition>", conditionStartingIndex);
			// If there is a condition, add not function as the first function in the condition.
			// head
			builder.replace(conditionStartingIndex+"<Condition>".length()+1, conditionStartingIndex+"<Condition>".length()+1, notFunc);
			// tail
			builder.replace(conditionEndingIndex+notFunc.length(), conditionEndingIndex+notFunc.length(), applyClosure);
			// save...
			String mutantFileName = getMutantFileName("ANF"+ruleIndex+"_"+mutantIndex);
			int bugPosition = ruleIndex;
			int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
			PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" ANF"+ruleIndex+"_"+mutantIndex, mutantFileName, bugPositions);
			mutantList.add(mutant);
			mutants.add(mutant);
			saveStringToTextFile(builder.toString(), mutantFileName);
			mutantIndex++;
		}	
		return mutants;
	}
	
	// RNF
	/**
	 * Remove not function in condition, if there exists.
	 */
	/**
	 * It deletes the Not function defined in the condition.
	 */
	public void createRemoveNotFunctionMutants() {	
		int ruleIndex = 1;
		for (CombinerElement rule : policy.getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule myrule = (Rule) tree;
				createRemoveNotFunctionMutants(myrule, ruleIndex);
				ruleIndex++;
			}
		}
	}
	
	public List<PolicyMutant> createRemoveNotFunctionMutants(Rule myrule,  int ruleIndex) {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		int mutantIndex = 1;
		String notFunc = "<Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:not\">\n";
		String applyClosure = "</Apply>\n";
		String id = myrule.getId().toString();
		int effect = myrule.getEffect();
		// builder
		StringBuilder builder = new StringBuilder();
		policy.encode(builder);
		// find the corresponding rule.
		int ruleStartingIndex = builder.indexOf("<Rule RuleId=\"" + id + "\" Effect=\"" + (effect==0 ? "Permit" : "Deny") + "\"  >");
		// int ruleEndingIndex = builder.indexOf("</Rule>", ruleStartingIndex) + 7+1; // +1 to include '\n'
		// default target starting/ending index.
		int targetStartingIndex = ruleStartingIndex + ("<Rule RuleId=\"" + id + "\" Effect=\"" + (effect==0 ? "Permit" : "Deny") + "\"  >").length()+1; // +1 to include '\n'
		int targetEndingIndex = targetStartingIndex;
		// if there exists a target, update the targetEndingIndex; otherwise, use default.
		if(!myrule.isTargetEmpty()) {
			targetEndingIndex = builder.indexOf("</Target>", targetStartingIndex) + 9+1; // +1 to include '\n'		
		} 
		// default condition starting/ending index.
		int conditionStartingIndex = targetEndingIndex;
		int conditionEndingIndex = conditionStartingIndex;
		// if there exists a condition, update the conditionEndingIndex; otherwise, use default.
		if (!myrule.isConditionEmpty()) {
			conditionEndingIndex = builder.indexOf("</Condition>", conditionStartingIndex);
			// check if there exists a not function.
			int notFunctionIndex = -1; 
			notFunctionIndex = builder.indexOf(notFunc, conditionStartingIndex);
			if (notFunctionIndex != -1 && notFunctionIndex < conditionEndingIndex) {
				// remove head
				builder.replace(notFunctionIndex, notFunctionIndex+notFunc.length(), "");
				// remove tail
				builder.replace(conditionEndingIndex-notFunc.length()-applyClosure.length(), 
						conditionEndingIndex-notFunc.length(), "");
				// save...
				String mutantFileName = getMutantFileName("RNF"+ruleIndex+"_"+mutantIndex);
				int bugPosition = ruleIndex;
				int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
				PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" RNF"+ruleIndex+"_"+mutantIndex, mutantFileName, bugPositions);
				mutantList.add(mutant);
				mutants.add(mutant);
				saveStringToTextFile(builder.toString(), mutantFileName);
				mutantIndex++;
			}
		}
		return mutants;
	}

	// FCF
	/**
	 * Flip comparison function in target.
	 * ignore equal. applys to integer function only.
	 * < to >=, <= to >, >= to <, > to <=
	 */
	public void createFlipComparisonFunctionMutants() {

		int ruleIndex = 1;
		for (CombinerElement rule : policy.getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule myrule = (Rule) tree;
				createFlipComparisonFunctionMutants(myrule, ruleIndex);
				ruleIndex++;
			}
		}
	}
	
	public List<PolicyMutant> createFlipComparisonFunctionMutants(Rule myrule, int ruleIndex) {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		int mutantIndex = 1;
		// The same functions apply to both integers and strings.
//		String[] strFunc = {"urn:oasis:names:tc:xacml:1.0:function:string-less-than", 
//				"urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal", 
//				"urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal", 
//				"urn:oasis:names:tc:xacml:1.0:function:string-greater-than"};
		String[] intFunc = {"urn:oasis:names:tc:xacml:1.0:function:integer-less-than", 
				"urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal", 
				"urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal", 
				"urn:oasis:names:tc:xacml:1.0:function:integer-greater-than"};
//		String strEqual = "urn:oasis:names:tc:xacml:1.0:function:string-equal";
		String intEqual = "urn:oasis:names:tc:xacml:1.0:function:integer-equal";
//		String strFuncHeading = "urn:oasis:names:tc:xacml:1.0:function:string";
		String intFuncHeading = "urn:oasis:names:tc:xacml:1.0:function:integer";
		String id = myrule.getId().toString();
		int effect = myrule.getEffect();
		// builder
		StringBuilder builder = new StringBuilder();
		policy.encode(builder);
		// find the corresponding rule.
		int ruleStartingIndex = builder.indexOf("<Rule RuleId=\"" + id + "\" Effect=\"" + (effect==0 ? "Permit" : "Deny") + "\"  >");
		// int ruleEndingIndex = builder.indexOf("</Rule>", ruleStartingIndex) + 7+1; // +1 to include '\n'
		// default target starting/ending index.
		int targetStartingIndex = ruleStartingIndex + ("<Rule RuleId=\"" + id + "\" Effect=\"" + (effect==0 ? "Permit" : "Deny") + "\"  >").length()+1; // +1 to include '\n'
		int targetEndingIndex = targetStartingIndex;
		// if there exists a target, update the targetEndingIndex; otherwise, use default.
		if(!myrule.isTargetEmpty()) {
			targetEndingIndex = builder.indexOf("</Target>", targetStartingIndex) + 9+1; // +1 to include '\n'
			// Flip the all occurrence of functions to flip.
//			int str_func_occur = builder.indexOf(strFuncHeading, targetStartingIndex);
			int int_func_occur = builder.indexOf(intFuncHeading, targetStartingIndex);
//			while (str_func_occur >= targetStartingIndex && str_func_occur < targetEndingIndex) {
//				StringBuilder builder_str = new StringBuilder();
//				policy.encode(builder_str);	
//				int func_end = builder_str.indexOf("\"", str_func_occur);
//				if (! builder_str.substring(str_func_occur, func_end).equals(strEqual))
//					for (int i=0; i<=3; i++) {	
//						if (builder_str.substring(str_func_occur, func_end).equals(strFunc[i])) {
//							builder_str.replace(str_func_occur, func_end, strFunc[3-i]);
//							String mutantFileName = getMutantFileName("FCF"+mutantIndex);
//							mutantList.add(new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" FCF"+mutantIndex, mutantFileName, ruleIndex));					
//							saveStringToTextFile(builder_str.toString(), mutantFileName);
//							mutantIndex++;
//							break;
//						}
//					}					
//				str_func_occur = builder.indexOf(strFuncHeading, str_func_occur+strFuncHeading.length());
//				System.out.println("STRING " + str_func_occur);
//			}
			
			//System.out.println("Target from " + targetStartingIndex + " to " + targetEndingIndex);
			//System.out.println("INTEGER RULE # " + ruleIndex + " " + int_func_occur);
			
			while (int_func_occur >= targetStartingIndex && int_func_occur < targetEndingIndex) {
				StringBuilder builder_int = new StringBuilder();
				policy.encode(builder_int);	
				int func_end = builder_int.indexOf("\"", int_func_occur);
				if (! builder_int.substring(int_func_occur, func_end).equals(intEqual)) {
					for (int i=0; i<=3; i++) {	
						if (builder_int.substring(int_func_occur, func_end).equals(intFunc[i])) {
							builder_int.replace(int_func_occur, func_end, intFunc[3-i]);
							String mutantFileName = getMutantFileName("FCF"+ruleIndex+"_"+mutantIndex);
							int bugPosition = ruleIndex;
							int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
							PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" FCF"+ruleIndex+"_"+mutantIndex, mutantFileName, bugPositions);
							mutantList.add(mutant);		
							mutants.add(mutant);
							saveStringToTextFile(builder_int.toString(), mutantFileName);
							mutantIndex++;
							break;
						}
					}	
				}
				int_func_occur = builder.indexOf(intFuncHeading, int_func_occur+intFuncHeading.length());
				//System.out.println("INTEGER " + int_func_occur);
			}	
			
		}
		return mutants;
	}
	
	// CCF
	// for integer: >, >=, =, <=, <, !=
	// for string: =, !=
	/**
	 * It replaces a comparison function with another one.
	 */
	public void createChangeComparisonFunctionMutants() {
		int ruleIndex = 1;
		for (CombinerElement rule : policy.getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule myrule = (Rule) tree;
				createChangeComparisonFunctionMutants(myrule, ruleIndex);
				ruleIndex++;
			}
		}
			
	}
	
	public List<PolicyMutant> createChangeComparisonFunctionMutants(Rule myrule, int ruleIndex) {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		int mutantIndex = 1;
		// The same functions apply to both integers and strings.
		String[] strFunctionKey = {"urn:oasis:names:tc:xacml:1.0:function:string-equal", 
				"urn:oasis:names:tc:xacml:1.0:function:string-greater-than", 
				"urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal", 
				"urn:oasis:names:tc:xacml:1.0:function:string-less-than", 
				"urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal"};
		String[] intFunctionKey = {"urn:oasis:names:tc:xacml:1.0:function:integer-equal", 
				"urn:oasis:names:tc:xacml:1.0:function:integer-greater-than", 
				"urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal", 
				"urn:oasis:names:tc:xacml:1.0:function:integer-less-than", 
				"urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal"};
		String strFuncHeading = "urn:oasis:names:tc:xacml:1.0:function:string";
		String intFuncHeading = "urn:oasis:names:tc:xacml:1.0:function:integer";
		String id = myrule.getId().toString();
		int effect = myrule.getEffect();
		// builder
		StringBuilder builder = new StringBuilder();
		policy.encode(builder);			
		// find the corresponding rule.
		int ruleStartingIndex = builder.indexOf("<Rule RuleId=\"" + id + "\" Effect=\"" + (effect==0 ? "Permit" : "Deny") + "\"  >");
		int ruleEndingIndex = builder.indexOf("</Rule>", ruleStartingIndex) + 7+1; // +1 to kill the blank line.
		// Replace the first occurence of the function.
		int str_func_occur = builder.indexOf(strFuncHeading, ruleStartingIndex);
		int int_func_occur = builder.indexOf(intFuncHeading, ruleStartingIndex);
		if (str_func_occur > ruleStartingIndex && str_func_occur < ruleEndingIndex) {
			for (String func : strFunctionKey) {
				StringBuilder builder_str = new StringBuilder();
				policy.encode(builder_str);	
				int func_end = builder_str.indexOf("\"", str_func_occur);
				if ( !builder_str.substring(str_func_occur, func_end).equals(func)) {
					builder_str.replace(str_func_occur, func_end, func);
					String mutantFileName = getMutantFileName("CCF"+ruleIndex+"_"+mutantIndex);
					int bugPosition = ruleIndex;
					int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
					PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" CCF"+ruleIndex+"_"+mutantIndex, mutantFileName, bugPositions);
					mutantList.add(mutant);
					mutants.add(mutant);
					saveStringToTextFile(builder_str.toString(), mutantFileName);
					mutantIndex++;
				}
			}
		}
		if (int_func_occur > ruleStartingIndex && int_func_occur < ruleEndingIndex) {
			for (String func : intFunctionKey) {
				StringBuilder builder_int = new StringBuilder();
				policy.encode(builder_int);	
				int func_end = builder_int.indexOf("\"", int_func_occur);
				if ( !builder_int.substring(int_func_occur, func_end).equals(func)) {
					builder_int.replace(int_func_occur, func_end, func);
					String mutantFileName = getMutantFileName("CCF"+mutantIndex);
					int bugPosition = ruleIndex;
					int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
					PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" CCF"+mutantIndex, mutantFileName, bugPositions);
					mutantList.add(mutant);					
					mutants.add(mutant);
					saveStringToTextFile(builder_int.toString(), mutantFileName);
					mutantIndex++;
				}
			}
		}
		return mutants;
	}
	
	// RPTE
	/**
	 *  - rule target mutation
	 * Remove one of the parallel AllOf/AnyOf/Match from RULE TARGET, if such a form exists.
	 * A target has the following structure: Target: AnyOf - AllOf - Match
	 */
	public void createRemoveParallelTargetElementMutants() {
		int ruleIndex=1;
		for (CombinerElement rule : policy.getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule myrule = (Rule) tree;
				if (!myrule.isTargetEmpty()) {
					createRemoveParallelTargetElementMutants(myrule, ruleIndex);
				}
				ruleIndex++;
			}
		}
	}
	
	public List<PolicyMutant> createRemoveParallelTargetElementMutants(Rule myrule, int ruleIndex) {
		List<PolicyMutant> mutants = new ArrayList<PolicyMutant>();
		 int mutantIndex = 1;
		Target target = (Target) myrule.getTarget();
		// Analyze AnyOf...
		if(target == null) {
			System.err.println("target is null");
			return mutants;
		}
		List<AnyOfSelection> listAnyOf = target.getAnyOfSelections();
		if (listAnyOf.size()!=0) {
			if (listAnyOf.size()>1) {
				// Mutation for multiple AnyOf...
				for (int i=0; i<listAnyOf.size(); i++) {
					AnyOfSelection original = listAnyOf.get(i);
					listAnyOf.remove(i);
					StringBuilder builder = new StringBuilder();
					policy.encode(builder);
					String mutantFileName = getMutantFileName("RPTE"+ruleIndex+"_"+mutantIndex);
					int bugPosition = ruleIndex;
					int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
					PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" RPTE"+ruleIndex+"_"+mutantIndex, mutantFileName, bugPositions);
					mutantList.add(mutant);
					mutants.add(mutant);
					saveStringToTextFile(builder.toString(), mutantFileName);
					listAnyOf.add(i, original);				
					mutantIndex++;
				}
			}
			// for each AnyOf, analyze AllOf...
			for (AnyOfSelection selAnyOf : listAnyOf) {
				List<AllOfSelection> listAllOf = selAnyOf.getAllOfSelections();
				if (listAllOf.size()!=0) {
					if (listAllOf.size()>1) {
						// Mutation for multiple AllOf
						for (int i=0; i<listAllOf.size(); i++) {
							AllOfSelection original = listAllOf.get(i);
							listAllOf.remove(i);
							StringBuilder builder = new StringBuilder();
							policy.encode(builder);
							String mutantFileName = getMutantFileName("RPTE"+ruleIndex+"_"+mutantIndex);
							int bugPosition = ruleIndex;
							int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
							PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" RPTE"+ruleIndex+"_"+mutantIndex, mutantFileName, bugPositions);
							mutantList.add(mutant);
							mutants.add(mutant);
							saveStringToTextFile(builder.toString(), mutantFileName);
							listAllOf.add(i, original);
							mutantIndex++;
						}
					}
					// for each AllOf, analyze Match...
					for (AllOfSelection selAllOf: listAllOf) {
						List<TargetMatch> listMatch = selAllOf.getMatches();
						if (listMatch.size()!=0) {
							if (listMatch.size()>1) {
								// Mutation for multiple AllOf
								for (int i=0; i<listMatch.size(); i++) {
									TargetMatch original = listMatch.get(i);
									listMatch.remove(i);
									StringBuilder builder = new StringBuilder();
									policy.encode(builder);
									String mutantFileName = getMutantFileName("RPTE"+ruleIndex+"_"+mutantIndex);
									int bugPosition = ruleIndex;
									int[] bugPositions = appendArray(this.baseMutant.getFaultLocation(), bugPosition);
									PolicyMutant mutant = new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" RPTE"+ruleIndex+"_"+mutantIndex, mutantFileName, bugPositions);
									mutantList.add(mutant);
									mutants.add(mutant);
									saveStringToTextFile(builder.toString(), mutantFileName);
									listMatch.add(i, original);
									mutantIndex++;
								}
							}
						}
					}
				}
			}		
		}
		return mutants;
	}

	// RPCE
	/**
	 * Remove one of the parallel Apply from condition, if such a form exists.
	 */
	public void createRemoveParallelConditionElementMutants() {
//		int mutantIndex=1;
//		int ruleIndex=1;
//		for (CombinerElement rule : policy.getChildElements()) {
//			PolicyTreeElement tree = rule.getElement();
//			if (tree instanceof Rule) {
//				Rule myrule = (Rule) tree;
//				if (!myrule.isConditionEmpty()) {
//					Condition condition = myrule.getCondition();
//					// Analyze AnyOf...
//					List children = condition.getChildren();
//					StringBuilder builder = new StringBuilder();
//					System.out.println(children);
//					condition.getExpression().encode(builder);
//					System.out.println(builder); // The condition string.
//				}
//			}
//		}
	}

	
	//----------------------------------------------------------------------------------------------------
	//-------------------functional methods---------------------------------------------------
	/**
	 * Collect Attributes from rules' target and condition
	 * @param policy
	 * @return
	 */
	protected ArrayList<MyAttr> collectAttributes(Policy policy) {
		PolicyX x = new PolicyX(policy);
		ArrayList<MyAttr> attr = new ArrayList<MyAttr>();
		for (CombinerElement rule : policy.getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule myrule = (Rule) tree;
				// Pull attributes out from target:
				AbstractTarget abtarget = myrule.getTarget();
				try {
					x.getTargetAttribute((Target) abtarget, attr); // cast issue 2.0->3.0
				} catch (Exception e) {
					e.printStackTrace();
				}
				// Pull attributes out from condition:
				x.getConditionAttribute(myrule.getCondition(), attr);
			}
		}
		return attr;
	}
	
	/**
	 * Build a false target given the collection of attributes.
	 * @param attr
	 * @return
	 * @throws Exception
	 */
	protected String buildFalseTarget(ArrayList<MyAttr> attr) throws Exception {
		String function = "";
		String value1 = "";
		String value2 = "";
		
		if(attr.size()<1)
			throw new Exception("At least 1 attribute needed");
		String dataType = attr.get(0).getDataType();
		String name = attr.get(0).getName();
		String category = attr.get(0).getCategory();
		if(dataType.contains("string")) {
			function = str_function;
			value1 = str_value1;
			value2 = str_value2;
		} else if(dataType.contains("integer")) {
			function = int_function;
			value1 = int_value1;
			value2 = int_value2;
		} else {
			throw new Exception("Unsupported dataType");
		}
		
		String falseTarget = "";
		falseTarget+="\t<Target>\n";
	    falseTarget+="\t<AnyOf>\n"; 
	    //------------------------------// idea: let (a=x and a=y) to make a false target.
        falseTarget+="\t\t<AllOf>\n";
        falseTarget+="\t\t\t<Match MatchId=\"" + function + "\">\n";
        falseTarget+="\t\t\t\t<AttributeValue DataType=\"" + dataType + "\">" + value1 + "</AttributeValue>\n";
        falseTarget+="\t\t\t\t<AttributeDesignator AttributeId=\"" + name +"\" Category=\"" + category + "\" DataType=\"" + dataType + "\" MustBePresent=\"true\"/>\n";
        falseTarget+="\t\t\t</Match>\n";
        //falseTarget+="\t\t</AllOf>\n";
        //------------------------------
        //falseTarget+="\t\t<AllOf>\n";
        falseTarget+="\t\t\t<Match MatchId=\"" + function + "\">\n";
        falseTarget+="\t\t\t\t<AttributeValue DataType=\"" + dataType + "\">" + value2 + "</AttributeValue>\n";
        falseTarget+="\t\t\t\t<AttributeDesignator AttributeId=\"" + name +"\" Category=\"" + category + "\" DataType=\"" + dataType + "\" MustBePresent=\"true\"/>\n";
        falseTarget+="\t\t\t</Match>\n";
        falseTarget+="\t\t</AllOf>\n";
        //------------------------------
        falseTarget+="\t</AnyOf>\n";
        falseTarget+="\t</Target>\n";
		//-----------------------------
		return falseTarget;
	}

	/**
	 * Build an always-false condition using a=x && a=y
	 * @param attr
	 * @return
	 * @throws Exception
	 */
	private String buildFalseCondition(ArrayList<MyAttr> attr) throws Exception {
		String function = "";
		String function_one_and_only = "";
		String value1 = "";
		String value2 = "";
		
		if(attr.size()<1)
			throw new Exception("At least 1 attribute needed");
		String dataType = attr.get(0).getDataType();
		String name = attr.get(0).getName();
		String category = attr.get(0).getCategory();
		
		if(dataType.contains("string")) {
			function = str_function;
			function_one_and_only = str_function_one_and_only;
			value1 = str_value1;
			value2 = str_value2;
			
		} else if(dataType.contains("integer")) {
			function = int_function;
			function_one_and_only = int_function_one_and_only;
			value1 = int_value1;
			value2 = int_value2;
		} else {
			throw new Exception("Unsupported dataType");
		}
		// Note: one-and-only function seems essential.
		String falseCondition = "";
		falseCondition+="\t<Condition>\n";
		falseCondition+="\t\t<Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:and\">\n";
		falseCondition+="\t\t\t<Apply FunctionId=\"" + function + "\">\n";
		falseCondition+="\t\t\t\t<Apply FunctionId=\"" + function_one_and_only + "\">\n";
		falseCondition+="\t\t\t\t\t<AttributeDesignator AttributeId=\"" + name + "\" Category=\"" + category + "\" DataType=\"" + dataType + "\" MustBePresent=\"true\"/>\n";
		falseCondition+="\t\t\t\t</Apply>\n";
		falseCondition+="\t\t\t\t<AttributeValue DataType=\"" + dataType + "\">" + value1 + "</AttributeValue>\n";
		falseCondition+="\t\t\t</Apply>\n";
		falseCondition+="\t\t\t<Apply FunctionId=\"" + function + "\">\n";
		falseCondition+="\t\t\t\t<Apply FunctionId=\"" + function_one_and_only + "\">\n";
		falseCondition+="\t\t\t\t\t<AttributeDesignator AttributeId=\"" + name + "\" Category=\"" + category + "\" DataType=\"" + dataType + "\" MustBePresent=\"true\"/>\n";
		falseCondition+="\t\t\t\t</Apply>\n";
		falseCondition+="\t\t\t\t<AttributeValue DataType=\"" + dataType + "\">" + value2 + "</AttributeValue>\n";
		falseCondition+="\t\t\t</Apply>\n";
		falseCondition+="\t\t</Apply>\n";
		falseCondition+="\t</Condition>\n";
		
        return falseCondition;
	}
	
	/**
	 * For comparison.
	 */
	@SuppressWarnings("unused")
	private void createTheOriginalPolicy() {
			StringBuilder builder = new StringBuilder();
			policy.encode(builder);
			String mutantFileName = getMutantFileName("ORG");
			//mutantList.add(new PolicyMutant(this.baseMutant.getNumber() + " " + PolicySpreadSheetMutantSuite.MUTANT_KEYWORD+" ORG", mutantFileName, 0));
			saveStringToTextFile(builder.toString(), mutantFileName);
	}
	
	// Get mutant filename. in "mutation folder".
	public String getMutantFileName(String mutantID) {
		int lastDotIndex = mutantFileNameBase.lastIndexOf(".");
		if (lastDotIndex>0)
			return mutantFileNameBase.substring(0, lastDotIndex)+"_"+mutantID+".xml";
		else 
			return mutantFileNameBase+"_"+mutantID+".xml";			
	}

	// Put mutant spread sheet into the parent folder of mutation. 
	public String getMutantsSpreadSheetFileName() {
		int lastDotIndex = mutantSpreadSheetNameBase.lastIndexOf(".");
		if (lastDotIndex>0)
			return mutantSpreadSheetNameBase.substring(0, lastDotIndex)+"_mutants.xls";
		else 
			return mutantSpreadSheetNameBase+"_mutants.xls";			
	}

    public Policy loadPolicy(String policyFile) {

       Policy policy = null;
       InputStream stream = null;

       try {
           // create the factory
           DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
           factory.setIgnoringComments(true);
           factory.setNamespaceAware(true);
           factory.setValidating(false);

           // create a builder based on the factory & try to load the policy
           DocumentBuilder db = factory.newDocumentBuilder();
           stream = new FileInputStream(policyFile);
           Document doc = db.parse(stream);

           // handle the policy, if it's a known type
           Element root = doc.getDocumentElement();
           String name = DOMHelper.getLocalName(root);

           if (name.equals("Policy")) {
               policy = Policy.getInstance(root);                
           } 
       } catch (Exception e) {
       		e.printStackTrace();
       } finally {
           if(stream != null){
               try {
                   stream.close();
               } catch (IOException e) {
               }
           }
       }
     return policy;
   }
    
	public static void saveStringToTextFile(String fileString, String fileName) {
		File file = new File(fileName);
		saveStringToTextFile(fileString, file);
	}

	public static void saveStringToTextFile(String fileString, File file) {
		PrintWriter out = null;
		if (file.exists()) {
			file.delete();
		}
		try {
			out = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		out.print(fileString);
		out.close();
	}

	public static void main(String[] args) throws Exception{
		//new PolicyMutator("Experiments//kmarket-blue-policy//kmarket-blue-policy.xml");
		//new PolicyMutator("Experiments//conference3//conference3.xml");
		//new PolicyMutator("Experiments//fedora3//fedora-rule3.xml");
		StringBuilder builder = new StringBuilder();
		builder.append("StreetTaco");
		System.out.println(builder.replace(0,0,"W"));
	}
	
	public Policy getPolicy(){
		return this.policy;
	}
	
	public String getMutantSpreedSheetFolderName(){
		return this.mutantSpreadSheetFolderName;
	}
	
	public String getMutantFileNameBase(){
		return this.mutantFileNameBase;
	}
	
	public ArrayList<PolicyMutant> getMutantList()
	{
		return this.mutantList;
	}

	public List<Rule> getRuleList() {
		List<Rule> ruleList = new ArrayList<Rule>();
		for (CombinerElement rule : this.getPolicy().getChildElements()) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				ruleList.add((Rule)tree);
			}
		}
		return ruleList;
	}

}
