package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.SparrowNSDataSet;
import gov.usgswim.sparrow.service.SharedApplication;
import oracle.mapviewer.share.Field;
import oracle.mapviewer.share.ext.NSDataSet;
import oracle.mapviewer.share.ext.NSRow;

/**
 * This action copies data from a DataTable to a new instance of an Oracle
 * NSDataSet, which is used by MapViewer to theme rendered maps.
 * 
 * The heavy lifting of actually calculating results is handled in either
 * the CalcAnalysis or the BuildComparison actions.
 *
 */
public class NSDataSetBuilder extends Action<NSDataSet> {
	
	/** Context used to build the results from */
	private SparrowColumnSpecifier data;
	
	/** A hash of row numbers that are in the reaches to be mapped. **/
	private ReachRowValueMap inclusionMap;
	
	/** The value to use for NA values. */
	private Long NAValue = DEFAULT_NA_VALUE;
	
	/** The default value for NAValue */
	public static final long DEFAULT_NA_VALUE = -9000000000000000000L;
	
	/** Array of output data.  Hold reference just so we can print debug info */
	private NSRow[] nsRows;

	
	@Override
	public NSDataSet doAction() throws Exception {
		//PredictionContext.DataColumn dc = context.getDataColumn();

		DataTable table = data.getTable();
		int rowCount = table.getRowCount();
			
		
		nsRows = new NSRow[rowCount];
		PredictData predictData = null;	//Only loaded if needed

		//Build the row of data
		for (int r = 0; r < rowCount; r++) {
			Field[] row = new Field[2];

			long id = -1L;
			try {
				id = table.getIdForRow(r);
			} catch (NullPointerException npe) {
				//This data is likely not coming from a regular prediction,
				//Which would have row IDs assigned to it.
				//This is not really incorrect, just an efficient way of not
				//storing row ids if we don't already have them.
				if (predictData == null) {
					Integer cId = data.getContextId();
					PredictionContext pc = SharedApplication.getInstance().getPredictionContext(cId);
					predictData = SharedApplication.getInstance().getPredictData(pc.getModelID());
					
					log.warn("NSDataSetBuilder found that there were no row IDs" +
							"for model " +	pc.getModelID() + ", context " + cId +
							".  Using row IDs from the PredictionData instead.");

				}
				
				id = predictData.getTopo().getIdForRow(r);
			}

			row[0] = new Field(id);
			row[0].setKey(true);


			if (inclusionMap != null && ! inclusionMap.hasRowNumber(r)) {
				//This is an excluded row
				
				if (NAValue != null) {
					row[1] = new Field(NAValue);
				} else {
					row[1] = new Field();
					row[1].setNull();
				}
				
			} else {
				//This is a standard included row
				
				Double val = data.getDouble(r);
	
				if (val == null || val.isInfinite() || val.isNaN()) {
					row[1] = new Field();
					row[1].setNull();
				} else {
					row[1] = new Field(val);
				}
			}


			NSRow nsRow = new NSRow(row);
			nsRows[r] = nsRow;
		}

		return new SparrowNSDataSet(nsRows);
		//return new NSDataSet(nsRows);
	}

	/**
	 * Returns the 1st ten rows of the NSDataset as a string.
	 */
	@Override
	protected String getPostMessage() {
		
		Long modelID = data.getModelId();
		
		if (nsRows != null) {
			
			if (log.isTraceEnabled()) {
				int maxRow = 10;
				if (maxRow > nsRows.length) maxRow = nsRows.length;
		
				StringBuffer sb = new StringBuffer();
				
				sb.append("NSData contained " + nsRows.length + " rows for model id " + data.getModelId() + NL);
				sb.append("These are the first ten rows of the NSDataSet: " + NL);
				for (int r = 0; r < maxRow; r++)  {
					for (int c = 0; c < nsRows[0].size(); c++)  {
						sb.append(nsRows[r].get(c).toString());
						if (nsRows[r].get(c).isKey()) sb.append("[Key] ");
						if (nsRows[r].get(c).isLabelText()) sb.append("[Lab] ");
						if ((c + 1) < nsRows[0].size()) sb.append("| ");
					}
					sb.append(NL);
				}
				return sb.toString();
			} else {
				return "NSData contained " + nsRows.length + " rows for model id " + data.getModelId();
			}
		} else {
			return "No NSData to output (nsRows was null)";
		}
	}
	
	/**
	 * The DataColumn to create a map data set for.
	 * @param data
	 */
	public void setData(SparrowColumnSpecifier data) {
		this.data = data;
	}

	/**
	 * Assign a hash of row numbers (not ids) that should be included in the map.
	 * Rows not present in the hash will be set to the equivalant of 'NA'.
	 * 
	 * The structure/type of this hash is based on the hash used to calc
	 * delivery fractions.  Rather than repackage it to something specifically
	 * for this usage, its just used as is.
	 * 
	 * The key (Integer) is a row number.
	 * The value (DeliveryReach for the delivery hash, but here we don't care)
	 * will be non-null if the reach is to be included.  If null, its NA.
	 * 
	 * @param inclusionHash
	 */
	public void setInclusionMap(ReachRowValueMap inclusionHash) {
		this.inclusionMap = inclusionHash;
	}

	/**
	 * Assign the value that will be used for excluded values.
	 * 
	 * By default, this value is -9000000000000000000, which is just a bit
	 * larger than the lowest possible Long value.
	 * 
	 * Assigning null is permitted and results in the NA values not being
	 * mapped, regardless of bin settings.
	 * @param nAValue
	 */
	public void setNAValue(Long nAValue) {
		NAValue = nAValue;
	}

}
