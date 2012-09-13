package gov.usgswim.sparrow.service.predict;

import gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.Delimiters;
import gov.usgs.webservices.framework.formatter.JSONFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.sparrow.service.AbstractPipeline;

import java.util.Date;

public class PredictPipeline extends AbstractPipeline<PredictExportRequest>{

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

	public PredictPipeline(){
		super(new PredictExportService(), new PredictExportParser());
	}

	@Override
	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		CommentedSparrowFlatteningFormatter formatter = new CommentedSparrowFlatteningFormatter(outputType);
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
