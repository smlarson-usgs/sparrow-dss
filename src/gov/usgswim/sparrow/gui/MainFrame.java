package gov.usgswim.sparrow.gui;

import java.awt.Dimension;

import javax.swing.JFrame;

import oracle.jdeveloper.layout.PaneConstraints;
import oracle.jdeveloper.layout.PaneLayout;

public class MainFrame extends JFrame {
	private MainPanel mainPanel1 = new MainPanel();
	private PaneLayout paneLayout1 = new PaneLayout();

	public MainFrame() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.getContentPane().setLayout(paneLayout1);
		this.setSize( new Dimension(400, 300) );
		this.getContentPane().add(mainPanel1,
														new PaneConstraints("mainPanel1", "mainPanel1",PaneConstraints.ROOT, 1.0f));
														
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}


}
