package gov.usgswim.sparrow;

import org.apache.commons.lang.builder.HashCodeBuilder;
import gov.usgswim.Immutable;

import java.io.Serializable;

/**
 * Represents a single adjustment to a source
 */
@Immutable
public class Adjustment<K extends Comparable<K>> implements Comparable<Adjustment<K>>, Serializable {
	private final int id;
	private final double val;
	private final AdjustmentType type;
	
	/**
	 * A string containing a delimited list, in pairs, of the source id and the
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
		GROSS_ADJUST("gross_src_adj", "Bulk adjust the source by multiplying by a coef.");
		
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
	
	public Adjustment(Adjustment.AdjustmentType adjType, int index, double value) {
		type = adjType;
		id = index;
		val = value;
	}

	public Adjustment.AdjustmentType getType() {
		return type;
	}

	public int getId() {
		return id;
	}

	public double getValue() {
		return val;
	}
	
	public void adjust(Data2D source) {
		if (source instanceof Data2DColumnCoefView) {
			Data2DColumnCoefView view = (Data2DColumnCoefView)source;
			view.setCoef(getId(), getValue());
		}
		
	}

	public int compareTo(Adjustment<K> that) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;
		
		if (type.compareTo(that.getType()) != 0) {
			return type.compareTo(that.getType());
		} else {
			if (that.getId() < id) {
				return BEFORE;
			} else if (that.getId() > id) {
				return AFTER;
			} else {
				if (that.getValue() < val) {
					return BEFORE;
				} else if (that.getValue() > val) {
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
		return (type.equals(that.getType()) && that.getId() == id && val != that.getValue());
	}

	
	public boolean equals(Object object) {
		if (object instanceof Adjustment) {
			Adjustment that = (Adjustment) object;
			return (type.equals(that.getType()) && that.getId() == id && val == that.getValue());
		} else {
			return false;
		}
	}

	public int hashCode() {
		//starts w/ some random numbers just to create unique results
		return new HashCodeBuilder(123, 11).
			append(type.ordinal()).
			append(id).
			append(val).
			toHashCode();
	}
}
