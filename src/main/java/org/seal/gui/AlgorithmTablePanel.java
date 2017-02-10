package org.seal.gui;

import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;

public class AlgorithmTablePanel extends GeneralTablePanel {
	private JPanel requestPanel;
	// test change here
	
	public AlgorithmTablePanel(Vector<Vector<Object>> data, String[] columnNames,
			int totalColumnCount, JPanel requestPanel) {
		super(data, columnNames, totalColumnCount);
		this.requestPanel = requestPanel;
		// TODO Auto-generated constructor stub
	}
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			int selectedRow = table.getSelectedRow();			
		     if (selectedRow >= 0) {
		    	Vector<Object> selected = super.tableModel.data.get(selectedRow);
		    	String request = selected.get(3).toString();
		    	System.out.println(request);
		    	requestPanel.removeAll();
		    	GeneralTablePanel gt = RequestTable.getRequestTable(request, false);
		    	RequestTable.setPreferredColumnWidths(gt, this.getSize().getWidth());
		    	requestPanel.add(gt);
		    	requestPanel.validate();
		    	requestPanel.updateUI();
		    	

		     }
		}
		validate();
		updateUI();
	}
	
}
