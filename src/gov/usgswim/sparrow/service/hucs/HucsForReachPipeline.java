package gov.usgswim.sparrow.service.hucs;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.JSONFormatter;
import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.sparrow.service.AbstractPipeline;
import gov.usgswim.sparrow.util.JDBCUtil;
import gov.usgswim.sparrow.util.PropertyLoaderHelper;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;

public class HucsForReachPipeline extends AbstractPipeline<HucsForReachRequest>{

	public static JSONFormatter configure(JSONFormatter jFormatter) {
		jFormatter.identifyRepeatedTagElement("hucs", "huc2");
		jFormatter.identifyRepeatedTagElement("hucs", "huc4");
		jFormatter.identifyRepeatedTagElement("hucs", "huc6");
		jFormatter.identifyRepeatedTagElement("hucs", "huc8");
		return jFormatter;
	}
	
	// ===============
	// INSTANCE FIELDS
	// ===============
	private static PropertyLoaderHelper props = new PropertyLoaderHelper("gov/usgswim/sparrow/service/hucs/HucServiceTemplate.properties");

	
	public HucsForReachPipeline(){
		super(null, null );
	}

	@Override
	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		// no flattening for PredictContext pipeline
		return new XMLPassThroughFormatter();
	}

	@Override
	public HucsForReachRequest parse(HttpServletRequest request) throws Exception {

		// Special handling for a GET request
		if ("GET".equals(request.getMethod()) ) {
			String[] paramChain = AbstractHttpRequestParser.parseExtraPath(request);
			System.out.println(paramChain[0] + " - " + paramChain);
			if (paramChain.length == 3 
					&& StringUtils.isNumeric(paramChain[0])
					&& StringUtils.isNumeric(paramChain[1])                                
				) {
				
				Integer modelID = Integer.parseInt(paramChain[0]);
				Long reachID = Long.parseLong(paramChain[1]);
				String desiredAttribute = paramChain[2].toLowerCase();
				
				return new HucsForReachRequest(modelID, reachID, desiredAttribute);
			} else {
				throw new Exception("expected URL of the form \".../huc/$MODEL-ID$/$REACH-ID$/$desired-attribute-name$\"");
			}

		} else {
			// HucsForReachPipeline does not handle POST requests. 
			return null;
		}
	}


	@Override
	public XMLStreamReader getXMLStreamReader(HucsForReachRequest request, boolean isNeedsCompleteFirstRow) throws Exception {
		// ignore isNeedsCompleteFirstRow
		StringBuilder result = new StringBuilder();
		String attName = request.attributeName;
		if ( attName != null && attName.startsWith("huc")) {
			result.append("<hucs>");
			result.append(findHucsXML(attName, request.reachID, request.modelID));
			result.append("</hucs>");
		}
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		System.out.println(result.toString());
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(result.toString()));
		
		return reader;
	}

	/**
	 * @param hucType should be from the Enum HucTypes, or "hucs" for all huc types
	 * @param reachID
	 * @param modelID
	 * @return XML list of hucs, no container element
	 * <code><br/>
	 *	&lt;huc2 id="02" name="MID ATLANTIC"/&gt;<br/>
	 * 	&lt;huc4 id="0203" name="LOWER HUDSON-LONG ISLAND"/&gt;<br/>
	 * 	&lt;huc6 id="020301" name="LOWER HUDSON"/&gt;<br/>
	 * 	&lt;huc8 id="02030104" name="SANDY HOOK-STATEN ISLAND"/&gt;<br/>
	 * </code>
	 * @throws IOException
	 * @throws NamingException
	 * @throws SQLException
	 */
	public static StringBuilder findHucsXML(String hucType, long reachID, long modelID) throws IOException, NamingException, SQLException {
		StringBuilder result = new StringBuilder();
		
		if ("hucs".equals(hucType)) {
			// return results for all the huc types
			for (HucTypes type: HucTypes.values()) {
				result.append(findHucsXML(type.name(), reachID, modelID));
			}
		} else {
			// return results for the specific huc type
			String hucsForReachQuery = props.getText("HUCsForReachSQL",
					new String[] {
						"HucType", hucType,
						"ReachID", Long.toString(reachID),
						"ModelID", Long.toString(modelID),
			});
			
			DataTableWritable dt = JDBCUtil.queryToDataTable(hucsForReachQuery);
			
			for (int i=0; i<dt.getRowCount(); i++) {
				String itemXML = props.getText("HUCItemXML",
					new String[] {
						"HucType", hucType,
						"ID", dt.getString(i, 1),
						"Name", dt.getString(i, 2),
				});

				result.append(itemXML);
			}
		}
		return result;
		

	}
	
	@Override
	public IFormatter getConfiguredJSONFormatter() {
		return configure(new JSONFormatter());
	}
	
	
	

}
