package gov.usgswim.sparrow.service.idbypoint;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import gov.usgswim.sparrow.service.SharedApplication;

import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import org.junit.Ignore;
import org.junit.Test;


public class IDByPointServiceTest {


	// Exploration while creating the id by point service. Trying to understand
	// the output of Oracle's GEOM operation
	@Ignore
	@Test public void testSDOGeometry() throws SQLException {
		Connection conn = SharedApplication.getConnectionFromCommandLineParams();
		String sqlQuery = "SELECT "
			+ "	geo.reach_geom, "
			+ "	attrib.identifier AS identifier, attrib.reach_name AS reach_name,"
			+ "	attrib.huc2, lkp2.name as huc2name, "
			+ "	attrib.huc4, lkp4.name as huc4name, "
			+ "	attrib.huc6, lkp6.name as huc6name, "
			+ "	attrib.huc8, lkp8.name as huc8name, "
			+ "	SDO_GEOM.SDO_MIN_MBR_ORDINATE(geo.reach_geom, m.diminfo, 1) AS MIN_LONG, "
			+ "	SDO_GEOM.SDO_MIN_MBR_ORDINATE(geo.reach_geom, m.diminfo, 2) AS MIN_LAT, "
			+ "	SDO_GEOM.SDO_MAX_MBR_ORDINATE(geo.reach_geom, m.diminfo, 1) AS MAX_LONG, "
			+ "	SDO_GEOM.SDO_MAX_MBR_ORDINATE(geo.reach_geom, m.diminfo, 2) AS MAX_LAT, "
			+ "	get_median_point(geo.reach_geom).sdo_point.x marker_long, "
			+ "    get_median_point(geo.reach_geom).sdo_point.y marker_lat "
			+ "FROM "
			+ "	((((MODEL_ATTRIB_VW attrib inner join MODEL_GEOM_VW geo "
			+ "		ON attrib.model_reach_id = geo.model_reach_id) "
			+ "	LEFT OUTER JOIN STREAM_NETWORK.huc2_LKP lkp2 "
			+ "		ON attrib.huc2 = lkp2.huc2) "
			+ "	LEFT OUTER JOIN STREAM_NETWORK.huc4_LKP lkp4 "
			+ "		ON attrib.huc4 = lkp4.huc4) "
			+ "	LEFT OUTER JOIN STREAM_NETWORK.huc6_LKP lkp6 "
			+ "		ON attrib.huc6 = lkp6.huc6) "
			+ "	LEFT OUTER JOIN STREAM_NETWORK.huc8_LKP lkp8 "
			+ "		ON attrib.huc8 = lkp8.huc8, "
			+ "	user_sdo_geom_metadata m "
			+ "WHERE "
			+ "	attrib.sparrow_model_id = 22 "
			+ "	AND attrib.identifier = 24502"
			+ "	AND m.table_name = 'MODEL_REACH_GEOM' "
			+ "	AND m.column_name = 'REACH_GEOM' ";

		Statement stmt = conn.createStatement();
		OracleResultSet ors = (OracleResultSet) stmt.executeQuery(sqlQuery);
		ors.next();

		STRUCT dbObject = (STRUCT)  ors.getObject(1);
		JGeometry geom = JGeometry.load(dbObject);

		int type = geom.getType();
		int srid = geom.getSRID();
		int dimensions = geom.getDimensions();
		long numberOfPoints = geom.getNumPoints();
		long size = geom.getSize();
		boolean isPoint = geom.isPoint();
		boolean isCircle = geom.isCircle();
		boolean hasArcs = geom.hasCircularArcs();
		boolean isGeodeticMember = geom.isGeodeticMBR();
		boolean isLRSGeometry = geom.isLRSGeometry();
		boolean isMultiPoint = geom.isMultiPoint();
		boolean isRectangle = geom.isRectangle();

		double[] points = geom.getPoint();
		int[] elementInfo = geom.getElemInfo();
		int numberOfElements = (elementInfo == null)? 0: elementInfo.length/3;

		double[] ordinates = geom.getOrdinatesArray();
		double[] firstPoint = geom.getFirstPoint();
		double[] lastPoint = geom.getLastPoint();
		Point2D labelPoint = geom.getLabelPoint();
		Point2D javaPoint = geom.getJavaPoint();
		Point2D[] javaPoints = (isMultiPoint)? geom.getJavaPoints(): null;
		double[] member = geom.getMBR();
		Shape shape = geom.createShape();

		{
			// output stuff
			System.out.println("operation: " + type);
			System.out.println("srid: " + srid);
			System.out.println("dimensions: " + dimensions);
			System.out.println("numberOfPoints: " + numberOfPoints);
			System.out.println("size: " + size);
			System.out.println("isPoint: " + isPoint);
			System.out.println("isCircle: " + isCircle);
			System.out.println("hasArcs: " + hasArcs);
			System.out.println("isGeodeticMember: " + isGeodeticMember);
			System.out.println("isLRSGeometry: " + isLRSGeometry);
			System.out.println("isMultiPoint: " + isMultiPoint);
			System.out.println("isRectangle: " + isRectangle);
			System.out.println("MBR: " + member[0] + " " + member[1] + " " + member[2] + " " + member[3]);

			System.out.println("numberOfElements: " + numberOfElements);

			if (elementInfo != null) {
				for (int i=0; i<numberOfElements; i=i+3) {
					System.out.println(i + " -- " + elementInfo[i] + ", " + elementInfo[i+1] + ", " + elementInfo[i+2]);
				}
			}

			if (javaPoints != null) {
				System.out.println("JAVAPOINTS");
				for (Point2D point: javaPoints) {
					System.out.println("(" + point.getX() + ", " + point.getY() + ")");
				}
			}

			if (ordinates != null) {
				System.out.println("ORDINATES");
				for (int i=0; i<numberOfPoints; i++) {
					System.out.print("  [" + (i+1) + "]  (");
					for (int j=0; j<dimensions; j++) {
						System.out.print(ordinates[i*dimensions + j]);
						if (j<dimensions - 1) {
							System.out.print(", ");
						}
					}
					System.out.println(")");

				}
				System.out.println();
			}

		}


	}

}
