package gov.usgswim.sparrow.service.hucs;

import java.io.StringReader;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgs.webservices.framework.utils.TemporaryHelper;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.sparrow.service.AbstractPipeline;
import gov.usgswim.sparrow.util.JDBCUtil;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;

public class HucsForReachPipeline extends AbstractPipeline<HucsForReachRequest>{

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
				String desiredAttribute = paramChain[2];
				
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
		StringBuilder result = new StringBuilder();
		if ("huc8".equals(request.attributeName)) {
			String attributesQuery = "SELECT attrib.IDENTIFIER, attrib.HUC8, lkp.name " 
				+ " FROM SPARROW_DSS.model_attrib_vw attrib LEFT OUTER JOIN STREAM_NETWORK.HUC8_LKP lkp ON attrib.HUC8 = lkp.huc8 "
				+ " where sparrow_model_id = " + request.modelID
				+ " and IDENTIFIER = " + request.reachID;
			DataTableWritable dt = JDBCUtil.queryToDataTable(attributesQuery);
			result.append("<hucsForReachResponse reachID=\"").append(request.reachID).append("\">");
			for (int i=0; i<dt.getRowCount(); i++) {
				result.append("<huc id=\"").append(dt.getString(i, 0));
				result.append("\" huc8=\"").append(dt.getString(i, 1)).append("\" >");
				result.append(dt.getString(i, 2));
				result.append("</huc>");
			}
			result.append("</hucsForReachResponse>");
		}
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(result.toString()));
		
		return reader;
	}
	
	
	

}
