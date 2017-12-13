package org.seal.xacml.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.File;
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
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;

import org.seal.xacml.NameDirectory;
import org.seal.xacml.TaggedRequest;
import org.seal.xacml.TestRecord;
import org.seal.xacml.TestSuiteDemo;
import org.seal.xacml.combiningalgorithms.LoadPolicyDemo;
import org.seal.xacml.combiningalgorithms.PolicyX;
import org.seal.xacml.components.MutationBasedTestMutationMethods;
import org.seal.xacml.coverage.DecisionCoverage;
import org.seal.xacml.coverage.MCDC;
import org.seal.xacml.coverage.RuleCoverage;
import org.seal.xacml.gui.DebugPanel.susPiciousColumnCellRenderer;
import org.seal.xacml.mutation.MutationBasedTestGenerator;
import org.seal.xacml.policyUtils.PolicyLoader;
import org.seal.xacml.semanticCoverage.TestSuite;
import org.seal.xacml.utils.ExceptionUtil;
import org.seal.xacml.utils.TestUtil;
import org.seal.xacml.xpa.XPA;
import org.umu.editor.XMLFileFilter;
import org.wso2.balana.AbstractPolicy;

public class TestPanel extends JPanel {
	private XPA demo;

	private String workingTestSuiteFileName;
	private TestSuiteDemo testSuite;

	private Vector<Vector<Object>> data;
	private TestTablePanel requestTablePanel;
	private JPanel requestPanel;
	private String type;

	private boolean hasFailure;

	private static int verdictCol;
	
	public TestPanel(XPA demo) {
		this.demo = demo;
	}

	public XPA getDemo() {
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

	private JRadioButton exclusiveRuleCoverageRadio = new JRadioButton("Exclusive rule coverage");
	private JRadioButton DecisionCoverageRadio = new JRadioButton("Decision coverage");
	
	
	private JRadioButton MCDCRadio = new JRadioButton("MC\\DC ");
	private JRadioButton MCDCRadio_NoError = new JRadioButton("MC\\DC_NoError");
	private JRadioButton DecisionCoverageRadio_NoError = new JRadioButton("Decision coverage_NoError");
	
	private JPanel createPanel() {
		JPanel myPanel = new JPanel();
		exclusiveRuleCoverageRadio.setSelected(true);

		final ButtonGroup group = new ButtonGroup();
		group.add(exclusiveRuleCoverageRadio);
		group.add(DecisionCoverageRadio);
		group.add(MCDCRadio);
		group.add(MCDCRadio_NoError);
		group.add(DecisionCoverageRadio_NoError);
		
		myPanel.setLayout(new GridLayout(3, 3));
		myPanel.add(exclusiveRuleCoverageRadio);
		myPanel.add(DecisionCoverageRadio);
		myPanel.add(DecisionCoverageRadio_NoError);
		myPanel.add(MCDCRadio);
		myPanel.add(MCDCRadio_NoError);
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
			PolicyX policyx = new PolicyX(policy);
			policyx.initBalana(this.demo);
			String policyFilePath = demo.getWorkingPolicyFilePath();
			if (exclusiveRuleCoverageRadio.isSelected()) {
				try{
					this.type = NameDirectory.RULE_COVERAGE;
					RuleCoverage requestGenerator = new RuleCoverage(policyFilePath); 
					List<String> requests = requestGenerator.generateRequests();
					testSuite = new TestSuiteDemo(policyFilePath,requests,NameDirectory.RULE_COVERAGE);
					testSuite.save();
					workingTestSuiteFileName = TestUtil.getTestSuiteMetaFilePath(policyFilePath, NameDirectory.RULE_COVERAGE);
				}catch(Exception e){
					ExceptionUtil.handleInDefaultLevel(e);
				}
			} else if (DecisionCoverageRadio.isSelected()) {
				try{
					DecisionCoverage requestGenerator = new DecisionCoverage(policyFilePath,true);
					List<String> requests = requestGenerator.generateTests();
					testSuite = new TestSuiteDemo(policyFilePath,requests,NameDirectory.DECISION_COVERAGE);
					testSuite.save();
					workingTestSuiteFileName = TestUtil.getTestSuiteMetaFilePath(policyFilePath, NameDirectory.DECISION_COVERAGE);
				}catch(Exception e){
					ExceptionUtil.handleInDefaultLevel(e);
				}
			} else if (DecisionCoverageRadio_NoError.isSelected()) {
				try{
					DecisionCoverage requestGenerator = new DecisionCoverage(policyFilePath,false);
					List<String> requests = requestGenerator.generateTests();
					testSuite = new TestSuiteDemo(policyFilePath,requests,NameDirectory.DECISION_COVERAGE_NO_ERROR);
					testSuite.save();
					workingTestSuiteFileName = TestUtil.getTestSuiteMetaFilePath(policyFilePath, NameDirectory.DECISION_COVERAGE_NO_ERROR);
				}catch(Exception e){
					ExceptionUtil.handleInDefaultLevel(e);
				}
			} else if (MCDCRadio.isSelected()) {
				try{
					MCDC requestGenerator = new MCDC(policyFilePath,false);
					List<String> requests = requestGenerator.generateTests();
					testSuite = new TestSuiteDemo(policyFilePath,requests,NameDirectory.DECISION_COVERAGE_NO_ERROR);
					testSuite.save();
					workingTestSuiteFileName = TestUtil.getTestSuiteMetaFilePath(policyFilePath, NameDirectory.DECISION_COVERAGE_NO_ERROR);
				}catch(Exception e){
					ExceptionUtil.handleInDefaultLevel(e);
				}
			}
			
			
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
		MutationBasedTestGenerator testGenerator;
		List<TaggedRequest> taggedRequests;
		try{
			testGenerator = new MutationBasedTestGenerator(demo.getWorkingPolicyFilePath());
			MutationBasedTestMutationMethods mbtMethods = new MutationBasedTestMutationMethods();
			int result = JOptionPane.showConfirmDialog(demo, mbtMethods.createPanel(),"Please Select Mutation Methods",JOptionPane.OK_CANCEL_OPTION);
			String policyFilePath = demo.getWorkingPolicyFilePath();
			this.type = NameDirectory.MUTATION_BASED_TEST;
			if (result == JOptionPane.OK_OPTION) {
				List<String> mutationMethods = mbtMethods.getMutationOperatorList();
				taggedRequests = testGenerator.generateRequests(mutationMethods);
				TestSuiteDemo suite = new TestSuiteDemo(policyFilePath, this.type, taggedRequests);
				suite.save();
				this.testSuite = suite;
				this.workingTestSuiteFileName = TestUtil.getTestSuiteMetaFilePath(policyFilePath, NameDirectory.MUTATION_BASED_TEST);
				setUpTestPanel();
			}
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
						
						
						
						requestTablePanel.table.getColumnModel().getColumn(5).setCellRenderer(new verdictColumnCellRenderer());

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
	
	static class verdictColumnCellRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row,int column) {
			Component c = super.getTableCellRendererComponent(table, value,isSelected, hasFocus, row, column);

//			double d = (double)value;
			if (column == 5) {
				if (new String("fail").equals(value.toString())) {
					System.out.println("executed.....");
					Color myColor = new Color(255,0,0);
					c.setBackground(myColor);
				}else {
					Color myColor = new Color(255,255,255);
					c.setBackground(myColor);
				}
			}
			
			
			return c;
		}
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
			TestSuiteDemo testSuite = new TestSuiteDemo(recordList, "GenTests/",this.type);
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
		path = path + File.separator + "test_suites" + File.separator + name + testMethod + File.separator + name + testMethod + ".xls";
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
