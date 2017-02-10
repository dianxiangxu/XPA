package org.seal.repair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.seal.coverage.PolicySpreadSheetTestSuite;
import org.seal.faultlocalization.SpectrumBasedDiagnosisResults;
import org.seal.faultlocalization.SpectrumBasedFaultLocalizer;
import org.seal.faultlocalization.TestCellResult;
import org.seal.mutation.PolicyMutant;
import org.seal.mutation.PolicyMutator;
import org.wso2.balana.Rule;

public class PolicyRepairer {
	String testSuiteFile = null;
	private int numTriesBeforSucceed;
	@SuppressWarnings("serial")
	static private List<List<String>> repairMethodPairList = new ArrayList<List<String>>() {
		{
			add(new ArrayList<String>(Arrays.asList("repairRandomOrder", null)));
			add(new ArrayList<String>(Arrays.asList("repairOneByOne", null)));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "jaccard")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "tarantula")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "ochiai")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "ochiai2")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "cbi")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "hamann")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "simpleMatching")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "sokal")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "naish2")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "goodman")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "sorensenDice")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "anderberg")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "euclid")));
			add(new ArrayList<String>(Arrays.asList("repairSmartly", "rogersTanimoto")));
			
		}
	};
	
	public PolicyRepairer(String testSuiteFile) {
		this.testSuiteFile = testSuiteFile;
	}
	
	private PolicyMutant find1stCorrectMutant(List<PolicyMutant> mutantList) throws Exception	{
		if(mutantList == null) {
			return null;
		}
		for(PolicyMutant mutant: mutantList) {
			//System.out.println(mutant.getMutantFilePath() + "\n");
			List<Boolean> testResults = getTestResults(testSuiteFile, mutant.getMutantFilePath());
			boolean is_repaired = booleanListAnd(testResults);
			if(is_repaired) {
				return mutant;
			}
		}
		return null;
	}
	
	static List<Boolean> getTestResults(String testSuiteFile, String mutantFile) throws Exception {
		PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(testSuiteFile, mutantFile);
		TestCellResult[] testCellResults = testSuite.runAllTestsOnMutant();
		List<Boolean> testResults = new ArrayList<Boolean>();
		for(TestCellResult res : testCellResults) {
			testResults.add(res.getVerdict());
		}	
		return testResults;
	}
	
	private List<PolicyMutant> findAllCorrectMutants(List<PolicyMutant> mutantList) throws Exception	{
		List<PolicyMutant> correctMutants = new ArrayList<PolicyMutant>();
		for(PolicyMutant mutant: mutantList) {
			//System.out.println(mutant.getMutantFilePath() + "\n");
			PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(testSuiteFile, mutant.getMutantFilePath());
			TestCellResult[] testCellResults = testSuite.runAllTestsOnMutant();
			List<Boolean> testResults = new ArrayList<Boolean>();
			for(TestCellResult res : testCellResults) {
				testResults.add(res.getVerdict());
			}
			boolean is_repaired = booleanListAnd(testResults);
			if(is_repaired) {
				correctMutants.add(mutant);
			}
		}
		return correctMutants;
	}
	
	/**
	 * @param policyFileToRepair, file path of the policy file to be repaired
	 * @return file path of repaired file; null if cannot be repaired
	 * @throws Exception
	 * generate all mutants at once and check which one can pass the test suite
	 */
	public PolicyMutant repair(PolicyMutant policyFileToRepair) throws Exception {
		PolicyMutator mutator = new PolicyMutator(policyFileToRepair);
		mutator.createAllMutants();
		List<PolicyMutant> mutants = mutator.getMutantList();
		return find1stCorrectMutant(mutants);
	}
	
	public void testByPosition(String policyFileToRepair) throws Exception {
		PolicyMutator mutator = new PolicyMutator(policyFileToRepair);
		mutator.createAllMutants();
		List<PolicyMutant> mutants = mutator.getMutantList();
		for(PolicyMutant mutant: findAllCorrectMutants(mutants)) {
			System.out.println(mutant.getMutantFilePath());
		}
		
		System.out.println("=================================");
		
		PolicyMutator mutatorByPosition = new PolicyMutator(policyFileToRepair);
		mutatorByPosition.createAllMutants();
		List<PolicyMutant> mutantsByPosition = mutatorByPosition.getMutantList();
		for(PolicyMutant mutant: findAllCorrectMutants(mutantsByPosition)) {
			System.out.println(mutant.getMutantFilePath());
		}
		
	}
	
	/**
	 * @param booleanList
	 * @return result of logical AND on all elements of the boolean array
	 */
	static boolean booleanListAnd(List<Boolean> booleanList) {
		boolean result = true;
		for(boolean b: booleanList) {
			result = result && b;
		}
		return result;
	}
	
	
	/**
	 * @param policyFileToRepair, file path of the policy file to be repaired
	 * @return file path of repaired file; null if cannot be repaired
	 * @throws Exception
	 * use fault localizer to find bugPosition, then generate mutants accordingly
	 * to repair
	 */
	public PolicyMutant repairSmartly(PolicyMutant policyFileToRepair, String faultLocalizeMethod) throws Exception {
		PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(testSuiteFile,
				policyFileToRepair.getMutantFilePath());
		testSuite.runAllTests();//we need to run tests to get coverage information, which is in turn used to get suspicion rank
		List<Integer> suspicionRank = getSuspicionRank(policyFileToRepair, faultLocalizeMethod); 
		return repairBySuspicionRank(policyFileToRepair, suspicionRank);
	}

	 static List<Integer> getSuspicionRank(PolicyMutant policyFileToRepair, String faultLocalizeMethod) throws Exception {
		List<Integer> suspicionRank = new ArrayList<Integer>();
		SpectrumBasedDiagnosisResults diagnosisResults = 
				SpectrumBasedFaultLocalizer.applyOneFaultLocalizerToPolicyMutant(faultLocalizeMethod);
		suspicionRank = diagnosisResults.getRuleIndexRankedBySuspicion();
		suspicionRank.add(0, -1);// a temporary solution for fault in combining algorithms
		return suspicionRank;
	}

	 public static List<Integer> getRandomSuspicionRank(PolicyMutant policyFileToRepair) throws Exception {
		List<Integer> suspicionRank = new ArrayList<Integer>();
		PolicyMutator mutator = new PolicyMutator(policyFileToRepair);
		int maxRules = mutator.getPolicy().getChildElements().size();
		for(int bugPosition = -1; bugPosition <= mutator.getRuleList().size(); bugPosition++) {
			suspicionRank.add(bugPosition);
		}
		suspicionRank.add(maxRules);
		Collections.shuffle(suspicionRank);
		return suspicionRank;
	 }
	
	public PolicyMutant repairRandomOrder(PolicyMutant policyFileToRepair) throws Exception {
		List<Integer> suspicionRank = getRandomSuspicionRank(policyFileToRepair);
		return repairBySuspicionRank(policyFileToRepair, suspicionRank);
	}
	/**
	 * @param policyFileToRepair, file path of the policy file to be repaired
	 * @return file path of repaired file; null if cannot be repaired
	 * @throws Exception
	 * generate a mutant a time and check whether it can pass the test suite
	 */
	public PolicyMutant repairOneByOne(PolicyMutant policyFileToRepair) throws Exception {
		List<Integer> suspicionRank = new ArrayList<Integer>();
		PolicyMutator mutator = new PolicyMutator(policyFileToRepair);
		int maxRules = mutator.getPolicy().getChildElements().size();
		for(int bugPosition = -1; bugPosition <= mutator.getRuleList().size(); bugPosition++) {
			suspicionRank.add(bugPosition);
		}
		suspicionRank.add(maxRules);
		return repairBySuspicionRank(policyFileToRepair, suspicionRank);
	}
	
	public PolicyMutant repairBySuspicionRank(PolicyMutant policyFileToRepair, List<Integer> suspicionRank) throws Exception {
		PolicyMutant correctMutant = null;
		PolicyMutator mutator = new PolicyMutator(policyFileToRepair);
		List<Rule> ruleList = mutator.getRuleList();
		int maxRules = mutator.getPolicy().getChildElements().size();
		//bugPosition equals to -1 indicates fault in combining algorithm
		int bugPosition = Integer.MAX_VALUE;
		for(int i = 0; i < suspicionRank.size(); i++) {
			bugPosition = suspicionRank.get(i);
			if(bugPosition == -1) {
				correctMutant = repairBugPositionCombiningAlgorithm(mutator);
				if(correctMutant != null) {
					break;
				}
			} else if (bugPosition == 0) {
				correctMutant = repairBugPositionPolicyTarget(mutator);
				if(correctMutant != null) {
					break;
				}
			} else if (bugPosition == maxRules) {
				correctMutant = repairBugPositionMaxRules(mutator);
				if(correctMutant != null) {
					break;
				}
			} else {
				//take care, ruleIndex begins from one
				if(bugPosition-1 >= ruleList.size()) {
					//this is caused by the RER(createRemoveRuleMutants())
					continue;
				}
				correctMutant = repairBugPositionRules(mutator, ruleList.get(bugPosition-1), bugPosition);
				if(correctMutant != null) {
					break;
				}
			}
		}
		this.setNumTriesBeforSucceed(suspicionRank.indexOf(bugPosition));
		System.out.printf("repaired in the %d-th try\n",this.getNumTriesBeforSucceed());
		return correctMutant;
	}
	
	public int getNumTriesBeforSucceed() {
		return numTriesBeforSucceed;
	}

	private void setNumTriesBeforSucceed(int numTriesBeforSucceed) {
		this.numTriesBeforSucceed = numTriesBeforSucceed;
	}

	private void deleteMutantFile(List<PolicyMutant> mutantList, PolicyMutant correctMutant) {
		for (PolicyMutant mutant: mutantList) {
			if (mutant != correctMutant) {
				mutant.clear();
			}
		}
	}
	
	private PolicyMutant repairBugPositionCombiningAlgorithm(PolicyMutator mutator) throws Exception {
		//create mutant methods who's bugPosition == -1
		// CRC
		List<PolicyMutant> mutantList = mutator.createCombiningAlgorithmMutants();
		PolicyMutant correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		return correctMutant;
	}
	
	private PolicyMutant repairBugPositionPolicyTarget(PolicyMutator mutator) throws Exception {
		List<PolicyMutant> mutantList = null;
		PolicyMutant correctMutant = null;
		//create mutant methods who's bugPosition == 0
		// PTT
		mutantList = mutator.createPolicyTargetTrueMutants();
		correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		if(correctMutant != null) {
			return correctMutant;
		}
		// PTF
		mutantList = mutator.createPolicyTargetTrueMutants();
		correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		if(correctMutant != null) {
			return correctMutant;
		}
		//FPR
		mutantList = mutator.createFirstPermitRuleMutants();
		correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		if(correctMutant != null) {
			return correctMutant;
		}
		//FDR
		mutantList = mutator.createFirstDenyRuleMutants();
		correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		if(correctMutant != null) {
			return correctMutant;
		}
		return correctMutant;
	}
	
	private PolicyMutant repairBugPositionMaxRules(PolicyMutator mutator
			) throws Exception {
		List<Rule> ruleList = mutator.getRuleList();
		List<PolicyMutant> mutantList = null;
		PolicyMutant correctMutant = null;
		int maxRules = mutator.getPolicy().getChildElements().size();
		//RER
		//BECAREFUL!!! bugPosition is maxRules
		for(int ruleIndex = 1; ruleIndex <= ruleList.size(); ruleIndex++) {
			//take care, ruleIndex begins from one
			mutantList = mutator.createRemoveRuleMutants(ruleList.get(ruleIndex-1), ruleIndex, maxRules);
			correctMutant = find1stCorrectMutant(mutantList);
			deleteMutantFile(mutantList, correctMutant);
			if(correctMutant != null) {
				return correctMutant;
			}
		}
		return correctMutant;
	}
	
	private PolicyMutant repairBugPositionRules(PolicyMutator mutator, 
			Rule myrule, int ruleIndex) throws Exception {
		List<PolicyMutant> mutantList = null;
		PolicyMutant correctMutant = null;
		//CRE
		mutantList = mutator.createRuleEffectFlippingMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		if(correctMutant != null) {
			return correctMutant;
		}
//		//ANR
//		mutantList = mutator.createAddNewRuleMutants(myrule, ruleIndex);
//		correctMutant = find1stCorrectMutant(mutantList);
//		deleteMutantFile(mutantList, correctMutant);
//		if(correctMutant != null) {
//			return correctMutant;
//		}
		//RTT
		mutantList = mutator.createRuleTargetTrueMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		if(correctMutant != null) {
			return correctMutant;
		}
		//RTF
		mutantList = mutator.createRuleTargetFalseMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		if(correctMutant != null) {
			return correctMutant;
		}
		//RCT
		mutantList = mutator.createRuleConditionTrueMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		if(correctMutant != null) {
			return correctMutant;
		}
		//RCF
		mutantList = mutator.createRuleConditionFalseMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		if(correctMutant != null) {
			return correctMutant;
		}
		//ANF
		mutantList = mutator.createAddNotFunctionMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		if(correctMutant != null) {
			return correctMutant;
		}
		//RNF
		mutantList = mutator.createRemoveNotFunctionMutants(myrule,  ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		if(correctMutant != null) {
			return correctMutant;
		}
		//FCF
		mutantList = mutator.createFlipComparisonFunctionMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		if(correctMutant != null) {
			return correctMutant;
		}
		//CCF
		mutantList = mutator.createChangeComparisonFunctionMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		if(correctMutant != null) {
			return correctMutant;
		}
		//RPTE
		mutantList = mutator.createRemoveParallelTargetElementMutants(myrule, ruleIndex);
		correctMutant = find1stCorrectMutant(mutantList);
		deleteMutantFile(mutantList, correctMutant);
		if(correctMutant != null) {
			return correctMutant;
		}
		return correctMutant;
	}

	public static List<List<String>> getRepairMethodPairList() {
		return PolicyRepairer.repairMethodPairList;
	}

}

