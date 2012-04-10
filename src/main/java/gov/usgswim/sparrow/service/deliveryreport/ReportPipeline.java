package gov.usgswim.sparrow.service.deliveryreport;

import gov.usgs.webservices.framework.formatter.*;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.sparrow.service.AbstractPipeline;

import java.util.Date;

public class ReportPipeline extends AbstractPipeline<ReportRequest>{

	// TODO eliminate duplicate code from IDByPointPipeline.configure(new JSONFormatter());
	public static JSONFormatter configure(JSONFormatter jFormatter) {
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "reachGroup");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "adjustment");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "logicalSet");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "reach");
		jFormatter.identifyRepeatedTagElement("columns", "group");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "col");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "r");
		jFormatter.identifyRepeatedTagElement("data", "section");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "c");

		return jFormatter;
	}

	public ReportPipeline(){
		super(new ReportService(), new ReportRequestParser());
	}

	@Override
	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		
		IFormatter formatter = null;
		
		switch (outputType) {
		case HTML:
		case XHTML:
			formatter = new SparrowExportHtmlFormatter(outputType);
			break;
		default:
			formatter = new CommentedSparrowFlatteningFormatter(outputType);
		}
		
		return formatter;
	}

	@Override
	public IFormatter getConfiguredJSONFormatter() {
		return configure(new JSONFormatter());
	}
	
	private class CommentedSparrowFlatteningFormatter extends SparrowFlatteningFormatter {
		public CommentedSparrowFlatteningFormatter(OutputType type) {
			super(type);
			
			switch (type) {
			
			case EXCEL:
				Delimiters xlsD = Delimiters.makeExcelDelimiter("USGS", new Date().toString());
				this.setDelimiters(xlsD);
				break;
			case CSV:
				Delimiters csvD = Delimiters.CSV_DELIMITERS;
				this.setDelimiters(csvD);
				break;
			case DATA:
			case TAB:
				Delimiters tabD = Delimiters.TAB_DELIMITERS;
				this.setDelimiters(tabD);
				break;
			default:
				break;
			}
		}
	}
}
