package gov.usgswim.sparrow.service.binning;

import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.cachefactory.BinningRequest.BIN_TYPE;
import gov.usgswim.sparrow.parser.ResponseFormat;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;
import gov.usgswim.sparrow.service.predict.PredictExportRequest;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class BinningRequest implements XMLStreamParserComponent, PipelineRequest{
    
    private String xmlRequest;
    private ResponseFormat responseFormat;
    private Integer contextId;
    private Integer binCount;
    private BIN_TYPE binType;

    public BinningRequest(Integer contextId, Integer binCount, BIN_TYPE binType) {
        this.contextId = contextId;
        this.binCount = binCount;
        this.binType = binType;
        this.responseFormat = new ResponseFormat();
        responseFormat.setMimeType("xml");
        responseFormat.setAttachment(false);
    }
    
    public Integer getContextId() {
        return contextId;
    }

    public Integer getBinCount() {
        return binCount;
    }

    public BIN_TYPE getBinType() {
        return binType;
    }

    public PredictExportRequest parse(XMLStreamReader in)
    throws XMLStreamException, XMLParseValidationException {
        throw new XMLStreamException("Unsupported request format.");
    }

    public void checkValidity() throws XMLParseValidationException {
        if (!isValid()) {
            // throw a custom error message depending on the error
            throw new XMLParseValidationException("Unsupported request format.");
        }
    }

    public boolean isValid() {
        return false;
    }

    public String getXMLRequest() {
        return xmlRequest;
    }

    public void setXMLRequest(String request) {
        xmlRequest = request;       
    }

    public void setResponseFormat(ResponseFormat respFormat) {
        this.responseFormat = respFormat;
    }

    public ResponseFormat getResponseFormat() {
        if (responseFormat == null) {
            setResponseFormat(new ResponseFormat());
        }
        return responseFormat;
    }

    public String getParseTarget() {
        return null;
    }

    public boolean isParseTarget(String name) {
        return false;
    }
}
