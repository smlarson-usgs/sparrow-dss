package gov.usgswim.sparrow.gui;

import gov.usgswim.sparrow.deprecated.Data2D;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.commons.lang.StringUtils;


public class ResultGrid extends JPanel implements DataChangeListener {

	private final String dataType;
	
	private JTable table = new ResultTable(new ResultTableModel());
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
