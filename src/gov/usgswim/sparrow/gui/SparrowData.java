package gov.usgswim.sparrow.gui;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Double2D;
import gov.usgswim.sparrow.Int2D;
import gov.usgswim.sparrow.PredictSimple;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.awt.Frame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class SparrowData implements DataChangeListener {
	protected static Logger log = Logger.getLogger(SparrowData.class);
	
	/* Constants used to mark which type of data changed in a change event */
	public static final String DATA_TYPE_TOPO = "DATA_TYPE_TOPO";
	public static final String DATA_TYPE_COEF = "DATA_TYPE_COEF";
	public static final String DATA_TYPE_SRC = "DATA_TYPE_SRC";
	public static final String DATA_TYPE_DECAY = "DATA_TYPE_DECAY";
	public static final String DATA_TYPE_RESULT = "DATA_TYPE_RESULT";

	Int2D topoData;
	Double2D coefData;
	Double2D srcData;
	Double2D decayData;
	
	Double2D result;
	Frame rootFrame;
	
	// Create the listener list
	protected EventListenerList listenerList = new EventListenerList();
	
	static SparrowData instance;
	
	
	public SparrowData() {
	  instance = this;
	}
	
	public void init(Frame rootFrame) {
	  this.rootFrame = rootFrame;
	}
	
	public static SparrowData getInstance() {
		return instance;
	}
	
	public void doRun() {
		if (
					topoData != null &&
					coefData != null &&
					decayData != null &&
					srcData != null) {


			PredictSimple predict = new PredictSimple(topoData.getData(), coefData.getData(), srcData.getData(), decayData.getData(), srcData.getHeadings());
			
			
			result = predict.doPredict();
			
			
			fireDataChangeEvent(new DataChangeEvent(this, DATA_TYPE_RESULT, result));
			
			System.out.println("Success!");
			JOptionPane.showMessageDialog(this.rootFrame, "Success!");
			

			
		} else {
			JOptionPane.showMessageDialog(this.rootFrame, "One of the source files isn't specified or doesn't exist");
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
	  log.debug("SparrowData firing data event type: " + evt.getDataType() + " data: " + evt.getData());
		
		Object[] listeners = listenerList.getListenerList();
		// Each listener occupies two elements - the first is the listener class
		// and the second is the listener instance
		for (int i=0; i<listeners.length; i+=2) {
				if (listeners[i]==DataChangeListener.class) {
						((DataChangeListener)listeners[i+1]).dataChanged(evt);
				}
		}
	}

	public void dataChanged(DataChangeEvent evt) {
		log.debug("SparrowData received event from : " + evt.getSource() + " type: " + evt.getDataType() + " data: " + evt.getData());
		
		if (evt.getData() instanceof File) {
			File f = (File) evt.getData();
			
			try {
				if (DATA_TYPE_TOPO.equals(evt.getDataType())) {
					topoData = TabDelimFileUtil.readAsInteger(f, true);
					fireDataChangeEvent(new DataChangeEvent(this, DATA_TYPE_TOPO, topoData));
				} else if (DATA_TYPE_COEF.equals(evt.getDataType())) {
					coefData = TabDelimFileUtil.readAsDouble(f, true);
					fireDataChangeEvent(new DataChangeEvent(this, DATA_TYPE_COEF, coefData));
				} else if (DATA_TYPE_SRC.equals(evt.getDataType())) {
					srcData = TabDelimFileUtil.readAsDouble(f, true);
					fireDataChangeEvent(new DataChangeEvent(this, DATA_TYPE_SRC, srcData));
				} else if (DATA_TYPE_DECAY.equals(evt.getDataType())) {
					decayData = TabDelimFileUtil.readAsDouble(f, true);
					fireDataChangeEvent(new DataChangeEvent(this, DATA_TYPE_DECAY, decayData));
				}
			} catch (FileNotFoundException e) {
				log.error("File Note found", e);
				JOptionPane.showMessageDialog(this.rootFrame, "File Note found: " + e.getMessage());
			} catch (IOException io) {
				log.error("IOException", io);
				JOptionPane.showMessageDialog(this.rootFrame, "IOException: " + io.getMessage());
			}
			
		}
   
	}
	
	
	public void setRootFrame(Frame rootFrame) {
		this.rootFrame = rootFrame;
	}

	public Frame getRootFrame() {
		return rootFrame;
	}

	/*
	public void setTopoData(Int2D topo) {
		this.topoData = topo;
	}

	public Int2D getTopoData() {
		return topoData;
	}

	public void setCoef(Double2D coef) {
		this.coefData = coef;
	}

	public Double2D getCoef() {
		return coefData;
	}

	public void setSrc(Double2D src) {
		this.srcData = src;
	}

	public Double2D getSrc() {
		return srcData;
	}

	public void setResult(Double2D result) {
		this.result = result;
	}

	public Double2D getResult() {
		return result;
	}
	*/


}
