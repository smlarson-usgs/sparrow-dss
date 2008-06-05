package gov.usgswim.sparrow.service.idbypoint;

import static gov.usgswim.sparrow.service.predict.ValueType.incremental;
import static gov.usgswim.sparrow.service.predict.ValueType.total;
import gov.usgs.webservices.framework.utils.TemporaryHelper;
import gov.usgswim.ThreadSafe;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.adjustment.FilteredDataTable;
import gov.usgswim.datatable.impl.DataTableUtils;
import gov.usgswim.service.HttpService;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowModelProperties;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predict.AggregateType;
import gov.usgswim.sparrow.service.predict.ValueType;
import gov.usgswim.sparrow.util.PropertyLoaderHelper;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
//TODO not complete
@ThreadSafe
public class IDByPointService implements HttpService<IDByPointRequest> {
	// =============
	// STATIC FIELDS
	// =============
	protected static Logger log =
		Logger.getLogger(IDByPointService.class); //logging for this class

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
	public IDByPointService() {}

	// ================
	// INSTANCE METHODS
	// ================
	public XMLStreamReader getXMLStreamReader(PipelineRequest o, boolean isNeedsFlattening)  throws Exception{
		return getXMLStreamReader((IDByPointRequest) o, isNeedsFlattening);
	}

	public XMLStreamReader getXMLStreamReader(IDByPointRequest req, boolean isNeedsFlattening) throws Exception {
		// TODO isNeedsFlattening ignored for now because using custom flattener
		// TODO extract to method when satisfied.
		IDByPointResponse response = new IDByPointResponse();
		
		//Find the model ID and reach ID. Note that the model ID must be known before calling 
		response.modelID = populateModelID(req);
		assert(response.modelID != null);
		response.setReach(populateReachID(req, response));
	
		// populate each of the sections
		if (req.hasAdjustments()) {
			retrieveAdjustments(req.getContextID(), req, response);
		}
		if (req.hasAttributes()) {
			retrieveAttributes(req, response);
		}
		if (req.hasPredicted()) {
			response.predictionsXML = retrievePredictedsForReach(req.getContextID(), req.getModelID(), Long.valueOf(response.reachID));
		}
		
		response.statusOK = true;
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(response.toXML()));

