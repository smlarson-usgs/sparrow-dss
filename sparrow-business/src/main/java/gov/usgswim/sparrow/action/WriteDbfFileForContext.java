package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnIndex;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.HashMap;

/**
 * Creates a dbf file containing an ID column and a data column.
 * 
 * The columns are written in order as follows:
 * <ol>
 * <li>[ID Column of named specified] : Integer number of 10 digits.
 * <li>VALUE : Decimal number of 14 digits total, four of which are decimal places.
 * </ol>
 * 
 * Note that 10 digits is the NHD definition of the COMID:
 * http://nhd.usgs.gov/nhd_faq.html#q119
 * 
 * @author eeverman
 *
 */
public class WriteDbfFileForContext extends Action<HashMap> {

	//User config
	private PredictionContext context;
	
	//Self initialized
	private ColumnIndex columnIndex;
	private ColumnData dataColumn;
	private ReachRowValueMap reachRowValueMap;
	
	public WriteDbfFileForContext(PredictionContext context) {
		this.context = context;
	}
    
	protected WriteDbfFileForContext() {
		// Created for testing
	}

	@Override
	protected void initFields() throws Exception {
        
		dataColumn = context.getDataColumn().getColumnData();
		columnIndex = SharedApplication.getInstance().getPredictData(context.getModelID()).getTopo().getIndex();
		
		DataSeriesType type = context.getAnalysis().getDataSeries();
		
		//grab the delivery fraction map if this is a delivery data series.
		//This is used to weed out the reaches that are not upstream of the
		//user selected terminal reaches.
		if (type.isDeliveryRequired()) {

			TerminalReaches tReaches = context.getTerminalReaches();

			assert(tReaches != null) : "client should not submit a delivery request without reaches";

			reachRowValueMap = SharedApplication.getInstance().getDeliveryFractionMap(tReaches);

			if (reachRowValueMap == null) {
				throw new Exception("Unable to find or calculate the delivery fraction map");
			}
		} else {
			reachRowValueMap = null;
		}
	}
	
	

	@Override
	protected void validate() {
		if (context == null) {
			addValidationError("The context connot be null");
		}
	}

        
	@Override
	public HashMap doAction() throws Exception {
		// Acquire model output values for load into the postgres table 
                GetModelOutputValues output = new GetModelOutputValues(columnIndex, dataColumn, reachRowValueMap);
                return output.run();
	}
        
	
	@Override
	public Long getModelId() {
		if (context != null) {
			return context.getModelID();
		} else {
			return null;
		}
	}

}
