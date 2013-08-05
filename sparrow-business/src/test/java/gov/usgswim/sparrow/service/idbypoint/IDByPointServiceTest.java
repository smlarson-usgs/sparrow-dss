package gov.usgswim.sparrow.service.idbypoint;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.SparrowServiceTestBaseWithDB;
import gov.usgswim.sparrow.SparrowTestBase;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.service.SharedApplication;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author cschroed
 */
public class IDByPointServiceTest extends
		SparrowServiceTestBaseWithDB{

	public IDByPointServiceTest() {
	}

	/**
	 * Test of retrieveAttributes method, of class IDByPointService.
	 */
	@Test
	public void testRetrieveAttributes() throws Exception {

		//first grab a reach id from a model that we know is in the database.
		Connection conn = null;
		final Long MODEL_ID = SparrowTestBase.TEST_MODEL_ID;
		Long anExampleReachId = -1L;
		final int BASIC_ATTRIBUTE_COUNT = 14;
		final int SPARROW_ATTRIBUTE_COUNT = 10;
		try{

			conn = SharedApplication.getInstance().getROConnection();
			PreparedStatement stmt = conn.prepareStatement("SELECT identifier FROM model_attrib_vw WHERE sparrow_model_id = ?");

			stmt.setLong(1, MODEL_ID);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			 anExampleReachId = rs.getLong("identifier");
			rs.close();
		}
		catch (SQLException e){
			throw e;
		}
		finally{
			if(null != conn){
				conn.close();
			}
		}
		if(-1 != anExampleReachId){
			IDByPointRequest req = null;
			IDByPointResponse response = new IDByPointResponse();
			response.modelID = MODEL_ID;
			response.reachID = anExampleReachId;

			IDByPointService instance = new IDByPointService();
			instance.retrieveAttributes(req, response);
			assertTrue(BASIC_ATTRIBUTE_COUNT == response.basicAttributes.getColumnCount());
			assertTrue(SPARROW_ATTRIBUTE_COUNT == response.sparrowAttributes.getColumnCount());
			List<Integer> colsWithDocId = Arrays.asList(new Integer[]{11,12,13});
			for(Integer colWithDocId : colsWithDocId){
				assertTrue(null != response.basicAttributes.getProperty(colWithDocId, TableProperties.DOC_ID.toString()));
			}
			System.out.println(response.attributesXML);

		}else{
			fail();
		}
	}

	/**
	 * Test of getAttributeData method, of class IDByPointService.
	 */
//	@Test
	public void testGetAttributeData() throws Exception {
		System.out.println("getAttributeData");
		long modelId = 0L;
		long reachId = 0L;
		IDByPointService instance = new IDByPointService();
		DataTable expResult = null;
		DataTable result = instance.getAttributeData(modelId, reachId);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}
}