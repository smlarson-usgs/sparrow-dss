package gov.usgswim.sparrow;

/**
 * A collection of adjustments to make on the sources for a project
 */
public interface AdjustmentSet {
	/**
	 * Creates a new Data2D source by creating a coef-view using the same underlying
	 * data w/ coefficients on top.  This strategy allows the underlying data
	 * to be cached and not modified.
	 *
	 * If no adjustments are made, the passed data is returned and no view is created,
	 * so DO NOT count on the returned data being a view - check using ==.
	 *
	 * @param source
	 * @return
	 */
	public Data2D adjustSources(Data2D source);

	/**
	 * Returns all the adjustments in the correct order.
	 * 
	 * The returned array is safe - it is not tied to the underlying storage.
	 * @return
	 */
	public Adjustment[] getAdjustments();
	
	/**
	 * Returns the number of adjustments
	 * @return
	 */
	public int getAdjustmentCount();
	
	/**
	 * This method is intended to be a full-proof way to determine if the set
	 * has adjustments to make.  Future types of adjustments might not be 'countable',
	 * so this should be more reliable then using t count.
	 * @return
	 */
	public boolean hasAdjustments();
}
