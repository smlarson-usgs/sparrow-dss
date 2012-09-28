package gov.usgswim.sparrow;

import gov.usgs.cida.datatable.DataTable;

/**
 * Prediction topo data - the basic data loaded from the DB to represent basic
 * topo data.
 * 
 * Note that several operations are available based on row or reach ID.  In all
 * cases, row access will be faster.
 * 
 * @author eeverman
 */
public interface TopoData extends DataTable.Immutable {

	//Basic Access Methods
	
	/**
	 * The From node of the topology for this reach.
	 * @param row
	 * @return 
	 */
	public int getFromNode(int row);
	
	/**
	 * The To Node of the topology for this reach.
	 * @param row
	 * @return 
	 */
	public int getToNode(int row);
	
	/**
	 * True if this reach transmits its load to the downstream To Node.
	 * @param row
	 * @return 
	 */
	public boolean isIfTran(int row);
	
	/**
	 * The hydrological sequence order of this reach.  Leafy reaches should have
	 * the lowest hyd seq numbers, downstream reaches the highest.
	 * @param row
	 * @return 
	 */
	public int getHydSeq(int row);
	
	/**
	 * Returns true if the reach is a shore line reach.
	 * @param row
	 * @return 
	 */
	public boolean isShoreReach(int row);
	
	/**
	 * Returns the fraction for the reach, which is the fraction of load
	 * entering the top of this reach from the From Node.  It should normally
	 * be one in most cases except at a diversion.
	 * @param row
	 * @return 
	 */
	public double getFrac(int row);
	
	
	//
	//Topological Methods
	
	/**
	 * Returns true if a reach can have upstream reaches.
	 * 
	 * A reach would not have upstream reaches if it is a shore reach.
	 * 
	 * @param row
	 * @return 
	 */
	public boolean isAllowedUpstreamReaches(int row);
	
	/**
	 * Returns true if a reach can have upstream reaches.
	 * 
	 * A reach would not have upstream reaches if it is a shore reach.
	 * 
	 * @param reachId
	 * @return 
	 */
	public boolean isIdAllowedUpstreamReaches(long reachId);
	
	/**
	 * Returns true if a reach can have downstream reaches.
	 * 
	 * A reach would not be considered to have downstream reaches if it is a
	 * shore reach or its IFTRAN is turned off (zero).
	 * 
	 * @param row
	 * @return 
	 */
	public boolean isAllowedDownstreamReaches(int row);
	
	/**
	 * Returns true if a reach can have downstream reaches.
	 * 
	 * A reach would not be considered to have downstream reaches if it is a
	 * shore reach or its IFTRAN is turned off (zero).
	 * 
	 * @param reachId
	 * @return 
	 */
	public boolean isIdAllowedDownstreamReaches(long reachId);
	
	/**
	 * Returns true if this reach is part of a diversion, meaning that not all of
	 * the upstream flow enters the top of this reach.
	 * 
	 * @param row
	 * @return 
	 */
	public boolean isPartOfDiversion(int row);
	
	/**
	 * Returns true if this reach is part of a diversion, meaning that not all of
	 * the upstream flow enters the top of this reach.
	 * 
	 * @param reachId
	 * @return 
	 */
	public boolean isIdPartOfDiversion(long reachId);
	
	/**
	 * Returns a list of 'valid' upstream reaches.
	 * 
	 * The specified reach must return true for reachCanHaveUpstreamReaches or
	 * no reaches are returned.  If the reach is allowed to have upstream reaches,
	 * only reaches that return true for reachCanHaveDownstreamReaches are returned.
	 * 
	 * @param row
	 * @return An array of row numbers or an empty array if no reaches are allowed or found.
	 */
	public int[] findAllowedUpstreamReaches(int row);
	
	/**
	 * Returns a list of 'valid' upstream reaches.
	 * 
	 * The specified reach must return true for reachCanHaveUpstreamReaches or
	 * no reaches are returned.  If the reach is allowed to have upstream reaches,
	 * only reaches that return true for reachCanHaveDownstreamReaches are returned.
	 * 
	 * @param reachId
	 * @return  An array of reach IDs or an empty array if no reaches are allowed or found.
	 */
	public long[] findAllowedUpstreamReachIds(long reachId);
	
