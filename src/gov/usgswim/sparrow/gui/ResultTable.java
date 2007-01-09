package gov.usgswim.sparrow.gui;

import gov.usgswim.sparrow.Data2DCompare;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class ResultTable extends JTable {
	public final static Color small = new Color(255, 252, 172);
	public final static Color med = new Color(255, 222, 172);
	public final static Color large = new Color(255, 99, 94);
	
	public ResultTable(TableModel dm) {
		super(dm);
		
		Enumeration cols = getColumnModel().getColumns();
		while (cols.hasMoreElements()) {
			((TableColumn)cols.nextElement()).setHeaderRenderer(new MultiLineHeaderRenderer());
		}  
	}
	

	public Dimension getPreferredScrollableViewportSize() {
		Dimension size = super.getPreferredScrollableViewportSize();
		return new Dimension(Math.min(getPreferredSize().width, size.width), size.height);
	}
	
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		Component c = super.prepareRenderer(renderer, row, column);
		
		ResultTableModel model = (ResultTableModel) getModel();
		
		if (model.getData() != null && model.getData() instanceof Data2DCompare) {
		
			double d = ((Data2DCompare) model.getData()).compare(row, column) ;
			JComponent jc = (JComponent)c;

			if (d == 0 || Math.abs(d) < .00003) {
				c.setBackground(null);
				jc.setToolTipText("Matches known result (actual deviation " + d + ")");
			} else if (Math.abs(d) < .0001) {
				c.setBackground(small);
				jc.setToolTipText("Small error: " + d);
			} else if (Math.abs(d) < .001) {
				c.setBackground(med);
				jc.setToolTipText("Medium error: " + d);
			} else {
				c.setBackground(large);
				jc.setToolTipText("HUGE error: " + d);
			}
			
			
		}
		
		return c;
	}

	public void tableChanged(TableModelEvent tableModelEvent) {
		super.tableChanged(tableModelEvent);
		
		Enumeration cols = getColumnModel().getColumns();
		while (cols.hasMoreElements()) {
			((TableColumn)cols.nextElement()).setHeaderRenderer(new MultiLineHeaderRenderer());
		}
		
		
	}

	class MultiLineHeaderRenderer extends JLabel implements TableCellRenderer {
		public MultiLineHeaderRenderer() {
			setOpaque(true);
			setForeground(UIManager.getColor("TableHeader.foreground"));
			setBackground(UIManager.getColor("TableHeader.background"));
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setHorizontalAlignment(JLabel.CENTER);
		}
	
		public Component getTableCellRendererComponent(JTable table, Object value,
						 boolean isSelected, boolean hasFocus, int row, int column) {
			setFont(table.getFont());
			String str = (value == null) ? "" : value.toString();
			this.setText(str);
			this.setToolTipText("[max error - if applicable]");
			//String[] strs = str.split("<br>");
			//setListData(strs);
			return this;
		}
	}
}
