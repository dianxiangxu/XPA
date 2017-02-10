package org.seal.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.seal.mutation.PolicyMutator;
import org.seal.mutation.PolicySpreadSheetMutantSuite;
import org.umu.editor.XMLFileFilter;

public class MutationPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	private XPA xpa;	
	private PolicySpreadSheetMutantSuite mutantSuite;
	private Vector<Vector<Object>> data;

	private GeneralTablePanel tablePanel;

	public MutationPanel(XPA xpa) {
		this.xpa = xpa;	
	}
	
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
	private JCheckBox boxFPR = new JCheckBox("First Permit Rules (FPR)");
	private JCheckBox boxFDR = new JCheckBox("First Deny Rules (FDR)");
	private JCheckBox boxRTR = new JCheckBox("Rule Type Replaced (RTR) - Not implemented");
	private JCheckBox boxFCF = new JCheckBox("Flip Comparison Function (FCF)");
	
	//private JCheckBox boxRUF = new JCheckBox("Remove Uniqueness Function (RUF) - Not Implemented"); Turner Lehmbecker
	
	private JCheckBox boxANF = new JCheckBox("Add Not Function (ANF)");
	private JCheckBox boxRNF = new JCheckBox("Remove Not Function (RNF)");
	private JCheckBox boxRPTE = new JCheckBox("Remove Parallel Target Element (RPTE)");
	private JCheckBox boxRPCE = new JCheckBox("Remove Parallel Condition Element (RPCE) - Not implemented");
	
	private JCheckBox boxSelectAll = new JCheckBox("Select All"); // All 13 types of mutation.
	private JCheckBox boxSelectEight = new JCheckBox("Select 8"); // 8 type (PTT, PTF, CRC, CRE, RTT, RTF, RCT, RCF)
	
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
		//myPanel.add(boxRUF);
		myPanel.add(boxSelectAll);
		myPanel.add(boxSelectEight);
		
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
		//boxANR.setSelected(selected); //temporarily not considered.
		boxRTT.setSelected(selected);
		boxRTF.setSelected(selected);
		boxRCT.setSelected(selected);
		boxRCF.setSelected(selected);
		boxFPR.setSelected(selected);
		boxFDR.setSelected(selected);
		//boxRTR.setSelected(selected); // Not implemented
		//boxFCF.setSelected(selected); // not applicable in our examples.
		boxANF.setSelected(selected);
		boxRNF.setSelected(selected);
		boxRPTE.setSelected(selected);
		//boxRPCE.setSelected(selected); // Not implemented
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
	
	public void setUpMutantPanel(){
		removeAll();
		setLayout(new BorderLayout());
		String[] columnNames = { "No", "Mutant Name", "Mutant File", "Bug Position", "Test Result" };
		try {
			data = mutantSuite.getMutantData();
			if (data.size() == 0) {
				JOptionPane.showMessageDialog(xpa, "There is no mutant!");
				return;
			}
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
	
	public void openMutants(){
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(false);
		if (xpa.getWorkingPolicyFile()!=null)
			fileChooser.setCurrentDirectory(xpa.getWorkingPolicyFile().getParentFile());
		fileChooser.setFileFilter(new XMLFileFilter("xls"));
		fileChooser.setDialogTitle("Open Mutants");
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File mutantSuiteFile = fileChooser.getSelectedFile();
			if (!mutantSuiteFile.toString().endsWith(".xls")) {
					JOptionPane.showMessageDialog(xpa,
							"The open File is not a mutant spreadsheet *.xls",
							"Error of Selection",
							JOptionPane.WARNING_MESSAGE);
			} else {
				try {
					mutantSuite = new PolicySpreadSheetMutantSuite(mutantSuiteFile.getAbsolutePath(), xpa.getWorkingPolicyFilePath());
					setUpMutantPanel();
				}
				catch(Exception e){
				}
			}
		}
	}

	public void generateMutants() {
		if (!xpa.hasWorkingPolicy()) {
			JOptionPane.showMessageDialog(xpa, "There is no policy.");
			return;
		}
		int result = JOptionPane.showConfirmDialog(xpa, createPanel(),
				"Please Select Mutation Methods",
				JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			try {
				PolicyMutator policyMutator = new PolicyMutator(
						xpa.getWorkingPolicyFilePath());
				if (boxPTT.isSelected()) {
					policyMutator.createPolicyTargetTrueMutants();
				}
				if (boxPTF.isSelected()) {
					policyMutator.createPolicyTargetFalseMutants();
				}
				if (boxCRC.isSelected()) {
					policyMutator.createCombiningAlgorithmMutants();
				}
				if (boxCRE.isSelected()) {
					policyMutator.createRuleEffectFlippingMutants();
				}
				if (boxRER.isSelected()) {
					policyMutator.createRemoveRuleMutants();
				}
				if (boxANR.isSelected()) {
					policyMutator.createAddNewRuleMutants();
				}
				if (boxRTT.isSelected()) {
					policyMutator.createRuleTargetTrueMutants();
				}
				if (boxRTF.isSelected()) {
					policyMutator.createRuleTargetFalseMutants();
				}
				if (boxRCT.isSelected()) {
					policyMutator.createRuleConditionTrueMutants();
				}
				if (boxRCF.isSelected()) {
					policyMutator.createRuleConditionFalseMutants();
				}
				if (boxFPR.isSelected()) {
					policyMutator.createFirstPermitRuleMutants();
				}
				if (boxFDR.isSelected()) {
					policyMutator.createFirstDenyRuleMutants();
				}
				if (boxRTR.isSelected()) {
					policyMutator.createRuleTypeReplacedMutants();
				}
				if (boxFCF.isSelected()) {
					policyMutator.createFlipComparisonFunctionMutants();
				}
				if (boxANF.isSelected()) {
					policyMutator.createAddNotFunctionMutants();
				}
				if (boxRNF.isSelected()) {
					policyMutator.createRemoveNotFunctionMutants();
				}
				if (boxRPTE.isSelected()) {
					policyMutator.createRemoveParallelTargetElementMutants();
				}
				if (boxRPCE.isSelected()) {
					policyMutator.createRemoveParallelConditionElementMutants();
				}
				mutantSuite = policyMutator.generateMutants(); // write to spreadsheet				
				setUpMutantPanel();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getMutationTestingResultsFileName(){
		return new File(xpa.getWorkingTestSuiteFileName()).getParent()+File.separator+"MutationTestingResults.xls";
	}

	public void testMutants(){
		if (mutantSuite==null) {
			JOptionPane.showMessageDialog(xpa, "There are no mutants.");
			return;
		}			
		if (!xpa.hasTests()){
			JOptionPane.showMessageDialog(xpa, "There are no tests.");
			return;
		}		
		try {
			String outputFileName = getMutationTestingResultsFileName();
			// Time this.
			final long startTime = System.currentTimeMillis();
			
			mutantSuite.runAndWriteDetectionInfoToExcelFile(outputFileName, xpa.getWorkingTestSuite());
			
			final long endTime = System.currentTimeMillis();
			System.out.println("Mutants testing time: " + (endTime - startTime)/1000.00 );
			
			mutantSuite.updateMutantTestResult(data);
			xpa.setToMutantPane();
			JOptionPane.showMessageDialog(xpa, "Mutation testing results are saved into file: \n" + outputFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
