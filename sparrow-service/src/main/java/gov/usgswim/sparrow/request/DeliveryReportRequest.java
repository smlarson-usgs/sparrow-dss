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
	

	/**
	 * Fully speced constructor.
	 * @param adjustmentGroups
	 * @param terminalReaches
	 */
	public DeliveryReportRequest(AdjustmentGroups adjustmentGroups, TerminalReaches terminalReaches, AggregationLevel aggLevel) {
	    this.adjustmentGroups = adjustmentGroups;
	    this.terminalReaches = terminalReaches;
			this.aggLevel = aggLevel;
	    cachedHash = buildHashCode();
	}
	
	public DeliveryReportRequest(AdjustmentGroups adjustmentGroups, TerminalReaches terminalReaches) {
	    this.adjustmentGroups = adjustmentGroups;
	    this.terminalReaches = terminalReaches;
			this.aggLevel = AggregationLevel.NONE;
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

		return hash.toHashCode();
	}


}
