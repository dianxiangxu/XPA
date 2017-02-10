package org.seal.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
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

import org.seal.combiningalgorithms.PolicyX;
import org.seal.combiningalgorithms.loadPolicy;
import org.seal.coverage.PolicyRunner;
import org.seal.coverage.PolicySpreadSheetTestRecord;
import org.seal.coverage.PolicySpreadSheetTestSuite;
import org.seal.mcdc.MCDC_converter2;
import org.umu.editor.XMLFileFilter;
import org.wso2.balana.Policy;

public class TestPanel extends JPanel {
	private XPA xpa;

	private String workingTestSuiteFileName;
	private PolicySpreadSheetTestSuite testSuite;

	private Vector<Vector<Object>> data;
	private TestTablePanel requestTablePanel;
	private JPanel requestPanel;

	private boolean hasFailure;
	
	public TestPanel(XPA xpa) {
		this.xpa = xpa;
	}

	public XPA getXPA() {
		return this.xpa;
	}
	

	public PolicySpreadSheetTestSuite getPolicySpreadSheetTestSuite() {
		return testSuite;
	}
	
	public void setTestSuite(PolicySpreadSheetTestSuite p)
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
		String[] columnNames = { "No", "Test Name", "Request File",
				"Expected Response", "Actual Response", "Verdict" };
		data = testSuite.getTestData();
		if (data.size() == 0) {
			JOptionPane.showMessageDialog(xpa, "There is no test!");
			return;
		}
		Vector<Object> selected = data.get(0);
		String request = selected.get(6).toString();
		requestPanel = new JPanel();
		requestPanel.setLayout(new BorderLayout());
		GeneralTablePanel gt = RequestTable.getRequestTable(request, false);
		gt.setMinRows(5);
		RequestTable.setPreferredColumnWidths(gt, this.getSize().getWidth());
		requestPanel.add(gt, BorderLayout.CENTER);

		requestTablePanel = new TestTablePanel(data, columnNames, 5,
				requestPanel);
		JScrollPane scrollpane = new JScrollPane(requestTablePanel);
		JSplitPane jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		jSplitPane.setTopComponent(scrollpane);
		jSplitPane.setBottomComponent(requestPanel);
		jSplitPane.setResizeWeight(0.7);

