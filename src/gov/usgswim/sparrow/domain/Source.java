package gov.usgswim.sparrow.domain;

/**
 * Domain Object representing a single source (used by many reaches) for a SPARROW Model.
 * 
 * Note that there no link back to the Model - this is a one-way metadata
 * class.  There *is* a getModelId method, thought this is mostly to completely
 * represent the db columns then a back-reference.
 */
public interface Source {
	
	/**
	 * The UUID for this source.  This is the database srcId.
	 * @return
	 */
	public Long getId();
	
	/**
	 * The intra-model srcId for this source.
	 *
	 * It is unique only within a model.
	 * @return
	 */
	public int getIdentifier();
	
	/**
	 * The SPARROW model name for this source.
	 * 
	 * This name is often abbreviated to the point that it is not human readable.
	 * @return
	 */
	public String getName();
	
	/**
	 * A human readable name for the source.
	 * @return
	 */
	public String getDisplayName();
	
	/**
	 * Descriptive information about the source.
	 * @return
	 */
	public String getDescription();
	
	/**
	 * Display order for the this source relative to other sources in a model.
	 * @return
	 */
	public int getSortOrder();
	
	
	/**
	 * The UUID (database srcId) of the model this source belongs to.
	 * @return
	 */
	public Long getModelId();
	
	/**
	 * The element that the source is measuring.
	 * @return The element that the source is measuring.
	 */
	public String getConstituent();
	
	/**
	 * The units in which the constituent is measured.
	 * @return The units in which the constituent is measured.
	 */
	public String getUnits();
}
