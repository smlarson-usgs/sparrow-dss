package gov.usgswim.sparrow;

import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.BasicAnalysis;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.Hashtable;
import java.util.Properties;

import oracle.mapviewer.share.ext.NSDataProvider;
import oracle.mapviewer.share.ext.NSDataSet;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class MapViewerSparrowDataProvider implements NSDataProvider {
	protected static Logger log =
		Logger.getLogger(MapViewerSparrowDataProvider.class); //logging for this class


	//Request parameter key constants
	/**
	 * Parameter name to specify a context.
	 */
	public static final String CONTEXT_ID = "context-id";
	
	/** 
	 * Parameter name to spec a model only.
	 * If specified, the Total Load of a unmodified model is mapped.
	 */
	public static final String MODEL_ID = "model-id";
	


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
	 *  contains a Non-spatial data provider tag that references this
	 *  implementation. As such this method needs to be reentrant.
	 *
	 * @param params - to be used when creating a data set.
	 * @return an instance of NSDataSet; null if failed.
	 * @throws Exception 
	 */
	public NSDataSet buildDataSet(Hashtable params) {

		String contextIdString = (String) params.get(CONTEXT_ID);
		contextIdString = StringUtils.trimToNull(contextIdString);
		Integer contextId = null;
		PredictionContext context;
		
		String modelIdString = (String) params.get(MODEL_ID);
		modelIdString = StringUtils.trimToNull(modelIdString);
		Long modelId = null;
		
		if (contextIdString != null) {
			log.debug("MapViewerSparrowDataProvider request received w/ context-id = '"
					+ contextIdString + "'");
			
			try {
				contextId = Integer.parseInt(contextIdString);
			} catch (NumberFormatException e1) {
				log.error("MapViewerSparrowDataProvider could not convert passed " +
				"context-id '" + contextIdString + "' to an integer.", e1);
				return null;
			}
			
			try {
				context = SharedApplication.getInstance().getPredictionContext(contextId);
			} catch (Exception e) {
				log.error("Could not fetch PredictionContext from storage.  " +
						"context-id '" + contextIdString, e);
				return null;
			}
		} else if (modelIdString != null) {
			
			log.debug("MapViewerSparrowDataProvider request received w/ model-id = '"
					+ modelIdString + "'");
			
			try {
				modelId = Long.parseLong(modelIdString);
			} catch (NumberFormatException e1) {
				log.error("MapViewerSparrowDataProvider could not convert passed " +
				"model-id '" + modelIdString + "' to an integer.", e1);
				return null;
			}
			
			try {
				context = buildBasicContext(modelId);
				contextId = context.getId();
			} catch (Exception e1) {
				log.error("MapViewerSparrowDataProvider could not create an empty " +
						"prediction context for model-id '" + modelIdString + "'.", e1);
						return null;
			}
			
			
		} else {
			log.error("MapViewerSparrowDataProvider could not find a model-id" +
					" or context-id in the request.");
			return null;
		}
		


		if (context != null) {

			NSDataSet nsData = null;	// The MapViewer data format for the data

			try {
				nsData = SharedApplication.getInstance().getNSDataSet(context);
			} catch (Exception e1) {
				log.error("MapViewerSparrowDataProvider errored while copying " +
						"data for context-id '" + contextId, e1);
				return null;
			}

			return nsData;

		} else {
			log.info("MapViewerSparrowDataProvider could not find a " +
					"context for id: " + contextId + ".  It may have expired.");
			return null;
		}



	}
	
	private PredictionContext buildBasicContext(Long modelId) throws Exception {
		AdjustmentGroups adjustments = new AdjustmentGroups(modelId);
		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.total,
				null, null, null);
		PredictionContext context = new PredictionContext(modelId,
				adjustments, analysis, null, null, null);
		
		SharedApplication.getInstance().putPredictionContext(context);
		
		return context;
	}

	/**
	 * Called once when this instance is destroyed
	 */
	@Override
	public void destroy() {
		//Nothing to do
	}



}

