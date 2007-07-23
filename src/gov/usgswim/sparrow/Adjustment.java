package gov.usgswim.sparrow;

import org.apache.commons.lang.builder.HashCodeBuilder;
import gov.usgswim.Immutable;

import java.io.Serializable;

/**
 * Represents a single adjustment to a source
 */
@Immutable
public class Adjustment<K extends Comparable<K>> implements Comparable<Adjustment<K>>, Serializable {
	private final int _srcId;
	private final int _reachId;
	private final double _val;
	private final AdjustmentType _type;
	
	/**
	 * A string containing a delimited list, in pairs, of the source _srcId and the
	 * decimal percentage to adjust it.  It is an error to provide a string that
	 * contains anything other then numbers and delimiters, or that contains
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
		SPECIFIC_ADJUST("specific", "Adjust a specific source at a specific reach.");
		
		private String _name;
		private String _desc;
		AdjustmentType(String name, String description) {
			_name = name;
			_desc = description;
		}
		
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
			return null;
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
	
	public void adjust(Data2D source, Data2D srcIndex, Data2D reachIndex) throws Exception {
		switch (_type) {
			case GROSS_SRC_ADJUST: {
				if (source instanceof Data2DColumnCoefView) {
					Data2DColumnCoefView data = (Data2DColumnCoefView)source;
					
					data.setCoef(mapSourceId(_srcId, srcIndex), getValue());
					
				} else {
					throw new Exception("Expecting instance of Data2DColumnCoefView");
				}
				break;
			}
			case SPECIFIC_ADJUST: {
				if (source instanceof Data2DWritable) {
					Data2DWritable data = (Data2DWritable)source;
					
					int reachRow = reachIndex.findRowById((double)_reachId);
					
					if (reachRow != -1) {
						data.setValueAt(_val, reachRow, mapSourceId(_srcId, srcIndex));
					} else {
						throw new Exception("Reach ID #" + _reachId + " not found");
					}
				} else {
					throw new Exception("Expecting instance of Data2DColumnCoefView");
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
	public int mapSourceId(int id, Data2D srcIndex) throws Exception {
		if (srcIndex != null) {
		
			int i = srcIndex.findRowById((double)id);

			if (i > -1) {
				return i;
			} else  {
				throw new Exception ("Source for id " + id + " not found");
			}
		} else {
			if (id > 0) {
				return id - 1;
			} else {
				throw new Exception("Invalid source id " + id + ", which must be greater then zero.");
			}
		}
	}

	public int compareTo(Adjustment<K> that) {
		final int BEFORE = 1;
		final int EQUAL = 0;
		final int AFTER = -1;
		
		if (_type.compareTo(that.getType()) != 0) {
			return _type.compareTo(that.getType());
		} else {
			if (that.getSrcId() < _srcId) {
				return BEFORE;
			} else if (that.getSrcId() > _srcId) {
				return AFTER;
			} else {
				if (that.getValue() < _val) {
					return BEFORE;
				} else if (that.getValue() > _val) {
					return AFTER;
				} else {
					return EQUAL;
				}
			}
			
		}
	}
	
	/**
	 * Returns true if the adjustment is conflicts with this adjustment.
	 * 
	 * @param that
	 * @return
	 */
	public boolean isConflicting(Adjustment that) {
		return (_type.equals(that.getType()) && that.getSrcId() == _srcId && _val != that.getValue());
	}

	
	public boolean equals(Object object) {
		if (object instanceof Adjustment) {
			Adjustment that = (Adjustment) object;
			return (_type.equals(that.getType()) && that.getSrcId() == _srcId && _val == that.getValue());
		} else {
			return false;
		}
	}

	public int hashCode() {
		//starts w/ some random numbers just to create unique results
		return new HashCodeBuilder(123, 11).
			append(_type.ordinal()).
			append(_srcId).
			append(_val).
			toHashCode();
	}
}
