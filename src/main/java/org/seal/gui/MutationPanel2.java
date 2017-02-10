package org.seal.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedHashSet;
import java.util.Vector;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.seal.combiningalgorithms.PolicyX;
import org.seal.combiningalgorithms.function;
import org.seal.combiningalgorithms.loadPolicy;
import org.seal.coverage.PolicySpreadSheetTestRecord;
import org.seal.coverage.PolicySpreadSheetTestSuite;
import org.seal.mutation.PolicyMutant;
import org.seal.mutation.PolicyMutator;
import org.seal.mutation.PolicySpreadSheetMutantSuite;
import org.umu.editor.XMLFileFilter;
import org.wso2.balana.Policy;
import org.wso2.balana.Rule;
import org.wso2.balana.combine.xacml3.DenyOverridesRuleAlg;
import org.wso2.balana.combine.xacml3.DenyUnlessPermitRuleAlg;
import org.wso2.balana.combine.xacml3.PermitOverridesRuleAlg;
import org.wso2.balana.combine.xacml3.PermitUnlessDenyRuleAlg;
import org.seal.combiningalgorithms.MyAttr;

import java.util.ArrayList;

public class MutationPanel2 extends JPanel {
	/*
	 * Use code to present the test needs to be generated 1 -> policy target
	 * false + 1st rule true 2 -> policy target true + 1st rule true 3 -> policy
	 * target true + 1 rule true // rule coverage 4 -> policy target true + rule
	 * target false + rule condition true 5 -> policy target true + rule target
	 * true + rule condition false 6 -> if CA != FA, return; else make first
	 * permit rule true 7 -> if CA != FA, return; else make first deny rule
	 * true; 8 -> combining algorithm; 9 -> MC/DC Coverage
	 */

	// HashSet<Integer> test_table = new HashSet<Integer>();

	private static final long serialVersionUID = 1L;

	private TestPanel testPanel;
	private PolicySpreadSheetTestSuite testSuite;

	private XPA xpa;
	private PolicySpreadSheetMutantSuite mutantSuite;
	private Vector<Vector<Object>> data;

	private GeneralTablePanel tablePanel;

	private ArrayList<PolicySpreadSheetTestRecord> tests;
	private ArrayList<PolicySpreadSheetTestRecord> valid;
	private ArrayList<PolicySpreadSheetTestRecord> optimal;
	private ArrayList<PolicySpreadSheetTestRecord> final_optimal;

	private PolicySpreadSheetTestRecord[] mergeArray;

	public MutationPanel2(XPA xpa, TestPanel testPanel) {
		this.xpa = xpa;
		this.testPanel = testPanel;
		tests = new ArrayList<PolicySpreadSheetTestRecord>();
		valid = new ArrayList<PolicySpreadSheetTestRecord>();
		optimal = new ArrayList<PolicySpreadSheetTestRecord>();
		final_optimal = new ArrayList<PolicySpreadSheetTestRecord>();
	}

	private JCheckBox boxPTT = new JCheckBox("Policy Target True (PTT)");
	private JCheckBox boxPTF = new JCheckBox("Policy Target False (PTF)");
	private JCheckBox boxCRC = new JCheckBox(
			"Change Rule CombiningAlgorithm (CRC)");
	private JCheckBox boxCRE = new JCheckBox("Flip Rule Effect (CRE)");
	private JCheckBox boxRER = new JCheckBox("Remove One Rule (RER)");
	private JCheckBox boxANR = new JCheckBox("Add a New Rule (ANR)");
	private JCheckBox boxRTT = new JCheckBox("Rule Target True (RTT)");
	private JCheckBox boxRTF = new JCheckBox("Rule Target False (RTF)");
	private JCheckBox boxRCT = new JCheckBox("Rule Condition True (RCT)");
	private JCheckBox boxRCF = new JCheckBox("Rule Condition False (RCF)");
	private JCheckBox boxFPR = new JCheckBox("First Permit Rules (FPR)");
	private JCheckBox boxFDR = new JCheckBox("First Deny Rules (FDR)");
	private JCheckBox boxRTR = new JCheckBox(
			"Rule Type Replaced (RTR) - Not implemented");
	// private JCheckBox boxRUF = new JCheckBox(
	// "Remove Uniqueness Function (RUF) - Not Implemented"); //Turner
	// Lehmbecker
	private JCheckBox boxFCF = new JCheckBox("Flip Comparison Function (FCF)");
	private JCheckBox boxANF = new JCheckBox("Add Not Function (ANF)");
	private JCheckBox boxRNF = new JCheckBox("Remove Not Function (RNF)");
	private JCheckBox boxRPTE = new JCheckBox(
			"Remove Parallel Target Element (RPTE)");
	private JCheckBox boxRPCE = new JCheckBox(
			"Remove Parallel Condition Element (RPCE) - Not implemented");

	private JCheckBox boxSelectAll = new JCheckBox("Select All"); // All 13
																	// types of
																	// mutation.
	private JCheckBox boxSelectEight = new JCheckBox("Select 8"); // 8 type
																	// (PTT,
																	// PTF, CRC,
																	// CRE, RTT,
																	// RTF, RCT,
																	// RCF)
	private JCheckBox boxOptimize = new JCheckBox(
			"Do optimization - will significantly increase generation time");
	private JCheckBox boxOTF = new JCheckBox(
			"On the fly optimization - faster than normal optimization but still slow");
	private JCheckBox boxOPT2 = new JCheckBox(
			"Optimize v2 - potentially faster");
	private JCheckBox boxOPT3 = new JCheckBox(
			"Optimize v3 - Fastest, but not the most accurate");
	private int validTests = 0;

