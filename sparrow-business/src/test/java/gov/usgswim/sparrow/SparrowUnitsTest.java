package gov.usgswim.sparrow;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SparrowUnitsTest {
	
	
	@Test
	public void verifyParseMethod() throws Exception {
		//case insensitive
		assertEquals(SparrowUnits.KG_PER_SQR_KM_PER_YEAR, SparrowUnits.parse("KG_PER_SQR_KM_PER_YEAR"));
		assertEquals(SparrowUnits.KG_PER_SQR_KM_PER_YEAR, SparrowUnits.parse("kg_PER_SQR_KM_PER_year"));
		
		//fail through to UNKNOWN / UNSPECIFIED if unreadable / null
		assertEquals(SparrowUnits.UNKNOWN, SparrowUnits.parse("not_a_unit"));
		assertEquals(SparrowUnits.UNSPECIFIED, SparrowUnits.parse(null));
		
		//read userName if enum name not recognized
		assertEquals(SparrowUnits.KG_PER_SQR_KM_PER_YEAR, SparrowUnits.parse(SparrowUnits.KG_PER_SQR_KM_PER_YEAR.getUserName()));
		
	}
	
}
