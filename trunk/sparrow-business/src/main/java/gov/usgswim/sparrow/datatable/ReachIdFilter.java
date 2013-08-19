package gov.usgswim.sparrow.datatable;

import java.io.Serializable;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.filter.RowFilter;
import gov.usgswim.sparrow.domain.TerminalReaches;
import java.util.Collection;

/**
 * A DataTable RowFilter that accepts all rows that are contained in the
 * TerminalReaches instance passed in the constructor.  The table being filtered
 * must have row IDs for rows to be accepted, and those row IDs must be the reach
 * IDs (ie, they must match the IDs in TerminalReaches).
 *
 * @author eeverman
 */
public class ReachIdFilter implements RowFilter, Serializable {

	private static final long serialVersionUID = 2L;

        private final Collection<Long> reachIds;

	public ReachIdFilter(Collection<Long> reachIds) {
		if(null == reachIds){
			throw new IllegalArgumentException();
		}
		this.reachIds = reachIds;
	}

	/**
	 * The table being filtered must have row IDs, otherwise no rows will be
	 * accepted.
	 */
	@Override
	public boolean accept(DataTable table, int rowNum) {
		return reachIds.contains(table.getIdForRow(rowNum));
	}

	@Override
	public Integer getEstimatedAcceptCount() {
		return reachIds.size();
	}

}
