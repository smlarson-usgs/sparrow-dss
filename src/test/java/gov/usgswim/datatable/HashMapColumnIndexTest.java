package gov.usgswim.datatable;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class HashMapColumnIndexTest {
	
	
	long[] idArray = new long[]{4, 9, 27, 0, -1};
	
	@Test
	public void testArrayConstruction() {
		HashMapColumnIndex index = new HashMapColumnIndex(idArray);
		
		//Check row ID for a given row number
		assertEquals(new Long(4), index.getIdForRow(0));
		assertEquals(new Long(9), index.getIdForRow(1));
		assertEquals(new Long(27), index.getIdForRow(2));
		assertEquals(new Long(0), index.getIdForRow(3));
		assertEquals(new Long(-1), index.getIdForRow(4));
		
		//check row numbers for given IDs
		assertEquals(0, index.getRowForId(4L));
		assertEquals(1, index.getRowForId(9L));
		assertEquals(2, index.getRowForId(27L));
		assertEquals(3, index.getRowForId(0L));
		assertEquals(4, index.getRowForId(-1L));
		
		//Validation
		assertTrue(index.isValidForRowNumber(1));
		assertTrue(index.isValidForRowNumber(4));
		assertFalse(index.isValidForRowNumber(-1));
		assertFalse(index.isValidForRowNumber(5));
	}
	
	@Test
	public void testListConstruction() {
		List<Long> idList = new ArrayList<Long>();
		
		for (int i = 0; i < idArray.length; i++) {
			idList.add(idArray[i]);
		}
		
		HashMapColumnIndex index = new HashMapColumnIndex(idList);
		
		//Check row ID for a given row number
		assertEquals(new Long(4), index.getIdForRow(0));
		assertEquals(new Long(9), index.getIdForRow(1));
		assertEquals(new Long(27), index.getIdForRow(2));
		assertEquals(new Long(0), index.getIdForRow(3));
		assertEquals(new Long(-1), index.getIdForRow(4));
		
		//check row numbers for given IDs
		assertEquals(0, index.getRowForId(4L));
		assertEquals(1, index.getRowForId(9L));
		assertEquals(2, index.getRowForId(27L));
		assertEquals(3, index.getRowForId(0L));
		assertEquals(4, index.getRowForId(-1L));
		
		//Validation
		assertTrue(index.isValidForRowNumber(1));
		assertTrue(index.isValidForRowNumber(4));
		assertFalse(index.isValidForRowNumber(-1));
		assertFalse(index.isValidForRowNumber(5));
	}
	
	
}
