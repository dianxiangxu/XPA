/* 	
	Author Dianxiang Xu
 */
package org.seal.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class GeneralTablePanel extends JPanel implements ActionListener,
		ListSelectionListener {
	private static final long serialVersionUID = 1L;

	private static final String insertRowBefore = "Insert Row Before";
	private static final String insertRowAfter = "Insert Row After";
	private static final String deleteRow = "Delete Row";
	private static final String adjustRowHeight = "Adjust Row Heights";

	protected GeneralTableModel tableModel;
	protected JTable table;

	private TextAreaCellEditor tableCellEditor;

	boolean isEditing = true;

	public GeneralTablePanel(Vector<Vector<Object>> data, String[] columnNames,
			int totalColumnCount) {

		tableModel = new GeneralTableModel(data, columnNames, totalColumnCount,
				isEditing);
		table = System.getProperty("os.name").contains("Mac") ? new JTableMac(
				tableModel) : new JTable(tableModel);

		// align headers of all columns
		TableCellRenderer rendererFromHeader = table.getTableHeader()
				.getDefaultRenderer();
		JLabel headerLabel = (JLabel) rendererFromHeader;
		headerLabel.setHorizontalAlignment(JLabel.CENTER);

		// align the header of first column
		/*
		 * DefaultTableCellHeaderRenderer headerRenderer = new
		 * DefaultTableCellHeaderRenderer();
		 * headerRenderer.setHorizontalAlignment(JLabel.CENTER);
		 * table.getColumnModel
		 * ().getColumn(0).setHeaderRenderer(headerRenderer);
		 */
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setDefaultRenderer(String.class, new TextAreaCellRenderer());
		table.setFillsViewportHeight(true);
		table.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
					// evt.consume();
					if (table.getSelectedRow() >= table.getRowCount() - 1
							&& !tableModel.hasEnoughEmptyRowsAtBottom())
						tableModel.insertRow(table.getRowCount());
				}
			}
		});

		/*
		 * // double click to adjust the row height table.addMouseListener( new
		 * MouseAdapter() { public void mouseClicked(MouseEvent e) { if
		 * (e.getClickCount() == 2 && !e.isConsumed()) { e.consume(); int
		 * selectedRow = table.getSelectedRow(); if (selectedRow >= 0){
		 * setToPreferredRowHeight(selectedRow); } } } });
		 */

		tableCellEditor = new TextAreaCellEditor(table.getFont(), null);
		table.setDefaultEditor(Object.class, tableCellEditor);

		// if (tableType==MIDTableType.OBJECT){
		// TableColumn modelColumn = table.getColumnModel().getColumn(1);
		// modelColumn.setCellEditor(new TextAreaCellEditor(table.getFont(),
		// editor.getModelPanel().getChoicesForMIMEntry(tableType)));
		// }

		if (isEditing)
			setupPopupMenu();
		ListSelectionModel listMod = table.getSelectionModel();
		listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listMod.addListSelectionListener(this);

		// align first column center
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

		setLayout(new BorderLayout());
		add(new JScrollPane(table), BorderLayout.CENTER);
		//setPreferredColumnWidths();
	}

	public JTable getTable() {
		return table;
	}

	public GeneralTableModel getTableModel() {
		return tableModel;
	}

	public void setMinRows(int rows) {
		tableModel.setMinimumRows(rows);
	}

	public void setFont(Font font) {
		super.setFont(font);
		if (tableCellEditor != null)
			tableCellEditor.updateFont(font);
		if (table != null) {
			table.setFont(font);
			for (int row = 0; row < table.getRowCount(); row++)
				setToPreferredRowHeight(row);
		}
	}



	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			int selectedRow = table.getSelectedRow();
			if (selectedRow >= 0) {
				// System.out.println("Row "+selectedRow+"\n"+tableModel.rowString(selectedRow));
				if (isEditing && selectedRow >= tableModel.getRowCount() - 1
						&& !tableModel.isEmptyRow(selectedRow))
					tableModel.addRow();
			}
		}
		validate();
		updateUI();
	}

	private void setToPreferredRowHeight(int rowIndex) {
		int height = 0;
		for (int c = 0; c < table.getColumnCount(); c++) {
			TableCellRenderer renderer = table.getCellRenderer(rowIndex, c);
			Component comp = table.prepareRenderer(renderer, rowIndex, c);
			height = Math.max(height, comp.getPreferredSize().height);
		}
		if (height != table.getRowHeight(rowIndex))
			table.setRowHeight(rowIndex, height);
	}

	private void setupPopupMenu() {
		final JPopupMenu popupMenu = new JPopupMenu();

		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				checkForTriggerEvent(e);
			}

			public void mouseReleased(MouseEvent e) {
				checkForTriggerEvent(e);
			}

			private void checkForTriggerEvent(MouseEvent e) {
				if (e.isPopupTrigger()) {
					popupMenu.removeAll();
					if (isEditing) {
						createPopupMenuItem(popupMenu, insertRowBefore,
								insertRowBefore);
						createPopupMenuItem(popupMenu, insertRowAfter,
								insertRowAfter);
						JMenuItem deleteRowItem = createPopupMenuItem(
								popupMenu, deleteRow, deleteRow);
						deleteRowItem.setEnabled(table.getSelectedRow() >= 0);
						// createPopupMenuItem(popupMenu, adjustRowHeight,
						// adjustRowHeight);
					}
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}

	private JMenuItem createPopupMenuItem(JPopupMenu popupMenu, String title,
			String command) {
		JMenuItem menuItem = popupMenu.add(title);
		menuItem.setActionCommand(command);
		menuItem.addActionListener(this);
		return menuItem;
	}

	// implements ActionListener
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == insertRowBefore) {
			int selectedRow = table.getSelectedRow();
			if (selectedRow == -1)
				selectedRow = 0;
			tableModel.insertRow(selectedRow);
		} else if (cmd == insertRowAfter) {
			int selectedRow = table.getSelectedRow();
			if (selectedRow == -1)
				selectedRow = table.getRowCount() - 1;
			tableModel.insertRow(selectedRow + 1);
		} else if (cmd == deleteRow) {
			tableModel.removeRow(table.getSelectedRow());
		} else if (cmd == adjustRowHeight) {
			for (int row = 0; row < table.getRowCount(); row++)
				setToPreferredRowHeight(row);
		}
	}

	public static void main(String[] args) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		Vector<Object> v1 = new Vector<Object>();
		v1.add("1");
		v1.add("picpup(x)");
		v1.add("handempty");
		v1.add("ontable");
		v1.add("when");
		v1.add("effect");

		v1.add("inscription");

		Vector<Object> v2 = new Vector<Object>();
		v2.add("1");
		v2.add("picpup(x)");
		v2.add("handempty");
		v2.add("ontable");
		v2.add("when");
		v2.add("effect");
		v2.add("inscription");

		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		data.add(v1);
		data.add(v2);

		String[] columnNames = { "C1", "C2", "C3", "C4" };

		JFrame frame = new JFrame("Table");

		JScrollPane scrollpane = new JScrollPane(new GeneralTablePanel(data,
				columnNames, 3));
		scrollpane.setPreferredSize(new Dimension(800, 500));
		frame.getContentPane().add(scrollpane);
		frame.pack();
		frame.setVisible(true);

	}

}
