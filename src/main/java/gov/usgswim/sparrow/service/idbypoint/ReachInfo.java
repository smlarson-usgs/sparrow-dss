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
	private final long modelID;
	private final int id;
	private final String name;
	private final transient Integer distInMeters;

	private final double minLong;
	private final double minLat;
	private final double maxLong;
	private final double maxLat;
	private final double markerLong;
	private final double markerLat;

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

	private double[] _catchOrdinates;
	private double[] _reachOrdinates;
	// IMPORTANT: If new fields are added, remember to clone them


	public ReachInfo(long modelID, int id, String name, Integer distInMeters,
			double minLong, double minLat, double maxLong, double maxLat,
			double markLong, double markLat,
			String huc2, String huc2Name, String huc4, String huc4Name,
			String huc6, String huc6Name, String huc8, String huc8Name
	) {
		this.modelID = modelID;
		this.id = id;
		this.name = name;
		this.distInMeters = distInMeters;
		this.minLong = minLong;
		this.minLat = minLat;
		this.maxLong = maxLong;
		this.maxLat = maxLat;
		this.markerLong = markLong;
		this.markerLat = markLat;
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
			writeNonNullTag(in, "id", asString(id));
			writeNonNullTag(in, "name", name);
			if (clickedLong != null) {
				writeClosedFullTag(in, "point",
						"lat", asString(clickedLat),
						"long", asString(clickedLong));
			}

			writeClosedFullTag(in, "bbox",
					"min-long", asString(minLong),
					"min-lat", asString(minLat),
					"max-long", asString(maxLong),
					"max-lat", asString(maxLat),
					"marker-long", asString(markerLong),
					"marker-lat", asString(markerLat)
			);
			if (_catchOrdinates != null && _catchOrdinates.length > 0) {
				StringBuilder coordinates = toPointsString(_catchOrdinates);
				writeClosedFullTag(in, "polygon",
						"points", coordinates.toString());
			} else if (_reachOrdinates != null && _reachOrdinates.length > 0) {
				StringBuilder coordinates = toPointsString(_reachOrdinates);
				writeClosedFullTag(in, "polyLine",
						"points", coordinates.toString());
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

	private StringBuilder toPointsString(double[] geomOrdinates) {
		assert(geomOrdinates != null && geomOrdinates.length > 0) : "It is the caller's responsibility to check geomOrdinates not null";
		int dimensions = 2; // This is an assumption
		//int numberOfPoints = geomOrdinates.length/dimensions;

		StringBuilder coordinates = new StringBuilder();
		for (int i=0; i<geomOrdinates.length; i=i+2) {
			coordinates.append(geomOrdinates[i])
			.append(",")
			.append(geomOrdinates[i+1])
			.append(" ");
		}
		return coordinates;
	}

	public ReachInfo cloneWithDistance(Integer distance) {
		ReachInfo clone = new ReachInfo(modelID, id, name, distance, minLong, minLat, maxLong, maxLat,
				markerLong, markerLat,
				huc2, huc2Name, huc4, huc4Name, huc6, huc6Name, huc8, huc8Name );
		// IMPORTANT: If new fields are added here, remember to clone them
		clone._catchOrdinates = _catchOrdinates;
		clone._reachOrdinates = _reachOrdinates;
		return clone;
	}

	// =================
	// GETTERS & SETTERS
	// =================
	public long getModelId() {
		return modelID;
	}

	public int getId() {
		return id;
	}

	public int getDistanceInMeters() {
		return distInMeters;
	}

	public String getName() {
		return name;
	}

	public double getMinLong() {
		return minLong;
	}

	public double getMinLat() {
		return minLat;
	}

	public double getMaxLong() {
		return maxLong;
	}

	public double getMaxLat() {
		return maxLat;
	}

	public void setClickedPoint(double longitude, double latitude) {
		this.clickedLong = longitude;
		this.clickedLat = latitude;
	}

	public void setCatchGeometryOrdinates(double[] ordinates) {
		this._catchOrdinates = ordinates;
	}

	public void setReachGeometryOrdinates(double[] ordinates) {
		this._reachOrdinates = ordinates;
	}

}
