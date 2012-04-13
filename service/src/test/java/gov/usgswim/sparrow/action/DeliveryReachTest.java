package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.action.DeliveryReach;

import java.util.PriorityQueue;

import org.junit.Test;

/**
 * @author eeverman
 *
 */
public class DeliveryReachTest {

	/**
	 * The reach with the highest hydseq should always have highest priority,
	 * that is, it should come out of the queue first.
	 */
	@Test
	public void checkProperOrdering() {
		DeliveryReach reach1 = new DeliveryReach(99, .5, 1);
		DeliveryReach reach2 = new DeliveryReach(100, .5, 2);
		DeliveryReach reach3 = new DeliveryReach(101, .5, 3);
		
		PriorityQueue<DeliveryReach> que = new PriorityQueue<DeliveryReach>();
		
		que.add(reach2);
		que.add(reach1);
		que.add(reach3);
		
		assertEquals(reach3, que.poll());
		assertEquals(reach2, que.poll());
		assertEquals(reach1, que.poll());
		
	}
	

	@Test
	public void checkAddingDownstreamReaches() {
		DeliveryReach reach1 = new DeliveryReach(99, .5, 1);
		DeliveryReach reach2 = new DeliveryReach(100, .5, 2, reach1);
		DeliveryReach reach3 = new DeliveryReach(101, .5, 3, reach2);
		DeliveryReach reach4 = new DeliveryReach(101, .5, 3, reach3);
		
		assertEquals(0, reach1.getDownstreamReaches().length);
		assertEquals(1, reach2.getDownstreamReaches().length);
		assertEquals(reach1, reach2.getDownstreamReaches()[0]);
		
		reach2.addDownstreamReach(reach3);
		assertEquals(2, reach2.getDownstreamReaches().length);
		assertEquals(reach1, reach2.getDownstreamReaches()[0]);
		assertEquals(reach3, reach2.getDownstreamReaches()[1]);
		
		reach2.addDownstreamReach(reach4);
		assertEquals(3, reach2.getDownstreamReaches().length);
		assertEquals(reach1, reach2.getDownstreamReaches()[0]);
		assertEquals(reach3, reach2.getDownstreamReaches()[1]);
		assertEquals(reach4, reach2.getDownstreamReaches()[2]);
	}
	
	@Test
	public void testEqualsDownstreamReaches() {
		DeliveryReach reach1 = new DeliveryReach(99, .5, 1);
		DeliveryReach reach2 = new DeliveryReach(99, .6, 2, reach1);
		DeliveryReach reach3 = new DeliveryReach(101, .7, 3, reach2);
		DeliveryReach reach4 = new DeliveryReach(101, .7, 3, reach3);
		
		assertEquals(reach1, reach2);
		assertEquals(reach3, reach4);
		assertFalse(reach1.equals(reach3));
		assertFalse(reach1.equals(reach4));

	}
}
