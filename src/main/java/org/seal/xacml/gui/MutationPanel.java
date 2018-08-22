package org.seal.xacml.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.io.FileUtils;
import org.seal.xacml.NameDirectory;
import org.seal.xacml.TaggedRequest;
import org.seal.xacml.TestRecord;
import org.seal.xacml.components.JPanelPB;
import org.seal.xacml.components.MutationBasedTestMutationMethods;
import org.seal.xacml.mutation.MutationBasedTestGenerator;
import org.seal.xacml.mutation.PolicySpreadSheetMutantSuite;
import org.seal.xacml.policyUtils.PolicyLoader;
import org.seal.xacml.semanticCoverage.PolicyRunner;
import org.seal.xacml.semanticCoverage.TestSuite;
import org.seal.xacml.semanticMutation.Mutant;
import org.seal.xacml.semanticMutation.Mutator;
import org.seal.xacml.utils.FileIOUtil;
import org.seal.xacml.utils.MiscUtil;
import org.seal.xacml.utils.MutantUtil;
import org.seal.xacml.utils.PropertiesLoader;
import org.seal.xacml.utils.XACMLElementUtil;
import org.seal.xacml.xpa.XPA;
import org.umu.editor.XMLFileFilter;
import org.wso2.balana.AbstractPolicy;


public class MutationPanel extends JPanelPB {
	
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
//	private JCheckBox boxRCCF = new JCheckBox("Rule Change Comparition Function(RCCF)");
//	private JCheckBox boxPCCF = new JCheckBox("Policy Target Change Comparition Function(PCCF)");
//	 
	 
	
	private JCheckBox boxFPR = new JCheckBox("First Permit Rules (FPR)");
	private JCheckBox boxFDR = new JCheckBox("First Deny Rules (FDR)");
	private JCheckBox boxANF = new JCheckBox("Add Not Function (ANF)");
	private JCheckBox boxRNF = new JCheckBox("Remove Not Function (RNF)");
	private JCheckBox boxSelectM8 = new JCheckBox("Select Eight(M8)");
	
	private JCheckBox boxRPTE = new JCheckBox("Remove Parallel Target Element (RPTE)");
	
	private JCheckBox boxSelectAll = new JCheckBox("Select All"); 
	
	private JTable table;
	private static int xPathCol;
	
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
		
