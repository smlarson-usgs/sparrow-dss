package gov.usgswim.sparrow.gui;

import java.awt.BorderLayout;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

public class MainPanel extends JPanel {
	private BorderLayout borderLayout1 = new BorderLayout();
	private InputPanel input = new InputPanel();
	
	
	private ResultGrid topoGrid = new ResultGrid(SparrowData.DATA_TYPE_TOPO);
	private ResultGrid coefGrid = new ResultGrid(SparrowData.DATA_TYPE_COEF);
	private ResultGrid srcGrid = new ResultGrid(SparrowData.DATA_TYPE_SRC);
	private ResultGrid resultGrid = new ResultGrid(SparrowData.DATA_TYPE_RESULT);
	private ResultGrid knownGrid = new ResultGrid(SparrowData.DATA_TYPE_KNOWN);
	private JTabbedPane jTabbedPane = new JTabbedPane();

	public MainPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		
		jTabbedPane.add("Topo Data", topoGrid);
	  jTabbedPane.add("Coef Data", coefGrid);
	  jTabbedPane.add("Src Data", srcGrid);
	  jTabbedPane.add("Predictions", resultGrid);
		jTabbedPane.add("Known Results (reference)", knownGrid);
	  jTabbedPane.setPreferredSize(new Dimension(500, 150));
		
		this.add(input, BorderLayout.NORTH);
		this.add(jTabbedPane, BorderLayout.CENTER);
	}
}
