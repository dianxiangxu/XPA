package org.seal.gui;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.seal.combiningalgorithms.PolicyX;
import org.seal.combiningalgorithms.loadPolicy;
import org.wso2.balana.Policy;

public class AnalysisPanel extends JPanel{
	private XPA xpa;
	Vector<Vector<Object>> data;
	AlgorithmTablePanel tablePanel;
	JPanel requestPanel;
	
	public AnalysisPanel(XPA xpa) {
		this.xpa = xpa;
		setLayout(new BorderLayout());
		//
	}
	
	public void mutateCombiningAlgorithms(){
		if (!xpa.hasWorkingPolicy()) {
            JOptionPane.showMessageDialog(xpa, "There is no policy!");
			return;
		}
		try {
			
			loadPolicy lp = new loadPolicy();
			Policy policy = lp.getPolicy(xpa.getWorkingPolicyFilePath());
			System.out.println(xpa.getWorkingPolicyFilePath());
			String currentAlg = policy.getCombiningAlg().toString();
			System.out.println(currentAlg);
			if(currentAlg.contains("FirstApplicable")){
				currentAlg = "first-applicalbe";
			}else if(currentAlg.contains("PermitOverrides")){
				currentAlg = "permit-overrides";
			}else if(currentAlg.contains("DenyOverrides")){
				currentAlg = "deny-overrides";
			}else if(currentAlg.contains("DenyUnlessPermit")){
				currentAlg = "deny-unless-permit";
			}else{
				currentAlg = "permit-unless-deny";
			}
			PolicyX policyx = new PolicyX(policy);
			Vector<Vector<Object>> temp = new Vector<Vector<Object>>();

			temp = policyx.generateRequestForDifferenceRCAs();
			System.out.println("temp size" + temp.size());
			data = convertStrut(temp);
			//System.out.println(temp.size() + "datasize");
			Vector<Object> selected = data.get(0);
			String request = selected.get(3).toString();
			String[] columnNames = { "Compared to", "Result",currentAlg +  " result"
					 };
			
	    	requestPanel = new JPanel();
	    	requestPanel.setLayout(new BorderLayout());
	    	GeneralTablePanel gt = RequestTable.getRequestTable(request, false);
	    	RequestTable.setPreferredColumnWidths(gt, this.getSize().getWidth());
	    	requestPanel.add(gt);
			tablePanel = new AlgorithmTablePanel(data, columnNames, 3, requestPanel);
			
			JScrollPane scrollpane = new JScrollPane(tablePanel);
			JSplitPane jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

			jSplitPane.setTopComponent(scrollpane);
			jSplitPane.setBottomComponent(requestPanel);
			jSplitPane.setResizeWeight(0.7);
			add(jSplitPane, BorderLayout.CENTER);
		} catch (Exception e) {

		}
	}
	
	public Vector<Vector<Object>> convertStrut(Vector<Vector<Object>> input){
		Vector<Vector<Object>> output = new Vector<Vector<Object>>();
		for(Vector<Object> child : input){
			Vector<Object> data = new Vector<Object>();
			data.add(child.get(2).toString());
			data.add(child.get(3).toString());
			data.add(child.get(1).toString());
			data.add(child.get(4));
			output.add(data);
		}
		
		return output;
	}

	public void mutateRules(){
		if (!xpa.hasWorkingPolicy()) {
            JOptionPane.showMessageDialog(xpa, "There is no policy!");
			return;
		}
		
	}


}
