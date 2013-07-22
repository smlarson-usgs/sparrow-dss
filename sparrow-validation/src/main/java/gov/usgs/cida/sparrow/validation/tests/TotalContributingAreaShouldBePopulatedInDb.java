package gov.usgs.cida.sparrow.validation.tests;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.sparrow.validation.framework.TestResult;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.sparrow.validation.framework.*;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.service.SharedApplication;


/**
 * Ensures the Total Contributing Area is populated for a given model
 *
 * @author cschroed
 */
public class TotalContributingAreaShouldBePopulatedInDb extends SparrowModelValidationBase {


	@Override
	public boolean requiresDb() { return true; }
	@Override
	public boolean requiresTextFile() { return false; }

	public TotalContributingAreaShouldBePopulatedInDb(Comparator comparator, boolean failedTestIsOnlyAWarning) {
		super(comparator, failedTestIsOnlyAWarning);
	}

	@Override
	public TestResult testModel(Long modelId) throws Exception {
		TestResult testResult = new TestResult(modelId, this.getClass().getName());
		String queryStr =
			"SELECT COUNT(*) NULL_AREAS_FOR_MODEL \n" +
			"FROM model_attrib_vw\n" +
			"WHERE sparrow_model_id = '" + modelId + "'\n" +
//^ could be injection here, but don't care because this isn't public-facing and the executor must already know DB creds to run this anyway ^
			"AND tot_contrib_area  IS NULL";

		DataTableWritable queryResults = SharedApplication.queryToDataTable(queryStr);
		ColumnData column = queryResults.getColumn(0);
		Long numNullAreasForModel = column.getLong(0);


		if(0L != numNullAreasForModel){
			this.recordError(modelId, "Total Contributing Areas Missing From Model " + modelId);
			testResult.addError();
		}
		return testResult;
	}


}

