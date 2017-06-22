package org.seal.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.seal.combiningalgorithms.LoadPolicyDemo;
import org.seal.combiningalgorithms.PolicyXDemo;
import org.seal.policyUtils.PolicyLoader;
import org.seal.semanticCoverage.TestSuite;
import org.seal.testGeneration.Demo;
import org.seal.xacml.NameDirectory;
import org.seal.xacml.RequestGeneratorBase;
import org.seal.xacml.TaggedRequest;
import org.seal.xacml.TestRecord;
import org.seal.xacml.TestSuiteDemo;
import org.seal.xacml.coverage.RuleCoverage;
import org.seal.xacml.mutation.MutationBasedTestGenerator;
import org.seal.xacml.utils.ExceptionUtil;
import org.seal.xacml.xpa.utils.TestUtil;
import org.umu.editor.XMLFileFilter;
import org.wso2.balana.AbstractPolicy;

public class TestPanelDemo extends JPanel {
	private Demo demo;

	private String workingTestSuiteFileName;
	private TestSuiteDemo testSuite;

	private Vector<Vector<Object>> data;
	private TestTablePanelDemo requestTablePanel;
	private JPanel requestPanel;

	private boolean hasFailure;
	
	public TestPanelDemo(Demo demo) {
		this.demo = demo;
	}

	public Demo getDemo() {
		return this.demo;
	}
	

	public TestSuiteDemo getPolicySpreadSheetTestSuite() {
		return testSuite;
	}
	
	public void setTestSuite(TestSuiteDemo p)
	{
		this.testSuite = p;
	}
	
	public void setWorkingTestSuiteFileName(String filename)
	{
		this.workingTestSuiteFileName = filename;
	}

	public void setUpTestPanel() {
		removeAll();
		setLayout(new BorderLayout());
		String[] columnNames = { "No", "Name", "Request File","Expected Response", "Actual Response", "Verdict" };
		data = TestUtil.getTestRecordsVector(testSuite.getTestRecords());
		
		if (data.size() == 0) {
			JOptionPane.showMessageDialog(demo, "There is no test!");
			return;
		}
		Vector<Object> selected = data.get(0);
		String request = selected.get(6).toString();
		requestPanel = new JPanel();
		requestPanel.setLayout(new BorderLayout());
		GeneralTablePanelDemo gt = RequestTableDemo.getRequestTable(request, false);
		gt.setMinRows(5);
		RequestTableDemo.setPreferredColumnWidths(gt, this.getSize().getWidth());
		requestPanel.add(gt, BorderLayout.CENTER);

		requestTablePanel = new TestTablePanelDemo(data, columnNames, 5,
				requestPanel);
		JScrollPane scrollpane = new JScrollPane(requestTablePanel);
		JSplitPane jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		jSplitPane.setTopComponent(scrollpane);
		jSplitPane.setBottomComponent(requestPanel);
		jSplitPane.setResizeWeight(0.7);

		add(jSplitPane, BorderLayout.CENTER);
		demo.setToTestPane();
		demo.updateMainTabbedPane();
	}

	public void openTests() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(false);
		File workingPolicy = demo.getWorkingPolicyFile();
		if ( workingPolicy!= null){
			fileChooser.setCurrentDirectory(demo.getWorkingPolicyFile().getParentFile());
		}
		fileChooser.setFileFilter(new XMLFileFilter("xls"));
		fileChooser.setDialogTitle("Open Test Suite");
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File testSuiteFile = fileChooser.getSelectedFile();
			if (!testSuiteFile.toString().endsWith(".xls")) {
				JOptionPane.showMessageDialog(demo,
						"The open File is not a test suite *.xls",
						"Error of Selection", JOptionPane.WARNING_MESSAGE);
			} else {
				try {
					workingTestSuiteFileName = testSuiteFile.getAbsolutePath();
					if(workingPolicy!=null){
						testSuite = new TestSuiteDemo(workingTestSuiteFileName, workingPolicy.toString());
					}else{
						testSuite = new TestSuiteDemo(workingTestSuiteFileName,	"");
					}
					setUpTestPanel();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(demo, "Invalid test suite file");
				}
			}
		}
	}

