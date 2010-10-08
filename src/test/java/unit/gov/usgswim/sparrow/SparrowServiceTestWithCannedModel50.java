package gov.usgswim.sparrow;

/**
 * Identical to SparrowServiceTest, but it loads the model 50 predict data
 * from a file instead of from the db.
 * @author eeverman
 *
 */
public class SparrowServiceTestWithCannedModel50 extends SparrowServiceTest {

	/**
	 * Returns true to force model 50 to be loaded from file instead of from 
	 * the db.
	 */
	@Override
	public boolean loadModelDataFromFile() {
		return true;
	}

}
