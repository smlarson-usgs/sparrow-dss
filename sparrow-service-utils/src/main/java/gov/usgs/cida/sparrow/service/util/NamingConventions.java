package gov.usgs.cida.sparrow.service.util;

/**
 * Naming conventions that must be shared between the service application
 * and GeoServer.
 * 
 * @author eeverman
 */
public class NamingConventions {
	
	
	
	private static final String REUSABLE_SUFFIX = "reusable";
	
	private static final String DEFAULT_STYLE_SUFFIX = "default";
	
	public static final String FLOWLINE_DEFAULT_STYLE_SUFFIX = "flowline-" + DEFAULT_STYLE_SUFFIX;
	
	public static final String CATCHMENT_DEFAULT_STYLE_SUFFIX = "catchment-" + DEFAULT_STYLE_SUFFIX;
	
	/** Base namespace for the entire app, as used on GeoServer */
	public static final String APP_SPATIAL_NAMESPACE = "http://water.usgs.gov/nawqa/sparrow/dss/spatial";
	
	/** Short Geoserver workspace name for flowline (reach) layers */
	public static final String FLOWLINE_WORKSPACE_NAME = "sparrow-flowline";
	
	/** Full Geoserver namespace for flowline (reach) layers */
	public static final String FLOWLINE_NAMESPACE = APP_SPATIAL_NAMESPACE + "/" + FLOWLINE_WORKSPACE_NAME;
	
	/** Short Geoserver workspace name for flowline (reach) layers that are deemed to be reusable */
	public static final String FLOWLINE_REUSABLE_WORKSPACE_NAME = "sparrow-flowline" + "-" + REUSABLE_SUFFIX;
	
	/** Full Geoserver namespace for flowline (reach) layers that are deemed to be reusable */
	public static final String FLOWLINE_REUSABLE_NAMESPACE = APP_SPATIAL_NAMESPACE + "/" + FLOWLINE_REUSABLE_WORKSPACE_NAME;
	
	/** short Geoserver workspace name for catchment layers */
	public static final String CATCHMENT_WORKSPACE_NAME = "sparrow-catchment";
	
	/** full Geoserver namespace catchment layers */
	public static final String CATCHMENT_NAMESPACE = APP_SPATIAL_NAMESPACE + "/" + CATCHMENT_WORKSPACE_NAME;
	
	/** short Geoserver workspace name for catchment layers */
	public static final String CATCHMENT_REUSABLE_WORKSPACE_NAME = "sparrow-catchment" + "-" + REUSABLE_SUFFIX;
	
	/** full Geoserver namespace catchment layers */
	public static final String CATCHMENT_REUSABLE_NAMESPACE = APP_SPATIAL_NAMESPACE + "/" + CATCHMENT_REUSABLE_WORKSPACE_NAME;
	
	/**
	 * Creates a name for the layer that does not start with a number, so that
	 * is it safe to use an an XML element name.
	 * 
	 * A separate workspace is used for contexts that are deemed to be reusable,
	 * that is, they contain little or no customization and have a high likelyhood
	 * of being reused.
	 * 
	 * @param contextId
	 * @param reusable If true, the reusable workspace name is used.
	 * @return The layer name, prefixed with the appropriate workspace name.
	 */
	public static String getFullFlowlineLayerName(int contextId, boolean reusable) {
		if (reusable) {
			return FLOWLINE_REUSABLE_WORKSPACE_NAME + ":" + convertContextIdToXMLSafeName(contextId);
		} else {
			return FLOWLINE_WORKSPACE_NAME + ":" + convertContextIdToXMLSafeName(contextId);
		}
		
	}
	
	/**
	 * Creates a name for the layer that does not start with a number, so that
	 * is it safe to use an an XML element name.
	 * 
	 * A separate workspace is used for contexts that are deemed to be reusable,
	 * that is, they contain little or no customization and have a high likelyhood
	 * of being reused.
	 * 
	 * @param contextId
	 * @param reusable If true, the reusable workspace name is used.
	 * @return The layer name, prefixed with the appropriate workspace name.
	 */
	public static String getFullCatchmentLayerName(int contextId, boolean reusable) {
		if (reusable) {
			return CATCHMENT_REUSABLE_WORKSPACE_NAME + ":" + convertContextIdToXMLSafeName(contextId);
		} else {
			return CATCHMENT_WORKSPACE_NAME + ":" + convertContextIdToXMLSafeName(contextId);
		}
	}
	