		return reader;
	}

	// =======================
	// PRIVATE HELPER METHODS
	// =======================
	private Reach populateReachID(IDByPointRequest req, IDByPointResponse response) throws Exception {
		if (req.getReachID() != null) {
			return SharedApplication.getInstance().getReachByIDResult(new ReachID(response.modelID, req.getReachID()));
		} else if (req.getPoint() != null) {
			return SharedApplication.getInstance().getReachByPointResult(new ModelPoint(response.modelID, req.getPoint()));
		} else {
			throw new Exception("A context-id or a model-id is required for a id request");
		}
	}

	private Long populateModelID(IDByPointRequest req) throws Exception {
		if (req.getContextID() != null) {
			PredictionContext context = SharedApplication.getInstance().getPredictionContext(req.getContextID());
			if (context == null) throw new RuntimeException("Prediction Context with id " 
					+ req.getContextID() + " has not been registered. Perhaps the server has been restarted?");
			return context.getModelID();
		} else if (req.getModelID() != null) {
			return req.getModelID();
		} else {
			throw new RuntimeException("A context-id or a model-id is required for a id request");
		}
	}
	private void retrieveAdjustments(Integer predContextID, IDByPointRequest req, IDByPointResponse response) throws IOException {
		//	TODO move to DataLoader when done debugging
		// TODO replace with dynamic working code
		
//		<adjustments display="Adjustments">
//		<metadata rowCount="5" columnCount="5" row-id-name="source_id">
//			<columns>
//				<col name="Source Name" type="String" />
//				<col name="Units" type="String" />
//				<col name="Original Value" type="Number" />
//				<col name="Absolute Value" type="Number" />
//				<col name="Multiplier" type="Number" />
//				<col name="Adjusted Value" type="Number" />
//			</columns>
//		</metadata>
//		<data>
//			<r id="1"><c>Point Sources</c><c>N in Tons</c><c>354564</c><c></c><c></c><c>354564</c></r>
//			<r id="2"><c>Atmospheric Deposition</c><c>N in Tons / SQ. Mile</c><c>5135484</c><c></c><c></c><c>5135484</c></r>
//			<r id="3"><c>Fertilizer</c><c>N in Tons / Acre</c><c>38138991</c><c></c><c></c><c>38138991</c></r>
//			<r id="4"><c>Waste</c><c>Tons of Garbage</c><c>0</c><c></c><c></c><c>0</c></r>
//			<r id="5"><c>Non-Agricultural</c><c>Total Population</c><c>9534</c><c></c><c></c><c>9534</c></r>
//		</data>
//	</adjustments>
		
		// PLAN
		// 1) Use DataLoader.properties SelectSourceValues  to obtain SourceName, Units, Original Value
		// 2) Handle the prediction context in order to calculate AdjustValue and Multiplier. Note that one
		// or more of Adjusted Value and Multiplier may be null, depending on the kind of adjustment.
		// Adjustment may or may not have prediction context, and it may or may not have been run.
		// If pred context, exists, check whether there are adjustments(difficult?)
		// Adjustment is only for reporting purposes
		// Happy path: predicted data available, all cached.
		
		if (predContextID == null || predContextID.equals(0)) return; // no prediction context means no adjustments 
		
//		Get the prediction context from the cache
		PredictionContext contextFromCache = SharedApplication.getInstance().getPredictionContext(predContextID);
		PredictResult predictResult = SharedApplication.getInstance().getPredictResult(contextFromCache);
		PredictData predictData = SharedApplication.getInstance().getPredictData(contextFromCache.getModelID());
		
//		Get the Original, unadjusted source data and the adjusted source data
		DataTable orgSrc = predictData.getSrc();
		DataTable adjSrc = SharedApplication.getInstance().getAdjustedSource(contextFromCache.getAdjustmentGroups());
		
		response.adjustmentsXML = buildAdjustment(orgSrc, adjSrc, req.getModelID(), Long.valueOf(response.reachID));
		
//		response.adjustmentsXML = props.getText("adjustmentsXMLResponse");
	}
	private String buildAdjustment(DataTable orgSrc, DataTable adjSrc, Long modelID, Long reachID) {
		StringBuilder sb = new StringBuilder();
		int rowID = orgSrc.getRowForId(reachID);
		for (int j=0; j<orgSrc.getColumnCount(); j++) {
			// TODO add r @id
			String orgValString = orgSrc.getString(0, j);
			String adjValString = adjSrc.getString(0, j);
			
			sb.append("<r>");
			sb.append("<c>").append(orgSrc.getName(j)).append("</c>");
			sb.append("<c>").append("").append("</c>"); // TODO add units
			sb.append("<c>").append(orgValString).append("</c>");
			sb.append("<c>").append("").append("</c>"); // TODO add Absolute value
			sb.append("<c>").append("").append("</c>"); // TODO add Multiplier
			sb.append("<c>").append(adjValString).append("</c>");
			sb.append("</r>");
		}
		return sb.toString();
	}

	private String retrievePredictedsForReach(Integer predictionContextID, Long modelID, Long reachID) throws IOException {
		// TODO move to DataLoader when done debugging
		
		// Get the nominal and adjusted prediction results
		PredictionContext nominalPredictionContext = null;
		PredictResult adjustedPrediction = null;
		
		if (predictionContextID != null) {
			// use prediction context to get the predicted results from cache if available
			PredictionContext contextFromCache = SharedApplication.getInstance().getPredictionContext(predictionContextID);
			adjustedPrediction = SharedApplication.getInstance().getPredictResult(contextFromCache);
			nominalPredictionContext = new PredictionContext(contextFromCache.getModelID(), null, null, null, null);
		} else {
			nominalPredictionContext = new PredictionContext(modelID, null, null, null, null);
		}
		PredictResult nominalPrediction = SharedApplication.getInstance().getPredictResult(nominalPredictionContext);
		
		String incrementalContribution = buildPredSection(nominalPrediction, adjustedPrediction, reachID, incremental, "Incremental Contribution Values", "inc");
		String totalContribution = buildPredSection(nominalPrediction, adjustedPrediction, reachID, total, "Total (Measurable) Values", "inc");
		
		// predictedXMLResponse
		return props.getText("predictedXMLResponse", 
				new String[] {
				"rowCount", "" + nominalPrediction.getColumnCount(),
				"incContribution", incrementalContribution,
				"totalContribution", totalContribution,
		});
	}

	private String buildPredSection(PredictResult nominalPrediction, PredictResult adjustedPrediction, Long id, ValueType type, String display, String name) {
		if (nominalPrediction == null ||  id == null || id == 0) return "";
		String typeName = (type == null)? "": type.name();
		String isTotalVal = AggregateType.sum.name();
		List<Integer> relevantColumns = new ArrayList<Integer>();
		Integer totalColumn = null;
		
		int rowID = nominalPrediction.getRowForId(id);
		// Collect all the relevant column indices for this row.
		for (int j=0; j<nominalPrediction.getColumnCount(); j++) {
			boolean isDesiredType = typeName.equals(nominalPrediction.getProperty(j, PredictResult.VALUE_TYPE_PROP));
			String aggType = nominalPrediction.getProperty(j, PredictResult.AGGREGATE_TYPE_PROP);
			boolean isTotal = (aggType != null ) && isTotalVal.equals(aggType);
			
			if (isDesiredType) {
				if (isTotal) {
					totalColumn = j;
				} else {
					relevantColumns.add(j);
				}
			}
		}
		
		// Assume adjustedPrediction has same Column structure and rows in same order.
		// Otherwise, we have to rewrite the following code.
		assert((adjustedPrediction == null) ||
				(	nominalPrediction.getRowCount() == adjustedPrediction.getRowCount()
						&& nominalPrediction.getColumnCount() == adjustedPrediction.getColumnCount())):
							"Assume adjustedPrediction has same column structure and rows in same order";
				
		
		StringBuilder sb = null;

		sb = new StringBuilder("<section display=\"");
		sb.append(display).append("\" name=\"").append(name).append("\">\n");
		
		for (Integer j : relevantColumns) {
			String columnName = nominalPrediction.getName(j);
			sb.append("<r><c>").append(columnName).append("</c><c>");
			String value = nominalPrediction.getString(rowID, j);
			value = (value == null)? "N/A": value;
			sb.append(value).append("</c>");
			
			if (adjustedPrediction == null || adjustedPrediction.getString(rowID, j) == null) {
				// no predicted value available
				sb.append("<c>N/A</c><c/>");
			} else {
				// add predicted value
				String predValueString = adjustedPrediction.getString(rowID, j);
				Double predValue = Double.valueOf(predValueString);
				Double nomValue = Double.valueOf(value);
				Double percentChange = calculatePercentageChange(predValue, nomValue);
				
				sb.append("<c>").append(predValueString).append("</c><c>").append(percentChange.toString()).append("</c>");
			}

			sb.append("</r>");
		}
		
		// add in the total TODO (with predicted)
		sb.append("<r><c>").append(nominalPrediction.getName(totalColumn)).append("</c><c>");
		sb.append(nominalPrediction.getString(rowID, totalColumn)).append("</c>");
		if (adjustedPrediction == null || adjustedPrediction.getString(rowID, totalColumn) == null) {
			// no predicted total available
			sb.append("<c>N/A</c>");
		} else {
			// add predicted total
			String predValueString = adjustedPrediction.getString(rowID, totalColumn);
			Double predValue = Double.valueOf(predValueString);
			Double nomValue = Double.valueOf(nominalPrediction.getString(rowID, totalColumn));
			Double percentChange = calculatePercentageChange(predValue, nomValue);
			sb.append("<c>").append(predValueString).append("</c><c>").append(percentChange.toString()).append("</c>");
		}
		sb.append("</r>");

		sb.append("</section>");

		return sb.toString();
	}

	public static Double calculatePercentageChange(Double newVal, Double baseVal) {
		if ( newVal.equals(baseVal)) {
			// This takes care of the case when both nominal and predicted are zero
			return 0D;
		} else if (!baseVal.equals(0D)) {
			return 100*(newVal - baseVal)/baseVal;
		}
		return Double.NaN; // division by zero because baseVal == 0 and newVal != 0
	}

	private void retrieveAttributes(IDByPointRequest req, IDByPointResponse response) throws IOException, SQLException, NamingException {
		// TODO move to DataLoader when done debugging
		// TODO replace with dynamic working code
		
		{	// HACK temporary. review later
			// use the submitted reach id if the response was not looked up.
			int identifier = (response.reachID == 0)? req.getReachID(): response.reachID;
			response.reachID = identifier;
		}

		String attributesQuery = props.getText("attributesSelectClause") + " FROM MODEL_ATTRIB_VW "
		+ " WHERE IDENTIFIER=" + response.reachID 
		+ " AND SPARROW_MODEL_ID=" + req.getModelID();
		
		DataTableWritable attributes = queryToDataTable(attributesQuery);
		// TODO [IK] This 4 is hardcoded for now. Have to go back and use SparrowModelProperties to do properly
		response.sparrowAttributes = new FilteredDataTable(attributes, 0, 4); // first four columns
		response.basicAttributes = new FilteredDataTable(attributes, 4, attributes.getColumnCount()- 4); // remaining columns
		
		StringBuilder basicAttributesSection = toSection(response.basicAttributes, "Basic Attributes", "basic_attrib");
		StringBuilder sparrowAttributesSection = toSection(response.sparrowAttributes, "SPARROW Attributes", "sparrow_attrib");

		// attributesXMLResponse
		response.attributesXML = props.getText("attributesXMLResponse", 
				new String[] {
				"AttributesCount", Integer.toString(attributes.getColumnCount()),
				"BasicAttributes", basicAttributesSection.toString(),
				"SparrowAttributes", sparrowAttributesSection.toString(),
		});
		
		// TODO Create a combo XMLStreamReader to enable several streamreader to be assembled sequentially, using hasNext to query.
		// This makes the pieces combineable.

	}

	private DataTableWritable queryToDataTable(String query) throws NamingException, SQLException {
		Connection conn = getConnection();
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		ResultSet rset = st.executeQuery(query);
		DataTableWritable attributes = DataTableUtils.toDataTable(rset);
		closeConnection(conn, rset);
		return attributes;
	}

	private StringBuilder toSection(DataTable basicAttributes, String display, String name) {
		StringBuilder sb = null;
		if (basicAttributes != null) {
			sb = new StringBuilder("<section display=\"");
			sb.append(display).append("\" name=\"").append(name).append("\">\n");
			for (int j=0; j<basicAttributes.getColumnCount(); j++) {
				String columnName = basicAttributes.getName(j);
				sb.append("<r><c>").append(columnName).append("</c><c>");
				String value = basicAttributes.getString(0, j);
				value = (value == null)? "N/A": value;
				sb.append(value).append("</c>");
				String units = basicAttributes.getUnits(j);
				// HACK look up the units if not available in DataTable attributes
				// TODO [IK] Should populate the DataTable units property rather
				// than do lookup, but right now that would
				// touch too many classes and needs to be tested.
				units = (units == null)? SparrowModelProperties.HARDCODED_ATTRIBUTE_UNITS.get(columnName): units;

				if (units != null) {
					sb.append("<c>").append(units).append("</c>");
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




	protected Connection getConnection() throws NamingException, SQLException {
		return SharedApplication.getInstance().getConnection();
	}

	public void shutDown() {
		xoFact = null;
	}

}
