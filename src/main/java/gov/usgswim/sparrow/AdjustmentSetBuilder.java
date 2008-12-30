package gov.usgswim.sparrow;

import gov.usgswim.ImmutableBuilder;
import gov.usgswim.NotThreadSafe;
import gov.usgswim.datatable.DataTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

/**
 * A Builder for AdjustmentSet.
 * 
 * Note that unlike the Immutable AdjustmentSetImm, this class does NOT indicate
 * equality for identical sets of adustments.
 */
@NotThreadSafe
public class AdjustmentSetBuilder implements ImmutableBuilder<AdjustmentSetImm>, AdjustmentSet {

	protected TreeSet<Adjustment> adjustments;

	public AdjustmentSetBuilder() {
		adjustments = new TreeSet<Adjustment>();
	}


	/**
	 * Adds gross adjustments.
	 * 
	 * @param adjs A map containing adjustment names and values.
	 * @deprecated Use addAdjustment
	 */
	public synchronized void addGrossSrcAdjustments(Map adjs) {

		String val = (String) adjs.get( Adjustment.AdjustmentType.GROSS_SRC_ADJUST.toString() );
		adjustments.addAll(parseGrossAdj(val));

	}


	/**
	 * Adds the adjustment.
	 * 
	 * Adjustments have a normal sort order
	 * 
	 * @param adj  An adjustment to add
	 */
	public synchronized void addAdjustment(Adjustment adj) {
		adjustments.add(adj);
	}


	public Adjustment[] getAdjustments() {
		return adjustments.toArray(new Adjustment[adjustments.size()]);
	}

	public int getAdjustmentCount() {
		return adjustments.size();
	}

	public boolean hasAdjustments() {
		return adjustments.size() > 0;
	}

	/**
	 * Reads the gross adjustments from a string
	 * @param adj
	 * @return
	 */
	public List<Adjustment> parseGrossAdj(String adj) {

		adj = StringUtils.trimToNull(adj);

		if (adj != null ) {
			String[] adjs = StringUtils.split(adj, ", ;:|[]{}()");
			List<Adjustment> list = new ArrayList<Adjustment>(adjs.length / 2 + 1);

			for (int i = 0; i < adjs.length; i+=2)  {
				int col = Integer.parseInt(adjs[i]);
				double coef = Double.parseDouble(adjs[i + 1]);
				list.add(new Adjustment(Adjustment.AdjustmentType.GROSS_SRC_ADJUST, col, coef));
			}
			
			return list;
		}
		return null;
	}

	public DataTable adjust(DataTable source, DataTable srcIndex, DataTable reachIndex) throws Exception {
		return AdjustmentSetImm.adjustSources(adjustments, source, srcIndex, reachIndex);
	}

	public AdjustmentSetImm toImmutable() {
		Object[] src = adjustments.toArray();
		Adjustment[] dest = new Adjustment[src.length];

		for (int i = 0; i < src.length; i++)  {
			dest[i] = (Adjustment) src[i];
		}

		return new AdjustmentSetImm(dest);

	}
}

