package gov.usgswim.sparrow.service.binning;

import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_NAMESPACE;
import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_PREFIX;
import gov.usgs.webservices.framework.dataaccess.BasicTagEvent;
import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;

import java.math.BigDecimal;

import javax.xml.stream.XMLStreamException;

public class BinningSerializer extends BasicXMLStreamReader {
    
    public static String TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/binning-response/v0_1";
    public static String TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/binning-response/v0_1.xsd";
    private BinningServiceRequest request;
    private BigDecimal[] bins;
    protected ParseState state = new ParseState();

    protected class ParseState{
        protected int r = 0;
        public boolean isDataFinished() {
            return r >= bins.length;
        }
    };
    
    public BinningSerializer(BinningServiceRequest request, BigDecimal[] bins) {
        super();
        this.request = request;
        this.bins = bins;
    }
    
    @Override
    public void readNext() throws XMLStreamException {
        try {
            if (!isStarted) {
                documentStartAction();
            }
            readValue();
            if (state.isDataFinished()) {
                if (isStarted && !isEnded) {
                    // Only output footer if the document was actually started
                    // and the footer has not been output.
                    documentEndAction();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new XMLStreamException(e);
        }
    }

    @Override
    protected BasicTagEvent documentStartAction() {
        super.documentStartAction();
        
        // add the namespaces
        this.setDefaultNamespace(TARGET_NAMESPACE);
        addNamespace(XMLSCHEMA_NAMESPACE, XMLSCHEMA_PREFIX);

        // opening element
        events.add(new BasicTagEvent(START_DOCUMENT));
        events.add(new BasicTagEvent(START_ELEMENT, "binning-response"));
        //.addAttribute(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE, "schemaLocation", TARGET_NAMESPACE + " " + TARGET_NAMESPACE_LOCATION));
        
        addNonNullBasicTag("contextId", request.getContextId().toString());
        addNonNullBasicTag("binType", request.getBinType().toString());
        
        addOpenTag("bins");
        return null;
    }

    @Override
    protected void documentEndAction() {
        super.documentEndAction();
        addCloseTag("bins");
        addCloseTag("binning-response");
        events.add(new BasicTagEvent(END_DOCUMENT));
    }

    protected void readValue() {
        if (!state.isDataFinished()) {
            // read the row
        	// Note that in 1.0E+4, the "+" has to be removed because Oracle mapviewer does not parse correctly
            addNonNullBasicTag("bin", bins[state.r++].toString().replaceFirst("\\+", ""));
            events.add(new BasicTagEvent(SPACE));
        }
    }

    @Override
    public void close() throws XMLStreamException {
        // not much needs to be done. no resources to release
        bins = null;
    }

    // ==========================
    // SIMPLE GETTERS AND SETTERS
    // ==========================
    public String getTargetNamespace() {
        return TARGET_NAMESPACE;
    }
}
