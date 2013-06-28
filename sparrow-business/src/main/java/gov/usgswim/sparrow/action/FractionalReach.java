package gov.usgswim.sparrow.action;


/**
 * Generically represents a reach and an associated fraction.
 * 
 * HydSeq is included to allow processing order to be sorted by the HydSeq
 * in a PriorityQue
 * 
 * @author eeverman
 */
public class FractionalReach implements Comparable<FractionalReach>, ReachValue {
	/** The row index of this reach	 */
	private int row;
	
	/** The calc'ed delivery coef for this reach	 */
	private double fraction;
	
	/** The hydrological sequence number of this reach */
	private int hydseq;
	
	
	/**
	 * Create a reach with no downstream reach.
	 * @param row
	 * @param fraction
	 * @param hydseq
	 */
	public FractionalReach(int row, double fraction, int hydseq) {
		this.row = row;
		this.fraction = fraction;
		this.hydseq = hydseq;
	}
	

	/**
	 * The reach with the highest hydseq should always have highest priority
	 * in a priority queue.  That is, it should come out of the queue first.
	 */
	@Override
	public int compareTo(FractionalReach o) {
		final int BEFORE = 1;
		final int EQUAL = 0;
		final int AFTER = -1;
		
		if (hydseq > o.hydseq) {
			return AFTER;
		} else if (hydseq < o.hydseq) {
			return BEFORE;
		} else {
			//Not really possible for standard hydseq numbers
			return EQUAL;
		}
	}
	
	/**
	 * Adds the fractional value to the current fractional value.
	 * 
	 * @param addedFractionalValue 
	 */
	public void addFraction(double addedFractionalValue) {
		fraction = fraction + addedFractionalValue;
	}

	@Override
	public int getRow() {
		return row;
	}

	public double getFraction() {
		return fraction;
	}
	
	/**
	 * Duplicates getFraction to implement the ReachValue interface.
	 * 
	 * @return 
	 */
	public double getValue() {
		return fraction;
	}

	public int getHydseq() {
		return hydseq;
	}

	/**
	 * For the purposes of this application, identity is completely tied to
	 * row.  If duplicates are found, the delivery vals should be merged into
	 * a single instance (which happens often).  Thus, it needs to be easy to
	 * find these duplicates.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FractionalReach) {
			FractionalReach comp = (FractionalReach) obj;
			
			return (comp.getRow() == row);
		} else {
			return false;
		}
	}
}
