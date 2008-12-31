package gov.usgswim.sparrow;

import gov.usgswim.Immutable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.adjustment.ColumnCoefAdjustment;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a single adjustment to a source
 */
@Immutable
public class Adjustment<K extends Comparable<K>> implements Comparable<Adjustment<K>>, Serializable {

	private static final long serialVersionUID = 8157974235794300226L;
	private final int _srcId;
	private final int _reachId;
	private final double _val;
	private final AdjustmentType _type;
	// Note: any new fields must be accounted for in the overridden equals, hashcode, and compareTo methods.

	/**
	 * A string containing a delimited list, in pairs, of the source _srcId and the
	 * decimal percentage to adjust it.  It is an error to provide a string that
	 * contains anything other than numbers and delimiters, or that contains
	 * an odd number of values.
	 *
	 * Example:  "1,.25,4,2,8,0"
	 *
	 * In this example:
	 * <ul>
	 * <li>Source #1 has its value multiplied by .25
	 * <li>Source #4 has its value multiplied by 2
	 * <li>Source #8 has its value multiplied by 0 (effectively turning it off)
	 * <li>All other sources not listed as assumed to be unchanged
	 * (that is, they are multiplied by 1).
	 * <ul>
	 */
	public enum AdjustmentType {
		GROSS_SRC_ADJUST("gross-src", "Bulk adjust the source by multiplying by a coef."),
		SPECIFIC_ADJUST("specific", "Adjust a specific source at a specific reach."),
		UNKNOWN("unknown", "Adjustment specified is unknown");

		private String _name;
		private String _desc;
		AdjustmentType(String name, String description) {
			_name = name;
			_desc = description;
		}

		@Override
		public String toString() {
			return _name;
		}

		public String getName() {
			return _name;
		}

		public String getDescription() {
			return _desc;
		}

		public static AdjustmentType find(String name) {
			for(AdjustmentType type: Adjustment.AdjustmentType.values()) {
				if (type._name.equalsIgnoreCase(name)) return type;
			}
			return UNKNOWN;
		}

	};

	public Adjustment(Adjustment.AdjustmentType adjType, int sourceId, double value) {
		_type = adjType;
		_srcId = sourceId;
		_val = value;
		_reachId = -1;
	}

	public Adjustment(Adjustment.AdjustmentType adjType, int sourceId, int reachId, double value) {
		_type = adjType;
		_srcId = sourceId;
		_val = value;
		_reachId = reachId;
	}

	public Adjustment.AdjustmentType getType() {
		return _type;
	}

	public int getSrcId() {
		return _srcId;
	}

	public int getReachId() {
		return _reachId;
	}

	public double getValue() {
		return _val;
	}

	public void adjust(DataTable source, DataTable srcIndex, DataTable reachIndex) throws Exception {
		switch (_type) {
			case GROSS_SRC_ADJUST: {
				if (source instanceof ColumnCoefAdjustment) {
					ColumnCoefAdjustment data = (ColumnCoefAdjustment)source;
//					data.setColumnMultiplier(column, multiplierValue)
					data.setColumnMultiplier(mapSourceId(_srcId, srcIndex), getValue());

				} else {
					throw new Exception("Expecting instance of RowColumnCoefAdjustment");
				}
				break;
			}
			case SPECIFIC_ADJUST: {
				if (source instanceof DataTableWritable) {
					DataTableWritable data = (DataTableWritable)source;

					int reachRow = reachIndex.findFirst(0, Integer.valueOf(_reachId));

					if (reachRow != -1) {
						data.setValue(Double.valueOf(_val), reachRow, mapSourceId(_srcId, srcIndex));
					} else {
						throw new Exception("Reach ID #" + _reachId + " not found");
					}
				} else {
					throw new Exception("Expecting instance of SparseOverrideAdjustment");
				}
				break;
			}
			default:
				throw new Exception("Unsupported Adjustment type '" + _type);
		}

	}

	/**
	 * Maps a source id to its column index in the src data.
	 * 
	 * If there is no source id map, it is assumed that there are no IDs for the sources (i.e.,
	 * the prediction is being run from a text file), and ID are auto generated
	 * such that the first column of the sources is given an id of 1 (not zero).
	 * 
	 * See the PredictionDataSet class, which implements the same strategy (and should
	 * be kept in sync).
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public int mapSourceId(int id, DataTable srcIndex) throws Exception {
		if (srcIndex != null) {

			int i = srcIndex.getRowForId(Long.valueOf(id));

			if (i > -1) {
				// Running from database, so has a sourceid table
				return i;
			}
			throw new Exception ("Source for id " + id + " not found");
		}
		// Running from text file so assume columns in order
		if (id > 0) {
			return id - 1;
		}
		throw new Exception("Invalid source id " + id + ", which must be greater then zero.");

	}

	public int compareTo(Adjustment<K> that) {
		final int BEFORE = 1;
		final int EQUAL = 0;
		final int AFTER = -1;

		int result = _type.compareTo(that.getType());
		if ( result != 0) return result;

		result = _reachId - that.getReachId();
		if (result != 0) return (result > 0)? BEFORE: AFTER;

		result = _srcId - that.getSrcId();
		if (result != 0) return (result > 0)? BEFORE: AFTER;

		double diff = _val - that.getValue();
		if (diff != 0) return (diff > 0)? BEFORE: AFTER;

		return EQUAL;
	}

	/**
	 * Returns true if the adjustment conflicts with this adjustment.
	 * 
	 * @param that
	 * @return
	 */
	public boolean isConflicting(Adjustment<?> that) {
		return (_type.equals(that.getType()) && that.getSrcId() == _srcId && _val != that.getValue());
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Adjustment) {
			Adjustment<?> that = (Adjustment<?>) object;
			return (_type.equals(that.getType()) && that._reachId == _reachId && that.getSrcId() == _srcId && _val == that.getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		//starts w/ some random numbers just to create unique results
		int hash = new HashCodeBuilder(123, 11).
		append(_type.ordinal()).
		append(_reachId).
		append(_srcId).
		append(_val).
		toHashCode();
		return hash;
	}
}

