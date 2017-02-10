package org.seal.gui;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;

public class TestTablePanel extends GeneralTablePanel {

	private JPanel requestPanel;

	public TestTablePanel(Vector<Vector<Object>> data, String[] columnNames,
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
		    	String request = selected.get(6).toString();
		    	requestPanel.removeAll();
		    	GeneralTablePanel gt = RequestTable.getRequestTable(request, false);
		    	gt.setMinRows(5);
		    	RequestTable.setPreferredColumnWidths(gt, this.getSize().getWidth());
		    	requestPanel.add(gt, BorderLayout.CENTER);
		    	requestPanel.validate();
		    	requestPanel.updateUI();
		    	

		     }
		}
		validate();
		updateUI();
	}
	
}
