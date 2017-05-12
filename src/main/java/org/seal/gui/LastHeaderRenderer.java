package org.seal.gui;
 
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableCellRenderer;
 
public class LastHeaderRenderer extends JLabel implements TableCellRenderer {
	private static final long serialVersionUID = 1L;
	public LastHeaderRenderer(TableCellRenderer defaultRenderer,Color c) {
        Border border = new EtchedBorder();
        Border margin = new EmptyBorder(0,30,0,0);
        setBorder(new CompoundBorder(border, margin));        
    }
     
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        setText(value.toString());
        Font f = getFont();
        setFont(f.deriveFont(f.getStyle() & ~Font.BOLD));
        return this;
    }
 
}