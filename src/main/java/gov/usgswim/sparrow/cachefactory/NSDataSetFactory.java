package gov.usgswim.sparrow.cachefactory;

import java.util.Hashtable;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.PredictResultCompare;
import gov.usgswim.sparrow.parser.AdvancedComparison;
import gov.usgswim.sparrow.parser.Comparison;
import gov.usgswim.sparrow.parser.NominalComparison;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

import oracle.mapviewer.share.Field;
import oracle.mapviewer.share.ext.NSDataSet;
import oracle.mapviewer.share.ext.NSRow;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This factory class creates a Comparison on demand for an EHCache.
 *
 * When the cache receives a getComparisonResult(PredictContext) call and it doesn't have a cache
 * entry for that request, the createEntry() method of this class is called
 * and the returned value is cached.
 *
 * The basic process followed in this class is:
 * <ul>
 * <li>Fetch both results to be compared from the analysis cache
 * <li>Create a view that compares the two results.
 * </ul>
 *
 * This class implements CacheEntryFactory, which plugs into the caching system
 * so that the createEntry() method is only called when a entry needs to be
 * created/loaded.
 *
 * Caching, blocking, and de-caching are all handled by the caching system, so
 * that this factory class only needs to worry about building a new entity in
 * (what it can consider) a single thread environment.
 *
 * @author eeverman
 *
 */
public class NSDataSetFactory implements CacheEntryFactory {

	protected static Logger log =
		Logger.getLogger(NSDataSetFactory.class); //logging for this class

	public Object createEntry(Object predictContext) throws Exception {
		PredictionContext context = (PredictionContext) predictContext;

		return buildDataSet(context);
	}
	
	/**
	 * This method creates and returns an instance of NSDataSet which contains
	 * all the Non-Spatial attribute data produced by this provider, based on
	 * the given parameters for a specific incoming map request.
	 *
	 * [Documentation taken directly from Oracle javadocs]
	 *
	 * Unlike the properties passed into the init() method which are global ones,
	 * the parameters here are specific to one map request. Note that starting
	 * with 11g, the params has changed from a Properties class to the Hashtable
	 * class. This change enables MapViewer to pass two context objects:
	 * HttpServletRequest and HttpSession (if one exists) to your implementation.
	 * These two objects are accessed through the key "mv-oms-request" and
	 * "mv-http-session" respectively. All other NSDP parameters embedded in a
	 *  map request can still be accessed from the params object using the same
	 *  key by calling the get(key) method of the hashtable. You may however
	 *  need to cast the return object into String type for these regular NSDP
	 *  parameters.
	 *
	 *  MapViewer calls this method when processing any map request that
	 *  contains a Non-spatial data provider tag that refrences this
	 *  implementation. As such this method needs to be reentrant.
	 *
	 * @param params - to be used when creating a data set.
	 * @return an instance of NSDataSet; null if failed.
	 */
	public NSDataSet buildDataSet(PredictionContext context) {
		long startTime = System.currentTimeMillis();	//Time started

		NSDataSet nsData = null;

		try {
			nsData = copyToNSDataSet(context);
		} catch (Exception e1) {
			log.error("NSDataSetFactory errored while copying " +
					"data for context-id '" + context.getModelID(), e1);
			return null;
		}

		log.info("NSDataSetFactory done for model #" +
				context.getModelID() + " (" + nsData.size() + " rows) Time: "
				+ (System.currentTimeMillis() - startTime) + "ms");

		return nsData;
	}


	public NSDataSet copyToNSDataSet(PredictionContext context) throws Exception {
		PredictionContext.DataColumn dc = context.getDataColumn();

		int rowCount = dc.getTable().getRowCount();
		NSRow[] nsRows = new NSRow[rowCount];

		//Build the row of data
		for (int r = 0; r < rowCount; r++) {
			Field[] row = new Field[2];

			long id = -1L;
			try {
				id = dc.getTable().getIdForRow(r);
			} catch (NullPointerException npe) {
				log.info("NSDataSetFactory needed to retrieve predict data for a row.  Model" +
						context.getModelID());
				PredictData nomPredictData = SharedApplication.getInstance().getPredictData(context.getModelID());
				id = nomPredictData.getTopo().getIdForRow(r);
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
			Double val = dc.getTable().getDouble(r, dc.getColumn());
			if (val != null) {
				row[1] = new Field(val);
			} else {
				row[1] = new Field();
				row[1].setNull();
			}

			NSRow nsRow = new NSRow(row);
			nsRows[r] = nsRow;
		}

		if (log.isDebugEnabled()) debugNSData(nsRows);

		return new NSDataSet(nsRows);
	}
	
	protected void debugNSData(NSRow[] nsRows) {
		int maxRow = 10;
		if (maxRow > nsRows.length) maxRow = nsRows.length;

		log.debug("MVSparrowDataProvider These are the first ten rows of data: ");
		for (int r = 0; r < maxRow; r++)  {
			StringBuffer sb = new StringBuffer();
			for (int c = 0; c < nsRows[0].size(); c++)  {
				sb.append(nsRows[r].get(c).toString());
				if (nsRows[r].get(c).isKey()) sb.append("[Key] ");
				if (nsRows[r].get(c).isLabelText()) sb.append("[Lab] ");
				if ((c + 1) < nsRows[0].size()) sb.append("| ");
			}
			log.debug(sb.toString());
		}
	}
}
