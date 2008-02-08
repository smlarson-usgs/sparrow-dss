package gov.usgswim.sparrow.service;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.PredictData;

import java.io.OutputStream;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

public class PredictSerializer extends AbstractSerializer {

	public static String TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/prediction-response/v0_1";
	public static String TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/prediction-response/v0_1.xsd";
	public static String T_PREFIX = "mod";
	
	public PredictSerializer() {
		super();
	}
	
	public String getTargetNamespace() {
		return TARGET_NAMESPACE;
	}
	
	
	public void writeResponse(OutputStream stream, PredictServiceRequest request, Data2D result, PredictData predictData) throws XMLStreamException {
		XMLEventWriter xw = xoFact.createXMLEventWriter(stream);
		writeResponse(xw, request, result, predictData);
	}
	
	
	/**
	 * Writes all the passed models to the XMLEventWriter.
	 * 
	 * @param xw
	 * @param models
	 * @throws XMLStreamException
	 */
	public void writeResponse(XMLEventWriter xw, PredictServiceRequest request, Data2D result, PredictData predictData) throws XMLStreamException {
	
		xw.setDefaultNamespace(TARGET_NAMESPACE);
		xw.add( evtFact.createStartDocument(ENCODING, XML_VERSION) );
		
		
		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "sparrow-prediction-response") );
		xw.add( evtFact.createNamespace(TARGET_NAMESPACE) );
		xw.add( evtFact.createNamespace(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE) );
		xw.add( evtFact.createAttribute(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE, "schemaLocation", TARGET_NAMESPACE_LOCATION) );

		writeRequest(xw, request);
		writeResponseSection(xw, request, result, predictData);
		
		xw.add( evtFact.createEndElement(T_PREFIX, TARGET_NAMESPACE, "sparrow-prediction-response") );
		xw.add( evtFact.createEndDocument() );
	}
	
	public void writeRequest(XMLEventWriter xw, PredictServiceRequest request) {
		//for now, just skip this optional element
	}
	
	public void writeResponseSection(javax.xml.stream.XMLEventWriter xw,
				PredictServiceRequest request, Data2D result, PredictData predictData) throws XMLStreamException {

		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "response") );
		writeMetadata(xw, request, result, predictData);
		writeData(xw, request, result, predictData);

		
		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "response") );

	}
	
	//TODO There is no test to verify writting the source values with the serializer
	public void writeMetadata(XMLEventWriter xw, PredictServiceRequest request,
				Data2D result, PredictData predictData) throws XMLStreamException {
				
				
		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "metadata") );
		xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, "rowCount", Integer.toString(result.getRowCount())) );
		xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, "columnCount", Integer.toString(result.getColCount())) );
		
		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "columns") );
		String[] attribNames = new String[] {"name", "type"};
		
		if (predictData != null && request.getDataSeries().equals(PredictServiceRequest.DataSeries.ALL)) {

			//Add a group for the source columns
			xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "group") );
			xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, "name", "Source Values") );
			for(String head : predictData.getSrc().getHeadings()) {
				writeElemEvent(xw, "col", null, attribNames, new String[] {head, "Number"});
			}
			xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "group") );
			
			//Add a group for the predicted value columns
			xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "group") );
			xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, "name", "Predicted Values") );
			
			for(String head : result.getHeadings()) {
				writeElemEvent(xw, "col", null, attribNames, new String[] {head, "Number"});
			}
		
			xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "group") );

		} else {
			//We don't have the original predict data, so the best we can do is just the predict columns
			for(String head : result.getHeadings()) {
				writeElemEvent(xw, "col", null, attribNames, new String[] {head, "Number"});
			}
			
		}
		
		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "columns") );
		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "metadata") );

	}
	
	public void writeData(XMLEventWriter xw, PredictServiceRequest request,
				Data2D result, PredictData predictData) throws XMLStreamException {
				
		//If true, write the source value columns into the data.  It should preceed the predict val columns
		boolean writeSrcs = predictData != null && request.getDataSeries().equals(PredictServiceRequest.DataSeries.ALL);
		Data2D src = predictData.getSrc();
		Data2D sys = predictData.getSys();
				
		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "data") );

			for (int r = 0; r < result.getRowCount(); r++)  {
			
				Integer id = result.getIdForRow(r);
				
				xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "r") );
				xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, "id", id.toString()) );
				
				//write source value columns (if requested)
				//Note that the results may be filtered, so we cannot assume that the
				//row ordering of src's matches the results.
				if (writeSrcs) {
					int srcRow = sys.findRowById(id);	//Find row num in sys for the current ID
					for (int c = 0; c < src.getColCount(); c++)  {
						writeElemEvent(xw, "c", Double.toString(src.getDouble(srcRow, c)));
					}
				}

				//write predicted data columns
				for (int c = 0; c < result.getColCount(); c++)  {
					writeElemEvent(xw, "c", Double.toString(result.getDouble(r, c)));
				}

				xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "r") );
				xw.add( evtFact.createCharacters("\n") );
			}
			
			
		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "data") );
	}
}