	/**
	 * Returns the complete namespace (with the http://water.usgs.gov... prefix)
	 * for flowline layers.  A different namespace is used if the layer is deemed
	 * to be likely to be reused.
	 * 
	 * @param reusable
	 * @return 
	 */
	public static String getFlowlineNamespace(boolean reusable) {
		if (reusable) {
			return FLOWLINE_REUSABLE_NAMESPACE;
		} else {
			return FLOWLINE_NAMESPACE;
		}
	}
	
	/**
	 * Returns the complete namespace (with the http://water.usgs.gov... prefix)
	 * for catchment layers.  A different namespace is used if the layer is deemed
	 * to be likely to be reused.
	 * 
	 * @param reusable
	 * @return 
	 */
	public static String getCatchmentNamespace(boolean reusable) {
		if (reusable) {
			return CATCHMENT_REUSABLE_NAMESPACE;
		} else {
			return CATCHMENT_NAMESPACE;
		}
	}
	
	/**
	 * Returns the name of the GeoServer workspace for flowline layers.
	 * A different namespace is used if the layer is deemed
	 * to be likely to be reused.
	 * 
	 * @param reusable
	 * @return 
	 */
	public static String getFlowlineWorkspaceName(boolean reusable) {
		if (reusable) {
			return FLOWLINE_REUSABLE_WORKSPACE_NAME;
		} else {
			return FLOWLINE_WORKSPACE_NAME;
		}
	}
	
	/**
	 * Returns the name of the GeoServer workspace for catchment layers.
	 * A different namespace is used if the layer is deemed
	 * to be likely to be reused.
	 * 
	 * @param reusable
	 * @return 
	 */
	public static String getCatchmentWorkspaceName(boolean reusable) {
		if (reusable) {
			return CATCHMENT_REUSABLE_WORKSPACE_NAME;
		} else {
			return CATCHMENT_WORKSPACE_NAME;
		}
	}
	
	
	/**
	 * Creates a name for the layer that does not start with a number, so that
	 * is it safe to use an an XML element name.
	 * 
	 * @param contextId
	 * @return 
	 */
	public static String convertContextIdToXMLSafeName(int contextId) {
		if (contextId >= 0) {
			return "P" + Integer.toString(contextId);
		} else {
			return "N" + Integer.toString(Math.abs(contextId));
		}
	}
	
	/**
	 * Returns the default style name for the flowline layer of a given context.
	 * 
	 * Note that only 'reusable' contexts should be given named default styles -
	 * These are context that have no adjustments and will be tile cached, which
	 * requires a named style.
	 * 
	 * @param contextId
	 * @return 
	 */
	public static String buildDefaultFlowlineStyleName(int contextId) {
		return convertContextIdToXMLSafeName(contextId) + "-" + FLOWLINE_DEFAULT_STYLE_SUFFIX;
	}
	
	/**
	 * Returns the default style name for the catchment layer of a given context.
	 * 
	 * Note that only 'reusable' contexts should be given named default styles -
	 * These are context that have no adjustments and will be tile cached, which
	 * requires a named style.
	 * 
	 * @param contextId
	 * @return 
	 */
	public static String buildDefaultCatchmentStyleName(int contextId) {
		return convertContextIdToXMLSafeName(contextId) + "-" + CATCHMENT_DEFAULT_STYLE_SUFFIX;
	}
	
	/**
	 * Converts an XML safe layer name back to its context ID.
	 * 
	 * @param encodedContextId encoded via convertContextIdToXMLSafeName
	 * @return 
	 */
	public static int convertXMLSafeNameToContextId(String encodedContextId) throws NumberFormatException {
		encodedContextId = encodedContextId.toUpperCase();
		
		if (encodedContextId.startsWith("P")) {
			return Integer.parseInt(encodedContextId.substring(1));
		} else if (encodedContextId.startsWith("N")) {
			return (-1) * (Integer.parseInt(encodedContextId.substring(1)));
		} else {
			throw new NumberFormatException("Unable to read context ID encoded as '" + encodedContextId + "'");
		}
	}
}
