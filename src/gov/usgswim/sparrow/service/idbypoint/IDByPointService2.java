package gov.usgswim.sparrow.service.idbypoint;

import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;
import gov.usgswim.ThreadSafe;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.service.HttpService;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.parser.Content;
import gov.usgswim.sparrow.service.DataTableSerializer;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.PropertyLoaderHelper;

import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
//TODO not complete
@ThreadSafe
public class IDByPointService2 implements HttpService<IDByPointRequest2> {
	// =============
	// STATIC FIELDS
	// =============
	protected static Logger log =
		Logger.getLogger(IDByPointService2.class); //logging for this class

	protected static String RESPONSE_MIME_TYPE = "application/xml";


	//They promise these factories are threadsafe
	private static Object factoryLock = new Object();
	//protected static XMLInputFactory xinFact;
	protected static XMLOutputFactory xoFact;

	// ===============
	// INSTANCE FIELDS
	// ===============
	private PropertyLoaderHelper props = new PropertyLoaderHelper("gov/usgswim/sparrow/service/idbypoint/IDByPointServiceTemplate.properties");

	// ===========
	// CONSTRUCTOR
	// ===========
	public IDByPointService2() {}

	// ================
	// INSTANCE METHODS
	// ================
	public XMLStreamReader getXMLStreamReader(PipelineRequest o, boolean isNeedsFlattening)  throws Exception{
		return getXMLStreamReader((IDByPointRequest2) o, isNeedsFlattening);
	}

	public XMLStreamReader getXMLStreamReader(IDByPointRequest2 req, boolean isNeedsFlattening) throws Exception {
		// TODO isNeedsFlattening ignored for now because using custom flattener
		// TODO extract to method when satisfied.
		IDByPointResponse response = new IDByPointResponse();
		response.modelID = req.getModelID();
		// TODO populate response.contextID;
		if (req.getPoint() != null) {
			retrieveReachIdentification(req, response);
		}
		Content requestedContent = req.getContent();
		if (requestedContent.hasAdjustments()) {
			retrieveAdjustments(req, response);
		}
		if (requestedContent.hasAttributes()) {
			retrieveAttributes(req, response);
		}
		if (requestedContent.hasPredicted()) {
			retrievePredicteds(req, response);
		}
		
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		String result = response.toXML();
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(response.toXML()));
		// HACK [eric or IK] temporarily converting into old request format. Remove when done.
		//BasicXMLStreamReader reader = returnOldResult(req);
		return reader;
	}

	/**
	 * @param req
	 * @return
	 * @throws Exception
	 * @deprecated
	 */
	private BasicXMLStreamReader returnOldResult(IDByPointRequest2 req) throws Exception {
		//	IDByPointRequest oldReq = new IDByPointRequest(req.getModelID(), req.getPoint(), req.getNumberOfResults());
		IDByPointRequest oldReq = new IDByPointRequest(req.getModelID(), req.getPoint(), 7);

		// TODO [eric] update this old cache code. Should use IDByPointRequest2 rather than IDByPointRequest
		DataTable result = SharedApplication.getInstance().getIdByPointCache().compute(oldReq);
		BasicXMLStreamReader reader = new DataTableSerializer(oldReq, result);
		return reader;
	}
	
	private void retrievePredicteds(IDByPointRequest2 req, IDByPointResponse response) throws IOException {
		//	TODO move to DataLoader when done debugging
		// TODO replace with dynamic working code
		response.predictionsXML = props.getText("predictedXMLResponse");
	}

	private void retrieveAttributes(IDByPointRequest2 req, IDByPointResponse response) throws IOException {
		//	TODO move to DataLoader when done debugging
		// TODO replace with dynamic working code
		response.attributesXML = props.getText("attributesXMLResponse");
	}

	private void retrieveAdjustments(IDByPointRequest2 req, IDByPointResponse response) throws IOException {
		//	TODO move to DataLoader when done debugging
		// TODO replace with dynamic working code
		response.adjustmentsXML = props.getText("adjustmentsXMLResponse");
	}

	private void retrieveReachIdentification(IDByPointRequest2 req, IDByPointResponse response) throws SQLException, NamingException {
		// TODO move to DataLoader when done debugging
		Double point = req.getPoint();
		try {
			String query = props.getText("FindReach", 
					new String[] {
						"ModelId", req.getPredictionContext().getModelID().toString(),
						"lng", java.lang.Double.valueOf( point.getX() ).toString(),
						"lat", java.lang.Double.valueOf( point.getY() ).toString()
				});
			Connection conn = getConnection();
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			ResultSet rset = st.executeQuery(query);
			
			if (rset.next()) {
				response.statusOK = true;
				// TODO set response.message
				// TODO set response.cacheLifetime
				response.distanceFromReach = rset.getInt("DIST_IN_METERS");
				response.reachID = rset.getInt("IDENTIFIER");
				response.reachName = rset.getString("REACH_NAME");
			} else {// no reach found near point
				response.statusOK = false;
				response.message = "No reach found near lat=" + point.y + " long=" + point.x;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			// wrap and rethrow for now
			// TODO decide how to handle these exceptions later
			throw new RuntimeException(e);
		}
		
	}


	protected Connection getConnection() throws NamingException, SQLException {
		return SharedApplication.getInstance().getConnection();
	}

	public void shutDown() {
		xoFact = null;
	}

}
