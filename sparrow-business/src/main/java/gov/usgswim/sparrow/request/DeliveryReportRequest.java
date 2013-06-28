package gov.usgswim.sparrow.request;

import gov.usgswim.Immutable;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.AggregationLevel;
import gov.usgswim.sparrow.domain.TerminalReaches;
import java.io.Serializable;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request for a delivery related report.
 *
 * @author eeverman
 */
@Immutable
public class DeliveryReportRequest implements Serializable {

	private static final long serialVersionUID = 2L;
	
	private final int cachedHash;
	
	private final AdjustmentGroups adjustmentGroups;
	private final TerminalReaches terminalReaches;
	private final AggregationLevel aggLevel;
	private final boolean reportYield;
	

	/**
	 * 
	 * @param adjustmentGroups
	 * @param terminalReaches
	 * @param aggLevel
	 * @param reportYield True if yield should be reported, false to report load.
	 */
	public DeliveryReportRequest(AdjustmentGroups adjustmentGroups, TerminalReaches terminalReaches, AggregationLevel aggLevel, boolean reportYield) {
	    this.adjustmentGroups = adjustmentGroups;
	    this.terminalReaches = terminalReaches;
			this.aggLevel = aggLevel;
			this.reportYield = reportYield;
			
	    cachedHash = buildHashCode();
	}
	
	/**
	 * Constructor that assumes the aggregation level is NONE.
	 * 
	 * @param adjustmentGroups
	 * @param terminalReaches
	 * @param reportYield True if yield should be reported, false to report load.
	 */
	public DeliveryReportRequest(AdjustmentGroups adjustmentGroups, TerminalReaches terminalReaches, boolean reportYield) {
	    this.adjustmentGroups = adjustmentGroups;
	    this.terminalReaches = terminalReaches;
			this.aggLevel = AggregationLevel.NONE;
			this.reportYield = reportYield;
					
	    cachedHash = buildHashCode();
	}
	
	public AdjustmentGroups getAdjustmentGroups() {
		return adjustmentGroups;
	}


	public TerminalReaches getTerminalReaches() {
		return terminalReaches;
	}
	
	public AggregationLevel getAggLevel() {
		return aggLevel;
	}
	
	/**
	 * Returns true to indicate that the report should show load as yield.
	 * If false, load is reported as load.
	 * @return 
	 */
	public boolean isReportYield() {
		return reportYield;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof DeliveryReportRequest) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}
	

	@Override
	public synchronized int hashCode() {
		return cachedHash;
	}
	
	private int buildHashCode() {
		HashCodeBuilder hash = new HashCodeBuilder(3457, 343463);
		hash.append(adjustmentGroups);
		hash.append(terminalReaches);
		hash.append(aggLevel);
		hash.append(reportYield);

		return hash.toHashCode();
	}


}
