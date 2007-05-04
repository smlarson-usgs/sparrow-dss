package gov.usgswim.sparrow.gui;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class SparrowGUIMain {
	public SparrowGUIMain() {
	}

	public static void main(String[] args) {
	  SparrowData data = new SparrowData();
		JFrame frame = new MainFrame();
		
    frame.setTitle("Sparrow");
    frame.setIconImage(new ImageIcon("sparrow.gif").getImage());
    
    
		data.init(frame);
		frame.pack();
		frame.setVisible(true);
	}
}
