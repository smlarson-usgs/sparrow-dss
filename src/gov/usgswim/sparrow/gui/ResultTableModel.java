package gov.usgswim.sparrow.gui;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DCompare;
import gov.usgswim.sparrow.Data2DWritable;

import gov.usgswim.sparrow.Double2DImm;
import gov.usgswim.sparrow.Int2DImm;

import java.text.DecimalFormat;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import java.text.NumberFormat;

public class ResultTableModel extends AbstractTableModel {
	protected static Logger log = Logger.getLogger(ResultTableModel.class);
	
	Data2D data;
	DecimalFormat format = new DecimalFormat("0.#######");
	
	public ResultTableModel(double[][] data) {
		this.data = new Double2DImm(data, null);
	}
	
	public ResultTableModel(int[][] data) {
		this.data = new Int2DImm(data, null);
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
	
	public Data2D getData() {
		return data;
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
				return data.getValue(row, col);
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
			if (data instanceof Data2DCompare) {
				return "<html>" + data.getHeading(i) + "<p>[" + format.format( ((Data2DCompare)data).findMaxCompareValue(i)) + "]";
			} else {
				return data.getHeading(i);
			}
		} else {
	    return StringUtils.EMPTY;
	  }
	}

	public void setValueAt(Object object, int i, int i1) {

		if (data instanceof Data2DWritable) {
			((Data2DWritable)data).setValueAt(object.toString(), i, i1);
			fireTableDataChanged();
		} else {
			JOptionPane.showMessageDialog(
				SparrowData.getInstance().getRootFrame(), "The data cannot be edited.", "Could not set the value", JOptionPane.INFORMATION_MESSAGE);
		}

	}

	public boolean isCellEditable(int i, int i1) {
		return true;
	}

}
