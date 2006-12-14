package gov.usgswim.sparrow.gui;

import javax.swing.JFrame;

public class SparrowGUIMain {
	public SparrowGUIMain() {
	}

	public static void main(String[] args) {
	  SparrowData data = new SparrowData();
		JFrame frame = new MainFrame();
		
		data.init(frame);
		frame.pack();
		frame.setVisible(true);
	}
}
