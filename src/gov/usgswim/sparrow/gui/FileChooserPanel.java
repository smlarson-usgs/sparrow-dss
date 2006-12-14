package gov.usgswim.sparrow.gui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;

public class FileChooserPanel extends JPanel {
	protected static Logger log = Logger.getLogger(FileChooserPanel.class);
	
	private String dataType;	//Used in notification events
	private String caption = "Sample Caption";
	private String fileDescriptiveName = "Sample File";

	private JTextField path = new JTextField();
	private JButton browse = new JButton();
	private FormLayout formLayout1 = new FormLayout("right:40dlu:grow, left:pref:none" , "c:pref:g, c:pref:g" );
	private JLabel label = new JLabel();

	// Create the listener list
	protected EventListenerList listenerList = new EventListenerList();

	static JFileChooser jfc = new JFileChooser();
	
	
	
	public FileChooserPanel(String dataType, String caption, String fileDescriptiveName) {
		try {
			this.dataType = dataType;
			this.caption = caption;
			this.fileDescriptiveName = fileDescriptiveName;
			
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(formLayout1);

		this.setBorder(BorderFactory.createTitledBorder(caption));
		path.setColumns(40);
		path.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					log.debug("FileChooser received action event: " + e.getActionCommand());
				  fireDataChangeEvent(new DataChangeEvent(this, dataType, getFile()));
				}
			});
		browse.setText("Browse...");
	  browse.setActionCommand("lookup_toposrc");
	  browse.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	          browserFile(e);
	        }
	      });
		
		//label.setText(caption);
		//label.setFocusable(false);
		//label.setLabelFor(path);
		this.add(path,
					 new CellConstraints(1, 2));
	  this.add(browse,
					 new CellConstraints(2, 2));
					 
		//this.add(label, new CellConstraints(1, 1, 2, 1));
	}
	
	private void browserFile(ActionEvent e) {
		
	  jfc.setDialogTitle("Choose the " + fileDescriptiveName + " file (tab delimited)");
		int returnVal = jfc.showOpenDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			path.setText( jfc.getSelectedFile().getPath() );
		  fireDataChangeEvent(new DataChangeEvent(this, dataType, getFile()));
		}
	}
	
	public boolean hasRealFile() {
	  if (path.getText() != null && path.getText().length() > 0) {
	    File f = new File(path.getText());
			return f.exists();
	  } else {
	    return false;
	  }
	}
	
	public File getFile() {
		if (path.getText() != null && path.getText().length() > 0) {
			return new File(path.getText());
		} else {
			return null;
		}
	}
	
	
	// This methods allows classes to register for MyEvents
	public void addDataChangeListener(DataChangeListener listener) {
			listenerList.add(DataChangeListener.class, listener);
	}
	
	// This methods allows classes to unregister for MyEvents
	public void removeDataChangeListener(DataChangeListener listener) {
			listenerList.remove(DataChangeListener.class, listener);
	}
	
	// This private class is used to fire MyEvents
	void fireDataChangeEvent(DataChangeEvent evt) {
	  log.debug("FileChooser firing data event type: " + evt.getDataType() + " data: " + evt.getData());
		
	  if (hasRealFile()) {
	    Object[] listeners = listenerList.getListenerList();
	    // Each listener occupies two elements - the first is the listener class
	    // and the second is the listener instance
	    for (int i=0; i<listeners.length; i+=2) {
	        if (listeners[i]==DataChangeListener.class) {
	            ((DataChangeListener)listeners[i+1]).dataChanged(evt);
	        }
	    }
	  } else {
		  JOptionPane.showMessageDialog(this.getRootPane(), "File Note found: " + path.getText());
		}
		

	}

}
