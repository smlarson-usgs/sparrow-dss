package gov.usgswim.sparrow.service.idbypoint;

import static gov.usgswim.sparrow.util.SimpleXMLBuilderHelper.asString;
import static gov.usgswim.sparrow.util.SimpleXMLBuilderHelper.writeClosedFullTag;
import static gov.usgswim.sparrow.util.SimpleXMLBuilderHelper.writeClosingTag;
import static gov.usgswim.sparrow.util.SimpleXMLBuilderHelper.writeNonNullTag;
import static gov.usgswim.sparrow.util.SimpleXMLBuilderHelper.writeOpeningTag;
import gov.usgswim.Immutable;

/**
 * Simple bean class to hold a reach that was identified by a user lat/long location.
 * @author eeverman
 *
 */
@Immutable
public class ReachInfo {
	private final Long modelID;
	private final Long reachId;
	private final String clientReachId;
	private final String name;
	private final transient Integer distInMeters;

	private transient Double clickedLong;
	private transient Double clickedLat;

	private final String huc2;
	private final String huc2Name;
	private final String huc4;
	private final String huc4Name;
	private final String huc6;
	private final String huc6Name;
	private final String huc8;
	private final String huc8Name;

	public ReachInfo(Long modelID, Long reachId, String clientReachId, String name, Integer distInMeters,
			String huc2, String huc2Name, String huc4, String huc4Name,
			String huc6, String huc6Name, String huc8, String huc8Name
	) {
		this.modelID = modelID;
		this.reachId = reachId;
		this.clientReachId = clientReachId;
		this.name = name;
		this.distInMeters = distInMeters;

		this.huc2 = huc2;
		this.huc2Name = huc2Name;
		this.huc4 = huc4;
		this.huc4Name = huc4Name;
		this.huc6 = huc6;
		this.huc6Name = huc6Name;
		this.huc8 = huc8;
		this.huc8Name = huc8Name;
	}

	public String toIdentificationXML() {
		StringBuilder in = new StringBuilder();
		// write <identification>
		writeOpeningTag(in, "identification",
				"distance-in-meters", asString(distInMeters));
		{
			writeNonNullTag(in, "id", clientReachId);
			writeNonNullTag(in, "name", name);
			if (clickedLong != null) {
				writeClosedFullTag(in, "point",
						"lat", asString(clickedLat),
						"long", asString(clickedLong));
			}

			writeOpeningTag(in, "hucs");
			{
				writeClosedFullTag(in, "huc8",
						"id", huc8,
						"name", huc8Name
				);
				writeClosedFullTag(in, "huc6",
						"id", huc6,
						"name", huc6Name
				);
				writeClosedFullTag(in, "huc4",
						"id", huc4,
						"name", huc4Name
				);
				writeClosedFullTag(in, "huc2",
						"id", huc2,
						"name", huc2Name
				);
			}
			writeClosingTag(in, "hucs");
		}
		writeClosingTag(in, "identification");

		return in.toString();
	}

	public ReachInfo cloneWithDistance(Integer distance) {
		ReachInfo clone = new ReachInfo(modelID, reachId, clientReachId, name,
				distance,
				huc2, huc2Name, huc4, huc4Name, huc6, huc6Name, huc8, huc8Name );

		return clone;
	}

	// =================
	// GETTERS & SETTERS
	// =================
	public Long getModelId() {
		return modelID;
	}

	public Long getReachId() {
		return reachId;
	}
	
	public String getClientReachId() {
		return clientReachId;
	}

	public int getDistanceInMeters() {
		return distInMeters;
	}

	public String getName() {
		return name;
	}

	public long getModelID() {
		return modelID;
	}

	public Integer getDistInMeters() {
		return distInMeters;
	}

	public String getHuc2() {
		return huc2;
	}

	public String getHuc2Name() {
		return huc2Name;
	}

	public String getHuc4() {
		return huc4;
	}

	public String getHuc4Name() {
		return huc4Name;
	}

	public String getHuc6() {
		return huc6;
	}

	public String getHuc6Name() {
		return huc6Name;
	}

	public String getHuc8() {
		return huc8;
	}

	public String getHuc8Name() {
		return huc8Name;
	}

	public void setClickedPoint(double longitude, double latitude) {
		this.clickedLong = longitude;
		this.clickedLat = latitude;
	}

}
