package gov.usgswim.sparrow;

/**
 * The series of Uncertainty data to load.
 * 
 * Read 'series' broadly:  For source specific series, the source must also
 * be specified for a complete series specification.
 * 
 * @author eeverman
 *
 */
public enum UncertaintySeries {

	TOTAL(false, false),
	TOTAL_PER_SOURCE(true, false),
	INCREMENTAL(false, true),
	INCREMENTAL_PER_SOURCE(true, true);
	
	/**
	 * True if the series is source specific (i.e., not all sources together)
	 */
	private boolean sourceSpecific;
	
	/**
	 * True if the series is incremental (i.e., not including upstream)
	 */
	private boolean incremental;
	
	UncertaintySeries(boolean _sourceSpecific, boolean _incremental) {
		sourceSpecific = _sourceSpecific;
		incremental = _incremental;
	}
	
	/**
	 * Returns true if this series is incremental, i.e., it does not look at
	 * combined errors of upstream reaches.
	 * 
	 * @return
	 */
	public boolean isIncremental() {
		return incremental;
	}
	
	/**
	 * Returns the inverse of isIncremental.
	 * @return
	 */
	public boolean isTotal() {
		return !incremental;
	}
	
	/**
	 * True if this series refers to a specific source, thus requires that we
	 * know which source we are talking about.
	 * 
	 * @return
	 */
	public boolean isSourceSpecific() {
		return sourceSpecific;
	}
	
	/**
	 * Returns the inverse of isSourceSpecific.
	 * 
	 * @return
	 */
	public boolean isAllSources() {
		return !sourceSpecific;
	}
}
