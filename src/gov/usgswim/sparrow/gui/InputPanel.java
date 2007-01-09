package gov.usgswim.sparrow.gui;

import gov.usgswim.sparrow.PredictSimple;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.awt.BorderLayout;

import java.awt.FlowLayout;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import oracle.jdeveloper.layout.PaneConstraints;
import oracle.jdeveloper.layout.PaneLayout;
import oracle.jdeveloper.layout.VerticalFlowLayout;

public class InputPanel extends JPanel {


	private FileChooserPanel topoPanel = new FileChooserPanel(SparrowData.DATA_TYPE_TOPO, "Topographic Data File", "Topographic Data");
	private FileChooserPanel coefPanel = new FileChooserPanel(SparrowData.DATA_TYPE_COEF, "Coefficient (Beta) Data File", "Coefficient (Beta) Data");
	private FileChooserPanel sourcePanel = new FileChooserPanel(SparrowData.DATA_TYPE_SRC, "Source Data File", "Source Data");
	private FileChooserPanel knownPanel = new FileChooserPanel(SparrowData.DATA_TYPE_KNOWN, "Known Result Value File", "Known Result Values (predict.txt)");
	private JButton runButton = new JButton("Run Predictions");
	private VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();

	public InputPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(verticalFlowLayout1);

		runButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						doRun(e);
					}
				});
		this.add(topoPanel, null);
	  this.add(coefPanel, null);
	  this.add(sourcePanel, null);
		this.add(knownPanel, null);
		this.add(runButton, null);

		SparrowData data = SparrowData.getInstance();
		topoPanel.addDataChangeListener(data);
	  coefPanel.addDataChangeListener(data);
	  sourcePanel.addDataChangeListener(data);
		knownPanel.addDataChangeListener(data);
	}


	private void doRun(ActionEvent e) {
	
		SparrowData data = SparrowData.getInstance();
		data.doRun();
		
	}
}