		add(jSplitPane, BorderLayout.CENTER);
		xpa.setToTestPane();
		xpa.updateMainTabbedPane();
	}

	public void openTests() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(false);
		if (xpa.getWorkingPolicyFile() != null)
			fileChooser.setCurrentDirectory(xpa.getWorkingPolicyFile()
					.getParentFile());
		fileChooser.setFileFilter(new XMLFileFilter("xls"));
		fileChooser.setDialogTitle("Open Test Suite");
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File testSuiteFile = fileChooser.getSelectedFile();
			if (!testSuiteFile.toString().endsWith(".xls")) {
				JOptionPane.showMessageDialog(xpa,
						"The open File is not a test suite *.xls",
						"Error of Selection", JOptionPane.WARNING_MESSAGE);
			} else {
				try {
					workingTestSuiteFileName = testSuiteFile.getAbsolutePath();
					testSuite = new PolicySpreadSheetTestSuite(
							workingTestSuiteFileName,
							xpa.getWorkingPolicyFilePath());
					setUpTestPanel();
				} catch (Exception e) {
					e.printStackTrace();
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
		if (!xpa.hasWorkingPolicy()) {
			JOptionPane.showMessageDialog(xpa, "There is no policy!");
			return;
		}
		int result = JOptionPane.showConfirmDialog(xpa, createPanel(),
				"Please Select Test Generation Strategy",
				JOptionPane.OK_CANCEL_OPTION);
		
		if (result == JOptionPane.OK_OPTION) {
			loadPolicy lp = new loadPolicy();
			Policy policy = lp.getPolicy(xpa.getWorkingPolicyFilePath());
			PolicyX policyx = new PolicyX(policy);
			policyx.initBalana(this.xpa);
			if (exclusiveRuleCoverageRadio.isSelected()) {
				PolicySpreadSheetTestSuite OnetrueOtherFalse = new PolicySpreadSheetTestSuite(
						policyx.generate_OneTrueOtherFalse(this),
						xpa.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("_Exclusive");
				OnetrueOtherFalse.writeToExcelFile(workingTestSuiteFileName);
			} else if (DecisionCoverageRadio.isSelected()) {
				PolicySpreadSheetTestSuite decisionCoverage = new PolicySpreadSheetTestSuite(
						policyx.generate_DecisionCoverage(this,
								policyx.buildDecisionCoverage(policy), "_DecisionCoverage"),
						xpa.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("_DecisionCoverage");
				decisionCoverage.writeToExcelFile(workingTestSuiteFileName);
			} else if (permitDenyPairCoverageRadio.isSelected()) {
				PolicySpreadSheetTestSuite PermitDenyCombine = new PolicySpreadSheetTestSuite(
						policyx.generate_ByDenyPermit(this),
						xpa.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("_PDpair");
				PermitDenyCombine.writeToExcelFile(workingTestSuiteFileName);
			} else if (rulePairCoverageRadio.isSelected()) {
				PolicySpreadSheetTestSuite ByTwo = new PolicySpreadSheetTestSuite(
						policyx.generate_ByTwo(this),
						xpa.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("_Pair");
				ByTwo.writeToExcelFile(workingTestSuiteFileName);
			} else if (DecisionCoverageRadio_NoError.isSelected()) {
				PolicySpreadSheetTestSuite decisionCoverage = new PolicySpreadSheetTestSuite(
						policyx.generate_DecisionCoverage(this,
								policyx.buildDecisionCoverage_NoId(policy), "_DecisionCoverage_NoError"),
						xpa.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("_DecisionCoverage_NoError");
				decisionCoverage.writeToExcelFile(workingTestSuiteFileName);
			} else if (MCDCRadio.isSelected()) {
				MCDC_converter2 converter = new MCDC_converter2();
				PolicySpreadSheetTestSuite mcdcTestSuite = new PolicySpreadSheetTestSuite(
						policyx.generate_MCDCCoverage(this,
								policyx.buildMCDC_Table(policy, converter, false),
								"_MCDCCoverage", converter),
						xpa.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("_MCDCCoverage");
				mcdcTestSuite.writeToExcelFile(workingTestSuiteFileName);
			} else if (MCDCRadio_NoError.isSelected()) {
				MCDC_converter2 converter = new MCDC_converter2();
				PolicySpreadSheetTestSuite mcdcTestSuite = new PolicySpreadSheetTestSuite(
						policyx.generate_MCDCCoverage(this,
								policyx.buildMCDC_Table_NoId(policy, converter, false),
								"_MCDCCoverage_NoError", converter),
						xpa.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("_MCDCCoverage_NoError");
				mcdcTestSuite.writeToExcelFile(workingTestSuiteFileName);
			} else if (Unique_MCDC.isSelected()) {
				MCDC_converter2 converter = new MCDC_converter2();
				PolicySpreadSheetTestSuite mcdcTestSuite = new PolicySpreadSheetTestSuite(
						policyx.generate_MCDCCoverage(this,
								policyx.buildMCDC_Table(policy, converter, true),
								"Unique_Case_MCDCCoverage", converter),
						xpa.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("Unique_Case_MCDCCoverage");
				mcdcTestSuite.writeToExcelFile(workingTestSuiteFileName);
			} else if (Unique_MCDC_NoError.isSelected()) {
				MCDC_converter2 converter = new MCDC_converter2();
				PolicySpreadSheetTestSuite mcdcTestSuite = new PolicySpreadSheetTestSuite(
						policyx.generate_MCDCCoverage(this,
								policyx.buildMCDC_Table_NoId(policy, converter, true),
								"Unique_Case_MCDCCoverage_NoError", converter),
						xpa.getWorkingPolicyFilePath());
				workingTestSuiteFileName = getTestsuiteXLSfileName("Unique_Case_MCDCCoverage_NoError");
				mcdcTestSuite.writeToExcelFile(workingTestSuiteFileName);
			}
			
			String dir = xpa.getWorkingPolicyFile().getParent();
			Runtime run = Runtime.getRuntime();

			try {
				testSuite = new PolicySpreadSheetTestSuite(
						workingTestSuiteFileName,
						xpa.getWorkingPolicyFilePath());
				setUpTestPanel();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void generateMutationBasedTests() {
		if (!xpa.hasWorkingPolicy()) {
			JOptionPane.showMessageDialog(xpa, "There is no policy!");
			return;
		}
		MutationPanel2 mutationPanel2 = new MutationPanel2(xpa, this);
		mutationPanel2.generateMutants();
	}
	
	public void runTests() {
		hasFailure = false; 
		if (!xpa.hasWorkingPolicy()) {
			JOptionPane.showMessageDialog(xpa, "There is no policy!");
			return;
		}
		if (!hasTests()) {
			JOptionPane.showMessageDialog(xpa, "There are no tests.");
			return;
		}

		try {
			PolicyRunner runner = new PolicyRunner(
					xpa.getWorkingPolicyFilePath());
			for (Vector<Object> child : data) {
				int result = runner.runTestWithoutOracle(child.get(1)
						.toString(), child.get(6).toString());
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
				xpa.setToTestPane();
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
			JOptionPane.showMessageDialog(xpa, "There are no tests.");
			return;
		}

		boolean hasOracleValue = false;
		for (Vector<Object> child : data) {
			if (!child.get(3).toString().equals("")) {
				hasOracleValue = true;
			}
		}
		if (hasOracleValue == false) {
			int count = 0;
			ArrayList<PolicySpreadSheetTestRecord> recordList = new ArrayList<PolicySpreadSheetTestRecord>();
			for (Vector<Object> child : data) {
				count++;
				child.set(3, child.get(4));
				
				PolicySpreadSheetTestRecord record = new PolicySpreadSheetTestRecord(
						child.get(1).toString(),
						child.get(2).toString(), child.get(6).toString(),
						child.get(3).toString());
				recordList.add(record);
			}
			PolicySpreadSheetTestSuite testSuite = new PolicySpreadSheetTestSuite(
					recordList, "GenTests/");
			testSuite.writeToExcelFile(workingTestSuiteFileName);
			System.out.println(workingTestSuiteFileName + " saved.");
			requestTablePanel.validate();
			requestTablePanel.updateUI();
		} else {
			JOptionPane.showMessageDialog(xpa, "Oracle values already exist!");
			return;
		}
	}

	public String getTestsuiteXLSfileName(String testMethod) {
		File file = xpa.getWorkingPolicyFile();
		String path = file.getParentFile().getAbsolutePath();
		String name = file.getName();
		name = name.substring(0, name.length() - 4);
		path = path + File.separator + "test_suites" + File.separator + name
				+ testMethod + File.separator + name + testMethod + ".xls";
		return path;
	}

	public String getTestOutputDestination(String testMethod) {

		File file = xpa.getWorkingPolicyFile();
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
	
	public boolean hasTestFailure(){
		return hasFailure;
	}
}
