package org.seal.xacml.components;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class MutationBasedTestMutationMethods {
	private JCheckBox boxPTT;
	private JCheckBox boxPTF;
	private JCheckBox boxCRE;
	private JCheckBox boxRER;
	private JCheckBox boxRTT;
	private JCheckBox boxRTF;
	private JCheckBox boxRCT;
	private JCheckBox boxRCF;
	private JCheckBox boxFPR;
	private JCheckBox boxFDR;
	private JCheckBox boxANF;
	private JCheckBox boxRNF;
	private JCheckBox boxRPTE;
	private JCheckBox boxCCA;
	//private JCheckBox boxRCCF;
	//private JCheckBox boxPCCF;
	private JCheckBox boxSelectM8;
	private JCheckBox boxSelectAll;
	
	
	public JCheckBox getBoxPTT() {
		return boxPTT;
	}


	public void setBoxPTT(JCheckBox boxPTT) {
		this.boxPTT = boxPTT;
	}


	public JCheckBox getBoxPTF() {
		return boxPTF;
	}


	public void setBoxPTF(JCheckBox boxPTF) {
		this.boxPTF = boxPTF;
	}

	public JCheckBox getBoxCRE() {
		return boxCRE;
	}


	public void setBoxCRE(JCheckBox boxCRE) {
		this.boxCRE = boxCRE;
	}


	public JCheckBox getBoxRER() {
		return boxRER;
	}


	public void setBoxRER(JCheckBox boxRER) {
		this.boxRER = boxRER;
	}


	public JCheckBox getBoxRTT() {
		return boxRTT;
	}


	public void setBoxRTT(JCheckBox boxRTT) {
		this.boxRTT = boxRTT;
	}


	public JCheckBox getBoxRTF() {
		return boxRTF;
	}


	public void setBoxRTF(JCheckBox boxRTF) {
		this.boxRTF = boxRTF;
	}


	public JCheckBox getBoxRCT() {
		return boxRCT;
	}


	public void setBoxRCT(JCheckBox boxRCT) {
		this.boxRCT = boxRCT;
	}


	public JCheckBox getBoxRCF() {
		return boxRCF;
	}


	public void setBoxRCF(JCheckBox boxRCF) {
		this.boxRCF = boxRCF;
	}


	public JCheckBox getBoxFPR() {
		return boxFPR;
	}


	public void setBoxFPR(JCheckBox boxFPR) {
		this.boxFPR = boxFPR;
	}


	public JCheckBox getBoxFDR() {
		return boxFDR;
	}


	public void setBoxFDR(JCheckBox boxFDR) {
		this.boxFDR = boxFDR;
	}


	public JCheckBox getBoxANF() {
		return boxANF;
	}


	public void setBoxANF(JCheckBox boxANF) {
		this.boxANF = boxANF;
	}


	public JCheckBox getBoxRNF() {
		return boxRNF;
	}


	public void setBoxRNF(JCheckBox boxRNF) {
		this.boxRNF = boxRNF;
	}


	public JCheckBox getBoxRPTE() {
		return boxRPTE;
	}


	public void setBoxRPTE(JCheckBox boxRPTE) {
		this.boxRPTE = boxRPTE;
	}

	public JCheckBox getBoxCCA() {
		return boxCCA;
	}


	public void setBoxCCA(JCheckBox boxCCA) {
		this.boxCCA = boxCCA;
	}

//	public JCheckBox getBoxRCCF() {
//		return boxRCCF;
//	}
//
//
//	public void setBoxRCCF(JCheckBox boxRCCF) {
//		this.boxRCCF = boxRCCF;
//	}
//
//	public JCheckBox getBoxPCCF() {
//		return boxPCCF;
//	}
//
//	public void setBoxPCCF(JCheckBox boxPCCF) {
//		this.boxPCCF = boxPCCF;
//	}
	
	public JCheckBox getBoxSelectAll() {
		return boxSelectAll;
	}


	public void setBoxSelectAll(JCheckBox boxSelectAll) {
		this.boxSelectAll = boxSelectAll;
	}
	
	public List<JCheckBox> getAllBoxes() {
		List<JCheckBox> boxes = new ArrayList<JCheckBox>();
		boxes.add(boxPTT);
		boxes.add(boxPTF);
		boxes.add(boxCCA);
		boxes.add(boxCRE);
		boxes.add(boxRTT);
		boxes.add(boxRTF);
		boxes.add(boxRCT);
		boxes.add(boxRCF);
		boxes.add(boxRER);
		boxes.add(boxFPR);
		boxes.add(boxFDR);
		boxes.add(boxANF);
		boxes.add(boxRNF);
//		boxes.add(boxRCCF);
//		boxes.add(boxPCCF);
		boxes.add(boxRPTE);
		boxes.add(boxSelectM8);
		boxes.add(boxSelectAll);
		return boxes;
	}


	public MutationBasedTestMutationMethods(){
		boxPTT = new JCheckBox("Policy Target True (PTT)");
		boxPTF = new JCheckBox("Policy Target False (PTF)");
		boxCCA = new JCheckBox("Change Combining Algorithm (CCA)");
		boxCRE = new JCheckBox("Flip Rule Effect (CRE)");
		boxRTT = new JCheckBox("Rule Target True (RTT)");
		boxRTF = new JCheckBox("Rule Target False (RTF)");
		boxRCT = new JCheckBox("Rule Condition True (RCT)");
		boxRCF = new JCheckBox("Rule Condition False (RCF)");
		boxRER = new JCheckBox("Remove One Rule (RER)");
		boxFPR = new JCheckBox("First Permit Rules (FPR)");
		boxFDR = new JCheckBox("First Deny Rules (FDR)");
		boxANF = new JCheckBox("Add Not Function (ANF)");
		boxRNF = new JCheckBox("Remove Not Function (RNF)");
//		boxRCCF = new JCheckBox("Rule Change Comparision Function (RCCF)");
//		boxPCCF = new JCheckBox("Policy Change Comparision Function (PCCF)");
		boxRPTE = new JCheckBox("Remove Parallel Target Element (RPTE)");
		boxSelectAll = new JCheckBox("Select All");
		boxSelectM8 = new JCheckBox("Select Eight(M8)");
		boxSelectAll.addActionListener(new ActionListener() {		 
			@Override
			public void actionPerformed(ActionEvent e) {
				if (boxSelectAll.isSelected())
		        	setAllIndividualBoxes(true);
		        else
		        	setAllIndividualBoxes(false);			
			}
        });
		
		boxSelectM8.addActionListener(new ActionListener() {		 
			@Override
			public void actionPerformed(ActionEvent e) {
				if (boxSelectM8.isSelected())
		        	setM8Boxes(true);
		        else
		        	setM8Boxes(false);			
			}
        });
		
		
	}
	
	public List<String> getMutationOperatorList(boolean filter){
		boolean flag = false;
		List<String> lst = new ArrayList<String>();
		if (boxCRE.isSelected()) {
			if(!filter||!flag)
			lst.add("createRuleEffectFlippingMutants");
			flag = true;
		}
		if (boxRER.isSelected()) {
			if(!filter||!flag)
			lst.add("createRemoveRuleMutants");
			flag = true;

		}
		if (boxRTT.isSelected()) {
			lst.add("createRuleTargetTrueMutants");
		}
		if (boxRTF.isSelected()) {
			
			if(!filter||!flag)
				
			lst.add("createRuleTargetFalseMutants");
			flag = true;
		}
		if (boxRCT.isSelected()) {
			lst.add("createRuleConditionTrueMutants");
		}
		if (boxRCF.isSelected()) {
			if(!filter||!flag)
				
			lst.add("createRuleConditionFalseMutants");
			flag = true;

		}
		if (boxFPR.isSelected()) {
			lst.add("createFirstPermitRuleMutants");
		}
		if (boxFDR.isSelected()) {
			lst.add("createFirstDenyRuleMutants");
		}
		if (boxANF.isSelected()) {
			if(!filter||!flag)
				
			lst.add("createAddNotFunctionMutants");
			flag = true;

		}
		if (boxRNF.isSelected()) {
			if(!filter||!flag)
				
			lst.add("createRemoveNotFunctionMutants");
			flag = true;

		}
		if (boxCCA.isSelected()) {
			lst.add("createCombiningAlgorithmMutants");
		}
//		if (boxRCCF.isSelected()) {
//			lst.add("createRuleChangeComparisonFunctionMutants");
//		}
//		if (boxPCCF.isSelected()) {
//			lst.add("createPolicyTargetChangeComparisonFunctionMutants");
//		}
		if (boxRPTE.isSelected()) {
			lst.add("createRemoveParallelTargetElementMutants");
		}
		if (boxPTT.isSelected()) {
			lst.add("createPolicyTargetTrueMutants");
		}
		if (boxPTF.isSelected()) {
			lst.add("createPolicyTargetFalseMutants");
		}
		
		return lst;
	}
	
	public void setAllIndividualBoxes(boolean selected) {
		boxPTT.setSelected(selected);
		boxPTF.setSelected(selected);
		boxCCA.setSelected(selected);
		boxCRE.setSelected(selected);
		boxRTT.setSelected(selected);
		boxRTF.setSelected(selected);
		boxRCT.setSelected(selected);
		boxRCF.setSelected(selected);
		boxRER.setSelected(selected);
		
		boxFPR.setSelected(selected);
		boxFDR.setSelected(selected);
		boxANF.setSelected(selected);
		boxRNF.setSelected(selected);
		boxRPTE.setSelected(selected);
//		boxRCCF.setSelected(selected);
//		boxPCCF.setSelected(selected);
		
		boxSelectAll.setSelected(selected);
	}
	
	public void setM8Boxes(boolean selected) {
		setAllIndividualBoxes(false);
		boxPTT.setSelected(selected);
		boxPTF.setSelected(selected);
		boxCCA.setSelected(selected);
		boxCRE.setSelected(selected);
		boxRTT.setSelected(selected);
		boxRTF.setSelected(selected);
		boxRCT.setSelected(selected);
		boxRCF.setSelected(selected);
		
		
	}
	

	
	public JPanel createPanel() {
		setAllIndividualBoxes(true);
		
		boxSelectAll.setSelected(true);
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(13, 2));
		panel.add(boxPTT);
		panel.add(boxPTF);
		panel.add(boxCRE);
		panel.add(boxRER);
		panel.add(boxRTT);
		panel.add(boxRTF);
		panel.add(boxRCT);
		panel.add(boxRCF);
		panel.add(boxFPR);
		panel.add(boxFDR);
		panel.add(boxANF);
		panel.add(boxRNF);
		panel.add(boxRPTE);
		panel.add(boxCCA);
//		panel.add(boxRCCF);
//		panel.add(boxPCCF);
		panel.add(boxSelectM8);
		panel.add(boxSelectAll);
		panel.setBorder(new TitledBorder(new EtchedBorder(), ""));
		return panel;
	}

}
