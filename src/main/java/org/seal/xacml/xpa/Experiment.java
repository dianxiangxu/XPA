package org.seal.xacml.xpa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.seal.xacml.NameDirectory;
import org.seal.xacml.PolicyTestSuite;
import org.seal.xacml.RequestGeneratorBase;
import org.seal.xacml.TaggedRequest;
import org.seal.xacml.TestRecord;
import org.seal.xacml.components.CombiningAlgorithmURI;
import org.seal.xacml.components.MutationBasedTestMutationMethods;
import org.seal.xacml.coverage.DecisionCoverage;
import org.seal.xacml.coverage.MCDC;
import org.seal.xacml.coverage.RuleCoverage;
import org.seal.xacml.coverage.RulePairCoverage;
import org.seal.xacml.gui.ResultConverter;
import org.seal.xacml.mutation.MutationBasedTestGenerator;
import org.seal.xacml.mutation.PNOMutationBasedTestGenerator;
import org.seal.xacml.mutation.PolicySpreadSheetMutantSuite;
import org.seal.xacml.policyUtils.PolicyLoader;
import org.seal.xacml.semanticCoverage.PolicyRunner;
import org.seal.xacml.semanticCoverage.TestSuite;
import org.seal.xacml.semanticMutation.Mutant;
import org.seal.xacml.semanticMutation.Mutator;
import org.seal.xacml.utils.FileIOUtil;
import org.seal.xacml.utils.MiscUtil;
import org.seal.xacml.utils.MutantUtil;
import org.seal.xacml.utils.TestUtil;
import org.seal.xacml.utils.XACMLElementUtil;
import org.wso2.balana.AbstractPolicy;

public class Experiment {
	private static StringBuilder times;
	private static StringBuilder tests;
	private static StringBuilder kills;
	private static StringBuilder mutationScores;
	private static StringBuilder mkpts;
	public static String currentPolicyCA;
	
