package gov.usgswim.sparrow.domain;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.Vector;

import org.apache.log4j.Logger;

import oracle.mapviewer.share.Field;
import oracle.mapviewer.share.ext.NSDataSet;
import oracle.mapviewer.share.ext.NSRow;

public class SparrowNSDataSet_new extends NSDataSet {
	protected static Logger log =
		Logger.getLogger(SparrowNSDataSet_new.class); //logging for this class
	
	private final static String NL = System.getProperty("line.separator");
	
	/** Context used to build the results from */
	private SparrowColumnSpecifier data;
	
	/** A hash of row numbers that are in the reaches to be mapped. **/
	private ReachRowValueMap inclusionMap;
	
	/** The value to use for NA values. */
	private Long NAValue = DEFAULT_NA_VALUE;
	
	/** base predict data - only loaded if the data has no row ids */
	private PredictData predictData = null;	//Only loaded if needed
	
	/** The default value for NAValue */
	public static final long DEFAULT_NA_VALUE = -9000000000000000000L;
	
	public SparrowNSDataSet_new(SparrowColumnSpecifier data, ReachRowValueMap inclusionMap) {
		super(new NSRow[0]);
		this.data = data;
		this.inclusionMap = inclusionMap;
	}
	
	
	public SparrowNSDataSet_new(NSRow[] arg0) {
		super(arg0);
	}

	public SparrowNSDataSet_new(Vector arg0) {
		super(arg0);
	}
	
	

	@Override
	public synchronized void close() {
		//Ignore the call to 'close'.  We cache this data, so we don't want it
		//destroyed.
	}


	@Override
	public synchronized NSRow getRow(int rowIndex) {

		//Build the row of data

		Field[] row = new Field[2];

		long id = -1L;
		try {
			id = data.getIdForRow(rowIndex);
		} catch (NullPointerException npe) {
			//This data is likely not coming from a regular prediction,
			//Which would have row IDs assigned to it.
			//This is not really incorrect, just an efficient way of not
			//storing row ids if we don't already have them.
			if (predictData == null) {
				Integer cId = data.getContextId();
				PredictionContext pc = null;
				try {
					pc = SharedApplication.getInstance().getPredictionContext(cId);
					predictData = SharedApplication.getInstance().getPredictData(pc.getModelID());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				log.warn("SparrowNSDataSet found that there were no row IDs" +
						"for model " +	pc.getModelID() + ", context " + cId +
						".  Using row IDs from the PredictionData instead.");

			}
			
			try {
				id = predictData.getTopo().getIdForRow(rowIndex);
			} catch (NullPointerException npee) {
				log.error("SparrowNSDataSet could not find a rowID in the topo data." +
						"  More log detail is req'ed for diagnose.");
				return null;
			}
		}

		row[0] = new Field(id);
		row[0].setKey(true);


		if (inclusionMap != null && ! inclusionMap.hasRowNumber(rowIndex)) {
			//This is an excluded row
			
			if (NAValue != null) {
				row[1] = new Field(NAValue);
			} else {
				row[1] = new Field();
				row[1].setNull();
			}
			
		} else {
			//This is a standard included row
			
			Double val = data.getDouble(rowIndex);

			if (val == null || val.isInfinite() || val.isNaN()) {
				row[1] = new Field();
				row[1].setNull();
			} else {
				row[1] = new Field(val);
			}
		}


		NSRow nsRow = new NSRow(row);
		return nsRow;
	}

	/**
	 * Supported, but not the intended method of use.
	 * Since this class dynamically builds each row as requested to limit memory
	 * use, asking for all the rows at once is inefficent.
	 * ee:  I don't think MapViewer would do this - should check.
	 */
	@Override
	public synchronized NSRow[] getRows() {
		DataTable table = data.getTable();
		int rowCount = table.getRowCount();
		NSRow[] nsRows = new NSRow[rowCount];
		
		PredictData predictData = null;	//Only loaded if needed

		//Build the row of data
		for (int r = 0; r < rowCount; r++) {
			nsRows[r] = getRow(r);
		}
		
		return nsRows;
	}

	/**
	 * Not supported.
	 * This implementation dynamically builds the rows as requested, so setting
	 * them all at once is not supported.
	 */
	@Override
	public synchronized void setRows(NSRow[] arg0) {
		//Ignore - this implementation doesn't keep a set of NSRows.
	}


	@Override
	public synchronized int size() {
		return data.getRowCount();
	}

}
