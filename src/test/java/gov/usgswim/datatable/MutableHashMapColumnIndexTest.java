package gov.usgswim.datatable;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MutableHashMapColumnIndexTest {
	
	
	long[] idArray = new long[]{4, 9, 27, 0, -1};
	
	@Test
	public void arrayConstruction() {
		MutableHashMapColumnIndex index = new MutableHashMapColumnIndex(idArray);
		
		doAssertionsForStdData(index);
	}
	
	@Test
	public void listConstruction() {
		List<Long> idList = new ArrayList<Long>();
		
		for (int i = 0; i < idArray.length; i++) {
			idList.add(idArray[i]);
		}
		
		MutableHashMapColumnIndex index = new MutableHashMapColumnIndex(idList);
		
		doAssertionsForStdData(index);
	}
	
	
	@Test
	public void conversionToImmutable() {
		MutableHashMapColumnIndex orgMutIndex = new MutableHashMapColumnIndex(idArray);
		ColumnIndex immIndex = orgMutIndex.toImmutable();
		
		assertFalse(immIndex instanceof MutableColumnIndex);

		doAssertionsForStdData(immIndex);
		orgMutIndex.setRowId(1, 99);
		
		//Check modified index values
		assertEquals(new Long(99), orgMutIndex.getIdForRow(1));
		assertEquals(1, orgMutIndex.getRowForId(99));
		
		//Verify the immutable version was not affected
		doAssertionsForStdData(immIndex);
	}
	
	@Test
	public void toMutableHasADetachedChild() {
		MutableHashMapColumnIndex orgMutIndex = new MutableHashMapColumnIndex(idArray);
		MutableColumnIndex newMutIndex = orgMutIndex.toMutable();

		//These can be (and should be) the same instance
		assertFalse(orgMutIndex.equals(newMutIndex));
		
		
		//Verify data
		doAssertionsForStdData(orgMutIndex);
		
		//Check modified index values
		orgMutIndex.setRowId(1, 99);
		assertEquals(new Long(99), orgMutIndex.getIdForRow(1));
		assertEquals(1, orgMutIndex.getRowForId(99));
		
		//new Mut should be unchanged
		doAssertionsForStdData(newMutIndex);
	}
	
	@Test
	public void toMutableHasADetachedParent() {
		MutableHashMapColumnIndex orgMutIndex = new MutableHashMapColumnIndex(idArray);
		MutableColumnIndex newMutIndex = orgMutIndex.toMutable();

		//These can be (and should be) the same instance
		assertFalse(orgMutIndex.equals(newMutIndex));
		
		
		//Verify data
		doAssertionsForStdData(orgMutIndex);
		
		//Check modified index values
		newMutIndex.setRowId(1, 99);
		assertEquals(new Long(99), newMutIndex.getIdForRow(1));
		assertEquals(1, newMutIndex.getRowForId(99));
		
		//org Mut should be unchanged
		doAssertionsForStdData(orgMutIndex);
	}
	
	@Test
	public void addANewIndexValueOneBeyondTheEndOfTheList() {
		MutableHashMapColumnIndex mutIndex = new MutableHashMapColumnIndex(idArray);
		mutIndex.setRowId(5, 99);
		
		//Check added index value
		assertEquals(new Long(99), mutIndex.getIdForRow(5));
		assertEquals(5, mutIndex.getMaxRowNumber());
		
		//Check previous last value to make sure its not overwritten
		assertEquals(new Long(-1), mutIndex.getIdForRow(4));
		
		//The index is still fully populated
		assertTrue(mutIndex.isFullyPopulated());
	}
	
	@Test
	public void addANewIndexValueSeveralBeyondTheEndOfTheList() {
		MutableHashMapColumnIndex mutIndex = new MutableHashMapColumnIndex(idArray);
		mutIndex.setRowId(14, 99);
		
		//Check added index value
		assertEquals(new Long(99), mutIndex.getIdForRow(14));
		assertEquals(14, mutIndex.getMaxRowNumber());
		
		//Check previous last value to make sure its not overwritten
		assertEquals(new Long(-1), mutIndex.getIdForRow(4));
		
		//All the IDs between the previous end row (4) and the new one (14) should be null
		assertNull(mutIndex.getIdForRow(5));
		assertNull(mutIndex.getIdForRow(6));
		assertNull(mutIndex.getIdForRow(12));
		assertNull(mutIndex.getIdForRow(13));
		
		//And now the index should report that it is not fully populated
		assertFalse(mutIndex.isFullyPopulated());
		
	}
	
	@Test
	public void nonFullyPopulatedShouldThrowAnExceptionOnToImmutable() {
		MutableHashMapColumnIndex mutIndex = new MutableHashMapColumnIndex(idArray);
		mutIndex.setRowId(14, 99);
		assertFalse(mutIndex.isFullyPopulated());
		
		try {
			mutIndex.toImmutable();
		} catch (IllegalArgumentException e) {
			//Expecting the error - ignore, test passes
			return;
		}
		
		assertTrue("This operation should have thrown an error, but it did not.", false);
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