//	private JRadioButton basicRuleCoverageRadio = new JRadioButton(
//			"Basic rule coverage");
	private JRadioButton exclusiveRuleCoverageRadio = new JRadioButton(
			"Exclusive rule coverage");
	private JRadioButton DecisionCoverageRadio = new JRadioButton(
			"Decision coverage");
	private JRadioButton permitDenyPairCoverageRadio = new JRadioButton(
			"Permit/Deny rule pair coverage");
	private JRadioButton rulePairCoverageRadio = new JRadioButton(
			"All rule pair coverage");
	private JRadioButton MCDCRadio = new JRadioButton("MC\\DC ");
	private JRadioButton MCDCRadio_NoError = new JRadioButton("MC\\DC_NoError");
	private JRadioButton DecisionCoverageRadio_NoError = new JRadioButton(
			"Decision coverage_NoError");
	private JRadioButton Unique_MCDC = new JRadioButton("Unique_case MC\\DC");
	private JRadioButton Unique_MCDC_NoError = new JRadioButton("Unique_case MC\\DC_NoError");

	private JPanel createPanel() {
		JPanel myPanel = new JPanel();
		exclusiveRuleCoverageRadio.setSelected(true);

		final ButtonGroup group = new ButtonGroup();
		//group.add(basicRuleCoverageRadio);
		group.add(exclusiveRuleCoverageRadio);
		group.add(DecisionCoverageRadio);
		group.add(permitDenyPairCoverageRadio);
		group.add(rulePairCoverageRadio);
		group.add(MCDCRadio);
		group.add(MCDCRadio_NoError);
		group.add(DecisionCoverageRadio_NoError);
		group.add(Unique_MCDC);

		myPanel.setLayout(new GridLayout(3, 3));
		//myPanel.add(basicRuleCoverageRadio);
		myPanel.add(exclusiveRuleCoverageRadio);
		myPanel.add(DecisionCoverageRadio);
		myPanel.add(DecisionCoverageRadio_NoError);
		myPanel.add(permitDenyPairCoverageRadio);
		myPanel.add(rulePairCoverageRadio);
		myPanel.add(MCDCRadio);
		myPanel.add(MCDCRadio_NoError);
		myPanel.add(Unique_MCDC);
		myPanel.add(Unique_MCDC_NoError);
		myPanel.setBorder(new TitledBorder(new EtchedBorder(), ""));

		return myPanel;
	}

	public void generateCoverageBasedTests() {
		if (!demo.hasWorkingPolicy()) {
			JOptionPane.showMessageDialog(demo, "There is no policy!");
			return;
		}
		int result = JOptionPane.showConfirmDialog(demo, createPanel(), "Please Select Test Generation Strategy", JOptionPane.OK_CANCEL_OPTION);
		
		if (result == JOptionPane.OK_OPTION) {
			LoadPolicyDemo lp = new LoadPolicyDemo();
			AbstractPolicy policy = lp.getPolicy(demo.getWorkingPolicyFilePath());
			PolicyXDemo policyx = new PolicyXDemo(policy);
			policyx.initBalana(this.demo);
			String policyFilePath = demo.getWorkingPolicyFilePath();
			if (exclusiveRuleCoverageRadio.isSelected()) {
				try{
					RuleCoverage requestGenerator = new RuleCoverage(policyFilePath); 
					List<String> requests = requestGenerator.generateRequests();
					testSuite = new TestSuiteDemo(policyFilePath,requests);
					testSuite.save();
					workingTestSuiteFileName = TestUtil.getTestSuiteMetaFilePath(policyFilePath, NameDirectory.RULE_COVERAGE);
				}catch(Exception e){
					ExceptionUtil.handleInDefaultLevel(e);
				}
				
				//OnetrueOtherFalse.writeToExcelFile(workingTestSuiteFileName);
			}/* else if (DecisionCoverageRadio.isSelected()) {
				TestSuiteDemo decisionCoverage = new TestSuiteDemo(
						DecisionCoverageTestGenerator.generateTests(this, "_DecisionCoverage",demo.getWorkingPolicyFilePath(),PolicyXDemo.balana),
						demo.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("_DecisionCoverage");
				decisionCoverage.writeToExcelFile(workingTestSuiteFileName);
			} else if (permitDenyPairCoverageRadio.isSelected()) {
				TestSuiteDemo PermitDenyCombine = new TestSuiteDemo(
						policyx.generate_ByDenyPermit(this),
						demo.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("_PDpair");
				PermitDenyCombine.writeToExcelFile(workingTestSuiteFileName);
			} else if (rulePairCoverageRadio.isSelected()) {
				TestSuiteDemo ByTwo = new TestSuiteDemo(
						policyx.generate_ByTwo(this),
						demo.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("_Pair");
				ByTwo.writeToExcelFile(workingTestSuiteFileName);
			} else if (DecisionCoverageRadio_NoError.isSelected()) {
				TestSuiteDemo decisionCoverage = new TestSuiteDemo(
						policyx.generate_DecisionCoverage(this,
								policyx.buildDecisionCoverage_NoId(policy), "_DecisionCoverage_NoError"),
						demo.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("_DecisionCoverage_NoError");
				decisionCoverage.writeToExcelFile(workingTestSuiteFileName);
			} else if (MCDCRadio.isSelected()) {
				MCDC_converter2 converter = new MCDC_converter2();
				TestSuiteDemo mcdcTestSuite = new TestSuiteDemo(
						policyx.generate_MCDCCoverage(this,
								policyx.buildMCDC_Table(policy, converter, false),
								"_MCDCCoverage", converter),
						demo.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("_MCDCCoverage");
				mcdcTestSuite.writeToExcelFile(workingTestSuiteFileName);
			} else if (MCDCRadio_NoError.isSelected()) {
				MCDC_converter2 converter = new MCDC_converter2();
				TestSuiteDemo mcdcTestSuite = new TestSuiteDemo(
						policyx.generate_MCDCCoverage(this,
								policyx.buildMCDC_Table_NoId(policy, converter, false),
								"_MCDCCoverage_NoError", converter),
						demo.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("_MCDCCoverage_NoError");
				mcdcTestSuite.writeToExcelFile(workingTestSuiteFileName);
			} else if (Unique_MCDC.isSelected()) {
				MCDC_converter2 converter = new MCDC_converter2();
				TestSuiteDemo mcdcTestSuite = new TestSuiteDemo(
						policyx.generate_MCDCCoverage(this,
								policyx.buildMCDC_Table(policy, converter, true),
								"Unique_Case_MCDCCoverage", converter),
						demo.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("Unique_Case_MCDCCoverage");
				mcdcTestSuite.writeToExcelFile(workingTestSuiteFileName);
			} else if (Unique_MCDC_NoError.isSelected()) {
				MCDC_converter2 converter = new MCDC_converter2();
				TestSuiteDemo mcdcTestSuite = new TestSuiteDemo(
						policyx.generate_MCDCCoverage(this,
								policyx.buildMCDC_Table_NoId(policy, converter, true),
								"Unique_Case_MCDCCoverage_NoError", converter),
						demo.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("Unique_Case_MCDCCoverage_NoError");
				mcdcTestSuite.writeToExcelFile(workingTestSuiteFileName);
			}*/
			
			String dir = demo.getWorkingPolicyFile().getParent();
			Runtime run = Runtime.getRuntime();
			try {
				testSuite = new TestSuiteDemo(workingTestSuiteFileName, demo.getWorkingPolicyFilePath());
				setUpTestPanel();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void generateMutationBasedTests() {
		if (!demo.hasWorkingPolicy()) {
			JOptionPane.showMessageDialog(demo, "There is no policy!");
			return;
		}
		//MutationPanel2 mutationPanel2 = new MutationPanel2(demo, this);
		//mutationPanel2.generateMutants();
		MutationBasedTestGenerator testGenerator;
		List<TaggedRequest> taggedRequests;
		try{
			testGenerator = new MutationBasedTestGenerator(demo.getWorkingPolicyFilePath());
			List<String> mutationMethods = new ArrayList<String>();
			mutationMethods.add("createPolicyTargetTrueMutants");
			mutationMethods.add("createPolicyTargetFalseMutants");
			mutationMethods.add("createRuleEffectFlippingMutants");
			mutationMethods.add("createRuleTargetTrueMutants");
			mutationMethods.add("createRuleTargetFalseMutants");
			mutationMethods.add("createRuleConditionTrueMutants");
			mutationMethods.add("createRuleConditionFalseMutants");
			
			taggedRequests = testGenerator.generateRequests(mutationMethods);
			String hi = "";
		}catch(Exception e){
			ExceptionUtil.handleInDefaultLevel(e);
		}
	}
	
	public void runTests() {
		hasFailure = false; 
		if (!demo.hasWorkingPolicy()) {
			JOptionPane.showMessageDialog(demo, "There is no policy!");
			return;
		}
		if (!hasTests()) {
			JOptionPane.showMessageDialog(demo, "There are no tests.");
			return; 
		}

		try {
			/*PolicyRunner runner = new PolicyRunner(
					demo.getWorkingPolicyFilePath());*/
			AbstractPolicy policy = PolicyLoader.loadPolicy(demo.getWorkingPolicyFile());
			for (Vector<Object> child : data) {
				int result = TestSuite.runTestWithoutOracle(policy, child.get(6).toString());
				// System.out.println(result);
				String actualResponse = ResultConverter.ConvertResult(result);
				child.set(4, actualResponse);
				if (child.get(3) != null && !child.get(3).toString().equals("")) {
					String expectedResponse = child.get(3).toString();
					if (actualResponse.equals(expectedResponse))
						child.set(5, "pass");
					else {
						child.set(5, "fail");
						hasFailure = true;
					}
				}
				demo.setToTestPane();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		requestTablePanel.validate();
		requestTablePanel.updateUI();
	}

	public void saveActualResponsesAsOracleValues() {
		if (!hasTests()) {
			JOptionPane.showMessageDialog(demo, "There are no tests.");
			return;
		}

		boolean hasOracleValue = false;
		for (Vector<Object> child : data) {
			if (!child.get(3).toString().equals("")) {
				hasOracleValue = true;
			}
		}
		if (!hasOracleValue) {
			List<TestRecord> recordList = new ArrayList<TestRecord>();
			for (Vector<Object> child : data) {
				recordList.add(TestUtil.getTestRecord(child));
			}
			TestSuiteDemo testSuite = new TestSuiteDemo(recordList, "GenTests/");
			testSuite.writeMetaFile(workingTestSuiteFileName);
			System.out.println(workingTestSuiteFileName + " saved.");
			requestTablePanel.validate();
			requestTablePanel.updateUI();
			this.testSuite = testSuite;
		} else {
			JOptionPane.showMessageDialog(demo, "Oracle values already exist!");
			return;
		}
	}

	public String getTestsuiteXLSfileName(String testMethod) {
		File file = demo.getWorkingPolicyFile();
		String path = file.getParentFile().getAbsolutePath();
		String name = file.getName();
		name = name.substring(0, name.length() - 4);
		path = path + File.separator + "test_suites" + File.separator + name
				+ testMethod + File.separator + name + testMethod + ".xls";
		return path;
	}

	public String getTestOutputDestination(String testMethod) {

		File file = demo.getWorkingPolicyFile();
		String path = file.getParentFile().getAbsolutePath();
		String name = file.getName();
		name = name.substring(0, name.length() - 4);
		path = path + File.separator + "test_suites" + File.separator + name
				+ testMethod;
		return path;
	}

	public String getWorkingTestSuiteFileName() {
		return workingTestSuiteFileName;
	}

	public boolean hasTests() {
		return data != null && data.size() > 0;
	}
	public TestSuiteDemo getTestSuite(){
		return testSuite;
	}
	public boolean hasTestFailure(){
		return hasFailure;
	}
}
