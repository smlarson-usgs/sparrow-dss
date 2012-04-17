package gov.usgswim.sparrow.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import gov.usgswim.sparrow.domain.Geometry;
import gov.usgswim.sparrow.domain.Segment;

/**
 * General utililty class to load geometry data to the Geometry domain class
 * with some cleanup as needed.
 * 
 * @author eeverman
 *
 */
public class GeometryUtil {
	
	public static final double DEFAULT_SIMPLIFICATION_TOLERANCE = .005D;
	
	/**
	 * Loads a polygon from a resultset containing an Oracle geometry column to a Geometry object.
	 * 
	 * Any internal holes in the geometry are filtered out.
	 * 
	 * @param resultSet	The ResultSet, which must have a current row to load from.
	 * @param geomColumnName	The name of the a column containing an Oracle geometry.
	 * @param simplify If true, the geometry is simplified using the default
	 * 	simplification tolorance of .005.
	 * @return
	 * @throws SQLException
	 */
	public static Geometry loadPolygon(ResultSet resultSet, String geomColumnName, boolean simplify) throws SQLException {
		if (simplify) {
			return loadPolygon(resultSet, geomColumnName, DEFAULT_SIMPLIFICATION_TOLERANCE);
		} else {
			return loadPolygon(resultSet, geomColumnName, 0d);
		}
	}
	
	/**
	 * Loads a polygon from a resultset containing an Oracle geometry column to a Geometry object.
	 * 
	 * Any internal holes in the geometry are filtered out.
	 * 
	 * @param resultSet	The ResultSet, which must have a current row to load from.
	 * @param geomColumnName	The name of the a column containing an Oracle geometry.
	 * @param simplificationTol Simplification tolerance.  0 to turn off.
	 * 
	 * @return
	 * @throws SQLException
	 */
	public static Geometry loadPolygon(ResultSet resultSet, String geomColumnName, double simplificationTol) throws SQLException {
		STRUCT geomStruct = (STRUCT) resultSet.getObject("GEOM");
		JGeometry jGeom = JGeometry.load(geomStruct);
		Geometry geom = loadPolygon(jGeom, simplificationTol);
		return geom;
	}
	
	/**
	 * Loads a polygon from Oracle JGeometry to a Geometry object.
	 * 
	 * Any internal holes in the geometry are filtered out.
	 * 
	 * @param jGeom	Oracle geometry to load from.
	 * @param simplify If true, the geometry is simplified using the default
	 * 	simplification tolorance of .005.
	 * @return
	 */
	public static Geometry loadPolygon(JGeometry jGeom, boolean simplify) {
		if (simplify) {
			return loadPolygon(jGeom, DEFAULT_SIMPLIFICATION_TOLERANCE);
		} else {
			return loadPolygon(jGeom, 0d);
		}
	}
	
	/**
	 * Loads a polygon from Oracle JGeometry to a Geometry object.
	 * 
	 * Any internal holes in the geometry are filtered out as well as any geometry
	 * that results in a line (i.e., three points of start, out, and back).
	 * 
	 * @param jGeom	Oracle geometry to load from.
	 * @param simplificationTol Simplification tolerance.  0 to turn off.
	 * 
	 * @return
	 */
	public static Geometry loadPolygon(JGeometry jGeom, double simplificationTol) {

		if (simplificationTol > 0d) {
			jGeom = jGeom.simplify(simplificationTol);
		}
		
		//Corresponds to the Oracle spatial SDO_ELEM_INFO_ARRAY.
		//Elements in the info array are in triplets as follows:
		// [0] The first vertex of the geom (one based)
		// [1] The type of geom:  1=Point, 2=LineSTring, 1003=Polygon w/ interior area, 2003=Polygon hole
		// [2] Conection type.  1=Straight line, 2=Arc
		int[] elementInfo = jGeom.getElemInfo();
		
		double[] dblOrds = jGeom.getOrdinatesArray();
		
		ArrayList<Segment> segmentList = new ArrayList<Segment>();
		
		for (int ei = 0; ei < elementInfo.length; ei += 3) {
			
			int elementType = elementInfo[ei + 1];
			
			if (elementType == 1003) {
				int startIndex = elementInfo[ei] - 1; //one based references (zero based array)
				int endIndex = dblOrds.length;	//first coord *not* taken
				
				//find the end index
				if (ei + 3 < elementInfo.length) {
					//not the last element, so the next element contains the
					//start of the next segment.
					endIndex = elementInfo[ei + 3] - 1;		//one based
				}
				
				
				if (endIndex - startIndex > 6) {
					//less than six coords is just a line (start, next point, and back again)
					float[] coords = loadPolygonCoordinates(dblOrds, startIndex, endIndex);
					Segment s = new Segment(coords, false);
					segmentList.add(s);
				}
				
				
			}
		}
		
		Segment[] segments = segmentList.toArray(new Segment[segmentList.size()]);
		Geometry geom = new Geometry(segments);
		
		return geom;
		
	}
	

	
	/**
	 * Load the subset verticies to a float array.
	 * 
	 * @param verticies Array of coords in pairs.
	 * @param startIndex the first coord included.
	 * @param endIndex The first coord *not* included.
	 * @return
	 */
	public static float[] loadPolygonCoordinates(double[] verticies, int startIndex, int endIndex) {


		float[] floatOrds = new float[endIndex - startIndex];
		int floatIndex = 0;
		
		for (int i = startIndex; i < endIndex; i++) {
			floatOrds[floatIndex] = (float) verticies[i];
			floatIndex++;
		}
		
		return floatOrds;
	}
}
