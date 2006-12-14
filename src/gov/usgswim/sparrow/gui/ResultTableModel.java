package gov.usgswim.sparrow.gui;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Double2D;

import gov.usgswim.sparrow.Int2D;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class ResultTableModel extends AbstractTableModel {
	protected static Logger log = Logger.getLogger(ResultTableModel.class);
	
	Data2D data;
	
	public ResultTableModel(double[][] data) {
		this.data = new Double2D(data, null);
	}
	
	public ResultTableModel(int[][] data) {
		this.data = new Int2D(data, null);
	}
	
	public ResultTableModel(Data2D data) {
		this.data = data;
	}
	
	public ResultTableModel() {
	}
	
	public void setData(Data2D data) {
		this.data = data;
	  fireTableStructureChanged();
	}

	public int getRowCount() {
	  if (data != null) {
	    return data.getRowCount();
	  } else {
	    return 0;
	  }
	}

	public int getColumnCount() {
	  if (data != null) {
			return data.getColCount();
	  } else {
	    return 0;
	  }
	}

	public Object getValueAt(int row, int col) {
	  if (data != null) {
			try {
				return data.getValueAt(row, col);
			} catch (Exception e) {
				log.error("The table asked for a value outside the Data2D range", e);
				return new Integer(0);
			}
	  } else {
	    return new Integer(0);
	  }
	}


	public String getColumnName(int i) {
	  if (data != null) {
	    return data.getHeading(i);
		} else {
	    return StringUtils.EMPTY;
	  }
	}

	public void setValueAt(Object object, int i, int i1) {
		try {
			data.setValueAt(object, i, i1);
		  fireTableDataChanged();
		} catch (Exception e) {
		  JOptionPane.showMessageDialog(
				SparrowData.getInstance().getRootFrame(), e.getMessage(), "Could not set the value", JOptionPane.ERROR_MESSAGE);
		}
	}

	public boolean isCellEditable(int i, int i1) {
		return true;
	}

}
