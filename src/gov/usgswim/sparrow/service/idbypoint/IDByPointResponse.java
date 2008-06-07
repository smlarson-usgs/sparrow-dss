package gov.usgswim.sparrow.service.idbypoint;

import static gov.usgswim.sparrow.util.SimpleXMLBuilderHelper.*;
import gov.usgswim.datatable.DataTable;

/**
 * A simple Bean object with a 
 * 
 * @author ilinkuo
 *
 */
public class IDByPointResponse {
	public Long modelID;
	public Integer contextID;
	public boolean statusOK;
	public String message;
	public Integer cacheLifetime;
	//
	public Long reachID;
	private Reach reach;
	//
	public DataTable adjustments;
	public DataTable predictions;
	public DataTable basicAttributes;
	public DataTable sparrowAttributes;

	// TODO remove these when working code successfully populates DataTable fields dynamically
	public String adjustmentsXML;
	public String predictionsXML;
	public String attributesXML;
	
	public Reach getReach() {
		return reach;
	}
	
	public void setReach(Reach reach) {
		this.reach = reach;
		this.reachID = Long.valueOf(reach.getId()); // keep in sync
	}	

	public String toXML() {

		// create root element <sparrow-id-response>
		StringBuilder in = new StringBuilder();
		writeOpeningTag(in, "sparrow-id-response", 
				"xmlns", "http://www.usgs.gov/sparrow/id-response-schema/v0_2",
				"xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance",
				"model-id", asString(modelID),
				"context-id", asString(contextID));
		in.append("\n");
		{
			// write <status>, <message>, <cache-lifetime-seconds>
			writeNonNullTag(in, "status", (statusOK)?"OK": "");
			writeNonNullTag(in, "message", message);
			writeNonNullTag(in, "cache-lifetime-seconds", asString(cacheLifetime));
			in.append("\n");
			
			in.append(reach.toIdentificationXML());
			in.append("\n");

			// TODO Replace this when populated
			if (adjustmentsXML != null) {
				in.append(adjustmentsXML);
			}
			in.append("\n");
			if (predictionsXML != null) {
				in.append(predictionsXML);
			}
			in.append("\n");
			if (attributesXML != null) {
				in.append(attributesXML);
			}
			in.append("\n");
		}
		writeClosingTag(in, "sparrow-id-response");

		return in.toString();
	}



}
