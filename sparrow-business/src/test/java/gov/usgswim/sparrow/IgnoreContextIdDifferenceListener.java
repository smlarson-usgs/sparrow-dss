package gov.usgswim.sparrow;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

/**
 * An XmlUnit DifferenceListener that ignores differences in contextIDs.
 * This is intended to allow XML responses to be compared while ignoring the
 * context IDs, which tend to change with every revision of the PredictionContext
 * hierarchy of classes.
 * 
 * This type of comparison is (obviously) not appropriate for testing the generation
 * of context ids....
 * 
 * @author eeverman
 */
public class IgnoreContextIdDifferenceListener implements DifferenceListener {

	/**
	 * Default constructor.
	 */
	public IgnoreContextIdDifferenceListener() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.custommonkey.xmlunit.DifferenceListener#differenceFound(org.custommonkey.xmlunit.Difference)
	 */
	@Override
	public int differenceFound(Difference difference) {
		if (difference.getControlNodeDetail().getNode() != null) {
			String n = difference.getControlNodeDetail().getNode().getLocalName();
			if ("context-id".equalsIgnoreCase(n)) {
				return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
			}
		}
		return RETURN_ACCEPT_DIFFERENCE;
	}

	/* (non-Javadoc)
	 * @see org.custommonkey.xmlunit.DifferenceListener#skippedComparison(org.w3c.dom.Node, org.w3c.dom.Node)
	 */
	@Override
	public void skippedComparison(Node control, Node test) {
		// Do nothing
	}

}
