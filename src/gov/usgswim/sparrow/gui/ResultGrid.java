package gov.usgswim.sparrow.gui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Double2D;

import gov.usgswim.sparrow.Int2D;

import java.awt.BorderLayout;

import java.awt.Color;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;

import java.awt.event.ComponentEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import oracle.jdeveloper.layout.PaneConstraints;
import oracle.jdeveloper.layout.PaneLayout;

import org.apache.commons.lang.StringUtils;

public class ResultGrid extends JPanel implements DataChangeListener {

	private final String dataType;
	
	private JTable table = new JTable(new ResultTableModel()) {
	  public Dimension getPreferredScrollableViewportSize() {
			Dimension size = super.getPreferredScrollableViewportSize();
	    return new Dimension(Math.min(getPreferredSize().width, size.width), size.height);
	  }
	};
	private BorderLayout borderLayout1 = new BorderLayout();
	private JScrollPane jScrollPane1 = new JScrollPane(table);

	public ResultGrid(String dataType) {
	
		this.dataType = dataType;
		
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {

		this.setLayout(borderLayout1);
		
		table.setGridColor(Color.BLACK);
		this.add(jScrollPane1, BorderLayout.CENTER);
		table.setPreferredScrollableViewportSize(getSize());

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setColumnSelectionAllowed(true);
		SparrowData data = SparrowData.getInstance();
	  data.addDataChangeListener(this);
	}
	
	public void setData(Data2D data) {
		((ResultTableModel) table.getModel()).setData(data);
	}

	public void dataChanged(DataChangeEvent event) {
		if (
					StringUtils.equals(dataType, event.getDataType()) &&
					event.getData() instanceof Data2D) {
					
		  setData( (Data2D) event.getData() );		
			
		}
	}
	


}
