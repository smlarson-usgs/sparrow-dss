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
 * 6/3/2013 EE
 * This comparitor now also ignores the units when comparing b/c there seem to
 * be encoding issues that cannot be (easily) fixed for now.  Logged as a task:
 * SPDSS-1087
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
			String parentNodeName = "";
			if(null != difference.getControlNodeDetail() &&
				null != difference.getControlNodeDetail().getNode() &&
				null != difference.getControlNodeDetail().getNode().getParentNode() &&
				null != difference.getControlNodeDetail().getNode().getParentNode().getLocalName()
			){

			parentNodeName = difference.getControlNodeDetail().getNode().getParentNode().getLocalName();
		}
			if (
				"context-id".equalsIgnoreCase(difference.getControlNodeDetail().getNode().getLocalName()) ||
				("units".equalsIgnoreCase(parentNodeName)) ){
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