		boxSelectM8.addActionListener(new ActionListener() {		 
			@Override
			public void actionPerformed(ActionEvent e) {
				if (boxSelectM8.isSelected())
		        	setM8Boxes(true);
		        else
		        	setM8Boxes(false);			
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
//		myPanel.add(boxRCCF);
//		myPanel.add(boxPCCF);
		myPanel.add(boxFPR);
		myPanel.add(boxFDR);
		myPanel.add(boxANF);
		myPanel.add(boxRNF);
		myPanel.add(boxRPTE);
		myPanel.add(boxSelectM8);
		
		myPanel.add(boxSelectAll);
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
		boxRTT.setSelected(selected);
		boxRTF.setSelected(selected);
		boxRCT.setSelected(selected);
		boxRCF.setSelected(selected);
//		boxRCCF.setSelected(selected);
//		boxPCCF.setSelected(selected);
		boxFPR.setSelected(selected);
		boxFDR.setSelected(selected);
		boxANF.setSelected(selected);
		boxANR.setSelected(selected);
		boxRNF.setSelected(selected);
		boxRPTE.setSelected(selected);
		boxSelectAll.setSelected(selected);
	}
	
		private void setM8Boxes(boolean selected) {
			
			boxSelectAll.setSelected(false);
			boxPTT.setSelected(selected);
			boxPTF.setSelected(selected);
			boxCRC.setSelected(selected);
			boxCRE.setSelected(selected);
			boxRTT.setSelected(selected);
			boxRTF.setSelected(selected);
			boxRCT.setSelected(selected);
			boxRCF.setSelected(selected);
			
		}
	public void setUpMutantPanel(List<Mutant> mutants, String mutantsFolder){
		removeAll();
		setLayout(new BorderLayout());
		String[] columnNames = { "No", "Mutant Name", "Mutant File", "Bug Position", "Test Result" };
		try {
			//data = mutantSuite.getMutantData();
			data = MutantUtil.getVectorsForMutants(mutants);
			
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
			
			
			table = tablePanel.getTable();
			xPathCol = table.getColumn("Mutant File").getModelIndex();
			
			table.getColumnModel().getColumn(xPathCol).setCellRenderer(new XPAthColumnCellRenderer());
			final String folder = mutantsFolder;
			table.addMouseListener(new java.awt.event.MouseAdapter() {
			    @Override
			    public void mouseClicked(java.awt.event.MouseEvent evt) {
			        int row = table.rowAtPoint(evt.getPoint());
			        int targetCol = table.getColumn("Mutant File").getModelIndex();

			        int col = table.columnAtPoint(evt.getPoint());
			        if (row >= 0 && col ==targetCol) {
			            String xPathString = table.getValueAt( row, col).toString();
			            
			            String pathString = xpa.getWorkingPolicyFilePath() ;
			            String splitString = null;
			            String resultString = null;
			            if(pathString!=null) {
			            	splitString = pathString.substring(0,pathString.lastIndexOf("/"));
			            	resultString = splitString  +  File.separator + folder + File.separator + xPathString ;
			            } else {
			            	resultString = mutantSuite.getMutantsDirectory() + File.separator + xPathString ;
			            }
			            
			            StringBuilder sb = new StringBuilder();
			            try (BufferedReader br = new BufferedReader(new FileReader(resultString))) {
			    			String sCurrentLine;
			    			while ((sCurrentLine = br.readLine()) != null) {
			    				sb.append(sCurrentLine).append("\n");
			    				sCurrentLine = br.readLine();
			    			}
			    		} catch (IOException e) {
			    			e.printStackTrace();
			    		} 
			            PopupFrame.showContent("Element at "+xPathString, sb.toString());

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
			
			
		} catch (Exception e) {

		}
	}
	public String readFile(String path) throws IOException{
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))){
        	String sCurrentLine;
        	while ((sCurrentLine = br.readLine()) != null) {
        		sb.append(sCurrentLine);
        	}

        }
      return sb.toString();
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
		else {
			fileChooser.setCurrentDirectory((new File(".")));
		}
		
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
				this.startProgressStatus();
				try {
					//mutantSuite = new PolicySpreadSheetMutantSuiteDemo(mutantSuiteFile.getAbsolutePath(), xpa.getWorkingPolicyFilePath());
					mutantSuite = new PolicySpreadSheetMutantSuite(mutantSuiteFile.getAbsolutePath(), MutantUtil.getPolicyName(mutantSuiteFile.toString()));
					String folder;
					if(mutantSuiteFile.getAbsolutePath().indexOf("secondOrderMutants") == -1) {
						folder = PropertiesLoader.getProperties("config").getProperty("mutantsFolderName");
					} else {
						folder = PropertiesLoader.getProperties("config").getProperty("secondOrderMutantsFolderName");
					}
					setUpMutantPanel(mutantSuite.getMutantList(), folder);
				}
				catch(Exception e){
					JOptionPane.showMessageDialog(xpa, "Invalid mutant suite.");
				}
				this.stopProgressStatus();
			}
		}
	}

	public void generateMutants() {
		if (!xpa.hasWorkingPolicy()) {
			JOptionPane.showMessageDialog(xpa, "There is no policy.");
			return;
		}
		MutationBasedTestMutationMethods mutPanel = new MutationBasedTestMutationMethods();
		
		int result = JOptionPane.showConfirmDialog(xpa, mutPanel.createPanel(),"Please Select Mutation Methods",JOptionPane.OK_CANCEL_OPTION);
		Map<String,String> mutantOperators = new HashMap<String,String>();
		if (result == JOptionPane.OK_OPTION) {
			this.startProgressStatus();
			try {
				File policyFile = xpa.getWorkingPolicyFile();
				AbstractPolicy policy = PolicyLoader.loadPolicy(policyFile);
				List<Mutant> mutants = new ArrayList<Mutant>();
		        Mutator mutator = new Mutator(new Mutant(policy, XACMLElementUtil.getPolicyName(policyFile)));
		        File mutantsFolder = new File(MutantUtil.getMutantsFolderForPolicyFile(policyFile).toString());
		        if(mutantsFolder.exists()){
		        	FileUtils.cleanDirectory(mutantsFolder);
		        } else{
		        	mutantsFolder.mkdir();
		        }
		        MutationBasedTestGenerator testGenerator = new MutationBasedTestGenerator(xpa.getWorkingPolicyFilePath());
				MutationBasedTestMutationMethods mbtMethods = new MutationBasedTestMutationMethods();
				String policyFilePath = xpa.getWorkingPolicyFilePath();
				//this.startProgressStatus();
				List<String> mutationMethods = new ArrayList<String>();
				mutationMethods.add("createCombiningAlgorithmMutants");
				List<TaggedRequest> taggedRequests = testGenerator.generateRequests(mutationMethods);
				AbstractPolicy p = PolicyLoader.loadPolicy(xpa.getWorkingPolicyFile());
				List<Mutant> tR = new ArrayList<Mutant>();
		        
		        for(String method:mutPanel.getMutationOperatorList(false)) {
		        	List<String> methods = new ArrayList<String>();
		        	methods.add(method);
			        List<Mutant> muts = mutator.generateSelectedMutants(methods);
			        for(Mutant mutant: muts){
			        	if(methods.get(0).equals("createCombiningAlgorithmMutants")) {
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
			        muts.removeAll(tR);
			        mutants.addAll(muts);
		        }
				mutantSuite = new PolicySpreadSheetMutantSuite(mutantsFolder.toString(),mutants,XACMLElementUtil.getPolicyName(policyFile)); // write to spreadsheet		
				mutantSuite.writePolicyMutantsSpreadSheet(mutants,XACMLElementUtil.getPolicyName(policyFile) + "_mutants.xls");
				setUpMutantPanel(mutants, PropertiesLoader.getProperties("config").getProperty("mutantsFolderName"));
		        
			
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.stopProgressStatus();
		}
		
	}
	
	public void generateSecondOrderMutants() {
		
		if (!xpa.hasWorkingPolicy()) {
			JOptionPane.showMessageDialog(xpa, "There is no policy.");
			return;
		}
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(24, 2));
		JLabel blankLbl = new JLabel("");
		JLabel label = new JLabel("Select First Order Mutation Operators");
		panel.add(label);
		panel.add(blankLbl);
		this.startProgressStatus();
		MutationBasedTestMutationMethods mutationOperators1 = new MutationBasedTestMutationMethods();
		for(JCheckBox box: mutationOperators1.getAllBoxes()) {
			panel.add(box);
		}
		mutationOperators1.setAllIndividualBoxes(true);

		JLabel blankLbl2 = new JLabel("");
		JLabel blankLbl3 = new JLabel("");
		JLabel blankLbl4 = new JLabel("");
		
		JLabel label2 = new JLabel("Select Second Order Mutation Operators");
		panel.add(blankLbl2);
		panel.add(blankLbl3);
		
		panel.add(label2);
		panel.add(blankLbl4);
		
		MutationBasedTestMutationMethods mutationOperators2 = new MutationBasedTestMutationMethods();
		for(JCheckBox box: mutationOperators2.getAllBoxes()) {
			panel.add(box);
		}
		mutationOperators2.setAllIndividualBoxes(true);
		
		int result = JOptionPane.showConfirmDialog(xpa, panel,"Please Select Mutation Methods",JOptionPane.OK_CANCEL_OPTION);
		Map<String,String> mutantOperators = new HashMap<String,String>();
		Random rand = new Random();
		if (result == JOptionPane.OK_OPTION) {
			try {
				File policyFile = xpa.getWorkingPolicyFile();
				AbstractPolicy policy = PolicyLoader.loadPolicy(policyFile);
		        Mutator mutator = new Mutator(new Mutant(policy, XACMLElementUtil.getPolicyName(policyFile)));
		        List<Mutant> mutants1 = mutator.generateSelectedMutants(mutationOperators1.getMutationOperatorList(false));
				List<Mutant> mutants2 = new ArrayList<Mutant>();
				int secondOrderOperatorsLength = mutationOperators2.getMutationOperatorList(false).size();
		        int max = mutants1.size() * secondOrderOperatorsLength;
		        int currentValue = 0;
		       for(Mutant mutant:mutants1) {
		        	 Mutator secondOrderMutator = new Mutator(new Mutant(mutant.getPolicy(), mutant.getName()));
				     List<Mutant> mutants = secondOrderMutator.generateSelectedMutants(mutationOperators2.getMutationOperatorList(false));
				     for(Mutant m: mutants) {
				    	 for(int fault:mutant.getFaultLocations()) {
				    		 m.addFaultLocationAt(fault, 0);
				    	}
				    }
				    currentValue += secondOrderOperatorsLength;

				    mutants2.addAll(mutants);	
				}
		        File mutantsFolder = new File(MutantUtil.getSecondOrderMutantsFolderForPolicyFile(policyFile).toString());
		        if(mutantsFolder.exists()){
		        	FileUtils.cleanDirectory(mutantsFolder);
		        } else{
		        	mutantsFolder.mkdir();
		        }
		        for(Mutant mutant: mutants2){
					FileIOUtil.saveMutant(mutant,mutantsFolder.toString());
				}
				mutantSuite = new PolicySpreadSheetMutantSuite(mutantsFolder.toString(),mutants2,XACMLElementUtil.getPolicyName(policyFile)); // write to spreadsheet		
				mutantSuite.writePolicyMutantsSpreadSheet(mutants2,XACMLElementUtil.getPolicyName(policyFile) + "_mutants.xls");
				setUpMutantPanel(mutants2, PropertiesLoader.getProperties("config").getProperty("secondOrderMutantsFolderName"));
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.stopProgressStatus();
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
			this.startProgressStatus();
			String outputFileName = getMutationTestingResultsFileName();
			List<String> requests = new ArrayList<String>();
			List<String> oracles = new ArrayList<String>();
			for(TestRecord record: xpa.getTestPanel().getPolicyTestSuite().getTestRecords()){
				requests.add(record.getRequest());
				oracles.add(record.getOracle());
			}
			TestSuite testSuite = new TestSuite(null,requests, oracles);
			File policyFile = xpa.getWorkingPolicyFile();
			
			File mutantsFolder = new File(MutantUtil.getMutantsFolderForPolicyFile(policyFile).toString());
		      
			int killedCount = mutantSuite.updateMutantTestResult(data,testSuite,mutantsFolder);
			mutantSuite.writeDetectionInfoToExcelFile(outputFileName, testSuite,mutantsFolder);
			this.stopProgressStatus();
			xpa.setToMutantPane();
			xpa.updateMainTabbedPane();
			int total = mutantSuite.getMutantList().size();
			int liveCount = total - killedCount;
			double ratio = killedCount / (double)total;
			String message = "Number of killed mutants : " + killedCount + System.lineSeparator();
			message += "Number of live mutants : " + liveCount + System.lineSeparator();
			message += "Mutation Score : " + (MiscUtil.roundNumberToTwoDecimalPlaces(ratio*100))+ "%" + System.lineSeparator()  + System.lineSeparator();
			message += "Mutation testing results are saved into file: \n" + outputFileName;
			JOptionPane.showMessageDialog(xpa, message, "Test Result" , JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static class XPAthColumnCellRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row,int column) {
			Component c = super.getTableCellRendererComponent(table, value,isSelected, hasFocus, row, column);
			if (column == xPathCol) {
				c.setForeground(Color.blue);
			}
			return c;
		}
	}
}


