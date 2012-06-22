package gov.usgswim.sparrow.datatable;

import java.io.Serializable;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.filter.RowFilter;
import gov.usgswim.sparrow.domain.TerminalReaches;

/**
 * A DataTable RowFilter that accepts all rows that are contained in the
 * TerminalReaches instance passed in the constructor.  The table being filtered
 * must have row IDs for rows to be accepted, and those row IDs must be the reach
 * IDs (ie, they must match the IDs in TerminalReaches).
 * 
 * @author eeverman
 */
public class TerminalReachesRowFilter implements RowFilter, Serializable {

	private static final long serialVersionUID = 1L;
	
	private final TerminalReaches tr;
	
	public TerminalReachesRowFilter(TerminalReaches filterByTheseTerminalReaches) {
		tr = filterByTheseTerminalReaches;
	}
	

	/**
	 * The table being filtered must have row IDs, otherwise no rows will be
	 * accepted.
	 */
	@Override
	public boolean accept(DataTable table, int rowNum) {
		return tr.contains(table.getIdForRow(rowNum));
	}

	@Override
	public Integer getEstimatedAcceptCount() {
		if (tr != null) {
			return tr.size();
		} else {
			return null;
		}
	}

}
