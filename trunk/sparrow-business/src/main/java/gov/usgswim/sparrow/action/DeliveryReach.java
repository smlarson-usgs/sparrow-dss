package gov.usgswim.sparrow.action;


public class DeliveryReach implements Comparable<DeliveryReach>, ReachValue {
	/** The row index of this reach	 */
	private int row;
	
	/** The calc'ed delivery coef for this reach	 */
	private double delivery;
	
	/** The hydrological sequence number of this reach */
	private int hydseq;
	
	/** The reach or reaches downstream of this reach.
	 *  If there is a split, there could be more than one downstream reach.
	 */
	private DeliveryReach[] downstreamReaches = null;
	
	/**
	 * Create a reach with no downstream reach.
	 * @param row
	 * @param delivery
	 * @param hydseq
	 */
	public DeliveryReach(int row, double delivery, int hydseq) {
		this.row = row;
		this.delivery = delivery;
		this.hydseq = hydseq;
		downstreamReaches = new DeliveryReach[0];
	}
	
	/**
	 * Create a reach with a single known downstream reach.
	 * If additional downstream reaches are discovered, they can be added.
	 * @param row
	 * @param delivery
	 * @param hydseq
	 * @param downstreamReach
	 */
	public DeliveryReach(int row, double delivery, int hydseq, DeliveryReach downstreamReach) {
		this.row = row;
		this.delivery = delivery;
		this.hydseq = hydseq;
		downstreamReaches = new DeliveryReach[] { downstreamReach };
	}

	/**
	 * The reach with the highest hydseq should always have highest priority
	 * in a priority queue.  That is, it should come out of the queue first.
	 */
	@Override
	public int compareTo(DeliveryReach o) {
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
	 * Adds a downstream reach to the list
	 * @param downstream
	 */
	public void addDownstreamReach(DeliveryReach downstream) {
		DeliveryReach[] newDSRs = new DeliveryReach[downstreamReaches.length + 1];
		System.arraycopy(downstreamReaches, 0, newDSRs, 0, downstreamReaches.length);
		newDSRs[newDSRs.length - 1] = downstream;
		downstreamReaches = newDSRs;
	}
	
	public void addDelivery(double addedDelivery) {
		delivery = delivery + addedDelivery;
	}

	public int getRow() {
		return row;
	}

	public double getDelivery() {
		return delivery;
	}
	
	/**
	 * Duplicates getFraction to implement the ReachValue interface.
	 * 
	 * @return 
	 */
	public double getValue() {
		return delivery;
	}

	public int getHydseq() {
		return hydseq;
	}
	
	public DeliveryReach[] getDownstreamReaches() {
		return downstreamReaches;
	}

	/**
	 * For the purposes of this application, identity is completely tied to
	 * row.  If duplicates are found, the delivery vals should be merged into
	 * a single instance (which happens often).  Thus, it needs to be easy to
	 * find these duplicates.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DeliveryReach) {
			DeliveryReach comp = (DeliveryReach) obj;
			
			return (comp.getRow() == row);
		} else {
			return false;
		}
	}
}
