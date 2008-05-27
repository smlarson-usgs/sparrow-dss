package gov.usgswim.sparrow.service.idbypoint;

import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;
import gov.usgs.webservices.framework.utils.TemporaryHelper;
import gov.usgswim.ThreadSafe;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.DataTableUtils;
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

import org.apache.commons.lang.NotImplementedException;
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

	private void retrieveAttributes(IDByPointRequest2 req, IDByPointResponse response) throws IOException, SQLException, NamingException {
		//	TODO move to DataLoader when done debugging
		// TODO replace with dynamic working code
		
		{	// HACK temporary. review later
			// use the submitted reach id if the response was not looked up.
			int identifier = (response.reachID == 0)? req.getReachID(): response.reachID;
			response.reachID = identifier;
		}
		String basicAttributesQuery = props.getText("basicAttSelectClause") + " FROM MODEL_ATTRIB_VW "
			+ " WHERE IDENTIFIER=" + response.reachID 
			+ " AND SPARROW_MODEL_ID=" + req.getModelID();
		
		
		Connection conn = getConnection();
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rset = st.executeQuery(basicAttributesQuery);
		response.basicAttributes = DataTableUtils.toDataTable(rset);
		closeConnection(conn, rset);
//props.getText("basicAttSelectClause") +
		String sparrowAttributesQuery =  props.getText("sparrowAttSelectClause") + " FROM MODEL_REACH "
			+ " WHERE IDENTIFIER=" + response.reachID
			+ " AND SPARROW_MODEL_ID=" + req.getModelID();

		conn = getConnection();
		st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		rset = st.executeQuery(sparrowAttributesQuery);
		response.sparrowAttributes = DataTableUtils.toDataTable(rset);
		closeConnection(conn, rset);
		 
		// DEBUG
//		TemporaryHelper.printDataTable(response.sparrowAttributes);
		
		StringBuilder basicAttributesSection = toSection(response.basicAttributes, "Basic Attributes", "basic_attrib");		
		StringBuilder sparrowAttributesSection = toSection(response.sparrowAttributes, "SPARROW Attributes", "sparrow_attrib");

		response.attributesXML = props.getText("attributesXMLResponseStart") + basicAttributesSection + sparrowAttributesSection + props.getText("attributesXMLResponseEnd");

		// TODO [IK]convert the attributes DataTables to XML. At this point, it's still just a mock.
		// TODO create a DataTableToRCXML (rc=row column) method to do this to convert to data portion, taking into account id
		// DataTableToRCXML(DataTable data, int cols); to fill/pad the rows.
		// makeRCHeaders(String... ) for column headers.
		// Create a combo XMLStreamReader to enable several streamreader to be assembled sequentially, using hasNext to query.
		// This makes the pieces combineable.
//		response.attributesXML = props.getText("attributesXMLResponse");
	}

	private StringBuilder toSection(DataTable basicAttributes, String display, String name) {
		StringBuilder sb = null;
		if (basicAttributes != null) {
			sb = new StringBuilder("<section display=\"");
			sb.append(display).append("\" name=\"").append(name).append("\">\n");
			for (int j=0; j<basicAttributes.getColumnCount(); j++) {
				sb.append("<r><c>").append(basicAttributes.getName(j)).append("</c><c>");
				String value = basicAttributes.getString(0, j);
				value = (value == null)? "N/A": value;
				sb.append(value).append("</c>");
				String units = basicAttributes.getUnits(j);
				if (units != null) {
					sb.append("<c>").append(units).append("</c");
				} else {
					sb.append("<c/>");
				}
				sb.append("</r>");
			}
			sb.append("</section>");
		}
		return sb;
	}

	private void closeConnection(Connection conn, ResultSet rset) {
		// TODO [IK] implement a utility method for closing connections and refactor to a different place. Doesn't belong here.
		try {
			if (rset != null) rset.close();
			if (conn != null) conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void retrieveAdjustments(IDByPointRequest2 req, IDByPointResponse response) throws IOException {
		//	TODO move to DataLoader when done debugging
		// TODO replace with dynamic working code
		
//	    <adjustments display="Adjustments">
//        <metadata rowCount="5" columnCount="5" row-id-name="source_id">
//            <columns>
//                <col name="Source Name" type="String" />
//                <col name="Units" type="String" />
//                <col name="Original Value" type="Number" />
//                <col name="Adjusted Value" type="Number" />
//                <col name="Multiplier" type="Number" />
//            </columns>
//        </metadata>
//        <data>
//            <r id="1"><c>Point Sources</c><c>N in Tons</c><c>143843</c><c></c><c></c></r>
//            <r id="2"><c>Atmospheric Deposition</c><c>N in Tons / SQ. Mile</c><c>38434</c><c></c><c></c></r>
//            <r id="3"><c>Fertilizer</c><c>N in Tons / Acre</c><c>94434</c><c></c><c></c></r>
//            <r id="4"><c>Waste</c><c>Tons of Garbage</c><c>143</c><c></c><c></c></r>
//            <r id="5"><c>Non-Agricultural</c><c>Total Population</c><c>91343</c><c></c><c></c></r>
//        </data>
//    	</adjustments>
		
		// PLAN
		// 1) Use DataLoader.properties SelectSourceValues  to obtain SourceName, Units, Original Value
		// 2) Handle the prediction context in order to calculate AdjustValue and Multiplier. Note that one
		// or more of Adjusted Value and Multiplier may be null, depending on the kind of adjustment
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
		// TODO need to add logic to synchronize reach ID between request and response
		{	// HACK temporary. review later
			// use the submitted reach id if the response was not looked up.
			int identifier = (response.reachID == 0)? req.getReachID(): response.reachID;
			response.reachID = identifier;
		}
		
	}


	protected Connection getConnection() throws NamingException, SQLException {
		return SharedApplication.getInstance().getConnection();
	}

	public void shutDown() {
		xoFact = null;
	}

}
