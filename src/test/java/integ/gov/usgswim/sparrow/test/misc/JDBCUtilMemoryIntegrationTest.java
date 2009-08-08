package gov.usgswim.sparrow.test.misc;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.util.DataLoader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import junit.framework.TestCase;
import oracle.jdbc.OracleDriver;

/**
 * @author ilinkuo
 * @deprecated
 */
public class JDBCUtilMemoryIntegrationTest extends TestCase {

	public static class MemoryTestBench {// copied from datatable
		// MemoryUsageTest.java, which
		// copied in turn from ???The south
		// african guy
		@SuppressWarnings("all")
		public long calculateMemoryUsage(ObjectFactory factory)
		throws Exception {
			Object handle = factory.makeObject();
			long mem0 = Runtime.getRuntime().totalMemory() -
			Runtime.getRuntime().freeMemory();
			long mem1 = Runtime.getRuntime().totalMemory() -
			Runtime.getRuntime().freeMemory();
			handle = null;

			Thread.sleep(1000);
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			mem0 = Runtime.getRuntime().totalMemory() -
			Runtime.getRuntime().freeMemory();
			handle = factory.makeObject();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			mem1 = Runtime.getRuntime().totalMemory() -
			Runtime.getRuntime().freeMemory();
			Thread.sleep(1000);
			return mem1 - mem0;
		}
		public void showMemoryUsage(ObjectFactory factory) throws Exception {
			long mem = calculateMemoryUsage(factory);
			System.out.println(
					factory.getClass().getName() + " produced " +
					factory.makeObject().getClass().getName() +
					" which took " + mem + " bytes");
			System.out.println("Total Memory: " + Runtime.getRuntime().totalMemory());
		}
		
		/**
		 * Returns both the elapsed time and the constructed object. Be careful,
		 * as this may result in a memory leak.
		 * 
		 * @param factory
		 * @return Object[2], where Object[0] = elapsed time, Object[1] = object
		 *         returned by factory
		 * @throws Exception 
		 * 
		 */
		public Object[] calculateMemoryUsageAndRetrieve(ObjectFactory factory)
		throws Exception {
//			Object handle = factory.makeObject();
			long mem0 = Runtime.getRuntime().totalMemory() -
			Runtime.getRuntime().freeMemory();
			long mem1 = Runtime.getRuntime().totalMemory() -
			Runtime.getRuntime().freeMemory();
//			handle = null;

			Thread.sleep(1000);
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			mem0 = Runtime.getRuntime().totalMemory() -
			Runtime.getRuntime().freeMemory();
			Object handle = factory.makeObject();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			mem1 = Runtime.getRuntime().totalMemory() -
			Runtime.getRuntime().freeMemory();
			Thread.sleep(1000);
			return new Object[] {Long.valueOf(mem1 - mem0), handle};
		}
		
		public Object showMemoryUsageAndRetrieve(ObjectFactory factory) throws Exception {
			Object[] result = calculateMemoryUsageAndRetrieve(factory);
			long mem = (Long)result[0];
			System.out.println(
					factory.getClass().getName() + " produced " +
					factory.makeObject().getClass().getName() +
					" which took " + mem + " bytes");
			System.out.println("Total Memory: " + Runtime.getRuntime().totalMemory());
			return result[1];
		}
		
	}

	public static interface ObjectFactory {
		public Object makeObject() throws Exception;
	}

	private Connection conn;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		String username = "SPARROW_DSS";
		String password = "***REMOVED***";
		String thinConn = "jdbc:oracle:thin:@130.11.165.152:1521:widw";
		DriverManager.registerDriver(new OracleDriver());
		conn = DriverManager.getConnection(thinConn,username,password);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (conn != null ) conn.close();
	}
	
	//=============
	// TEST METHODS
	// ============

	public void testloadFullModelDataSet() throws Exception {
		final int modelId = 1;
		System.out.println("======= Testing loadFullModelDataSet Components Memory Usage =======");
		ObjectFactory sourceIDFactory = new ObjectFactory() {
			public Object makeObject() throws SQLException, IOException {
				return DataLoader.loadSourceMetadata(conn, modelId);
			}
		};
		
		ObjectFactory topoFactory = new ObjectFactory() {
			public Object makeObject() throws SQLException, IOException {
				return DataLoader.loadTopo(conn, modelId);
			}
		};
		
		ObjectFactory decayFactory = new ObjectFactory() {
			public Object makeObject() throws SQLException, IOException {
				return DataLoader.loadDecay(conn, modelId, 0);
			}
		};

		MemoryTestBench mtb = new MemoryTestBench();
		System.out.print("sourceID: ");
		final DataTable sourceIDs = (DataTable) mtb.showMemoryUsageAndRetrieve(sourceIDFactory);
		
		System.out.print("topo: ");
		mtb.showMemoryUsage(topoFactory);
		
		System.out.print("decay: ");
		mtb.showMemoryUsage(decayFactory);
		
		ObjectFactory sourceReachCoefFactory = new ObjectFactory() {
			public Object makeObject() throws SQLException, IOException {
				return DataLoader.loadSourceReachCoef(conn, modelId, sourceIDs);
			}
		};
		
		ObjectFactory sourceValuesFactory = new ObjectFactory() {
			public Object makeObject() throws SQLException, IOException {
				return DataLoader.loadSourceValues(conn, modelId, sourceIDs);
			}
		};
		
		System.out.print("sourceReachCoef: ");
		mtb.showMemoryUsage(sourceReachCoefFactory);
		
		System.out.print("sourceValues: ");
		mtb.showMemoryUsage(sourceValuesFactory);
		
		
		
		ObjectFactory fullModelDataSetFactory = new ObjectFactory() {
			public Object makeObject() throws SQLException, IOException {
				return DataLoader.loadModelAndBootstrapData(conn, modelId);
			}
		};
		
		System.out.print("loadFullModelDataSet: ");
		mtb.showMemoryUsage(fullModelDataSetFactory);
		
		System.out.println("=== END TEST ===");

	}

}
