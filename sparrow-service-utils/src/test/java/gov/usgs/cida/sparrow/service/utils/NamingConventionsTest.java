package gov.usgs.cida.sparrow.service.utils;

import gov.usgs.cida.sparrow.service.util.NamingConventions;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author eeverman
 */
public class NamingConventionsTest {
	
	 @Test
	 public void trySomeSimpleConvertionsToXMLSafeValues() {
		 
		assertEquals("50N1234", NamingConventions.convertContextIdToXMLSafeName(50, -1234));
		assertEquals("50P1234", NamingConventions.convertContextIdToXMLSafeName(50, 1234));
		assertEquals("50P0", NamingConventions.convertContextIdToXMLSafeName(50, 0));
		
		//With workspace - non-reusable
		assertEquals(NamingConventions.FLOWLINE_WORKSPACE_NAME +  ":50N1234", NamingConventions.getFullFlowlineLayerName(50, -1234, false));
		assertEquals(NamingConventions.CATCHMENT_WORKSPACE_NAME + ":50P1234", NamingConventions.getFullCatchmentLayerName(50, 1234, false));
		assertEquals(NamingConventions.CATCHMENT_WORKSPACE_NAME + ":50P0", NamingConventions.getFullCatchmentLayerName(50, 0, false));
		
		//With workspace - reusable
		assertEquals(NamingConventions.FLOWLINE_REUSABLE_WORKSPACE_NAME +  ":50N1234", NamingConventions.getFullFlowlineLayerName(50, -1234, true));
		assertEquals(NamingConventions.CATCHMENT_REUSABLE_WORKSPACE_NAME + ":50P1234", NamingConventions.getFullCatchmentLayerName(50, 1234, true));
		assertEquals(NamingConventions.CATCHMENT_REUSABLE_WORKSPACE_NAME + ":50P0", NamingConventions.getFullCatchmentLayerName(50, 0, true));
	 }
	 
	 @Test
	 public void trySomeSimpleConvertionsBackToNumbers() {
		 
		assertEquals(-1234, NamingConventions.convertXMLSafeNameToContextId("50N1234"));
		assertEquals(1234, NamingConventions.convertXMLSafeNameToContextId("50P1234"));
		assertEquals(0, NamingConventions.convertXMLSafeNameToContextId("50P0"));
	 }
	 
	 @Test
	 public void modelResourceRegex() {
		assertTrue("50P721080852.dbf".matches(NamingConventions.buildModelRegex(50)));
		assertTrue("50N721080852.dbf".matches(NamingConventions.buildModelRegex(50)));
		assertTrue("0P721080852.dbf".matches(NamingConventions.buildModelRegex(0)));
		assertTrue("0NP721080852.dbf".matches(NamingConventions.buildModelRegex(0)));
		assertTrue("50P721080".matches(NamingConventions.buildModelRegex(50)));
		
		
		assertFalse("51P721080".matches(NamingConventions.buildModelRegex(50)));
		assertFalse("50Z721080".matches(NamingConventions.buildModelRegex(50)));
		assertFalse("51p721080".matches(NamingConventions.buildModelRegex(50)));
		assertFalse("51n721080".matches(NamingConventions.buildModelRegex(50)));
	 }
}