	public static void main(String[] args) throws Exception{
		times = new StringBuilder();
		times.append("Test Generation Time in Millis,RC,NE-DC,DC,NE-MCDC,MCDC,SMT,NO-SMT,Pair,PD-Pair").append(System.lineSeparator());
		
		tests = new StringBuilder();
		tests.append("Number of tests,RC,NE-DC,DC,NE-MCDC,MCDC,SMT,NO-SMT,Pair,PD-Pair").append(System.lineSeparator());
		
		kills = new StringBuilder();
		kills.append("Number of mutants killed,RC,NE-DC,DC,NE-MCDC,MCDC,SMT,NO-SMT,Pair,PD-Pair").append(System.lineSeparator());
		
		mkpts = new StringBuilder();
		mkpts.append("MKPT,RC,NE-DC,DC,NE-MCDC,MCDC,SMT,NO-SMT,Pair,PD-Pair").append(System.lineSeparator());
		
		mutationScores = new StringBuilder();
		mutationScores.append("Mutation Score,RC,NE-DC,DC,NE-MCDC,MCDC,SMT,NO-SMT,Pair,PD-Pair").append(System.lineSeparator());
		
		
		List<String> nameDirs = new ArrayList<String>();
		nameDirs.add(NameDirectory.RULE_COVERAGE);
		nameDirs.add(NameDirectory.DECISION_COVERAGE_NO_ERROR);
		nameDirs.add(NameDirectory.DECISION_COVERAGE);
		nameDirs.add(NameDirectory.MCDC_COVERAGE_NO_ERROR);
		nameDirs.add(NameDirectory.MCDC_COVERAGE);
		nameDirs.add(NameDirectory.MUTATION_BASED_TEST);
		nameDirs.add(NameDirectory.NON_OPTIMIZED_MUTATION_BASED_TEST);
		nameDirs.add(NameDirectory.RULE_PAIR_COVERAGE);
		nameDirs.add(NameDirectory.PERMIT_DENY_RULE_PAIR_COVERAGE);
		
		
		List<String> policyFilePaths = new ArrayList<String>();
//		policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/conference3/conference3.xml");
//		policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/fedora-rule3/fedora-rule3.xml");
//		policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/kmarket-blue-policy/kmarket-blue-policy.xml");
//		policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/kmarket-gold-policy/kmarket-gold-policy.xml");
//		policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/kmarket-sliver-policy/kmarket-sliver-policy.xml");
//		policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/sample/sample.xml");
//		policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/sample-do/sample-do.xml");
//		policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/sample-fa/sample-fa.xml");
//		policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/sample-dup/sample-dup.xml");
//		policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/sample-pud/sample-pud.xml");
//		policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/itrust3/itrust3.xml");
//		policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/itrust3-5/itrust3-5.xml");
		policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/itrust3-10/itrust3-10.xml");
		policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/itrust3-20/itrust3-20.xml");
			
	//	policyFilePaths.add("/home/roshan/Projects/XPA/Experiments/smt-strength-sample/smt-strength-sample.xml");
		
		for(String policyFilePath:policyFilePaths) {
			//String policyFilePath = "/home/roshan/Projects/XPA/Experiments/conference3/conference3.xml";
			
			String[] tokens = policyFilePath.split("/");
			String name = tokens[tokens.length-1].split("\\.")[0];
			AbstractPolicy policy = null;
			try{
				policy = PolicyLoader.loadPolicy(new File(policyFilePath));
			}catch(Exception e){
				e.printStackTrace();
			}
			
			if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("PO"))) {
				currentPolicyCA = "PO  ";
			}
			if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("DO"))) {
				currentPolicyCA = "DO  ";
			}
			if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("FA"))) {
				currentPolicyCA = "FA  ";
			}
			if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("PUD"))) {
				currentPolicyCA = "PUD  ";
			}
			if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("DUP"))) {
				currentPolicyCA = "DUP  ";
			}
			if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("OPO"))) {
				currentPolicyCA = "OPO  ";
			}
			if(policy.getCombiningAlg().getIdentifier().toString().equals(CombiningAlgorithmURI.map.get("ODO"))) {
				currentPolicyCA = "ODO  ";
			}
			
			PolicySpreadSheetMutantSuite ms = generateMutants(policyFilePath,name);
			generateTests(policyFilePath,name,nameDirs, ms);
		}
		System.out.println(times.toString());
		System.out.println(tests.toString());
		System.out.println(kills.toString());
		
		System.out.println(mutationScores.toString());
		
		System.out.println(mkpts.toString());
		
		File file = new File("/home/roshan/Projects/XPA/Experiments/data.csv");
		FileWriter fr = new FileWriter(file);
		BufferedWriter br = new BufferedWriter(fr);
		br.write(tests + System.lineSeparator() + System.lineSeparator() + System.lineSeparator());
		br.write(kills + System.lineSeparator() + System.lineSeparator() + System.lineSeparator());
		br.write(mutationScores + System.lineSeparator() + System.lineSeparator() + System.lineSeparator());
		br.write(mkpts + System.lineSeparator() + System.lineSeparator() + System.lineSeparator());
		br.write(times + System.lineSeparator() + System.lineSeparator() + System.lineSeparator());
		br.close();
		fr.close();
	}
	
	public static List<PolicyTestSuite> generateTests(String policyFilePath,String name, List<String> nameDirs, PolicySpreadSheetMutantSuite mutantSuite) throws Exception{
		List<PolicyTestSuite> lst = new ArrayList<PolicyTestSuite>();
		System.out.println(name);
		String time = name ;
		String numbers = name;
		String ms = name;
		String kill = name;
		String mkpt = name;
		
		RequestGeneratorBase[] requestGenerators = new RequestGeneratorBase[9];
		
		requestGenerators[0] = new RuleCoverage(policyFilePath); 
		requestGenerators[2] = new DecisionCoverage(policyFilePath,true);
		requestGenerators[1] = new DecisionCoverage(policyFilePath,false);
		requestGenerators[4] = new MCDC(policyFilePath,true);
        
		requestGenerators[3] = new MCDC(policyFilePath,false);
		
		requestGenerators[5] = new MutationBasedTestGenerator(policyFilePath);
		requestGenerators[6] = new PNOMutationBasedTestGenerator(policyFilePath);
		
		requestGenerators[7] = new RulePairCoverage(policyFilePath);
		requestGenerators[8] = new RulePairCoverage(policyFilePath);
		File file = new File("/home/roshan/Projects/XPA/Experiments/results.csv");
		FileWriter fr = new FileWriter(file, true);
		BufferedWriter br = new BufferedWriter(fr);
		String l = name + ",CRE,RTT,RTF,RCT,RCF,ANF,RNF,RER,PTT,PTF,FPR,FDR,CRC,RPTE,killed CRCs, not killed CRCs" + System.lineSeparator();
		br.write(l);
		br.close();
		fr.close();
		int count = 5;
		for(int i = 0 ; i < count ; i++) {
//			if(i == 4) {
//				continue;
//			}
//			if(i == 5 ) {
//				continue;
//			}
			long millis = System.currentTimeMillis();
			List<String> requests = null;
			List<TaggedRequest> taggedRequests = null;
			if( i < 5) {
				requests = requestGenerators[i].generateTests();
			}
			
			if( i == 5 || i == 6) {
				taggedRequests = requestGenerators[i].generateRequests(getMutationOperatorList(false));
			}
			if(i == 7) {
				requests = requestGenerators[i].generateTests(false);
			}
			if(i == 8) {
				requests = requestGenerators[i].generateTests(true);
			}
			long millis2 = System.currentTimeMillis();
			System.out.println("-------------");
			System.out.println((millis2-millis));
			time+= "," + String.valueOf(millis2-millis) ;
			System.out.println("-------------");
			
			PolicyTestSuite testSuite = null;
			if(i == 5 || i==6 ) {
				testSuite = new PolicyTestSuite(policyFilePath,nameDirs.get(i),taggedRequests);
				numbers += "," + String.valueOf(taggedRequests.size());
				
			} else {
				numbers += "," + String.valueOf(requests.size());
				testSuite = new PolicyTestSuite(policyFilePath,requests,nameDirs.get(i));
			} 
			testSuite.save();
			String workingTestSuiteFileName = TestUtil.getTestSuiteMetaFilePath(policyFilePath, nameDirs.get(i));
			
			Vector<Vector<Object>> data = TestUtil.getTestRecordsVector(testSuite.getTestRecords());
			
			AbstractPolicy policy = PolicyLoader.loadPolicy(new File(policyFilePath));
			for (Vector<Object> child : data) {
				int result = TestSuite.runTestWithoutOracle(policy, child.get(6).toString());
				// System.out.println(result);
				String actualResponse = ResultConverter.ConvertResult(result);
				child.set(4, actualResponse);
			
			}
			boolean hasOracleValue = false;
			for (Vector<Object> child : data) {
				if (!child.get(3).toString().equals("")) {
					hasOracleValue = true;
				}
			}
			String r = null;
			if (!hasOracleValue) {
				List<TestRecord> recordList = new ArrayList<TestRecord>();
				for (Vector<Object> child : data) {
					recordList.add(TestUtil.getTestRecord(child));
				}
				PolicyTestSuite tsuite = new PolicyTestSuite(recordList, "GenTests/",nameDirs.get(i));
				tsuite.writeMetaFile(workingTestSuiteFileName);
				System.out.println(workingTestSuiteFileName + " saved.");
				r= runTests(policyFilePath, name, nameDirs, i, tsuite, mutantSuite);
				
			}
			String[]tokens = r.split("==");
			kill += "," + tokens[0].trim();
			ms += "," + tokens[1].trim();
			mkpt += "," + tokens[2].trim();
		}
		tests.append(numbers).append(System.lineSeparator());
		kills.append(kill).append(System.lineSeparator());
		mutationScores.append(ms).append(System.lineSeparator());
		mkpts.append(mkpt).append(System.lineSeparator());
		times.append(time).append(System.lineSeparator());
		
		return lst;
		//kills.append(kill).append(System.lineSeparator());
	}
	public static  PolicySpreadSheetMutantSuite generateMutants(String policyFilePath, String name) throws Exception {
		File policyFile = new File(policyFilePath);
		AbstractPolicy policy = PolicyLoader.loadPolicy(policyFile);
		
		List<Mutant> mutants = new ArrayList<Mutant>();
        Mutator mutator = new Mutator(new Mutant(policy, XACMLElementUtil.getPolicyName(policyFile)));
        File mutantsFolder = new File(MutantUtil.getMutantsFolderForPolicyFile(policyFile).toString());
        if(mutantsFolder.exists()){
        	FileUtils.cleanDirectory(mutantsFolder);
        } else{
        	mutantsFolder.mkdir();
        }
        MutationBasedTestGenerator testGenerator = new MutationBasedTestGenerator(policyFilePath);
		MutationBasedTestMutationMethods mbtMethods = new MutationBasedTestMutationMethods();
		//this.startProgressStatus();
		List<String> mutationMethods = new ArrayList<String>();
		mutationMethods.add("createCombiningAlgorithmMutants");
		mutationMethods.add("createRuleConditionTrueMutants");
		mutationMethods.add("createRuleTargetTrueMutants");
		List<TaggedRequest> taggedRequests = testGenerator.generateRequests(mutationMethods);
		AbstractPolicy p = PolicyLoader.loadPolicy(policyFile);
		List<Mutant> tR = new ArrayList<Mutant>();
        
        for(String method:getMutationOperatorList(false)) {
        	List<String> methods = new ArrayList<String>();
        	methods.add(method);
	        List<Mutant> muts = mutator.generateSelectedMutants(methods);
	        for(Mutant mutant: muts){
	        	if(methods.get(0).equals("createCombiningAlgorithmMutants")||methods.get(0).equals("createRuleConditionTrueMutants")||methods.get(0).equals("createRuleTargetTrueMutants")) {
	        		boolean live = true;
	        		for(TaggedRequest t:taggedRequests) {
	        			
	        			int rp = PolicyRunner.evaluate(p, t.getBody());
	        			int rm = PolicyRunner.evaluate(mutant.getPolicy(), t.getBody());
	        			if (rp!=rm) {
	        				live = false;
	        				break;
	        			}
	        			
	        		}
	        		if(live) {
	        			tR.add(mutant);
	        			
	        			continue;
	        		}
	        		
	        	}
				FileIOUtil.saveMutant(mutant,mutantsFolder.toString());
				mutant.setPolicy(null);
			}
	        for(Mutant m:tR) {
	        System.out.println("Equivalent mutant -----> " + m.getName());
	        }
	        muts.removeAll(tR);
	        mutants.addAll(muts);
        }
        PolicySpreadSheetMutantSuite mutantSuite  = new PolicySpreadSheetMutantSuite(mutantsFolder.toString(),mutants,XACMLElementUtil.getPolicyName(policyFile)); // write to spreadsheet		
		mutantSuite.writePolicyMutantsSpreadSheet(mutants,XACMLElementUtil.getPolicyName(policyFile) + "_mutants.xls");
		return mutantSuite;
	}
	
	public static List<String> getMutationOperatorList(boolean filter){
		List<String> lst = new ArrayList<String>();
			lst.add("createRuleEffectFlippingMutants");
			lst.add("createRemoveRuleMutants");
			lst.add("createRuleTargetTrueMutants");
			lst.add("createRuleTargetFalseMutants");
			lst.add("createRuleConditionTrueMutants");
			lst.add("createRuleConditionFalseMutants");
			lst.add("createFirstPermitRuleMutants");
			lst.add("createFirstDenyRuleMutants");
			lst.add("createAddNotFunctionMutants");
			lst.add("createRemoveNotFunctionMutants");
			lst.add("createCombiningAlgorithmMutants");
			lst.add("createRemoveParallelTargetElementMutants");
			lst.add("createPolicyTargetTrueMutants");
			lst.add("createPolicyTargetFalseMutants");
		
		
		return lst;
	}
	
	public static String runTests(String policyFilePath,String name,List<String> nameDirs,int i,PolicyTestSuite ts, PolicySpreadSheetMutantSuite mutantSuite) throws Exception {
		String outputFileName = new File(policyFilePath).getParent() +File.separator +  name +"_test_suites" + File.separator +   nameDirs.get(i) + "_test_suite"+File.separator+"MutationTestingResults.xls";
		List<String> requests = new ArrayList<String>();
		List<String> oracles = new ArrayList<String>();
		for(TestRecord record: ts.getTestRecords()){
			requests.add(record.getRequest());
			oracles.add(record.getOracle());
		}
		TestSuite testSuite = new TestSuite(null,requests, oracles);
		File policyFile = new File(policyFilePath);
		
		File mutantsFolder = new File(MutantUtil.getMutantsFolderForPolicyFile(policyFile).toString());
		Vector<Vector<Object>> data = MutantUtil.getVectorsForMutants(mutantSuite.getMutantList());
		try {
			File file = new File("/home/roshan/Projects/XPA/Experiments/results.csv");
			FileWriter fr = new FileWriter(file, true);
			BufferedWriter br = new BufferedWriter(fr);
			String type =null;
			if(i==0) {
				type = "RC";
			} else if(i==1) {
				type = "NE-DC";
			}
			else if(i==2) {
				type = "DC";
			}else if(i==3) {
				type = "NE-MCDC";
			}else if(i==4) {
				type = "MCDC";
			} else {
				type = "";
			}
			br.write(type);
			br.close();
			fr.close();
		}catch(Exception e) {
			
		}
		int killedCount = mutantSuite.updateMutantTestResult(data,testSuite,mutantsFolder);
		mutantSuite.writeDetectionInfoToExcelFile(outputFileName, testSuite,mutantsFolder);
		int total = mutantSuite.getMutantList().size();
		int liveCount = total - killedCount;
		double ratio = killedCount / (double)total;
		String message = "Number of killed mutants : " + killedCount + System.lineSeparator();
		message += "Number of live mutants : " + liveCount + System.lineSeparator();
		message += "Mutation Score : " + (MiscUtil.roundNumberToTwoDecimalPlaces(ratio*100))+ "%" + System.lineSeparator()  + System.lineSeparator();
		message += "Mutation testing results are saved into file: \n" + outputFileName;
		String output = killedCount + "==" + MiscUtil.roundNumberToTwoDecimalPlaces(ratio*100) + "==" + MiscUtil.roundNumberToTwoDecimalPlaces(killedCount/(double)ts.getNumberOfTests());
		return output;
		
	}
}
