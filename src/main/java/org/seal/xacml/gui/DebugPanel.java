package org.seal.xacml.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.io.IOUtils;
import org.seal.coverage.PolicySpreadSheetTestRecord;
import org.seal.xacml.TestRecord;
import org.seal.xacml.policyUtils.PolicyLoader;
import org.seal.xacml.policyUtils.XpathSolver;
import org.seal.xacml.semanticCoverage.Coverage;
import org.seal.xacml.semanticCoverage.PolicyCoverageFactory;
import org.seal.xacml.semanticCoverage.TestSuite;
import org.seal.xacml.semanticFaultLocalization.SpectrumBasedDiagnosisResults;
import org.seal.xacml.semanticFaultLocalization.SpectrumBasedFaultLocalizer;
import org.seal.xacml.semanticMutation.Mutant;
import org.seal.xacml.semanticRepair.Repairer;
import org.seal.xacml.utils.FileIOUtil;
import org.seal.xacml.utils.MutantDiff;
import org.seal.xacml.utils.PropertiesLoader;
import org.seal.xacml.utils.XMLUtil;
import org.seal.xacml.xpa.XPA;


public class DebugPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private XPA xpa;
	private List<JRadioButton> faultLocalizationMethodRadioButtons;
	private JTextField depthField;
	private GeneralTablePanel tablePanel;
	private JCheckBox boxPTT = new JCheckBox("Policy Target True (PTT)");
	private JCheckBox boxPTF = new JCheckBox("Policy Target False (PTF)");
	private JCheckBox boxCRC = new JCheckBox("Change Rule CombiningAlgorithm (CRC)");
	private JCheckBox boxCRE = new JCheckBox("Flip Rule Effect (CRE)");
	private JCheckBox boxRER = new JCheckBox("Remove One Rule (RER)");
	private JCheckBox boxANR = new JCheckBox("Add a New Rule (ANR)");
	private JCheckBox boxRTT = new JCheckBox("Rule Target True (RTT)");
	private JCheckBox boxRTF = new JCheckBox("Rule Target False (RTF)");
	private JCheckBox boxRCT = new JCheckBox("Rule Condition True (RCT)");
	private JCheckBox boxRCF = new JCheckBox("Rule Condition False (RCF)");
	private JCheckBox boxRCCF = new JCheckBox("Rule Change Comparition Function(RCCF)");
	private JCheckBox boxPCCF = new JCheckBox("Policy Target Change Comparition Function(PCCF)");
	private JCheckBox boxFPR = new JCheckBox("First Permit Rules (FPR)");
	private JCheckBox boxFDR = new JCheckBox("First Deny Rules (FDR)");
	private JCheckBox boxANF = new JCheckBox("Add Not Function (ANF)");
	private JCheckBox boxRNF = new JCheckBox("Remove Not Function (RNF)");
	private JCheckBox boxSelectAll = new JCheckBox("Select All"); // All 13 types of mutation.
	private JTable table;
	private static int xPathCol;
	private static int suspiciousScoreCol;
	
	public DebugPanel(XPA xpa) {
		this.xpa = xpa;
	}

	public void localizeFault(){
		if(checkInputs()){
			if (hasFault(getTestSuite())) {
				int result = JOptionPane.showConfirmDialog(xpa, createPanel(false),"Please Select Fault Localization Method",JOptionPane.OK_CANCEL_OPTION);
				if(result == 0){
					for(JRadioButton button:faultLocalizationMethodRadioButtons){
						if(button.isSelected()){
							String method = button.getText();
							
							List<List<Coverage>> coverageMatrix = PolicyCoverageFactory.getCoverageMatrix();
				            SpectrumBasedFaultLocalizer faultLocalizer = new SpectrumBasedFaultLocalizer(coverageMatrix,PolicyCoverageFactory.getResults());
				            try{
					            SpectrumBasedDiagnosisResults diagnosisResults = new SpectrumBasedDiagnosisResults(faultLocalizer.applyFaultLocalizeMethod(method));
					            List<Integer> suspicionList = diagnosisResults.getIndexRankedBySuspicion();
					            List<Double> suspiciousScore = diagnosisResults.getSuspiciousScore();
					            InputStream stream = IOUtils.toInputStream(PolicyLoader.loadPolicy(xpa.getWorkingPolicyFile()).encode(), Charset.defaultCharset());
					            List<String> entryList = XpathSolver.getEntryListRelativeXPath(PolicyLoader.getDocument(stream));
					            setUpFaultLocalizationPanel(entryList,suspicionList,suspiciousScore);
				            }catch(Exception e){
				            	e.printStackTrace();
				            }
				        }
					}
					table = tablePanel.getTable();
					xPathCol = table.getColumn("XPath").getModelIndex();
					table.getColumnModel().getColumn(xPathCol).setCellRenderer(new XPAthColumnCellRenderer());
					table.addMouseListener(new java.awt.event.MouseAdapter() {
					    @Override
					    public void mouseClicked(java.awt.event.MouseEvent evt) {
					        int row = table.rowAtPoint(evt.getPoint());
					        int targetCol = table.getColumn("XPath").getModelIndex();

					        int col = table.columnAtPoint(evt.getPoint());
					        if (row >= 0 && col ==targetCol) {
					            String xPathString = table.getValueAt( row, col).toString();
					            String content = XMLUtil.getElementByXPath(xPathString, xpa.getWorkingPolicyFile());
//					            PopupFrame.showContent("Element at "+xPathString, content);
					            
					            popUp(xPathString, content);
					            
					        }
					    }
					});
					
					table.addMouseMotionListener(new java.awt.event.MouseAdapter() {
					    @Override
					    public void mouseMoved(java.awt.event.MouseEvent evt) {
					    	if(table.rowAtPoint(evt.getPoint())<table.getRowCount() && table.columnAtPoint(evt.getPoint())==xPathCol)
					    	{
					    	    setCursor(new Cursor(Cursor.HAND_CURSOR)); 
					    	}
					    	else
					    	{
					    	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					    	}
					    }
					});

							

				}
			} else{
				JOptionPane.showMessageDialog(xpa, "There is no fault in this policy");
			}
		}
	}

	public void popUp(String title, String content){
		JFrame frame = new JFrame("JOptionPane showMessageDialog example");
        JOptionPane.showMessageDialog(frame,
        		content,
        		"Element at "+title,
        	    JOptionPane.PLAIN_MESSAGE);
	}
	
	public void fixFault(){
		if(checkInputs()){
			TestSuite testSuite = getTestSuite();
			if (hasFault(testSuite)) {
				int result = JOptionPane.showConfirmDialog(xpa, createPanel(true),"Please provide following details",JOptionPane.OK_CANCEL_OPTION);
				if(result == 0){
					
					for(JRadioButton button:faultLocalizationMethodRadioButtons){
						if(button.isSelected()){
							String method = button.getText();
							
							int depth;
							try{
								depth = Integer.parseInt(depthField.getText());
							}catch(Exception e){
								JOptionPane.showMessageDialog(null, "Invalid depth! Default depth value of 2 is used");
								depth = 2;
							}
							Repairer repairer = new Repairer();
							try{
								Mutant faultyPolicy =  new Mutant(PolicyLoader.loadPolicy(xpa.getWorkingPolicyFile()),"");
								List<String> mutationMethods =getMutationOperatorList();
								Mutant mutant = repairer.repairWithSelectedMutantMethods(faultyPolicy, testSuite, method, mutationMethods, depth);
								if(mutant == null){
									JOptionPane.showMessageDialog(null, "The policy can not be repaired");
								} else{
										String fileName = xpa.getWorkingPolicyFile().toString().split("\\.")[0] + "-repaired.xml";
										 File fileToSave = new File(fileName);
					                	 if(!fileToSave.toString().endsWith(".xml")){
					                		 fileToSave = new File(fileToSave.toString() + ".xml");
					                	 }
					                	 FileIOUtil.writeFile(fileToSave, mutant.encode());
					                	 JOptionPane.showMessageDialog(null, "The repaired policy is saved at " + fileName);
					                	 String originalFile = xpa.getWorkingPolicyFilePath();
					                	 String repairedFile = fileName;
					                	 MutantDiff.show(originalFile, repairedFile);
					                
								}
							}catch(Exception e){
								e.printStackTrace();
							}
			                
				        }
					}
					
				}
			} else{
				JOptionPane.showMessageDialog(xpa, "There is no fault in this policy");
			}
		}
		
	}

	private boolean hasFault(TestSuite testSuite) {
		try{
			List<Boolean> results = testSuite.runTests(PolicyLoader.loadPolicy(xpa.getWorkingPolicyFile()));
			for(Boolean b:results){
				if(!b){
					return true;
				}
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	private TestSuite getTestSuite(){
		List<String> requests = new ArrayList<String>();
		List<String> oracles = new ArrayList<String>();
		for(TestRecord record: xpa.getTestPanel().getTestSuite().getTestRecords()){
			requests.add(record.getRequest());
			oracles.add(record.getOracle());
		}
		return new TestSuite(null,requests, oracles);
		
	}
	
	private boolean checkInputs(){
		if (!xpa.hasWorkingPolicy()) {
			JOptionPane.showMessageDialog(xpa, "There is no policy!");
			return false;
		}
		if (!xpa.hasTests()){
			JOptionPane.showMessageDialog(xpa, "There are no tests.");
			return false;
		}
		for(TestRecord record: xpa.getTestPanel().getTestSuite().getTestRecords()){
			if(record.getOracle().equals("")){
				JOptionPane.showMessageDialog(xpa, "There are no oracles in Test Suite");
				return false;
			}
		}
		return true;
	}
	
	private JPanel createPanel(boolean depth) {
		
		String[] methods = PropertiesLoader.getProperties("config").getProperty("faultLocalizationMethods").split(",");
		JRadioButton button;
		ButtonGroup group = new ButtonGroup();
		JPanel radioPanel = new JPanel();
		JLabel blankLbl = new JLabel("");
		if(depth){
			JLabel label = new JLabel("Depth for Repair:");
			depthField = new JTextField("2", 5);
			radioPanel.add(label);
			radioPanel.add(depthField);
			JLabel blankLbl2 = new JLabel("");
			JLabel blankLbl3= new JLabel("");
			radioPanel.add(blankLbl2);
			radioPanel.add(blankLbl3);
			radioPanel.setLayout(new GridLayout(methods.length/2+15, 2));
					
		}else{
			radioPanel.setLayout(new GridLayout(methods.length/2+3, 2));
			
		}
		JLabel methodsLbl = new JLabel("Select Fault Localization method:");
		
		radioPanel.add(methodsLbl);
		radioPanel.add(blankLbl);
		
		faultLocalizationMethodRadioButtons = new ArrayList<JRadioButton>();
		
		for(String method: methods){
			button = new JRadioButton(method);
			faultLocalizationMethodRadioButtons.add(button);
			group.add(button);
			radioPanel.add(button);
		}
		faultLocalizationMethodRadioButtons.get(0).setSelected(true);
		
		radioPanel.setBorder(new TitledBorder(new EtchedBorder(), ""));
		
		if(depth){
			JLabel blankLbl4 = new JLabel("");
			JLabel blankLbl5= new JLabel("");
			
			JLabel mutMethodsLbl = new JLabel("Select mutation methods:");
			radioPanel.add(blankLbl4);
			radioPanel.add(blankLbl5);
			
			radioPanel.add(mutMethodsLbl);
			JLabel blankLbl6= new JLabel("");
			
			radioPanel.add(blankLbl6);
			
			radioPanel.add(boxPTT);
			radioPanel.add(boxPTF);
			radioPanel.add(boxCRC);
			radioPanel.add(boxCRE);
			radioPanel.add(boxRER);
			radioPanel.add(boxANR);
			radioPanel.add(boxRTT);
			radioPanel.add(boxRTF);
			radioPanel.add(boxRCT);
			radioPanel.add(boxRCF);
			radioPanel.add(boxRCCF);
			radioPanel.add(boxPCCF);
			radioPanel.add(boxFPR);
			radioPanel.add(boxFDR);
			radioPanel.add(boxANF);
			radioPanel.add(boxRNF);
			radioPanel.add(boxSelectAll);
			setAllIndividualBoxes(true);
		}
		
		boxSelectAll.addActionListener(new ActionListener() {		 
			@Override
			public void actionPerformed(ActionEvent e) {
				if (boxSelectAll.isSelected())
		        	setAllIndividualBoxes(true);
		        else
		        	setAllIndividualBoxes(false);			
			}
        });
		
		return radioPanel;
	}
	
	
	public void setUpFaultLocalizationPanel(List<String> xpaths, List<Integer> suspicions, List<Double> suspiciousScores){
		removeAll();
		setLayout(new BorderLayout());
		String[] columnNames = { "No.","Element Index","Suspicious Score", "XPath"};
		try {
			Vector<Vector<Object>> data = new Vector<Vector<Object>>();
			for(int i = 0; i<suspicions.size();i++){
				Vector<Object> vector = new Vector<Object>();
				vector.add((i+1));
				vector.add(suspicions.get(i));
			
				vector.add(suspiciousScores.get(i));
				vector.add(xpaths.get(suspicions.get(i)));
				data.add(vector);
			}
			tablePanel = new GeneralTablePanel(data, columnNames, columnNames.length);
			tablePanel.setMinRows(data.size());
			

			
			
			table = tablePanel.getTable();
			suspiciousScoreCol = table.getColumn("Suspicious Score").getModelIndex();
			
			for(int i = 0; i<suspicions.size();i++){
				table.getColumnModel().getColumn(suspiciousScoreCol).setCellRenderer(new susPiciousColumnCellRenderer());
			}
			
			
			
		
			
			JScrollPane scrollpane = new JScrollPane(tablePanel);
			add(scrollpane, BorderLayout.CENTER);
			xpa.setToDebugPane();
			xpa.updateMainTabbedPane();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setAllIndividualBoxes(boolean selected) {
		boxPTT.setSelected(selected);
		boxPTF.setSelected(selected);
		boxCRC.setSelected(selected);
		boxCRE.setSelected(selected);
		boxRER.setSelected(selected);
		boxRTT.setSelected(selected);
		boxRTF.setSelected(selected);
		boxRCT.setSelected(selected);
		boxRCF.setSelected(selected);
		boxRCCF.setSelected(selected);
		boxPCCF.setSelected(selected);
		boxFPR.setSelected(selected);
		boxFDR.setSelected(selected);
		boxANF.setSelected(selected);
		boxANR.setSelected(selected);
		boxRNF.setSelected(selected);
		boxSelectAll.setSelected(selected);
	}
	
	private List<String> getMutationOperatorList(){
		List<String> lst = new ArrayList<String>();
		if (boxPTT.isSelected()) {
			lst.add("createPolicyTargetTrueMutants");
		}
		if (boxPTF.isSelected()) {
			lst.add("createPolicyTargetFalseMutants");
		}
		if (boxCRC.isSelected()) {
			lst.add("createCombiningAlgorithmMutants");
		}
		if (boxCRE.isSelected()) {
			lst.add("createRuleEffectFlippingMutants");
		}
		if (boxRER.isSelected()) {
			lst.add("createRemoveRuleMutants");
		}
		if (boxANR.isSelected()) {
			lst.add("createAddNewRuleMutants");
		}
		if (boxRTT.isSelected()) {
			lst.add("createRuleTargetTrueMutants");
		}
		if (boxRTF.isSelected()) {
			lst.add("createRuleTargetFalseMutants");
		}
		if (boxRCT.isSelected()) {
			lst.add("createRuleConditionTrueMutants");
		}
		if (boxRCF.isSelected()) {
			lst.add("createRuleConditionFalseMutants");
		}
		if (boxRCCF.isSelected()) {
			lst.add("createRuleChangeComparisonFunctionMutants");
		}
		if (boxPCCF.isSelected()) {
			lst.add("createPolicyTargetChangeComparisonFunctionMutants");
		}
		if (boxFPR.isSelected()) {
			lst.add("createFirstPermitRuleMutants");
		}
		if (boxFDR.isSelected()) {
			lst.add("createFirstDenyRuleMutants");
		}
		if (boxANF.isSelected()) {
			lst.add("createAddNotFunctionMutants");
		}
		if (boxRNF.isSelected()) {
			lst.add("createRemoveNotFunctionMutants");
		}
		return lst;
	}
	
	static class XPAthColumnCellRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row,int column) {
			Component c = super.getTableCellRendererComponent(table, value,isSelected, hasFocus, row, column);
			if (column == xPathCol) {
			
				c.setForeground(Color.blue);
				
			}
			return c;
		}
	}
	
	static class susPiciousColumnCellRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row,int column) {
			Component c = super.getTableCellRendererComponent(table, value,isSelected, hasFocus, row, column);
			
			double d = (double)value;

			if (column == suspiciousScoreCol) {
			
				int r = 255;
				int g = 255;
				
				double temp = d * 100;
				temp = (temp < 0.0) ? temp * (-1) : temp;
				Long L = Math.round(temp);
				r = Integer.valueOf(L.intValue());
				
				if (d >= 0.0) {
					if (d > 0.0) {
						r += 100;
						r = (r > 255) ? 255 : r;
						g -= r;
					}
					System.out.println(r);
				} else {
					g -= r;
				}
				Color myColor = new Color(r,g,0);
				c.setBackground(myColor);
				
			}
			return c;
		}
	}

}
