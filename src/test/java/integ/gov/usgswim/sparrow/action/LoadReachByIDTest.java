package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.SparrowUnitTest;
import gov.usgswim.sparrow.cachefactory.ReachID;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import gov.usgswim.sparrow.datatable.SingleColumnCoefDataTable;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.AreaOfInterest;
import gov.usgswim.sparrow.parser.BasicAnalysis;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.NominalComparison;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.idbypoint.ReachInfo;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;
import static gov.usgswim.sparrow.service.ConfiguredCache.*;


/**
 * There is one 'hole' in this set of tests.  To save a bit of work, we did not
 * manually load all upstream values into the .tab files - we stopped at reach 9681.
 * For incremental tests, we turn off transport for 9681 allowing the test to
 * validate that all reaches not listed in the .tab are zero.  For total
 * comparisons where the upstream values are important we can't do that
 * w/o generating values that can't be matched to what you would be able to
 * see/validate in the UI, so we leave the transport for reach 9681 ON.
 * 
 * As a consequence, total comparison are not able to exhaustively exclude that
 * there may be non-upstream values which are non-zero, as well as upstream
 * reaches which could be incorrect.
 * 
 * @author eeverman
 */
public class LoadReachByIDTest  extends SparrowDBTest {
	
	static ReachID reachId;
	
	@Override
	public void doSetup() throws Exception {
		
		//Uncomment to debug
		setLogLevel(Level.DEBUG);
		reachId = new ReachID(SparrowDBTest.TEST_MODEL_ID, 6000L);
	}
	
	@Test
	public void basicTest() throws Exception {

		LoadReachByID action = new LoadReachByID();
		action.setReachId(reachId);
		ReachInfo info = action.run();
		assertEquals(6000L, info.getId());
		assertEquals("LITTLE R, W FK", info.getName());
		assertEquals("03", info.getHuc2());
		assertEquals("0305", info.getHuc4());
		assertEquals("030501", info.getHuc6());
		assertEquals("03050106", info.getHuc8());
		
		//spatial attribs
		assertEquals(-81.27737d, info.getMarkerLong(), .00001);
		assertEquals(34.46994d, info.getMarkerLat(), .00001);
		assertEquals(-81.29193d, info.getMinLong(), .00001);
		assertEquals(34.41154d, info.getMinLat(), .00001);
		assertEquals(-81.24072d, info.getMaxLong(), .00001);
		assertEquals(34.52984d, info.getMaxLat(), .00001);
	}
	
	
}

