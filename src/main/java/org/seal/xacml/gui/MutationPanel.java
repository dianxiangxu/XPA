package org.seal.xacml.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.seal.xacml.TestRecord;
import org.seal.xacml.components.MutationBasedTestMutationMethods;
import org.seal.xacml.components.ProgressBarUtil;
import org.seal.xacml.gui.DebugPanel.XPAthColumnCellRenderer;
import org.seal.xacml.mutation.PolicySpreadSheetMutantSuiteDemo;
import org.seal.xacml.policyUtils.PolicyLoader;
import org.seal.xacml.semanticCoverage.TestSuite;
import org.seal.xacml.semanticMutation.Mutant;
import org.seal.xacml.semanticMutation.Mutator;
import org.seal.xacml.utils.FileIOUtil;
import org.seal.xacml.utils.MutantUtil;
import org.seal.xacml.utils.PropertiesLoader;
import org.seal.xacml.utils.XACMLElementUtil;
import org.seal.xacml.utils.XMLUtil;
import org.seal.xacml.xpa.XPA;
import org.umu.editor.XMLFileFilter;
import org.w3c.dom.NodeList;
import org.wso2.balana.AbstractPolicy;

import sun.security.provider.certpath.Builder;

import com.sun.xml.internal.txw2.Document;




public class MutationPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	private XPA xpa;	
	private PolicySpreadSheetMutantSuiteDemo mutantSuite;
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
	private JCheckBox boxRCCF = new JCheckBox("Rule Change Comparition Function(RCCF)");
	private JCheckBox boxPCCF = new JCheckBox("Policy Target Change Comparition Function(PCCF)");
	 
	 
	private JTable table;
	private static int xPathCol;
	
	private JCheckBox boxFPR = new JCheckBox("First Permit Rules (FPR)");
	private JCheckBox boxFDR = new JCheckBox("First Deny Rules (FDR)");
	//private JCheckBox boxRTR = new JCheckBox("Rule Type Replaced (RTR) - Not implemented");
	//private JCheckBox boxFCF = new JCheckBox("Flip Comparison Function (FCF)");
	
	//private JCheckBox boxRUF = new JCheckBox("Remove Uniqueness Function (RUF) - Not Implemented"); Turner Lehmbecker
	
	private JCheckBox boxANF = new JCheckBox("Add Not Function (ANF)");
	private JCheckBox boxRNF = new JCheckBox("Remove Not Function (RNF)");
	//private JCheckBox boxRPTE = new JCheckBox("Remove Parallel Target Element (RPTE)");
	//private JCheckBox boxRPCE = new JCheckBox("Remove Parallel Condition Element (RPCE) - Not implemented");
	
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
		myPanel.add(boxRCCF);
		myPanel.add(boxPCCF);
		myPanel.add(boxFPR);
		myPanel.add(boxFDR);
		//myPanel.add(boxRTR);
		//myPanel.add(boxFCF);
		myPanel.add(boxANF);
		myPanel.add(boxRNF);
		//myPanel.add(boxRPTE);
		//myPanel.add(boxRPCE);
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
		boxRCCF.setSelected(selected);
		boxPCCF.setSelected(selected);
		boxFPR.setSelected(selected);
		boxFDR.setSelected(selected);
		//boxRTR.setSelected(selected); // Not implemented
		//boxFCF.setSelected(selected); // not applicable in our examples.
		boxANF.setSelected(selected);
		boxANR.setSelected(selected);
		boxRNF.setSelected(selected);
		//boxRPTE.setSelected(selected);
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
	
	public void setUpMutantPanel(List<Mutant> mutants){
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
			table.addMouseListener(new java.awt.event.MouseAdapter() {
			    @Override
			    public void mouseClicked(java.awt.event.MouseEvent evt) {
			        int row = table.rowAtPoint(evt.getPoint());
			        int targetCol = table.getColumn("Mutant File").getModelIndex();

			        int col = table.columnAtPoint(evt.getPoint());
			        if (row >= 0 && col ==targetCol) {
			            String xPathString = table.getValueAt( row, col).toString();
			            
			            String pathString = xpa.getWorkingPolicyFilePath() ;
			            
			            String splitString = pathString.substring(0,pathString.lastIndexOf("/"));
			            
			            String resultString = splitString  +  File.separator + PropertiesLoader.getProperties("config").getProperty("mutantsFolderName") + File.separator + xPathString ;			            			      
			            
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
					//mutantSuite = new PolicySpreadSheetMutantSuiteDemo(mutantSuiteFile.getAbsolutePath(), xpa.getWorkingPolicyFilePath());
					mutantSuite = new PolicySpreadSheetMutantSuiteDemo(mutantSuiteFile.getAbsolutePath(), MutantUtil.getPolicyName(mutantSuiteFile.toString()));
					
					setUpMutantPanel(mutantSuite.getMutantList());
				}
				catch(Exception e){
					JOptionPane.showMessageDialog(xpa, "Invalid mutant suite.");
				}
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
			try {
				File policyFile = xpa.getWorkingPolicyFile();
				AbstractPolicy policy = PolicyLoader.loadPolicy(policyFile);
		        Mutator mutator = new Mutator(new Mutant(policy, XACMLElementUtil.getPolicyName(policyFile)));
		        List<Mutant> mutants = mutator.generateSelectedMutants(mutPanel.getMutationOperatorList());
				
		        File mutantsFolder = new File(MutantUtil.getMutantsFolderForPolicyFile(policyFile).toString());
		        if(mutantsFolder.exists()){
		        	FileUtils.cleanDirectory(mutantsFolder);
		        } else{
		        	mutantsFolder.mkdir();
		        }
		        for(Mutant mutant: mutants){
					FileIOUtil.saveMutant(mutant,mutantsFolder.toString());
				}
				mutantSuite = new PolicySpreadSheetMutantSuiteDemo(mutantsFolder.toString(),mutants,XACMLElementUtil.getPolicyName(policyFile)); // write to spreadsheet		
				mutantSuite.writePolicyMutantsSpreadSheet(mutants,XACMLElementUtil.getPolicyName(policyFile) + "_mutants.xls");
				setUpMutantPanel(mutants);
				
			
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		        List<Mutant> mutants1 = mutator.generateSelectedMutants(mutationOperators1.getMutationOperatorList());
				List<Mutant> mutants2 = new ArrayList<Mutant>();
				int secondOrderOperatorsLength = mutationOperators2.getMutationOperatorList().size();
		        int max = mutants1.size() * secondOrderOperatorsLength;
		        int currentValue = 0;
		        ProgressBarUtil pb = new ProgressBarUtil();
				for(Mutant mutant:mutants1) {
		        	 Mutator secondOrderMutator = new Mutator(new Mutant(mutant.getPolicy(), mutant.getName()));
				     List<Mutant> mutants = secondOrderMutator.generateSelectedMutants(mutationOperators2.getMutationOperatorList());
				     for(Mutant m: mutants) {
				    	 for(int fault:mutant.getFaultLocations()) {
				    		 m.addFaultLocationAt(fault, 0);
				    	}
				    }
				    currentValue += secondOrderOperatorsLength;
				        pb.showProgress(currentValue-1, max); 

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
				mutantSuite = new PolicySpreadSheetMutantSuiteDemo(mutantsFolder.toString(),mutants2,XACMLElementUtil.getPolicyName(policyFile)); // write to spreadsheet		
				mutantSuite.writePolicyMutantsSpreadSheet(mutants2,XACMLElementUtil.getPolicyName(policyFile) + "_mutants.xls");
				setUpMutantPanel(mutants2);
				pb.close();
			
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
			
			List<String> requests = new ArrayList<String>();
			List<String> oracles = new ArrayList<String>();
			for(TestRecord record: xpa.getTestPanel().getTestSuite().getTestRecords()){
				requests.add(record.getRequest());
				oracles.add(record.getOracle());
			}
			TestSuite testSuite = new TestSuite(null,requests, oracles);
			
						
			final long endTime = System.currentTimeMillis();
			System.out.println("Mutants testing time: " + (endTime - startTime)/1000.00 );
			mutantSuite.updateMutantTestResult(data,testSuite);
			mutantSuite.writeDetectionInfoToExcelFile(outputFileName, testSuite);
			
			xpa.setToMutantPane();
			xpa.updateMainTabbedPane();
			JOptionPane.showMessageDialog(xpa, "Mutation testing results are saved into file: \n" + outputFileName);
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
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
}


