package gov.usgswim.sparrow.test;

import org.apache.log4j.Logger;
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

	protected static Logger log = Logger.getLogger(IgnoreContextIdDifferenceListener.class);
	
	public static final String IGNORE_VALUE_STRING = "IGNORE";
	
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
				("units".equalsIgnoreCase(parentNodeName)) ||
				(difference.getControlNodeDetail().getValue() != null && difference.getControlNodeDetail().getValue().equals(IGNORE_VALUE_STRING))){
					return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
			}
		}
		
		if (log.isDebugEnabled() &&
						difference.getControlNodeDetail().getValue() != null &&
						difference.getTestNodeDetail().getValue() != null) {
			
			String ctrlStr = difference.getControlNodeDetail().getValue();
			String testStr = difference.getTestNodeDetail().getValue();
			
			StringBuffer ctrlUni = new StringBuffer();
			StringBuffer testUni = new StringBuffer();
			
			String loopStr = (ctrlStr.length() > testStr.length())? ctrlStr : testStr;
			
			for (int i = 0; i < loopStr.length(); i++) {
				
				if (i < ctrlStr.length()) {
					ctrlUni.append(Integer.toHexString(Character.codePointAt(ctrlStr, i)));
					ctrlUni.append(' ');
				}
				if (i < testStr.length()) {
					testUni.append(Integer.toHexString(Character.codePointAt(testStr, i)));
					testUni.append(' ');
				}
			}
			
			
			log.debug("Ctrl value String: " + ctrlStr);
			log.debug("Test value String: " + testStr);
			log.debug("Ctrl value Unicode: " + ctrlUni);
			log.debug("Test value Unicode: " + testUni);
			
			
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
