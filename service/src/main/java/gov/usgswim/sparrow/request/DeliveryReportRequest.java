package gov.usgswim.sparrow.request;

import gov.usgswim.Immutable;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
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

	private static final long serialVersionUID = 1L;
	
	private final int cachedHash;
	
	private final AdjustmentGroups adjustmentGroups;
	private final TerminalReaches terminalReaches;
	

	/**
	 * Fully speced constructor.
	 * @param adjustmentGroups
	 * @param terminalReaches
	 */
	public DeliveryReportRequest(AdjustmentGroups adjustmentGroups, TerminalReaches terminalReaches) {
	    this.adjustmentGroups = adjustmentGroups;
	    this.terminalReaches = terminalReaches;
	    cachedHash = buildHashCode();
	}
	
	public AdjustmentGroups getAdjustmentGroups() {
		return adjustmentGroups;
	}


	public TerminalReaches getTerminalReaches() {
		return terminalReaches;
	}
	

	@Override
	public synchronized int hashCode() {
		return cachedHash;
	}
	
	public int buildHashCode() {
		HashCodeBuilder hash = new HashCodeBuilder(3457, 343463);
		hash.append(adjustmentGroups);
		hash.append(terminalReaches);

		return hash.toHashCode();
	}


}
