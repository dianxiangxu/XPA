package org.seal.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.IOUtils;
import org.seal.coverage.PolicySpreadSheetTestRecord;
import org.seal.policyUtils.PolicyLoader;
import org.seal.policyUtils.XpathSolver;
import org.seal.semanticCoverage.Coverage;
import org.seal.semanticCoverage.PolicyCoverageFactory;
import org.seal.semanticCoverage.TestSuite;
import org.seal.semanticFaultLocalization.SpectrumBasedDiagnosisResults;
import org.seal.semanticFaultLocalization.SpectrumBasedFaultLocalizer;
import org.seal.semanticMutation.Mutant;
import org.seal.semanticRepair.Repairer;
import org.seal.testGeneration.Demo;
import org.seal.xpa.util.FileIOUtil;
import org.seal.xpa.util.MutantDiff;
import org.seal.xpa.util.PropertiesLoader;


public class DebugPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private Demo xpa;
	private List<JRadioButton> faultLocalizationMethodRadioButtons;
	private JTextField depthField;
	private GeneralTablePanel tablePanel;
	
	
	public DebugPanel(Demo xpa) {
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
				}
			} else{
				JOptionPane.showMessageDialog(xpa, "There is no fault in this policy");
			}
		}
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
								Mutant mutant = repairer.repairMutant(faultyPolicy, testSuite, method, depth);
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
		for(PolicySpreadSheetTestRecord record: xpa.getTestPanel().getTestSuite().getTestRecord()){
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
		for(PolicySpreadSheetTestRecord record: xpa.getTestPanel().getTestSuite().getTestRecord()){
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
		radioPanel.setLayout(new GridLayout(methods.length/2+3, 2));
		radioPanel.setBorder(new TitledBorder(new EtchedBorder(), ""));
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
			JScrollPane scrollpane = new JScrollPane(tablePanel);
			add(scrollpane, BorderLayout.CENTER);
			xpa.setToDebugPane();
			xpa.updateMainTabbedPane();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
