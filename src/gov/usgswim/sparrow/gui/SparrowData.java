package gov.usgswim.sparrow.gui;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.FilteredDataTable;
import gov.usgswim.sparrow.PredictRunner;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;

/**
 * Central location for shared data, events, and actions.
 * 
 * Basic flow / Events model:
 * There are really two sources of events, which are related.  InputPanels
 * fire dataChanged events which this class listens for.  These events are
 * tagged w/ a DATA_TYPE_xxx contant and pass a File object.
 * 
 * SparrowData loads the data in the file into a publicly available instance
 * variable and fires another event of the same DATA_TYPE_xxx type, this time
 * with a Data2D instace as the passed value.  ResultGrids listen for this
 * event and update their display accordingly.
 * 
 * Result values are handled in a similar fashion, however, no source file is
 * needed so only the 2nd event takes place.
 */
public class SparrowData implements DataChangeListener {
	protected static Logger log = Logger.getLogger(SparrowData.class);
	
	/* Constants used to mark which type of data changed in a change event */
	public static final String DATA_TYPE_TOPO = "DATA_TYPE_TOPO";
	public static final String DATA_TYPE_COEF = "DATA_TYPE_COEF";
	public static final String DATA_TYPE_SRC = "DATA_TYPE_SRC";
	public static final String DATA_TYPE_KNOWN = "DATA_TYPE_KNOWN";
	public static final String DATA_TYPE_RESULT = "DATA_TYPE_RESULT";
	public static final String DATA_TYPE_ANCIL = "DATA_TYPE_ANCIL";
	
	public static final int[] DEFAULT_COMP_COLUMN_MAP =
		new int[] {40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 39, 15};

	DataTable topoData;
	DataTable coefData;
	DataTable srcData;
	DataTable knownData;
	DataTable ancilData;
	
	int[] compColumnMap = DEFAULT_COMP_COLUMN_MAP;
	
	DataTable result;
	DataTableCompare resultComp;	//Comparison of result to knownData
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
					srcData != null) {

			//The coefData contains two metadata columns and two decay coef columns.
			//These need to be masked out of the coef data.
			
			
			DataTable trimmedCoef = new FilteredDataTable(coefData, 4, coefData.getColumnCount() - 4);
			DataTable decayCoef = new FilteredDataTable(coefData, 1, 2);
			
			PredictRunner predict = new PredictRunner(topoData, trimmedCoef, srcData, decayCoef);
			
			long startTime = System.currentTimeMillis();
			int iterationCount = 100;
			
			for (int i = 0; i < iterationCount; i++)  {
				result = predict.doPredict2();
			}

			log.info("Predict complete.  Total Time: " + (System.currentTimeMillis() - startTime) + "ms for " +
				srcData.getColumnCount() + " sources, " + srcData.getRowCount() + " reaches, and " + iterationCount + " iterations."
			);
			
			if (knownData != null) {
				resultComp = new DataTableCompare(result, knownData, compColumnMap);
				fireDataChangeEvent(new DataChangeEvent(this, DATA_TYPE_RESULT, resultComp));
			} else {
				fireDataChangeEvent(new DataChangeEvent(this, DATA_TYPE_RESULT, result));
			}
			
			JOptionPane.showMessageDialog(this.rootFrame,
				"<html>Success!" +
				"<p><p>Predict complete.  Total Time: " + (System.currentTimeMillis() - startTime) + "ms for " +
				srcData.getColumnCount() + " sources, " + srcData.getRowCount() + " reaches, and " + iterationCount + " iterations."
			);
			
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
					topoData = TabDelimFileUtil.readAsInteger(f, true, -1);
					fireDataChangeEvent(new DataChangeEvent(this, DATA_TYPE_TOPO, topoData));
				} else if (DATA_TYPE_COEF.equals(evt.getDataType())) {
					DataTable coefDataFull = TabDelimFileUtil.readAsDouble(f, true, -1);
					
					//coefData includes multiple iterations, indicated in the first column.
					//we want iteration 0.
					int firstRow = coefDataFull.findFirst(0, Double.valueOf(0d));
					int lastRow = coefDataFull.findLast(0, Double.valueOf(0d));
					
					if (firstRow < 0 || lastRow < firstRow) {
						log.error("The coef data does not include an iteration zero!");
						throw new IllegalArgumentException("The coef data does not include an iteration zero!");
					}
					
					coefData = new FilteredDataTable(coefDataFull, firstRow, lastRow - firstRow + 1, 0, coefDataFull.getColumnCount());
					
					fireDataChangeEvent(new DataChangeEvent(this, DATA_TYPE_COEF, coefData));
				} else if (DATA_TYPE_SRC.equals(evt.getDataType())) {
					srcData = TabDelimFileUtil.readAsDouble(f, true, -1);
					fireDataChangeEvent(new DataChangeEvent(this, DATA_TYPE_SRC, srcData));
				} else if (DATA_TYPE_KNOWN.equals(evt.getDataType())) {
					knownData = TabDelimFileUtil.readAsDouble(f, true, -1);
					
					fireDataChangeEvent(new DataChangeEvent(this, DATA_TYPE_KNOWN, knownData));
					
					//also fire for result data if this adds comparison info

					if (result != null) {
						resultComp = new DataTableCompare(result, knownData, compColumnMap);
						fireDataChangeEvent(new DataChangeEvent(this, DATA_TYPE_RESULT, resultComp));
					}
					
				} else if (DATA_TYPE_ANCIL.equals(evt.getDataType())) {
					ancilData = TabDelimFileUtil.readAsDouble(f, true, -1);
					fireDataChangeEvent(new DataChangeEvent(this, DATA_TYPE_ANCIL, ancilData));
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
