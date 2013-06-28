package gov.usgswim.sparrow;

/**
 * Identical to SparrowServiceTestBaseClass, but it loads the model 50 predict data
 * from a file instead of from the db.
 * @author eeverman
 *
 */
public class SparrowServiceTestBaseWithDBandCannedModel50 extends SparrowServiceTestBaseWithDB {

	/**
	 * Returns true to force model 50 to be loaded from file instead of from 
	 * the db.
	 */
	@Override
	public boolean loadModelDataFromFile() {
		return true;
	}

}
