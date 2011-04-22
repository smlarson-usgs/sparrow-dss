package gov.usgswim.sparrow.util;

import java.sql.ResultSet;
import java.sql.SQLException;

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
	 * Any internal holes in the geometry are filtered out.
	 * 
	 * @param jGeom	Oracle geometry to load from.
	 * @param simplificationTol Simplification tolerance.  0 to turn off.
	 * 
	 * @return
	 */
	public static Geometry loadPolygon(JGeometry jGeom, double simplificationTol) {

		Geometry geom = null;
		Segment[] segments = null;

		JGeometry[] jGeomSegments = jGeom.getElements();
		
		segments = new Segment[jGeomSegments.length];
		
		for (int i = 0; i < jGeomSegments.length; i++) {
			if (simplificationTol == 0d) {
				segments[i] = new Segment(loadPolygonCoordinates(jGeomSegments[i]), false);
			} else {
				JGeometry jSimple = jGeomSegments[i].simplify(simplificationTol);
				segments[i] = new Segment(loadPolygonCoordinates(jSimple), false);
			}

		}
		
		geom = new Geometry(segments);
		
		return geom;
	}
	
	/**
	 * Loads coordinates from a polygon type geometry.
	 * If the geometry contains holes, the holes are not loaded.
	 * 
	 * Note:  This method will probably work for loading other types of geometry,
	 * but its not clear if other types of geometry (especially lines) might put
	 * multiple disjoint lines into one JGeometry Element.  If it does, then
	 * only the first section of that line string would be returned as a side-
	 * effect of attempting to filter out holes in polygons.
	 * 
	 * @param jGeom
	 * @return
	 */
	public static float[] loadPolygonCoordinates(JGeometry jGeom) {

		double[] dblOrds = jGeom.getOrdinatesArray();
		
		int startIndex = 0;
		int endIndex = 0;
		
		int[] info = jGeom.getElemInfo();
		
		//Corresponds to the Oracle spatial SDO_ELEM_INFO_ARRAY.
		//Elements in the info array are in triplets as follows:
		// [0] The first vertex of the geom (one based)
		// [1] The type of geom:  1=Point, 2=LineSTring, 1003=Polygon w/ interior area, 2003=Polygon hole
		// [2] Conection type.  1=Straight line, 2=Arc
		if (info.length > 3) {
			//There is at least one hole, so only take the first closed section,
			//skipping the holes.
			startIndex = info[0] - 1;	//One based
			endIndex = info[4] - 2; 	//element 4 contains the start index of the next section
		} else {
			startIndex = 0;
			endIndex = dblOrds.length - 1;
		}
		
		float[] floatOrds = new float[endIndex - startIndex + 1];
		
		for (int i = 0; i<floatOrds.length; i++) {
			floatOrds[i] = (float) dblOrds[i + startIndex];
		}
		
		return floatOrds;
	}
}
