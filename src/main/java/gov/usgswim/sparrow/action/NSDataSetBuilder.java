package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.PredictionContext;
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

	private final static String NL = System.getProperty("line.separator");
	
	/** Context used to build the results from */
	DataColumn data;
	
	/** Array of output data.  Hold reference just so we can print debug info */
	private NSRow[] nsRows;

	
	@Override
	protected NSDataSet doAction() throws Exception {
		//PredictionContext.DataColumn dc = context.getDataColumn();

		int rowCount = data.getTable().getRowCount();
		nsRows = new NSRow[rowCount];
		PredictData predictData = null;	//Only loaded if needed

		//Build the row of data
		for (int r = 0; r < rowCount; r++) {
			Field[] row = new Field[2];

			long id = -1L;
			try {
				id = data.getTable().getIdForRow(r);
			} catch (NullPointerException npe) {
				//This data is likely not coming from a regular prediction,
				//Which would have row IDs assigned to it.
				//This is not really incorrect, just an efficient way of not
				//storing row ids if we don't already have them.
				if (predictData == null) {
					Integer cId = data.getContextId();
					PredictionContext pc = SharedApplication.getInstance().getPredictionContext(cId);
					predictData = SharedApplication.getInstance().getPredictData(pc.getModelID());
					
					log.debug("NSDataSetBuilder found that there were no row IDs" +
							"for model " +	pc.getModelID() + ", context " + cId +
							".  Using row IDs from the PredictionData instead.");

				}
				
				id = predictData.getTopo().getIdForRow(r);
			}
			// TODO [IK] try alternate path with id as string to try to solve
			// huc aggregation artificial key issue. This would require that the
			// DataTable argument passed in NOT use HUC ID as a DataTable ID,
			// which requires it to be a number, but rather as a column of data
			// in the datatable, and that the copyToNSDataSet be smart enough
			// to figure out what to do.
			row[0] = new Field(id);
			row[0].setKey(true);

			// Value
			Double val = data.getTable().getDouble(r, data.getColumn());
			if (val != null) {
				row[1] = new Field(val);
			} else {
				row[1] = new Field();
				row[1].setNull();
			}

			NSRow nsRow = new NSRow(row);
			nsRows[r] = nsRow;
		}

		return new NSDataSet(nsRows);
	}

	/**
	 * Returns the 1st ten rows of the NSDataset as a string.
	 */
	@Override
	protected String getPostMessage() {
		
		if (nsRows != null) {
			int maxRow = 10;
			if (maxRow > nsRows.length) maxRow = nsRows.length;
	
			StringBuffer sb = new StringBuffer();
			
			sb.append("These are the first ten rows of the NSDataSet: ");
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
			return "No NSData to output (nsRows was null)";
		}
	}
	
	public void setData(DataColumn data) {
		this.data = data;
	}

}
