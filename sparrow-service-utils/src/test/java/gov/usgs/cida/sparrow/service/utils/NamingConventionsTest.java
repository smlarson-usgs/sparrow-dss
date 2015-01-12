package gov.usgs.cida.sparrow.service.utils;

import gov.usgs.cida.sparrow.service.util.NamingConventions;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author eeverman
 */
public class NamingConventionsTest {
	
	 @Test
	 public void trySomeSimpleConvertionsToXMLSafeValues() {
		 
		assertEquals("N1234", NamingConventions.convertContextIdToXMLSafeName(-1234));
		assertEquals("P1234", NamingConventions.convertContextIdToXMLSafeName(1234));
		assertEquals("P0", NamingConventions.convertContextIdToXMLSafeName(0));
		
		//With workspace - non-reusable
		assertEquals(NamingConventions.FLOWLINE_WORKSPACE_NAME +  ":N1234", NamingConventions.getFullFlowlineLayerName(-1234, false));
		assertEquals(NamingConventions.CATCHMENT_WORKSPACE_NAME + ":P1234", NamingConventions.getFullCatchmentLayerName(1234, false));
		assertEquals(NamingConventions.CATCHMENT_WORKSPACE_NAME + ":P0", NamingConventions.getFullCatchmentLayerName(0, false));
		
		//With workspace - reusable
		assertEquals(NamingConventions.FLOWLINE_REUSABLE_WORKSPACE_NAME +  ":N1234", NamingConventions.getFullFlowlineLayerName(-1234, true));
		assertEquals(NamingConventions.CATCHMENT_REUSABLE_WORKSPACE_NAME + ":P1234", NamingConventions.getFullCatchmentLayerName(1234, true));
		assertEquals(NamingConventions.CATCHMENT_REUSABLE_WORKSPACE_NAME + ":P0", NamingConventions.getFullCatchmentLayerName(0, true));
	 }
	 
	 @Test
	 public void trySomeSimpleConvertionsBackToNumbers() {
		 
		assertEquals(-1234, NamingConventions.convertXMLSafeNameToContextId("N1234"));
		assertEquals(1234, NamingConventions.convertXMLSafeNameToContextId("P1234"));
		assertEquals(0, NamingConventions.convertXMLSafeNameToContextId("P0"));
	 }
}
