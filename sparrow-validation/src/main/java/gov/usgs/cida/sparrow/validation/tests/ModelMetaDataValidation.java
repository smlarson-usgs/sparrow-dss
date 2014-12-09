package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.sparrow.validation.framework.BaseTextFileTester;
import gov.usgs.cida.sparrow.validation.framework.TestResult;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.sparrow.validation.framework.*;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.service.SharedApplication;
import java.math.BigDecimal;


/**
 * Compares the db value for cumulative watershed area to the aggregated value,
 * built by adding up all the catchments upstream of the reach.
 * 
 * @author eeverman
 */
public class ModelMetaDataValidation extends SparrowModelValidationBase {
	
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	public ModelMetaDataValidation() {
		super(null, false);
	}
	
	public TestResult testModel(Long modelId) throws Exception {

		PredictData dbPredictData = SharedApplication.getInstance().getPredictData(modelId);
		SparrowModel model = dbPredictData.getModel();
		
		
		ConstituentType type = ConstituentType.SUSPENDED_SEDIMENT.fromStringIgnoreCase(model.getConstituent());
		
		if (type == null) {
			recordError(modelId, "This model has an unrecognized Constituent Type of '" + model.getConstituent() + "'.  "
					+ "It must be one of the ConstituentType enum values or its type must be added as an enum.  "
					+ "If added a new enum, it must have a concentration detection limit specified.");
		} else {
			BigDecimal dl = model.getDetectionLimit(DataSeriesType.total_concentration, ComparisonType.none);

			if (dl == null) {
			recordError(modelId, "This model has a Constituent Type of '" + model.getConstituent() + "', "
					+ "but no concentration Detection Limit specified for that type.  "
					+ "The ConstituentType enum must have a DL specified for this constituent.");
			}
		}
		
		


			


		return result;
	}
	

}

