package gov.usgswim.sparrow.revised;

import gov.usgswim.sparrow.revised.transformers.NSDatasetTransformer;

/**
 * This class contains a number of hacks that need to be removed later. I
 * suppose I could also introduce a @hack notation
 *
 * @author ikuoikuo
 *
 */
public class HacksToRemove {
	public static void hack1() {
		NSDatasetTransformer.transformSource(null, null);
	}


}