	private JPanel createPanel() {
		setAllIndividualBoxes(true);

		boxSelectAll.setSelected(true);
		boxSelectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (boxSelectAll.isSelected())
					setAllIndividualBoxes(true);
				else
					setAllIndividualBoxes(false);
			}
		});

		boxSelectEight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (boxSelectEight.isSelected())
					setEightBoxes(true);
				else
					setEightBoxes(false);
			}
		});

		JPanel myPanel = new JPanel();
		myPanel.setLayout(new GridLayout(13, 2));
		myPanel.add(boxPTT);
		myPanel.add(boxPTF);
		myPanel.add(boxCRC);
		myPanel.add(boxCRE);
		myPanel.add(boxRER);
		myPanel.add(boxANR);
		myPanel.add(boxRTT);
		myPanel.add(boxRTF);
		myPanel.add(boxRCT);
		myPanel.add(boxRCF);
		myPanel.add(boxFPR);
		myPanel.add(boxFDR);
		myPanel.add(boxRTR);
		myPanel.add(boxFCF);
		myPanel.add(boxANF);
		myPanel.add(boxRNF);
		myPanel.add(boxRPTE);
		myPanel.add(boxRPCE);
		// myPanel.add(boxRUF);
		myPanel.add(boxSelectAll);
		myPanel.add(boxSelectEight);
		myPanel.add(boxOptimize);
		myPanel.add(boxOPT2);
		myPanel.setBorder(new TitledBorder(new EtchedBorder(), ""));

		return myPanel;
	}

	// set all individual checked boxes.
	private void setAllIndividualBoxes(boolean selected) {
		boxPTT.setSelected(selected);
		boxPTF.setSelected(selected);
		boxCRC.setSelected(selected);
		boxCRE.setSelected(selected);
		boxRER.setSelected(selected);
		// boxANR.setSelected(selected); //temporarily not considered.
		boxRTT.setSelected(selected);
		boxRTF.setSelected(selected);
		boxRCT.setSelected(selected);
		boxRCF.setSelected(selected);
		boxFPR.setSelected(selected);
		boxFDR.setSelected(selected);
		// boxRTR.setSelected(selected); // Not implemented
		// boxFCF.setSelected(selected); // not applicable in our examples.
		boxANF.setSelected(selected);
		boxRNF.setSelected(selected);
		boxRPTE.setSelected(selected);
		// boxRPCE.setSelected(selected); // Not implemented
		boxSelectAll.setSelected(selected);
		boxSelectEight.setSelected(false);
	}

	// set Eight type checked boxes. (PTT, PTF, CRC, CRE, RTT, RTF, RCT, RCF)
	private void setEightBoxes(boolean selected) {

		setAllIndividualBoxes(false);

		boxPTT.setSelected(selected);
		boxPTF.setSelected(selected);
		boxCRC.setSelected(selected);
		boxCRE.setSelected(selected);
		boxRTT.setSelected(selected);
		boxRTF.setSelected(selected);
		boxRCT.setSelected(selected);
		boxRCF.setSelected(selected);

		boxSelectEight.setSelected(selected);
	}

	public void setUpMutantPanel() {
		removeAll();
		setLayout(new BorderLayout());
		try {
			String[] columnNames = { "No", "Mutant Name", "Mutant File",
					"Bug Position", "Test Result" };
			data = mutantSuite.getMutantData();
			System.out.println(data.size() + " data size");
			System.out.println(data.toString());
			tablePanel = new GeneralTablePanel(data, columnNames, 5);
			tablePanel.setMinRows(30);
			JScrollPane scrollpane = new JScrollPane(tablePanel);
			add(scrollpane, BorderLayout.CENTER);
			xpa.setToMutantPane();
			xpa.updateMainTabbedPane();
		} catch (Exception e) {

		}
	}

	public void openMutants() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(false);
		if (xpa.getWorkingPolicyFile() != null)
			fileChooser.setCurrentDirectory(xpa.getWorkingPolicyFile()
					.getParentFile());
		fileChooser.setFileFilter(new XMLFileFilter("xls"));
		fileChooser.setDialogTitle("Open Mutants");
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File mutantSuiteFile = fileChooser.getSelectedFile();
			if (!mutantSuiteFile.toString().endsWith(".xls")) {
				JOptionPane.showMessageDialog(xpa,
						"The open File is not a test suite *.xls",
						"Error of Selection", JOptionPane.WARNING_MESSAGE);
			} else {
				try {
					mutantSuite = new PolicySpreadSheetMutantSuite(
							mutantSuiteFile.getAbsolutePath(),
							xpa.getWorkingPolicyFilePath());
					setUpMutantPanel();
				} catch (Exception e) {
				}
			}
		}
	}

	public void generateMutants() {
		if (!xpa.hasWorkingPolicy()) {
			JOptionPane.showMessageDialog(xpa, "There is no policy.");
			return;
		}

		loadPolicy lp = new loadPolicy();
		Policy policy = lp.getPolicy(xpa.getWorkingPolicyFilePath());
		System.out.println(xpa.getWorkingPolicyFilePath());
		PolicyX policyx = new PolicyX(policy);
		policyx.initBalana(xpa);
		function f = new function();
		File out = new File(
				this.testPanel.getTestOutputDestination("_MutationTests"));
		if (!out.isDirectory() && !out.exists())
			out.mkdir();
		else
			f.deleteFile(out);
		List<Rule> rules = policyx.getRuleFromPolicy(policy);
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		String testPath = this.testPanel
				.getTestsuiteXLSfileName("_MutationTests");
		testPanel.setWorkingTestSuiteFileName(testPath);
		int result = JOptionPane.showConfirmDialog(xpa, createPanel(),
				"Please Select Mutation Methods", JOptionPane.OK_CANCEL_OPTION);
		String policyPath = xpa.getWorkingPolicyFilePath();
		
		int mlistIndex = 0;
		int tlistIndex = 0;

		ArrayList<PolicyMutant> testable = new ArrayList<PolicyMutant>();

		long totalOptimizationTime = 0;
		long totalGenerationTime = 0;

		boolean opt = boxOptimize.isSelected();
		if (result == JOptionPane.OK_OPTION) {
			try {
				PolicyMutator policyMutator = new PolicyMutator(
						xpa.getWorkingPolicyFilePath());
				if (boxPTT.isSelected()) {
					policyMutator.createPolicyTargetTrueMutants();
					// PolicySpreadSheetTestRecord PTT =
					// policyx.generate_PolicyTargetTrue(getTestPanel());
					// tests.add(PTT);
					PolicySpreadSheetTestRecord ptt = policyx
							.generate_PolicyTargetTrue(getTestPanel());
					if (ptt != null) {
						tests.add(ptt);
						String mutantPath = policyMutator
								.getMutantFileName("PTT1");
						String reqPath = ptt.getRequest();
						if (validation(policyPath, mutantPath, reqPath)) {
							mlistIndex++;
							tlistIndex++;
							valid.add(ptt);
							validTests++;
						}
					}
					getMutantsByType(policyMutator.getMutantList(), testable,
							"PTT");
				}
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
					getMutantsByType(policyMutator.getMutantList(), testable,
							"PTF");
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

				}
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
		}
	}

	private String getMutationTestingResultsFileName() {
		return new File(xpa.getWorkingTestSuiteFileName()).getParent()
				+ File.separator + "MutationTestingResults.xls";
	}

	public void testMutants() {
		if (mutantSuite == null) {
			JOptionPane.showMessageDialog(xpa, "There are no mutants.");
			return;
		}
		if (!xpa.hasTests()) {
			JOptionPane.showMessageDialog(xpa, "There are no tests.");
			return;
		}
		try {
			String outputFileName = getMutationTestingResultsFileName();
			// Time this.
			final long startTime = System.currentTimeMillis();

			mutantSuite.runAndWriteDetectionInfoToExcelFile(outputFileName,
					xpa.getWorkingTestSuite());

			final long endTime = System.currentTimeMillis();
			System.out.println("Mutants testing time: " + (endTime - startTime)
					/ 1000.00);

			mutantSuite.updateMutantTestResult(data);
			xpa.setToMutantPane();
			JOptionPane.showMessageDialog(xpa,
					"Mutation testing results are saved into file: \n"
							+ outputFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * public String getTestOutputDestination(String testMethod) {
	 * 
	 * File file = xpa.getWorkingPolicyFile(); String path =
	 * file.getParentFile().getAbsolutePath(); String name = file.getName();
	 * name = name.substring(0, name.length() - 4); path = path + File.separator
	 * + "test_suites" + File.separator + name + testMethod; return path; }
	 */

	public TestPanel getTestPanel() {
		return this.testPanel;
	}

	private boolean validation(String policyPath, String mutantPath,
			String request) {
		if(request == null || request.compareTo("") == 0 || request.compareTo(" ") == 0 || request.compareTo("\n") == 0 || request.compareTo("\r") == 0)
			return false;
		// System.out.println(request);
		loadPolicy lp = new loadPolicy();
		Policy original = lp.getPolicy(policyPath);
		Policy mutant = lp.getPolicy(mutantPath);
		// PolicyX policyx = new PolicyX(original);
		if (mutant != null) {
			int pres = lp.PolicyEvaluate(original, request);
			int mres = lp.PolicyEvaluate(mutant, request);
			// System.out.println("POLICY DECISION: " + pres +
			// "\nMUTANT DECISION: " + mres);
			if (pres == mres)
				return false;
			else
				return true;
		} else
			return false;
	}
	
	//check if 2 requests give the same decision for a mutant
	private boolean checkResult(String req1, String req2, String mutant)
	{
		String request1 = "";
		String request2 = "";
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(req1));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while(line != null)
			{
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			request1 = sb.toString();
			br.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(req2));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while(line != null)
			{
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			request2 = sb.toString();
			br.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		loadPolicy lp = new loadPolicy();
		Policy m = lp.getPolicy(mutant);
		if(m != null)
		{
			int res1 = lp.PolicyEvaluate(m, request1);
			int res2 = lp.PolicyEvaluate(m, request2);
			if(res1 == res2)
				return true;
			else
				return false;
		}
		else
			return false;
	}
	
	private void determineValidTests_RPTE(ArrayList<PolicySpreadSheetTestRecord> records, PolicyMutator mutator, String policyPath)
	{
		for(PolicySpreadSheetTestRecord record : records)
		{
			valid.add(record);
			validTests++;
		}
	}
	
	private void determineValidTests_CRC(ArrayList<PolicySpreadSheetTestRecord> records, PolicyMutator mutator, String policyPath, String type)
	{
		
	}

	private void determineValidTests(
			ArrayList<PolicySpreadSheetTestRecord> records,
			PolicyMutator mutator, String policyPath, String type) {
		if (records.size() == 0)
			return;
		if (records.size() == 1) {
			tests.add(records.get(0));
			String req = records.get(0).getRequest();
			int successes = 0;
			int matches = 0;
			for (PolicyMutant m : mutator.getMutantList()) {
				String muType = m.getNumber().substring(
						m.getNumber().indexOf(' ') + 1,
						m.getNumber().length() - 1);
				if (muType.compareTo(type) == 0) {
					matches++;
					String mutantPath = m.getMutantFilePath();
					successes += validation(policyPath, mutantPath, req) ? 1
							: 0;
				}
			}
			if (successes == matches) {
				valid.add(records.get(0));
				validTests++;
			}
			if (!valid.contains(records.get(0))) {
				System.err.println(req);
				Runtime run = Runtime.getRuntime();
				try {
					run.exec("rm " + req);
				} catch (Exception e) {
					System.err.println("File deletion failed");
				}
			}
		} else {
			for (PolicySpreadSheetTestRecord record : records) {
				tests.add(record);
				String req = record.getRequest();
				int mutantNum = records.indexOf(record) + 1;
				String mutantPath = mutator.getMutantFileName(type + mutantNum);
				if (validation(policyPath, mutantPath, req)) {
					valid.add(record);
					validTests++;
				} else {
					Runtime run = Runtime.getRuntime();
					try
					{
						run.exec("rm " + req);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					tests.remove(record);
				}
			}
		}
	}

	private void optimize(ArrayList<PolicySpreadSheetTestRecord> tests,
			PolicySpreadSheetTestRecord[] records,
			ArrayList<PolicyMutant> mutants, PolicyMutator mutator) {
		String policyPath = xpa.getWorkingPolicyFilePath();
		if (tests.size() == 1)
			return;
		for (int i = 0; i < records.length; i++) {
			if (records[i] == null)
				continue;

			PolicySpreadSheetTestRecord psstr = records[i];
			String req = testPanel.getTestOutputDestination("_MutationTests")
					+ File.separator + psstr.getRequestFile();
			String testNum = psstr.getNumber().substring(
					psstr.getNumber().indexOf(' ') + 1,
					psstr.getNumber().length());
			for (int j = 0; j < records.length; j++) {
				if (records[j] == null)
					continue;

				String mnum = records[j].getNumber().substring(
						records[j].getNumber().indexOf(' ') + 1,
						records[j].getNumber().length());
				String mutantPath = mutator.getMutantFileName(mnum);
				if (validation(policyPath, mutantPath, req)
						&& mnum.compareTo(testNum) == 0)
					continue;
				if (validation(policyPath, mutantPath, req)
						&& mnum.compareTo(testNum) != 0) {
					String rem = testPanel
							.getTestOutputDestination("_MutationTests")
							+ File.separator + records[j].getRequestFile();
					Runtime run = Runtime.getRuntime();
					try {
						run.exec("rm " + rem);
					} catch (Exception e) {
						System.err.println("Unable to delete associated file");
					}
					records[j] = null;
				} else
					continue;
			}
		}
		valid = new ArrayList<PolicySpreadSheetTestRecord>();
		for (int i = 0; i < records.length; i++)
			if (records[i] != null)
				valid.add(records[i]);
	}

	private void optimize2(ArrayList<PolicySpreadSheetTestRecord> records,
			PolicyMutator mutator) {
		// LinkedHashSet<PolicySpreadSheetTestRecord> record_set = new
		// LinkedHashSet<PolicySpreadSheetTestRecord>();
		// for(PolicySpreadSheetTestRecord record : records)
		// record_set.add(record);
		String policyPath = xpa.getWorkingPolicyFilePath();
		for (int i = 0; i < records.size(); i++) {
			PolicySpreadSheetTestRecord psstr = records.get(i);
			String req = testPanel.getTestOutputDestination("_MutationTests")
					+ File.separator + psstr.getRequestFile();
			String testNum = psstr.getNumber().substring(
					psstr.getNumber().indexOf(' ') + 1,
					psstr.getNumber().length());
			for (int j = 0; j < records.size(); j++) {
				PolicySpreadSheetTestRecord m = records.get(j);
				String mnum = m.getNumber().substring(
						m.getNumber().indexOf(' ') + 1, m.getNumber().length());
				String mutantPath = mutator.getMutantFileName(mnum);
				if (validation(policyPath, mutantPath, req)
						&& mnum.contains(testNum))
					continue;
				else if (validation(policyPath, mutantPath, req)
						&& !mnum.contains(testNum)) {
					String rem = testPanel
							.getTestOutputDestination("_MutationTests")
							+ File.separator + records.get(j).getRequestFile();
					Runtime run = Runtime.getRuntime();
					try {
						run.exec("rm " + rem);
					} catch (Exception e) {
						System.err.println("Unable to delete associated file");
					}
					records.remove(j);
				}
			}
		}
		valid = new ArrayList<PolicySpreadSheetTestRecord>();
		for (PolicySpreadSheetTestRecord record : records)
			valid.add(record);
	}

	private void optimize3(ArrayList<PolicySpreadSheetTestRecord> records,
			PolicyMutator mutator) {
		String policyPath = xpa.getWorkingPolicyFilePath();
		for (int i = 0; i < 14; i++)// do a round of
													// optimization for each
													// mutation type
		{
			for (int j = 0; j < records.size() - 1; j++) {
				PolicySpreadSheetTestRecord psstr = records.get(j);
				PolicySpreadSheetTestRecord next = records.get(j + 1);
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + psstr.getRequestFile();
				String testNum = psstr.getNumber().substring(
						psstr.getNumber().indexOf(' ') + 1,
						psstr.getNumber().length());
				String mnum = next.getNumber().substring(
						next.getNumber().indexOf(' ') + 1,
						next.getNumber().length());
				String mutantPath = mutator.getMutantFileName(mnum);
				if (validation(policyPath, mutantPath, req)) {
					String rem = testPanel
							.getTestOutputDestination("_MutationTests")
							+ File.separator + next.getRequestFile();
					Runtime run = Runtime.getRuntime();
					try {
						run.exec("rm" + rem);
					} catch (Exception e) {
						System.err.println("Unable to delete associated file");
					}
					records.remove(j);
				} else
					continue;
			}
		}
		valid = new ArrayList<PolicySpreadSheetTestRecord>();
		for (PolicySpreadSheetTestRecord record : records)
			valid.add(record);
	}

	private void getMutantsByType(ArrayList<PolicyMutant> mutants,
			ArrayList<PolicyMutant> sublist, String type) {
		for (PolicyMutant m : mutants) {
			if (m.getNumber().contains(type))
				sublist.add(m);
		}
	}

	private int getTestsByType(ArrayList<PolicySpreadSheetTestRecord> records,
			ArrayList<PolicySpreadSheetTestRecord> sublist, String type,
			int current) {
		while (records.get(current).getNumber().contains(type)
				&& current < records.size()) {
			sublist.add(records.get(current));
			current++;
		}
		return current;
	}

	private long onTheFlyOptimization(
			ArrayList<PolicySpreadSheetTestRecord> records,
			ArrayList<PolicyMutant> mutants, PolicyMutator mutator) {
		long tot = 0;
		PolicySpreadSheetTestRecord[] recs = new PolicySpreadSheetTestRecord[records
				.size()];
		String policyPath = xpa.getWorkingPolicyFilePath();
		for (int i = 0; i < records.size(); i++) {
			recs[i] = records.get(i);
		}
		long start = System.currentTimeMillis();
		for (PolicySpreadSheetTestRecord ptr : records) {
			String req = testPanel.getTestOutputDestination("_MutationTests")
					+ File.separator + ptr.getRequestFile();
			String testNum = ptr.getNumber().substring(
					ptr.getNumber().indexOf(' ') + 1, ptr.getNumber().length());
			for (int i = 0; i < mutants.size(); i++) {
				PolicyMutant m = mutants.get(i);
				String mutant = m.getNumber().substring(
						m.getNumber().indexOf(' ') + 1, m.getNumber().length());
				String mutantPath = mutator.getMutantFileName(mutant);
				if (validation(policyPath, mutantPath, req)
						&& m.getNumber().compareTo(testNum) != 0) {
					for (int k = 0; k < records.size(); k++) {
						PolicySpreadSheetTestRecord rec = records.get(k);
						;
						String recNum = rec.getNumber().substring(
								rec.getNumber().indexOf(' ') + 1,
								rec.getNumber().length());
						if (recNum.compareTo(mutant) == 0)
							recs[k] = null;
					}
				} else
					continue;
			}
		}
		tot += System.currentTimeMillis() - start;
		valid = new ArrayList<PolicySpreadSheetTestRecord>();
		for (int i = 0; i < recs.length; i++)
			if (recs[i] != null)
				valid.add(recs[i]);
		return tot;
	}

	private void optimize4(ArrayList<PolicySpreadSheetTestRecord> records,
			PolicyMutator mutator) {
		PolicySpreadSheetTestRecord[] input = new PolicySpreadSheetTestRecord[records
				.size()];
		for (int i = 0; i < input.length; i++)
			input[i] = records.get(i);
		mergeArray = new PolicySpreadSheetTestRecord[input.length];
		doOptimization(0, input.length - 1, input, mutator);
		valid = new ArrayList<PolicySpreadSheetTestRecord>();
		for (int i = 0; i < input.length; i++)
			if (input[i] != null)
				valid.add(mergeArray[i]);
	}

	private void doOptimization(int low, int high,
			PolicySpreadSheetTestRecord[] records, PolicyMutator mutator) {
		if (low < high) {
			int mid = low + (high - low) / 2;
			doOptimization(low, mid, records, mutator);
			doOptimization(mid + 1, high, records, mutator);
			merge_kill(low, mid, high, records, mutator);
		}
	}

	private void merge_kill(int low, int mid, int high,
			PolicySpreadSheetTestRecord[] records, PolicyMutator mutator) {
		for (int i = low; i <= high; i++)
			mergeArray[i] = records[i];

		String policy = xpa.getWorkingPolicyFilePath();
		int i = low;
		int j = high;
		int k = low;
		int l = high;
		while (j >= low && i <= high)// search from bottom up
		{
			if (i == j)// i and j point to same request/test combination
			{
				j--;
				i++;
				continue;
			}
			if (mergeArray[i] != null) {
				PolicySpreadSheetTestRecord psstr = mergeArray[i];
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + psstr.getRequestFile();
				String testNum = psstr.getNumber().substring(
						psstr.getNumber().indexOf(' ') + 1,
						psstr.getNumber().length());
				if (mergeArray[j] != null) {
					PolicySpreadSheetTestRecord m = mergeArray[j];
					String mnum = m.getNumber().substring(
							m.getNumber().indexOf(' ') + 1,
							m.getNumber().length());
					String mutant = mutator.getMutantFileName(mnum);
					if (validation(policy, mutant, req)
							&& mnum.compareTo(testNum) != 0) {
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + mutant;
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("rm" + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						mergeArray[j] = null;
						records[j] = null;
						j--;
					} else
						i++;
				} else
					j--;
			} else
				i++;
		}
		while (l >= low && k <= high)// search from top down
		{
			if (k == l)// k and l point to same test/mutant combo
			{
				k++;
				l--;
				continue;
			}
			if (mergeArray[l] != null) {
				PolicySpreadSheetTestRecord psstr = mergeArray[l];
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + psstr.getRequestFile();
				String testNum = psstr.getNumber().substring(
						psstr.getNumber().indexOf(' ') + 1,
						psstr.getNumber().length());
				if (mergeArray[k] != null) {
					PolicySpreadSheetTestRecord m = mergeArray[k];
					String mnum = m.getNumber().substring(
							m.getNumber().indexOf(' ') + 1,
							m.getNumber().length());
					String mutant = mutator.getMutantFileName(mnum);
					if (validation(policy, mutant, req)
							&& mnum.compareTo(testNum) != 0) {
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + mutant;
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("rm" + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						mergeArray[k] = null;
						records[k] = null;
						k++;
					} else
						l--;
				} else
					k++;
			} else
				l--;
		}
		/*
		 * i = low; j = i + 1; while(j <= high && i <= high)//semi-iterative
		 * bottom up { if(mergeArray[i] != null) { PolicySpreadSheetTestRecord
		 * psstr = mergeArray[i]; String req =
		 * testPanel.getTestOutputDestination("_MutationTests") + File.separator
		 * + psstr.getRequestFile(); String testNum =
		 * psstr.getNumber().substring(psstr.getNumber().indexOf(' ') + 1,
		 * psstr.getNumber().length()); if(mergeArray[j] != null) {
		 * PolicySpreadSheetTestRecord m = mergeArray[j]; String mnum =
		 * m.getNumber().substring(m.getNumber().indexOf(' ') + 1,
		 * m.getNumber().length()); String mutant =
		 * mutator.getMutantFileName(mnum); if(validation(policy, mutant, req)
		 * && mnum.compareTo(testNum) != 0) { String rem =
		 * testPanel.getTestOutputDestination("_MutationTests") + File.separator
		 * + mutant; Runtime run = Runtime.getRuntime(); try {
		 * run.exec("rm" + rem); } catch(Exception e) {
		 * e.printStackTrace(); } mergeArray[j] = null; records[j] = null; j++;
		 * } else { i = j; j = i + 1; continue; } } else j++; } else i++; }
		 * 
		 * l = high; k = l - 1; while(l >= low && k >= low)//semi-iterative top
		 * down { if(mergeArray[l] != null) { PolicySpreadSheetTestRecord psstr
		 * = mergeArray[l]; String req =
		 * testPanel.getTestOutputDestination("_MutationTests") + File.separator
		 * + psstr.getRequestFile(); String testNum =
		 * psstr.getNumber().substring(psstr.getNumber().indexOf(' ') + 1,
		 * psstr.getNumber().length()); if(mergeArray[k] != null) {
		 * PolicySpreadSheetTestRecord m = mergeArray[k]; String mnum =
		 * m.getNumber().substring(m.getNumber().indexOf(' ') + 1,
		 * m.getNumber().length()); String mutant =
		 * mutator.getMutantFileName(mnum); if(validation(policy, mutant, req)
		 * && mnum.compareTo(testNum) != 0) { String rem =
		 * testPanel.getTestOutputDestination("_MutationTests") + File.separator
		 * + mutant; Runtime run = Runtime.getRuntime(); try {
		 * run.exec("rm" + rem); } catch(Exception e) {
		 * e.printStackTrace(); } mergeArray[k] = null; records[k] = null; k--;
		 * } else { l = k; k = l - 1; continue; } } else k--; } else l--; }
		 */
		// Total for merge_kill: 2n iterations
		// Overall: 2nlogn or O(nlogn)
	}

	private void optimize5(ArrayList<PolicySpreadSheetTestRecord> records,
			PolicyMutator mutator) {
		PolicySpreadSheetTestRecord[] test = new PolicySpreadSheetTestRecord[records
				.size()];
		for (int i = 0; i < records.size(); i++)
			test[i] = records.get(i);
		String policy = xpa.getWorkingPolicyFilePath();
		int i = 0;
		int j = test.length - 1;
		int k = 0;
		int l = test.length - 1;

		while (i < test.length && j >= 0) {
			if (i == j) {
				i++;
				j--;
				continue;
			}
			if (test[i] != null) {
				PolicySpreadSheetTestRecord psstr = test[i];
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + psstr.getRequestFile();
				String testNum = psstr.getNumber().substring(
						psstr.getNumber().indexOf(' ') + 1,
						psstr.getNumber().length());
				if (test[j] != null) {
					PolicySpreadSheetTestRecord m = test[j];
					String mnum = m.getNumber().substring(
							m.getNumber().indexOf(' ') + 1,
							m.getNumber().length());
					String mutant = mutator.getMutantFileName(mnum);
					String req2 = testPanel.getTestOutputDestination("_MutationTests")
							+ File.separator + m.getRequestFile();
					if(m.getNumber().compareTo(psstr.getNumber()) == 0 && mnum.compareTo(testNum) != 0)
					{
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + m.getRequestFile();
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("rm " + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[j] = null;
						j--;
					}
					else if (validation(policy, mutant, req)
							&& mnum.compareTo(testNum) != 0) {
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + m.getRequestFile();
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("rm " + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[j] = null;
						j--;
					} 
					/*else if(checkResult(req, req2, mutant) && mnum.compareTo(testNum) != 0)
					{
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + m.getRequestFile();
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("rm " + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[j] = null;
						j--;
					}*/
					else
						i++;
				} else
					j--;
			} else
				i++;
		}

		while (l >= 0 && k < test.length) {
			if (k == l) {
				k++;
				l--;
				continue;
			}
			if (test[l] != null) {
				PolicySpreadSheetTestRecord psstr = test[l];
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + psstr.getRequestFile();
				String testNum = psstr.getNumber().substring(
						psstr.getNumber().indexOf(' ') + 1,
						psstr.getNumber().length());
				if (test[k] != null) {
					PolicySpreadSheetTestRecord m = test[k];
					String mnum = m.getNumber().substring(
							m.getNumber().indexOf(' ') + 1,
							m.getNumber().length());
					String mutant = mutator.getMutantFileName(mnum);
					String req2 = testPanel.getTestOutputDestination("_MutationTests")
							+ File.separator + m.getRequestFile();
					if(m.getNumber().compareTo(psstr.getNumber()) == 0 && mnum.compareTo(testNum) != 0)
					{
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + m.getRequestFile();
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("rm " + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[k] = null;
						k++;
						continue;
					}
					if (validation(policy, mutant, req)
							&& mnum.compareTo(testNum) != 0) {
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + m.getRequestFile();
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("rm " + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[k] = null;
						k++;
						continue;
					} 
					/*else if(checkResult(req, req2, mutant) && mnum.compareTo(testNum) != 0)
					{
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + m.getRequestFile();
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("rm " + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[k] = null;
						k++;
					}*/
					else
						l--;
				} else
					k++;
			} else
				l--;
		}

		i = 0;
		j = i + 1;
		while (j < test.length && i < test.length)// semi-iterative bottom up
		{
			if (test[i] != null) {
				PolicySpreadSheetTestRecord psstr = test[i];
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + psstr.getRequestFile();
				String testNum = psstr.getNumber().substring(
						psstr.getNumber().indexOf(' ') + 1,
						psstr.getNumber().length());
				if (test[j] != null) {
					PolicySpreadSheetTestRecord m = test[j];
					String mnum = m.getNumber().substring(
							m.getNumber().indexOf(' ') + 1,
							m.getNumber().length());
					String mutant = mutator.getMutantFileName(mnum);
					String req2 = testPanel.getTestOutputDestination("_MutationTests")
							+ File.separator + m.getRequestFile();
					if(m.getNumber().compareTo(psstr.getNumber()) == 0 && mnum.compareTo(testNum) != 0)
					{
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + m.getRequestFile();
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("rm " + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[j] = null;
						j++;
						continue;
					}
					if (validation(policy, mutant, req)
							&& mnum.compareTo(testNum) != 0) {
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + m.getRequestFile();
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("rm " + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[j] = null;
						j++;
						continue;
					} 
					/*else if(checkResult(req, req2, mutant) && mnum.compareTo(testNum) != 0)
					{
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + m.getRequestFile();
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("rm " + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[j] = null;
						j++;
					}*/
					else {
						i = j;
						j = i + 1;
						continue;
					}
				} else
					j++;
			} else
				i++;
		}

		l = test.length - 1;
		k = l - 1;
		while (l >= 0 && k >= 0)// semi-iterative top down
		{
			if (test[l] != null) {
				PolicySpreadSheetTestRecord psstr = test[l];
				String req = testPanel
						.getTestOutputDestination("_MutationTests")
						+ File.separator + psstr.getRequestFile();
				String testNum = psstr.getNumber().substring(
						psstr.getNumber().indexOf(' ') + 1,
						psstr.getNumber().length());
				if (test[k] != null) {
					PolicySpreadSheetTestRecord m = test[k];
					String mnum = m.getNumber().substring(
							m.getNumber().indexOf(' ') + 1,
							m.getNumber().length());
					String mutant = mutator.getMutantFileName(mnum);
					String req2 = testPanel.getTestOutputDestination("_MutationTests")
							+ File.separator + m.getRequestFile();
					if(m.getNumber().compareTo(psstr.getNumber()) == 0 && mnum.compareTo(testNum) != 0)
					{
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + m.getRequestFile();
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("rm " + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[k] = null;
						k--;
						continue;
					}
					if (validation(policy, mutant, req)
							&& mnum.compareTo(testNum) != 0) {
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + m.getRequestFile();
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("rm " + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[k] = null;
						k--;
						continue;
					}
					/*else if(checkResult(req, req2, mutant) && mnum.compareTo(testNum) != 0)
					{
						String rem = testPanel
								.getTestOutputDestination("_MutationTests")
								+ File.separator + m.getRequestFile();
						Runtime run = Runtime.getRuntime();
						try {
							run.exec("rm " + rem);
						} catch (Exception e) {
							e.printStackTrace();
						}
						test[k] = null;
						k--;
					}*/
					else {
						l = k;
						k = l - 1;
						continue;
					}
				} else
					k--;
			} else
				l--;
		}
		records = new ArrayList<PolicySpreadSheetTestRecord>();
		for(int x = 0; i < test.length; i++)
			if(test[x] != null)
				records.add(test[x]);
	}

	private void removeDuplicates(ArrayList<PolicySpreadSheetTestRecord> records, PolicyMutator mutator) 
	{
		Runtime run = Runtime.getRuntime();
		loadPolicy lp = new loadPolicy();
		Policy p = lp.getPolicy(xpa.getWorkingPolicyFilePath());
		for(int i = 0; i < records.size(); i++)
		{
			for(int j = i + 1; j < records.size(); j++)
			{
				if(records.get(i).getRequest().compareTo(records.get(j).getRequest()) == 0)
				{
					boolean remove = true;
					if(p.getCombiningAlg() instanceof DenyUnlessPermitRuleAlg || p.getCombiningAlg() instanceof PermitUnlessDenyRuleAlg)
					{
						String req = records.get(i).getRequest();
						String num = records.get(j).getNumber().substring(records.get(j).getNumber().indexOf(' ') + 1);
						String mutant = mutator.getMutantFileName(num);
						remove = validation(xpa.getWorkingPolicyFilePath(), mutant, req);
					}
					if(remove)
					{
						String rem = testPanel.getTestOutputDestination("_MutationTests")
								+ File.separator + records.get(j).getRequestFile();
						try
						{
							run.exec("rm " + rem);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						records.remove(j);
					}
				}
			}
		}
	}

	private LinkedHashSet<String> findDuplicateRequests(
			ArrayList<String> requests) {
		LinkedHashSet<String> duplicates = new LinkedHashSet<String>();

		boolean first = true;
		for (String s : requests) {
			if (!duplicates.contains(s)) {
				duplicates.add(s);
			}
		}
		return duplicates;
	}
	
	//assumes the smaller list is already optimized
	private void slist_part(ArrayList<PolicySpreadSheetTestRecord> small, ArrayList<PolicySpreadSheetTestRecord> big, PolicyMutator mutator)
	{
		 int add = big.size() % small.size();
		 int parts = big.size() / small.size();
		 int cpart = 0;
		 int start = 0;
		 int end = start + small.size();
		 PolicySpreadSheetTestRecord[] big_list = new PolicySpreadSheetTestRecord[big.size()];
		 for(int i = 0; i < big.size(); i++)
			 big_list[i] = big.get(i);
		 while(cpart < parts)
		 {
			 if(cpart == parts - 1)//if we're on the last partition
			 {
				start = end;
				end = small.size() + add; //if big.size() % small.size() != 0, add the remainder to the end index to make it even
										  //otherwise, we just add 0
				for(int i = 0; i < small.size(); i++)
				{
					PolicySpreadSheetTestRecord record = small.get(i);
					String req = testPanel.getTestOutputDestination("_MutationTests")
							+ File.separator + record.getRequestFile();
					for(int j = start; j < end; j++)
					{
						if(big_list[j] == null)
							continue;
						else
						{
							PolicySpreadSheetTestRecord mutant = big_list[j];
							String mnum = mutant.getNumber().substring(mutant.getNumber().indexOf(' ') + 1);
							String mpath = mutator.getMutantFileName(mnum);
							if(validation(xpa.getWorkingPolicyFilePath(), mpath, req))
							{
								Runtime run = Runtime.getRuntime();
								String rem = testPanel.getTestOutputDestination("_MutationTests")
										+ File.separator + mutant.getRequestFile();
								try
								{
									run.exec("rm " + rem);
								}
								catch(Exception e)
								{
									System.err.println("Could not delete file");
								}
								big_list[j] = null;
							}
						}
					}
				}
			 }
			 else
			 {
				 for(int i = 0; i < small.size(); i++)
				 {
					 PolicySpreadSheetTestRecord record = small.get(i);
					 String req = testPanel.getTestOutputDestination("_MutationTests")
							 + File.separator + record.getRequestFile();
					 for(int j = start; j < end; j++)
					 {
						 if(big_list[j] == null)
							 continue;
						 else
						 {
							 PolicySpreadSheetTestRecord mutant = big_list[j];
							 String mnum = mutant.getNumber().substring(mutant.getNumber().indexOf(' ') + 1);
							 String mpath = mutator.getMutantFileName(mnum);
							 if(validation(xpa.getWorkingPolicyFilePath(), mpath, req))
							 {
								 Runtime run = Runtime.getRuntime();
								 String rem = testPanel.getTestOutputDestination("_MutationTests")
										 + File.separator + mutant.getRequestFile();
								 try
								 {
									 run.exec("rm " + rem);
								 }
								 catch(Exception e)
								 {
									 System.err.println("Could not delete file");
								 }
								 big_list[j] = null;
							 }
						 }
					 }
				 }
				 start = end;
				 end = start + small.size();
			 }
			 cpart++;
		 }
		 valid = new ArrayList<PolicySpreadSheetTestRecord>();
		 for(PolicySpreadSheetTestRecord P : small)
			 valid.add(P);
		 for(PolicySpreadSheetTestRecord R : big_list)
			 if(R != null)
				 valid.add(R);
	}
	
	//list rotates/pivots around some point so that each test is tested against each mutant in a linear fashion
	//this pivot point is pseudo-random
	private void cross_pivot(ArrayList<PolicySpreadSheetTestRecord> records, PolicyMutator mutator)
	{
		PolicySpreadSheetTestRecord[] recs = new PolicySpreadSheetTestRecord[records.size()];
		for(int i = 0; i < recs.length; i++)
			recs[i] = records.get(i);
		int i = 0, j = recs.length - 1;
		while(i < recs.length && j >= 0)
		{
			if(recs[i] == null)
			{
				i++;
				continue;
			}
			if(recs[j] == null)
			{
				j--;
				continue;
			}
			if(j == i)
			{
				j--;
				i++;
				continue;
			}
			PolicySpreadSheetTestRecord rec1 = recs[i];
			PolicySpreadSheetTestRecord rec2 = recs[j];
			String req1 = testPanel.getTestOutputDestination("_MutationTests")
					+ File.separator + rec1.getRequestFile();
			String req2 = testPanel.getTestOutputDestination("_MutationTests")
					+ File.separator + rec2.getRequestFile();
			String mnum1 = rec1.getNumber().substring(rec1.getNumber().indexOf(' ') + 1);
			String mnum2 = rec2.getNumber().substring(rec2.getNumber().indexOf(' ') + 1);
			String mutant1 = mutator.getMutantFileName(mnum1);
			String mutant2 = mutator.getMutantFileName(mnum2);
			if(validation(xpa.getWorkingPolicyFilePath(), mutant2, req1) || rec1.getRequest().compareTo(rec2.getRequest()) == 0)
			{
				Runtime run = Runtime.getRuntime();
				try
				{
					run.exec("rm " + rec2);
				}
				catch(Exception e)
				{
					System.err.println("Unable to delete file");
				}
				recs[j] = null;
			}
			if(validation(xpa.getWorkingPolicyFilePath(), mutant1, req2) || rec2.getRequest().compareTo(rec1.getRequest()) == 0)
			{
				Runtime run = Runtime.getRuntime();
				try
				{
					run.exec("rm " + rec1);
				}
				catch(Exception e)
				{
					System.err.println("Unable to delete file");
				}
			}
			i++;
			j--;
		}
	}
	
	private void bigvsmall(ArrayList<PolicySpreadSheetTestRecord> big, ArrayList<PolicySpreadSheetTestRecord> small, PolicyMutator mutator)
	{
		for(int i = 0; i < big.size(); i++)
		{
			PolicySpreadSheetTestRecord record = big.get(i);
			String req = testPanel.getTestOutputDestination("_MutationTests")
					+ File.separator + record.getRequestFile();
			for(int j = 0; j < small.size(); j++)
			{
				PolicySpreadSheetTestRecord rec = small.get(j);
				String num = rec.getNumber().substring(rec.getNumber().indexOf(' ') + 1);
				String mutant = mutator.getMutantFileName(num);
				if(validation(xpa.getWorkingPolicyFilePath(), mutant, req))
				{
					String rem = testPanel.getTestOutputDestination("_MutationTests")
							+ File.separator + rec.getRequestFile();
					Runtime run = Runtime.getRuntime();
					try
					{
						run.exec("rm " + rem);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					small.remove(j);
				}
			}
		}
		for(PolicySpreadSheetTestRecord r : big)
			small.add(r);
	}
	
	private void jalb(ArrayList<PolicySpreadSheetTestRecord> records, PolicyMutator mutator)
	{
		PolicySpreadSheetTestRecord[] recs = new PolicySpreadSheetTestRecord[records.size()];
		for(int i = 0; i < recs.length; i++)
			recs[i] = records.get(i);
		int i = 0, j = i + 1;
		while(i < recs.length && j < recs.length)
		{
			if(recs[i] != null)
			{
				PolicySpreadSheetTestRecord rec = recs[i];
				String req = testPanel.getTestOutputDestination("_MutationTests")
						+ File.separator + rec.getRequestFile();
				if(recs[j] != null)
				{
					PolicySpreadSheetTestRecord m = recs[j];
					String mnum = m.getNumber().substring(m.getNumber().indexOf(' ') + 1, m.getNumber().length());
					String mutant = mutator.getMutantFileName(mnum);
					if(validation(xpa.getWorkingPolicyFilePath(), mutant, req))
					{
						String rem = testPanel.getTestOutputDestination("_MutationTests")
								+ File.separator + m.getRequestFile();
						Runtime run = Runtime.getRuntime();
						try
						{
							run.exec("rm " + rem);
						}
						catch(Exception e)
						{
							System.err.println("Unable to delete file");
						}
						recs[j] = null;
						j++;
						continue;
					}
					else
					{
						PolicySpreadSheetTestRecord rec2 = recs[j];
						String req2 = testPanel.getTestOutputDestination("_MutationTests")
								+ File.separator + rec2.getRequestFile();
						while(i < j && recs[i] != null)
						{
							PolicySpreadSheetTestRecord m2 = recs[i];
							String mnum2 = m2.getNumber().substring(m.getNumber().indexOf(' ') + 1);
							String mutant2 = mutator.getMutantFileName(mnum2);
							if(validation(xpa.getWorkingPolicyFilePath(), mutant2, req2))
							{
								String rem2 = testPanel.getTestOutputDestination("_MutationTests")
										+ File.separator + m2.getRequestFile();
								Runtime run = Runtime.getRuntime();
								try
								{
									run.exec("rm " + rem2);
								}
								catch(Exception e)
								{
									System.err.println("Unable to delete file");
								}
								recs[i] = null;
							}
							i++;
						}
						i = j;
						j = i + 1;
						continue;
					}
				}
				else
				{
					j++;
					continue;
				}
			}
			else
			{
				i++;
				continue;
			}
		}
		records = new ArrayList<PolicySpreadSheetTestRecord>();
		for(PolicySpreadSheetTestRecord P : recs)
			if(P != null)
				records.add(P);
	}
	
	private int smallest_divisor(int list_size)
	{
		if(list_size % 2 == 0)
			return 2;
		else
		{
			int stop = (int)Math.sqrt((double)list_size);
			int i = 3;
			for(; i * i < stop && list_size % i != 0; i += 2);
			if(list_size % i == 0)
				return i;
		}
		return 1;
	}
	
	private ArrayList<PolicySpreadSheetTestRecord> getRecordSublist(ArrayList<PolicySpreadSheetTestRecord> records, String type)
	{
		ArrayList<PolicySpreadSheetTestRecord> sublist = new ArrayList<PolicySpreadSheetTestRecord>();
		for(PolicySpreadSheetTestRecord r : records)
			if(r.getNumber().contains(type))
				sublist.add(r);
		return sublist;
	}
	
	private ArrayList<PolicySpreadSheetTestRecord> getFalseTrue(ArrayList<PolicySpreadSheetTestRecord> records)
	{
		ArrayList<PolicySpreadSheetTestRecord> sublist = new ArrayList<PolicySpreadSheetTestRecord>();
		for(PolicySpreadSheetTestRecord R : records)
			if(R.getNumber().contains("RTT") || R.getNumber().contains("RPTE"))
				sublist.add(R);
		return sublist;
	}
	
	private ArrayList<PolicySpreadSheetTestRecord> getTrueFalse(ArrayList<PolicySpreadSheetTestRecord> records)
	{
		ArrayList<PolicySpreadSheetTestRecord> sublist = new ArrayList<PolicySpreadSheetTestRecord>();
		for(PolicySpreadSheetTestRecord R : records)
			if(R.getNumber().contains("RCT") || R.getNumber().contains("ANF"))
				sublist.add(R);
		return sublist;
	}
	
	private void CRCvsCRE(ArrayList<PolicySpreadSheetTestRecord> records, ArrayList<PolicySpreadSheetTestRecord> crc_sublist, PolicyMutator mutator)
	{
		if(crc_sublist.size() == 0)
			return;
		if(getRecordSublist(records, "CRE").size() == 0)
			return;
		ArrayList<PolicySpreadSheetTestRecord> cre_sublist = getRecordSublist(records, "CRE");
		if(cre_sublist.size() <= 5)//small list
		{
			for(int i = 0; i < crc_sublist.size(); i++)
			{
				PolicySpreadSheetTestRecord record = crc_sublist.get(i);
				String req = record.getRequest();
				for(int j = 0; j < cre_sublist.size(); j++)
				{
					String num = cre_sublist.get(j).getNumber().substring(cre_sublist.get(j).getNumber().indexOf(' '));
					String mpath = mutator.getMutantFileName(num);
					if(validation(xpa.getWorkingPolicyFilePath(), mpath, req))//if request kills all mutants
					{
						String rem = cre_sublist.get(j).getRequestFile();
						Runtime run = Runtime.getRuntime();
						try
						{
							run.exec("rm " + rem);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
						PolicySpreadSheetTestRecord rr = cre_sublist.get(j);
						cre_sublist.remove(j);
						records.remove(rr);
					}
				}
			}
			return;
		}
		for(int i = 0; i < crc_sublist.size() && cre_sublist.size() > 0; i++)
		{
			PolicySpreadSheetTestRecord record = crc_sublist.get(i);
			String req = record.getRequest();
			for(int j = 0; j < cre_sublist.size(); j++)
			{
				PolicySpreadSheetTestRecord mutant = cre_sublist.get(j);
				String num = mutant.getNumber().substring(mutant.getNumber().indexOf(' ') + 1);
				String mpath = mutator.getMutantFileName(num);
				if(validation(xpa.getWorkingPolicyFilePath(), mpath, req))
				{
					String rem = testPanel.getTestOutputDestination("_MutationTests")
							+ File.separator + mutant.getRequestFile();
					Runtime run = Runtime.getRuntime();
					try
					{
						run.exec("rm " + rem);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					cre_sublist.remove(j);
					records.remove(mutant);
				}
			}
		}
	}
	
	private void CRCvsCRC(ArrayList<PolicySpreadSheetTestRecord> records, ArrayList<PolicySpreadSheetTestRecord> crc, PolicyMutator mutator)
	{
		if(crc.size() == 0)
			return;
		if(records.size() == crc.size() + 1)//if the total number of records equals the number of CRC tests + 1, test suite is optimal
			return;
		for(int i = 0; i < crc.size(); i++)
		{
			PolicySpreadSheetTestRecord record = crc.get(i);
			for(int j = i + 1; j < crc.size(); j++)
			{
				PolicySpreadSheetTestRecord mutant = crc.get(j);
				String num = mutant.getNumber().substring(mutant.getNumber().indexOf(' ') + 1);
				String mpath = mutator.getMutantFileName(num);
				if(record.getRequest().compareTo(mutant.getRequest()) == 0)
				{
					String rem = testPanel.getTestOutputDestination("_MutationTests")
							+ File.separator + mutant.getRequestFile();
					Runtime run = Runtime.getRuntime();
					try
					{
						run.exec("rm " + rem);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					crc.remove(j);
					records.remove(mutant);
				}
				else if(validation(xpa.getWorkingPolicyFilePath(), mpath, record.getRequest()))
				{
					String rem = testPanel.getTestOutputDestination("_MutationTests")
							+ File.separator + mutant.getRequestFile();
					Runtime run = Runtime.getRuntime();
					try
					{
						run.exec("rm " + rem);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					crc.remove(j);
					records.remove(mutant);
				}
			}
		}
	}
	
	private void RPTEvsRPTE(ArrayList<PolicySpreadSheetTestRecord> records, ArrayList<PolicySpreadSheetTestRecord> rpte, PolicyMutator mutator)
	{
		if(rpte.size() == 0)
			return;
		for(int i = 0; i < rpte.size(); i++)
		{
			PolicySpreadSheetTestRecord record = rpte.get(i);
			for(int j = i + 1; j < rpte.size(); j++)
			{
				PolicySpreadSheetTestRecord mutant = rpte.get(j);
				String num = mutant.getNumber().substring(mutant.getNumber().indexOf(' ') + 1);
				String mpath = mutator.getMutantFileName(num);
				if(validation(xpa.getWorkingPolicyFilePath(), mpath, record.getRequest()))
				{
					String rem = testPanel.getTestOutputDestination("_MutationTests")
							+ File.separator + mutant.getRequestFile();
					Runtime run = Runtime.getRuntime();
					try
					{
						run.exec("rm " + rem);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					rpte.remove(j);
					records.remove(mutant);
				}
			}
		}
	}
	
	private void RTTvsRPTE(ArrayList<PolicySpreadSheetTestRecord> records, ArrayList<PolicySpreadSheetTestRecord> rpte, PolicyMutator mutator)
	{
		ArrayList<PolicySpreadSheetTestRecord> rtt = getRecordSublist(records, "RTT");
		if(rtt.size() == 0 || rpte.size() == 0)
			return;
		for(int i = 0; i < rtt.size() && rpte.size() > 0; i++)
		{
			PolicySpreadSheetTestRecord record = rtt.get(i);
			for(int j = 0; j < rpte.size(); j++)
			{
				PolicySpreadSheetTestRecord mutant = rpte.get(j);
				String num = mutant.getNumber().substring(mutant.getNumber().indexOf(' ') + 1);
				String mpath = mutator.getMutantFileName(num);
				if(validation(xpa.getWorkingPolicyFilePath(), mpath, record.getRequest()))
				{
					String rem = testPanel.getTestOutputDestination("_MutationTests")
							+ File.separator + mutant.getRequestFile();
					Runtime run = Runtime.getRuntime();
					try
					{
						run.exec("rm " + rem);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					rpte.remove(j);
					records.remove(mutant);
				}
			}
		}
	}
	
	private void RPTEvsRTT(ArrayList<PolicySpreadSheetTestRecord> records, ArrayList<PolicySpreadSheetTestRecord> rpte, PolicyMutator mutator)
	{
		if(rpte.size() == 0)
			return;
		if(getRecordSublist(records, "RTT").size() == 0)
			return;
		ArrayList<PolicySpreadSheetTestRecord> rtt = getRecordSublist(records, "RTT");
		for(int i = 0; i < rpte.size() && rtt.size() > 0; i++)
		{
			PolicySpreadSheetTestRecord record = rpte.get(i);
			String req = record.getRequest();
			for(int j = 0; j < rtt.size(); j++)
			{
				PolicySpreadSheetTestRecord r = rtt.get(j);
				String num = r.getNumber().substring(r.getNumber().indexOf(' ') + 1);
				String mutant = mutator.getMutantFileName(num);
				if(validation(xpa.getWorkingPolicyFilePath(), mutant, req))
				{
					String rem = testPanel.getTestOutputDestination("_MutationTests")
							+ File.separator + r.getRequestFile();
					Runtime run = Runtime.getRuntime();
					try
					{
						run.exec("rm " + rem);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					records.remove(r);
					rtt.remove(j);
				}
			}
		}
	}
	
	private void CRCvsFT(ArrayList<PolicySpreadSheetTestRecord> records, ArrayList<PolicySpreadSheetTestRecord> crc, PolicyMutator mutator)
	{
		ArrayList<PolicySpreadSheetTestRecord> ft = getFalseTrue(records);
		Runtime run = Runtime.getRuntime();
		if(ft.size() == 0 || crc.size() == 0)
			return;
		for(int i = 0; i < crc.size() && ft.size() > 0; i++)
		{
			PolicySpreadSheetTestRecord record = crc.get(i);
			for(int j = 0; j < ft.size(); j++)
			{
				PolicySpreadSheetTestRecord mutant = ft.get(j);
				String num = mutant.getNumber().substring(mutant.getNumber().indexOf(' ') + 1);
				String mpath = mutator.getMutantFileName(num);
				if(validation(xpa.getWorkingPolicyFilePath(), mpath, record.getRequest()))
				{
					try
					{
						run.exec("rm " + testPanel.getTestOutputDestination("_MutationTests") + File.separator + mutant.getRequestFile()); 
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					records.remove(mutant);
					ft.remove(j);
				}
			}
		}
	}
	
	private void CRCvsTF(ArrayList<PolicySpreadSheetTestRecord> records, ArrayList<PolicySpreadSheetTestRecord> crc, PolicyMutator mutator)
	{
		ArrayList<PolicySpreadSheetTestRecord> tf = getTrueFalse(records);
		Runtime run = Runtime.getRuntime();
		if(tf.size() == 0 || crc.size() == 0)
			return;
		for(int i = 0; i < crc.size() && tf.size() > 0; i++)
		{
			PolicySpreadSheetTestRecord record = crc.get(i);
			for(int j = 0; j < tf.size(); j++)
			{
				PolicySpreadSheetTestRecord mutant = tf.get(j);
				String num = mutant.getNumber().substring(mutant.getNumber().indexOf(' ') + 1);
				String mpath = mutator.getMutantFileName(num);
				if(validation(xpa.getWorkingPolicyFilePath(), mpath, record.getRequest()))
				{
					try
					{
						run.exec("rm " + testPanel.getTestOutputDestination("_MutationTests") + File.separator + mutant.getRequestFile()); 
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					records.remove(mutant);
					tf.remove(j);
				}
			}
		}
	}
	
	private ArrayList<PolicyMutant> getKills(PolicySpreadSheetTestRecord record, PolicyMutator mutator)
	{
		ArrayList<PolicyMutant> killed_mutants = new ArrayList<PolicyMutant>();
		String req = record.getRequest();
		for(PolicyMutant M : mutator.getMutantList())
		{
			String mutant = mutator.getMutantSpreedSheetFolderName() + File.separator + M.getMutantFilePath();
			if(validation(xpa.getWorkingPolicyFilePath(), mutant, req))
				killed_mutants.add(M);
		}
		return killed_mutants;
	}
	
	private boolean compareKills(ArrayList<PolicyMutant> m1, ArrayList<PolicyMutant> m2)
	{
		for(PolicyMutant out : m1)
		{
			for(PolicyMutant in : m2)
				if(out.getNumber().compareTo(in.getNumber()) != 0)
					return false;
		}
		return true;
	}
	
	private int getPolicyEffect(Policy p)
	{
		if(p.getCombiningAlg() instanceof DenyUnlessPermitRuleAlg || p.getCombiningAlg() instanceof PermitOverridesRuleAlg)
			return 0;
		else if(p.getCombiningAlg() instanceof PermitUnlessDenyRuleAlg || p.getCombiningAlg() instanceof DenyOverridesRuleAlg)
			return 1;
		else
		{
			PolicyX temp = new PolicyX(p);
			return temp.getRuleFromPolicy(p).get(temp.getRuleFromPolicy(p).size() - 1).getEffect();
		}
	}
}
