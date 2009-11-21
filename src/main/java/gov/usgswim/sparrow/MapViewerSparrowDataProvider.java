package gov.usgswim.sparrow;

import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.Hashtable;
import java.util.Properties;

import oracle.mapviewer.share.Field;
import oracle.mapviewer.share.ext.NSDataProvider;
import oracle.mapviewer.share.ext.NSDataSet;
import oracle.mapviewer.share.ext.NSRow;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class MapViewerSparrowDataProvider implements NSDataProvider {
	protected static Logger log =
		Logger.getLogger(MapViewerSparrowDataProvider.class); //logging for this class


	//Request parameter key constants
	public static final String CONTEXT_ID = "context-id";


	public MapViewerSparrowDataProvider() {
	}

	/**
	 * Called once at creation time.
	 * @param properties
	 * @return
	 */
	@Override
	public boolean init(Properties properties) {
		return true;
	}

	/**
	 * This is the 10G Interface method.  Starting w/ 11G, only the
	 * <code>buildDataSet(Hashtable)</code> signature is supported.
	 *
	 * For now we can keep... at least until we upgrade the library to 11G.
	 * @deprecated
	 */
	@Override
	public NSDataSet buildDataSet(java.util.Properties params) {
		Hashtable<Object,Object> hash = new Hashtable<Object,Object>(13);

		for (Object key : params.keySet()) {
			hash.put(key, params.get(key));
		}

		return buildDataSet(hash);
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
	public NSDataSet buildDataSet(Hashtable<?,?> params) {
		long startTime = System.currentTimeMillis();	//Time started

		String idString = (String) params.get(CONTEXT_ID);
		idString = StringUtils.trimToNull(idString);

		if (idString != null) {
			log.debug("MapViewerSparrowDataProvider request received w/ context-id = '"
					+ idString + "'");

			Integer contextId;	//The context indentifier to locate the context.

			try {
				contextId = Integer.parseInt(idString);
			} catch (NumberFormatException e1) {
				log.error("MapViewerSparrowDataProvider could not convert passed " +
				"context-id '" + idString + "' to an integer.", e1);
				return null;
			}

			PredictionContext context = SharedApplication.getInstance().getPredictionContext(contextId);

			if (context != null) {

				NSDataSet nsData = null;	// The Mapviewer data format for the data

				try {
					nsData = copyToNSDataSet(context);
				} catch (Exception e1) {
					log.error("MapViewerSparrowDataProvider errored while copying " +
							"data for context-id '" + idString, e1);
					return null;
				}

				log.info("MapViewerSparrowDataProvider done for model #" +
						context.getModelID() + " (" + nsData.size() + " rows) Time: "
						+ (System.currentTimeMillis() - startTime) + "ms");

				return nsData;

			} else {
				log.info("MapViewerSparrowDataProvider could not find a " +
						"context for id: " + contextId + ".  It may have expired.");
				return null;
			}

		} else {
			log.error("MapViewerSparrowDataProvider request received w/o" +
					" a context-id parameter.");

			return null;
		}

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

	/**
	 * Called once when this instance is destroyed
	 */
	@Override
	public void destroy() {
		//Nothing to do
	}



}

