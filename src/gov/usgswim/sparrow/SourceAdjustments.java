package gov.usgswim.sparrow;

import java.util.ArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class SourceAdjustments {

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
			for(AdjustmentType type: AdjustmentType.values()) {
				if (type._name.equalsIgnoreCase(name)) return type;
			}
			return null;
		}
			
	};
	
	protected ArrayList<Adjustment> adjustments;
	
	public SourceAdjustments() {
	}
	
	
	/**
	 * Sets the adjustments for the given type.
	 * 
	 * Any existing adjustments of the same type are removed and replaces with
	 * the new ones, which are parsed from the passed string.
	 * 
	 * Adjustments are ordered.  Ajustments of the same type may overrite each other
	 * and the last ones wins.  Thus, the order must be preserved.
	 * 
	 * @param adjs A map containing adjustment names and values.
	 */
	public synchronized void setAdjustment(Map adjs) {
	
		for(AdjustmentType type: AdjustmentType.values()) {
			String val = (String) adjs.get( type.getName() );
			
			switch (type) {
				case GROSS_ADJUST:
				
					if (this.adjustments == null) {
						this.adjustments = parseGrossAdj(val);
					} else {
						for(int i=0; i<this.adjustments.size(); i++) {
							Adjustment a = this.adjustments.get(i);
							if (AdjustmentType.GROSS_ADJUST.equals(a.getType())) {
								this.adjustments.remove(i);
								i--;		//Rollback the index b/c we need to inspect this index again.
							}
						}
						this.adjustments.addAll(parseGrossAdj(val));
					}
					
					break;
				default:
					throw new IllegalStateException("Unexpected AdjustmentType.");
			}
		}
	}
	
	
	/**
	 * Sets the adjustments for the given type.
	 * 
	 * Any existing adjustments of the same type are removed and replaces with
	 * the new ones, which are parsed from the passed string.
	 * 
	 * Adjustments are ordered.  Ajustments of the same type may overrite each other
	 * and the last ones wins.  Thus, the order must be preserved.
	 * @param type The key for the AdjustmentType
	 * @param value An adjustment string to be parsed.
	 */
	public synchronized void setAdjustment(String type, String value) {
	
		AdjustmentType aType = AdjustmentType.find(type);
		
		if (aType != null) {
			setAdjustment(aType, value);
		} else {
			throw new IllegalArgumentException("Adjustment Type '" + type + "' not recognized.");
		}
		
	}
	
	/**
	 * Sets the adjustments for the given type.
	 * 
	 * Any existing adjustments of the same type are removed and replaces with
	 * the new ones, which are parsed from the passed string.
	 * 
	 * Adjustments are ordered.  Ajustments of the same type may overrite each other
	 * and the last ones wins.  Thus, the order must be preserved.
	 * 
	 * @param type An AdjustmentType for the adjustment
	 * @param value	A value string to be parsed.
	 */
	public synchronized void setAdjustment(AdjustmentType type, String value) {
		
		switch (type) {
			case GROSS_ADJUST:
			
				if (adjustments == null) {
					adjustments = parseGrossAdj(value);
				} else {
					for(int i=0; i<adjustments.size(); i++) {
						Adjustment a = adjustments.get(i);
						if (type.equals(a.getType())) {
							adjustments.remove(i);
							i--;		//Rollback the index b/c we need to inspect this index again.
						}
					}
					adjustments.addAll(parseGrossAdj(value));
				}
			
			break;
			default:	throw new IllegalArgumentException(type + " is unhandled.");
		}
		
	}
	
	/**
	 * Adds the adjustments at the end of the adjustment list.
	 * 
	 * Adjustments are ordered.  Ajustments of the same type may overrite each other
	 * and the last ones wins.  Thus, the order must be preserved.
	 * @param adj  An adjustment to add to the end of the list.
	 */
	public synchronized void addAdjustment(Adjustment adj) {
		if (adjustments == null) {
			adjustments = new ArrayList<Adjustment>();
		}
		
		adjustments.add(adj);
	}

	/**
	 * Modifies the passed source by creating a coef-view using the same underlying
	 * data, but adding coefficients.  This strategy allows the underlying data
	 * to be cached and not modified.
	 * 
	 * If no adjustments are made, the passed data is returned and no view is created,
	 * so DO NOT count on the returned data being a view - check using ==.
	 * 
	 * @param source
	 * @return
	 */
	public synchronized Data2D adjustSources(Data2D source) {

		if (adjustments != null) {
			
			
			Data2DColumnCoefView view = new Data2DColumnCoefView(source);
			
			for (int i = 0; i < adjustments.size(); i+=2)  {
				Adjustment a = adjustments.get(i);
				view.setCoef(a.getId(), a.getValue());
			}
			
			return view;
			
		} else {
			
			return source;	//no adjustment
		}
		
	}
	
	public List<Adjustment> getAdjustments() {
		return Collections.unmodifiableList(adjustments);
	}
	
	protected ArrayList<Adjustment> parseGrossAdj(String adj) {

		adj = StringUtils.trimToNull(adj);
		
		if (adj != null ) {
			String[] adjs = StringUtils.split(adj, ", ;:|[]{}()");
			ArrayList<Adjustment> list = new ArrayList<Adjustment>(adjs.length / 2 + 1);
			
			for (int i = 0; i < adjs.length; i+=2)  {
				int col = Integer.parseInt(adjs[i]);
				double coef = Double.parseDouble(adjs[i + 1]);
				list.add(new Adjustment(AdjustmentType.GROSS_ADJUST, col, coef));
			}
			
			return list;
		} else {
			return null;
		}
		
	}
	

}
