package org.seal.xacml.mutation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.seal.combiningalgorithms.MyAttr;
import org.seal.combiningalgorithms.function;
import org.seal.coverage.PolicySpreadSheetTestRecord;
import org.seal.coverage.PolicySpreadSheetTestSuite;
import org.seal.gui.TestPanel;
import org.seal.mutation.PolicyMutant;
import org.seal.policyUtils.PolicyLoader;
import org.seal.semanticMutation.Mutant;
import org.seal.semanticMutation.Mutator;
import org.seal.xacml.helpers.Z3StrExpressionHelper;
import org.seal.xacml.utils.PolicyElementUtil;
import org.w3c.dom.Document;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.wso2.balana.PolicyMetaData;
import org.wso2.balana.xacml3.AnyOfSelection;
import org.wso2.balana.xacml3.Target;
import org.xml.sax.SAXException;

public class MutationBasedTestGenerator {
	private static String policyFilePath;
	private static AbstractPolicy policy;
	private static Document doc;
	private static PolicyMetaData policyMetaData;
	private static List<String> requests;
	private static Z3StrExpressionHelper z3ExpressionHelper;

	private static void init(String path) throws IOException, SAXException, ParserConfigurationException, ParsingException{
		policyFilePath = path;
		doc = PolicyLoader.getDocument(new FileInputStream(policyFilePath));
		policyMetaData = PolicyLoader.loadPolicy(doc).getMetaData();
		requests = new ArrayList<String>();
		z3ExpressionHelper = new Z3StrExpressionHelper();
	}
	   
