package gov.usgswim.datatable;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class HashMapColumnIndexTest {
	
	
	long[] idArray = new long[]{4, 9, 27, 0, -1};
	
	@Test
	public void testArrayConstruction() {
		HashMapColumnIndex index = new HashMapColumnIndex(idArray);
		
		doAssertionsForStdData(index);
	}
	
	@Test
	public void testListConstruction() {
		List<Long> idList = new ArrayList<Long>();
		
		for (int i = 0; i < idArray.length; i++) {
			idList.add(idArray[i]);
		}
		
		HashMapColumnIndex index = new HashMapColumnIndex(idList);
		
		doAssertionsForStdData(index);
	}
	
	
	@Test
	public void testConversionToMutable() {
		HashMapColumnIndex orgImmIndex = new HashMapColumnIndex(idArray);
		MutableColumnIndex mutIndex = orgImmIndex.toMutable();

		doAssertionsForStdData(mutIndex);
		mutIndex.setRowId(1, 99);
		mutIndex.setRowId(2, new Long(101));
		
		//Check modified index values
		assertEquals(new Long(99), mutIndex.getIdForRow(1));
		assertEquals(1, mutIndex.getRowForId(99));
		assertEquals(new Long(101), mutIndex.getIdForRow(2));
		assertEquals(2, mutIndex.getRowForId(101));
		
		//Verify the original index was not affected
		doAssertionsForStdData(orgImmIndex);
	}
	
	@Test
	public void testToImmutable() {
		HashMapColumnIndex orgImmIndex = new HashMapColumnIndex(idArray);
		ColumnIndex newImmIndex = orgImmIndex.toImmutable();

		//These can be (and should be) the same instance
		assertEquals(orgImmIndex, newImmIndex);
	}
	
	/////
	/////
	/////
	
	protected void doAssertionsForStdData(ColumnIndex index) {
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
		
		//Check null is returned for row IDs outside the range of rows
		assertNull(index.getIdForRow(-1));
		assertNull(index.getIdForRow(5));
		
		//check that -1 is returned for non-existing IDs
		assertEquals(-1, index.getRowForId(99L));
		assertEquals(-1, index.getRowForId(-99L));
		assertEquals(-1, index.getRowForId(null));
		
		//Validation
		assertEquals(4, index.getMaxRowNumber());
		assertTrue(index.isFullyPopulated());
	}
	
	
}
