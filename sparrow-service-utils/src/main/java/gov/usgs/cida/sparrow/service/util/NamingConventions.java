package gov.usgs.cida.sparrow.service.util;

/**
 * Naming conventions that must be shared between the service application
 * and GeoServer.
 * 
 * @author eeverman
 */
public class NamingConventions {
	
	/** Short Geoserver workspace name for flowline (reach) layers */
	public static final String FLOWLINE_WORKSPACE_NAME = "sparrow-flowline";
	
	/** Full Geoserver namespace for flowline (reach) layers */
	public static final String FLOWLINE_NAMESPACE = "http://water.usgs.gov/nawqa/sparrow/dss/spatial/" + FLOWLINE_WORKSPACE_NAME;
	
	/** short Geoserver workspace name for catchment layers */
	public static final String CATCHMENT_WORKSPACE_NAME = "sparrow-catchment";
	
	/** full Geoserver namespace catchment layers */
	public static final String CATCHMENT_NAMESPACE = "http://water.usgs.gov/nawqa/sparrow/dss/spatial/" + CATCHMENT_WORKSPACE_NAME;
	
	/**
	 * Creates a name for the layer that does not start with a number, so that
	 * is it safe to use an an XML element name.
	 * 
	 * @param contextId
	 * @return The layer name, prefixed with the appropriate workspace name.
	 */
	public static String getFullFlowlineLayerName(int contextId) {
		return FLOWLINE_WORKSPACE_NAME + ":" + convertContextIdToXMLSafeName(contextId);
		
	}
	
	/**
	 * Creates a name for the layer that does not start with a number, so that
	 * is it safe to use an an XML element name.
	 * 
	 * @param contextId
	 * @return The layer name, prefixed with the appropriate workspace name.
	 */
	public static String getFullCatchmentLayerName(int contextId) {
		return CATCHMENT_WORKSPACE_NAME + ":" + convertContextIdToXMLSafeName(contextId);
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
