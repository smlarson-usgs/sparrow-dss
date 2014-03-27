package gov.usgswim.sparrow.service.idbypoint;

import static gov.usgswim.sparrow.util.SimpleXMLBuilderHelper.asString;
import static gov.usgswim.sparrow.util.SimpleXMLBuilderHelper.writeClosingTag;
import static gov.usgswim.sparrow.util.SimpleXMLBuilderHelper.writeNonNullTag;
import static gov.usgswim.sparrow.util.SimpleXMLBuilderHelper.writeOpeningTag;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.sparrow.service.util.ReturnStatus;

/**
 * A simple Bean object with a 
 * 
 * @author ilinkuo
 *
 */
public class IDByPointResponse {
	public Long modelID;
	public Integer contextID;
	public ReturnStatus status;
	public String message;
	public Integer cacheLifetime;
	//
	public Long reachID;
	private ReachInfo reach;
	//
	public DataTable adjustments;
	public DataTable predictions;
	public DataTable basicAttributes;
	public DataTable sparrowAttributes;

	// TODO remove these when working code successfully populates DataTable fields dynamically
	public String mapValueXML;
	public String adjustmentsXML;
	public String predictionsXML;
	public String attributesXML;
	
	public ReachInfo getReach() {
		return reach;
	}
	
	public void setReach(ReachInfo reach) {
		this.reach = reach;
		this.reachID = Long.valueOf(reach.getReachId()); // keep in sync
	}
	
	public static String writeXMLHead(ReturnStatus status, String message, Long modelId, Integer contextId) {
		StringBuilder in = new StringBuilder();
		writeOpeningTag(in, "sparrow-id-response", 
				"xmlns", "http://www.usgs.gov/sparrow/id-response-schema/v0_2",
				"xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance",
				"model-id", asString(modelId),
				"context-id", asString(contextId));
		in.append("\n");
		
		writeNonNullTag(in, "status", status.toString());
		writeNonNullTag(in, "message", message);
		
		writeOpeningTag(in, "results");
		in.append("\n");
		
		return in.toString();
	}

	public String toXML() {

		// create root element <sparrow-id-response>
		StringBuilder in = new StringBuilder();
		
		writeOpeningTag(in, "result");
		in.append("\n");
		{
			// write <status>, <message>, <cache-lifetime-seconds>
			writeNonNullTag(in, "status", status.toString());
			writeNonNullTag(in, "message", message);
			writeNonNullTag(in, "cache-lifetime-seconds", asString(cacheLifetime));
			in.append("\n");
			
			if (reach != null) {
    			in.append(reach.toIdentificationXML());
    			in.append("\n");
			}
			
			if (mapValueXML != null) {
				in.append(mapValueXML);
	            in.append("\n");
			}

			if (adjustmentsXML != null) {
				in.append(adjustmentsXML);
	            in.append("\n");
			}
			if (predictionsXML != null) {
				in.append(predictionsXML);
	            in.append("\n");
			}
			if (attributesXML != null) {
				in.append(attributesXML);
	            in.append("\n");
			}
		}
		writeClosingTag(in, "result");

		return in.toString();
	}
	
	public static String writeXMLFoot() {
		StringBuilder in = new StringBuilder();

		writeClosingTag(in, "results");
		in.append("\n");
		writeClosingTag(in, "sparrow-id-response");
		
		return in.toString();
	}



}