	public static List<String> generateRequests(String policyFilePath,List<String> mutationMethods){
		init(policyFilePath);
		Mutator mutator = new Mutator(new Mutant(policy, PolicyElementUtil.getPolicyName(policyFilePath)));
        List<Mutant> mutants = mutator.generateSelectedMutants(mutationMethods);
		try {
			policyMutator.createPolicyTargetTrueMutants();
			PolicySpreadSheetTestRecord ptt = policyx.generate_PolicyTargetTrue(getTestPanel());
			if (ptt != null) {
				tests.add(ptt);
				String mutantPath = policyMutator.getMutantFileName("PTT1");
				String reqPath = ptt.getRequest();
				if (validation(policyPath, mutantPath, reqPath)) {
					mlistIndex++;
					tlistIndex++;
					valid.add(ptt);
					validTests++;
				}
			}
			getMutantsByType(policyMutator.getMutantList(), testable, "PTT");
			/*
			if (boxPTF.isSelected() && !boxCRE.isSelected() && !boxRPTE.isSelected()) {
				policyMutator.createPolicyTargetFalseMutants();
				PolicySpreadSheetTestRecord ptf = policyx
						.generate_PolicyTargetFalse(getTestPanel());
				if (ptf != null) {
					tests.add(ptf);
					String mutantPath = policyMutator
							.getMutantFileName("PTF1");
					String reqPath = ptf.getRequest();
					if (validation(policyPath, mutantPath, reqPath)) {
						mlistIndex++;
						tlistIndex++;
						valid.add(ptf);
						validTests++;
					}
				}
				getMutantsByType(policyMutator.getMutantList(), testable,"PTF");
			}
			if (boxCRC.isSelected()) {
				policyMutator.createCombiningAlgorithmMutants();
				long start = System.currentTimeMillis();
				Vector<Vector<Object>> data = new Vector<Vector<Object>>();
				data = policyx.generateRequestForDifferenceRCAs();

				ArrayList<PolicySpreadSheetTestRecord> crc = new ArrayList<PolicySpreadSheetTestRecord>();
				int count = 1;
				for (Vector<Object> child : data) {
					String request = child.get(4).toString();
					if(request == null || request == "")
						continue;
					try {
						String path = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator
								+ "request"
								+ "CRC"
								+ count + ".txt";
						FileWriter fw = new FileWriter(path);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(request);
						bw.close();
						
						PolicySpreadSheetTestRecord ptr = null;
						ptr = new PolicySpreadSheetTestRecord(
								PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + "CRC" + count,
								"request" + "CRC" + count + ".txt", request, "");
						if(ptr != null){
							crc.add(ptr);
							count++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				determineValidTests(crc, policyMutator, policyPath, "CRC");
				totalGenerationTime += System.currentTimeMillis() - start;
				if(boxOptimize.isSelected());
					//removeDuplicates(valid);
				if (crc.size() >= 1)
					getMutantsByType(policyMutator.getMutantList(),
							testable, "CRC");	

			}
			ArrayList<PolicySpreadSheetTestRecord> CRE = new ArrayList<PolicySpreadSheetTestRecord>();
			if (boxCRE.isSelected()) {
				policyMutator.createRuleEffectFlippingMutants();
				long start = System.currentTimeMillis();
				CRE = policyx
						.generate_FlipRuleEffect(getTestPanel(), opt);
				determineValidTests(CRE, policyMutator, policyPath, "CRE");
				//valid.addAll(CRE);
				totalGenerationTime += System.currentTimeMillis() - start;
				if(boxOptimize.isSelected());
					//removeDuplicates(valid);
				if (CRE.size() >= 1)
					getMutantsByType(policyMutator.getMutantList(),
							testable, "CRE");
			}
			if (boxRER.isSelected() && !opt) {
				policyMutator.createRemoveRuleMutants();
				long start = System.currentTimeMillis();
				ArrayList<PolicySpreadSheetTestRecord> rer = policyx
						.generate_RemoveOneRule(getTestPanel(), opt);
				determineValidTests(rer, policyMutator, policyPath, "RER");
				totalGenerationTime += System.currentTimeMillis() - start;
				if(boxOptimize.isSelected());
					//removeDuplicates(valid);
				if (rer.size() >= 1)
					getMutantsByType(policyMutator.getMutantList(),
							testable, "RER");
			}

			if (boxANR.isSelected()) {
				// policyMutator.createAddNewRuleMutants();

			}

			if (boxRTT.isSelected()) {
				policyMutator.createRuleTargetTrueMutants();
				long start = System.currentTimeMillis();
				ArrayList<PolicySpreadSheetTestRecord> rtt = policyx
						.generate_RuleTargetTrue(getTestPanel(), opt);
				determineValidTests(rtt, policyMutator, policyPath, "RTT");
				totalGenerationTime += System.currentTimeMillis() - start;
				if(boxOptimize.isSelected());
					//removeDuplicates(valid);
				if (rtt.size() >= 1)
					getMutantsByType(policyMutator.getMutantList(),
							testable, "RTT");
			}

			if (boxRTF.isSelected() && !opt) {
				policyMutator.createRuleTargetFalseMutants();
				long start = System.currentTimeMillis();
				ArrayList<PolicySpreadSheetTestRecord> rtf = policyx
						.generate_RuleTargetFalse(getTestPanel(),
								policyMutator, opt);
				determineValidTests(rtf, policyMutator, policyPath, "RTF");
				totalGenerationTime += System.currentTimeMillis() - start;
				if(boxOptimize.isSelected());
					//removeDuplicates(valid);
				if (rtf.size() >= 1)
					getMutantsByType(policyMutator.getMutantList(),
							testable, "RTF");
			}

			if (boxRCT.isSelected()) {
				policyMutator.createRuleConditionTrueMutants();
				long start = System.currentTimeMillis();
				ArrayList<PolicySpreadSheetTestRecord> rct = policyx
						.generate_RuleConditionTrue(getTestPanel(), opt);
				determineValidTests(rct, policyMutator, policyPath, "RCT");
				totalGenerationTime += System.currentTimeMillis() - start;
				if(boxOptimize.isSelected());
					//removeDuplicates(valid);
				if (rct.size() >= 1)
					getMutantsByType(policyMutator.getMutantList(),
							testable, "RCT");
			}

			if (boxRCF.isSelected() && !opt) {
				policyMutator.createRuleConditionFalseMutants();
				long start = System.currentTimeMillis();
				ArrayList<PolicySpreadSheetTestRecord> rcf = policyx
						.generate_RuleConditionFalse(getTestPanel(), opt);
				determineValidTests(rcf, policyMutator, policyPath, "RCF");
				totalGenerationTime += System.currentTimeMillis() - start;
				if(boxOptimize.isSelected());
					//removeDuplicates(valid);
				if (rcf.size() >= 1)
					getMutantsByType(policyMutator.getMutantList(),
							testable, "RCF");
			}

			if (boxFPR.isSelected()) {
				policyMutator.createFirstPermitRuleMutants();
				long start = System.currentTimeMillis();
				ArrayList<PolicySpreadSheetTestRecord> fpr = policyx
						.generate_FirstPermitRule(getTestPanel(), opt);
				determineValidTests(fpr, policyMutator, policyPath, "FPR");
				totalGenerationTime += System.currentTimeMillis() - start;
				if(boxOptimize.isSelected());
					//removeDuplicates(valid);
				if (fpr.size() >= 1)
					getMutantsByType(policyMutator.getMutantList(),
							testable, "FPR");
			}

			if (boxFDR.isSelected()) {
				policyMutator.createFirstDenyRuleMutants();
				long start = System.currentTimeMillis();
				ArrayList<PolicySpreadSheetTestRecord> fdr = policyx
						.generate_FirstDenyRule(getTestPanel(), opt);
				determineValidTests(fdr, policyMutator, policyPath, "FDR");
				totalGenerationTime += System.currentTimeMillis() - start;
				if(boxOptimize.isSelected());
					//removeDuplicates(valid);
				if (fdr.size() >= 1)
					getMutantsByType(policyMutator.getMutantList(),
							testable, "FDR");
			}

			if (boxRTR.isSelected()) {
				// policyMutator.createRuleTypeReplacedMutants();

			}

			if (boxFCF.isSelected()) {
				// policyMutator.createFlipComparisonFunctionMutants();

			}

			if (boxANF.isSelected()) {
				policyMutator.createAddNotFunctionMutants();
				long start = System.currentTimeMillis();
				ArrayList<PolicySpreadSheetTestRecord> anf = policyx
						.generate_AddNotFunction(getTestPanel(), opt);
				determineValidTests(anf, policyMutator, policyPath, "ANF");
				totalGenerationTime += System.currentTimeMillis() - start;
				if(boxOptimize.isSelected());
					//removeDuplicates(valid);
				if (anf.size() >= 1)
					getMutantsByType(policyMutator.getMutantList(),
							testable, "ANF");
			}

			if (boxRNF.isSelected() && !opt) {
				policyMutator.createRemoveNotFunctionMutants();
				long start = System.currentTimeMillis();
				ArrayList<PolicySpreadSheetTestRecord> rnf = policyx
						.generate_RemoveNotFunction(getTestPanel(), opt);
				determineValidTests(rnf, policyMutator, policyPath, "RNF");
				totalGenerationTime += System.currentTimeMillis() - start;
				if(boxOptimize.isSelected());
					//removeDuplicates(valid);
				if (rnf.size() >= 1)
					getMutantsByType(policyMutator.getMutantList(),
							testable, "RNF");

			}
			
			//cross_pivot(valid, policyMutator);
			ArrayList<PolicySpreadSheetTestRecord> RPTE = new ArrayList<PolicySpreadSheetTestRecord>();
			if (boxRPTE.isSelected()) {
				policyMutator.createRemoveParallelTargetElementMutants();
				long start = System.currentTimeMillis();
				RPTE = policyx
						.generate_RemoveParallelTargetElement(getTestPanel(), opt);
				determineValidTests(RPTE, policyMutator, policyPath, "RPTE");
				totalGenerationTime += System.currentTimeMillis() - start;
				if (RPTE.size() >= 1)
					getMutantsByType(policyMutator.getMutantList(),
							testable, "RPTE");
			}

			if (boxRPCE.isSelected()) {
				// policyMutator.createRemoveParallelConditionElementMutants();

			}*/
			int numMutants = testable.size();
			mutantSuite = policyMutator.generateMutants(); // write to
															// spreadsheet
			setUpMutantPanel();
			if (validTests > 0)
				System.out
						.printf("Generated tests: " + tests.size()
								+ "\nValid tests: " + validTests
								+ "\nPercent valid: %.2f\n",
								((double) validTests / (double) tests
										.size()) * 100.00);
			System.out.println("Testable size: " + testable.size());

			if (boxOptimize.isSelected()) {
				ArrayList<PolicySpreadSheetTestRecord> crc = getRecordSublist(valid, "CRC");
				ArrayList<PolicySpreadSheetTestRecord> rpte = getRecordSublist(valid, "RPTE");
				long start = System.currentTimeMillis();
				removeDuplicates(valid, policyMutator);
				CRCvsCRE(valid, crc, policyMutator);
				if(CRE.size() > 5)
					CRCvsCRC(valid, crc, policyMutator);
				//RTTvsRPTE(valid, rpte, policyMutator);
				RPTEvsRTT(valid, rpte, policyMutator);
				//RPTEvsRPTE(valid, rpte, policyMutator);
				//CRCvsFT(valid, crc, policyMutator);
				//CRCvsTF(valid, crc, policyMutator);
				totalOptimizationTime += (System.currentTimeMillis() - start);
			}

			// PolicySpreadSheetTestRecord[] myTests = new
			// PolicySpreadSheetTestRecord[valid.size()];
			// for(int i = 0; i < valid.size(); i++)
			// myTests[i] = valid.get(i);

			// optimize(valid, myTests, policyMutator.getMutantList(),
			// policyMutator);
			
			if (validTests > 0)
				System.out
						.printf("Generated tests: " + tests.size()
								+ "\nValid tests: " + validTests
								+ "\nPercent valid: %.2f\n",
								((double) validTests / (double) tests
										.size()) * 100.00);
			System.out.println("Mutants: " + numMutants);
			System.out.println("Generation time: " + totalGenerationTime
					/ 1000.00);
			System.out.println("Optimization time: "
					+ totalOptimizationTime / 1000.00);
			System.out.println("Total time: " + (totalGenerationTime + totalOptimizationTime) / 1000.00);
			if (valid.size() > 0)
				System.out.println("Optimal tests: " + valid.size());
			
			String dir = xpa.getWorkingPolicyFile().getParent();
			System.err.println(dir);

			try {
				testSuite = new PolicySpreadSheetTestSuite(valid,
						xpa.getWorkingPolicyFilePath());
				testSuite.writeToExcelFile(testPath);
				testPanel.setTestSuite(testSuite);
				testPanel.setUpTestPanel();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	

	return null;
	}

	public PolicySpreadSheetTestRecord generate_PolicyTargetTrue(TestPanel t)
	{
		PolicySpreadSheetTestRecord ptr = null;
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		function f = new function();
		if(!policy.isTargetEmpty())
		{
			Target policyTarget = (Target)policy.getTarget();
			List<AnyOfSelection> anyOf = policyTarget.getAnyOfSelections();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			if(anyOf.size() != 0)
			{
				sb.append(False_Target(policyTarget, collector) + "\n");
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if(sat)
				{
					System.out.println(nameMap.size() + " map size");
					try
					{	
						z3.getValue(collector, nameMap);
						
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					System.out.println(collector.size() + " collector size");
					String request = f.print(collector);
					try
					{
						String path = t.getTestOutputDestination("_MutationTests") 
								+ File.separator + "requestPTT1.txt";
						FileWriter fw = new FileWriter(path);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(request);
						bw.close();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					ptr = new PolicySpreadSheetTestRecord(PolicySpreadSheetTestSuite.TEST_KEYWORD
							+ " PTT1", "requestPTT1.txt", request, "");
					generator.add(ptr);
				}
			}
		}
		return ptr;
	}
	
}