	/**
	 * Returns a list of all reaches immediately upstream of the specified reach.
	 * 
	 * WARNING: This method should normally not be used since reaches that are
	 * topologically upstream are not necessarily useful for model calculations.
	 * Use findAllowedUpstreamReaches instead.
	 * 
	 * @param row
	 * @return An array of reach row numbers or an empty array if no reaches are found.
	 */
	public int[] findAnyUpstreamReaches(int row);
	
	/**
	 * Returns a list of all reaches immediately upstream of the specified reach.
	 * 
	 * WARNING: This method should normally not be used since reaches that are
	 * topologically upstream are not necessarily useful for model calculations.
	 * Use findAllowedUpstreamReaches instead.
	 * 
	 * @param reachId
	 * @return An array of reach row IDs or an empty array if no reaches are found.
	 */
	public long[] findAnyUpstreamReachIds(long reachId);

	/**
	 * Centralized logic to determine the FRAC (fraction) value for a single reach.
	 * 
	 * NOTE:  The corrected value should NOT be used for creating predicted load
	 * values, since the reason the FRAC values are off may be a modeler's attempt
	 * to simulate some type of process that is not represented other places in the
	 * model.  It should be used, however, for calculating watershed area.
	 * 
	 * FRAC values at a node (where reaches connect) should always sum to one,
	 * meaning that all the flow is allocated in some way to downstream nodes.
	 * However, in some cases modelers have modified the FRAC values to not total
	 * to one to simulate non-network outflows, like municipal water utilities.
	 * 
	 * In other cases, the network modifications might just be errors.  The logic
	 * here forces the sum back to one and adjusts the returned frac value to be
	 * corrected for that adjusted total.
	 * 
	 * In the special case of a shore reach, 0 is always returned.  It really has
	 * no meaning since no load should enter the top of a shore reach.
	 * 
	 * @param row
	 * @return An adjusted frac value that would sum to 1 if called for all reaches at a node.
	 */
	public double getCorrectedFracForRow(int row);
	
	/**
	 * Centralized logic to determine the FRAC (fraction) value for a single reach.
	 * 
	 * NOTE:  The corrected value should NOT be used for creating predicted load
	 * values, since the reason the FRAC values are off may be a modeler's attempt
	 * to simulate some type of process that is not represented other places in the
	 * model.  It should be used, however, for calculating watershed area.
	 * 
	 * FRAC values at a node (where reaches connect) should always sum to one,
	 * meaning that all the flow is allocated in some way to downstream nodes.
	 * However, in some cases modelers have modified the FRAC values to not total
	 * to one to simulate non-network outflows, like municipal water utilities.
	 * 
	 * In other cases, the network modifications might just be errors.  The logic
	 * here forces the sum back to one and adjusts the returned frac value to be
	 * corrected for that adjusted total.
	 * 
	 * @param reachId
	 * @return An adjusted frac value that would sum to 1 if called for all reaches at a node.
	 * @throws Exception 
	 */
	public double getCorrectedFracForId(long reachId) throws Exception;
	
	
	//
	//Utility Methods
	
	
	/**
	 * Converts an array of row numbers to an array of row IDs.
	 * 
	 * If the passed array is empty or null, an empty array is returned.
	 * 
	 * @param rowNumbers
	 * @return A non-null array of reach Ids
	 */
	public long[] convertRowsToIds(int[] rowNumbers);
	
	
	
	/**
	 * Returns an array of reaches, including this reach, that leave the same
	 * FNODE.
	 * 
	 * Note that some of these reaches may be shore reaches, which for most purposes
	 * you would want to exclude for calculations.
	 * 
	 * @param row
	 * @return A non-null array of reach rows.  May be empty.
	 */
	public int[] findAnyReachLeavingSameNode(int row);
	
	/**
	 * Returns an array of flow reaches, possibly including the passed reach if a
	 * flow reach, leaving the same node that this reach leaves.
	 * 
	 * A 'flow' reach is a reach that, in the model, behaves as a standard river
	 * reach, that is, load enters the top of the reach and may or may not transmit
	 * to its end node.  Thus, this excludes shore reaches.
	 * 
	 * @param row
	 * @return A non-null array of reach rows.  May be empty.
	 */
	public int[] findFlowReachesLeavingSameNode(int row);
}
