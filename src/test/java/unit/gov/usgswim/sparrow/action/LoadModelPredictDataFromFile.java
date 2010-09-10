package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.filter.ColumnRangeFilter;
import gov.usgswim.datatable.filter.FilteredDataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.utils.DataTableUtils;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.SourceBuilder;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.domain.SparrowModelBuilder;
import gov.usgswim.sparrow.util.DLUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;

public class LoadModelPredictDataFromFile extends Action<PredictData> {

	//Constants
	public static final String SOURCE_METADATA_FILE = "src_metadata.txt";
	public static final String TOPO_FILE = "topo.txt";
	public static final String SOURCE_COEF_FILE = "coef.txt";
	public static final String SOURCE_VALUES_FILE = "src.txt";
	//public static final int SOURCE_ID_COL = 0;
	
	protected static final String BASE_MODEL_FILE_PATH =
		"/gov/usgswim/sparrow/test/shared/model50/";
	
	
	//Instance
	private Long modelId;
	

	
	/**
	 * Creates a Action to load the entire model
	 * @param modelId the ID of the Sparrow Model to load
	 * @param bootstrap	<b>true</b>		to load only the data required to run a prediction.
	 * 					<b>false</b>	to load the complete dataset for a model (including bootstrap data). 
	 */
	public LoadModelPredictDataFromFile(Long modelId) {
		this.modelId = modelId;
	}
	
	@Override
	public PredictData doAction() throws Exception {
		PredictDataBuilder dataSet = new PredictDataBuilder();
		
		dataSet.setSrcMetadata( loadSourceMetadata(modelId));
		dataSet.setModel( loadSparrowModel(modelId, dataSet.getSrcMetadata()) );
		dataSet.setTopo( loadTopo(modelId) );
		
		
		//Source coefs and delivery are stored in one file, so load and filter.
		DataTable allCoefs = loadAllSourceReachCoef(modelId, dataSet.getSrcMetadata());
		dataSet.setCoef(this.filterForSourceCoef(allCoefs));
		dataSet.setDelivery(this.filterForDelivery(allCoefs));
		
		dataSet.setSrc( loadSourceValues(modelId, dataSet.getSrcMetadata()) );
		
		return dataSet.toImmutable();
	}
	
	/**
	 * Builds the path to the specified model resource.
	 * @param modelId
	 * @param fileName
	 * @return
	 */
	public String getModelResourceFilePath(Long modelId, String fileName) {
		//Ignore the model - we always return model 50
		return BASE_MODEL_FILE_PATH + fileName;
	}
	
	public SparrowModel loadSparrowModel(long modelId, DataTable srcMetaData) {
		SparrowModelBuilder model = new SparrowModelBuilder();
		model.setApproved(true);
		model.setArchived(false);
		model.setPublic(true);
		model.setConstituent("Nitrogen");
		model.setContactId(50L);
		model.setDateAdded(new Date());
		model.setName("MRB02 Nitrogen");
		model.setDescription("2002 Total Nitrogen Model for the Southeastern U.S. (MRB2)");
		model.setEastBound(-88.2);
		model.setWestBound(-76.2);
		model.setNorthBound(36.4);
		model.setSouthBound(25.6);
		model.setEnhNetworkId(23L);
		model.setId(50L);
		model.setUnits("kg/year");
		
		for (int r=0; r< srcMetaData.getRowCount(); r++) {
			SourceBuilder src = new SourceBuilder();
			src.setId(srcMetaData.getLong(r, 0));
			src.setSortOrder(r + 1);
			src.setName(srcMetaData.getString(r, 1));
			src.setDisplayName(srcMetaData.getString(r, 2));
			src.setDescription(srcMetaData.getString(r, 3));
			src.setConstituent(srcMetaData.getString(r, 4));
			src.setUnits(srcMetaData.getString(r, 5));
			model.addSource(src);
		}
		
		model.setSessions(new HashSet<Entry<Object, Object>>());
		
		return model.toImmutable();
	}
	
	/**
	 * Returns metadata about the source types in the model.
	 *
	 * Typically 5-10 rows per model.
	 *
	 * <h4>Data Columns (sorted by SORT_ORDER)</h4>
	 * <h5>IDENTIFIER - The Row ID (not a column). The SparrowModel specific ID
	 * for the source (starting w/ 1)</h5>
	 * <ol>
	 * <li>SOURCE_ID** - (long) Nominally The db ID, but here is a duplicate of the row id.
	 * <li>NAME - (String) The full (long text) name of the source
	 * <li>DISPLAY_NAME - (String) The short name of the source, used for display
	 * <li>DESCRIPTION - (String) A description of the source (could be long)
	 * <li>CONSTITUENT - (String) The name of the Constituent being measured
	 * <li>UNITS - (String) The units the constituent is measured in
	 * <li>PRECISION - (int) The number of decimal places
	 * <li>IS_POINT_SOURCE (boolean) 'T' or 'F' values that can be mapped to boolean.
	 * </ol>
	 * 
	 * **Differs from db version of loading.
	 * 
	 * @param modelId
	 * @return
	 */
	public DataTable loadSourceMetadata(long modelId) {
		
		String[] headings = new String[] {
				"SOURCE_ID",	//is ID in text file (also copied as row id)
				"SORT_ORDER",	//Shouldn't be included in final data
				"NAME",
				"DISPLAY_NAME",
				"DESCRIPTION",
				"CONSTITUENT",
				"UNITS",
				"PRECISION",
				"IS_POINT_SOURCE"
		};
		
		Class<?>[] types= {
				Long.class,		//SOURCE_ID - (long) The database unique ID for the source
				Integer.class,	//SORT_ORDER - Don't include
				String.class,	//NAME - (String) The full (long text) name of the source
				String.class,	//DISPLAY_NAME - (String) The short name of the source, used for display
				String.class,	//DESCRIPTION - (String) A description of the source (could be long)
				String.class,	//CONSTITUENT - (String) The name of the Constituent being measured
				String.class,	//UNITS - (String) The units the constituent is measured in
				Integer.class,	//PRECISION - (int) The number of decimal places
				String.class	//IS_POINT_SOURCE (boolean) 'T' or 'F' values that can be mapped to boolean.
				};
		DataTableWritable sourceMeta =
			new SimpleDataTableWritable(headings, null, types);
		
		String filePath = getModelResourceFilePath(modelId, SOURCE_METADATA_FILE);
		DataTableWritable result = DataTableUtils.fill(sourceMeta, filePath, false, "\t", true);
		result.removeColumn(1);
		copyColumnAsRowId(result, 0);
		result.setName("sourceMetadata");
		

		return result.toImmutable();
	}
	
	/**
	 * Returns an ordered DataTable of all topological data in the MODEL
	 * <h4>Data Columns</h4>
	 * One row per reach (i = reach index)
	 * <h5>Row ID: MRB_ID from txt, which is the same as IDENTIFIER from DB</h5>
	 * <ol>
	 * <li>[i][0]MODEL_REACH** - Copy of text MRB_ID, since no db id.
	 * <li>[i][1]FNODE - The from node
	 * <li>[i][2]TNODE - The to node
	 * <li>[i][3]IFTRAN - 1 if this reach transmits to its end node, 0 otherwise
	 * <li>[i][4]HYDSEQ - Hydrologic sequence order (starting at 1, no gaps)
	 * </ol>
	 *
	 * **Differs from db version of loading.
	 * 
	 * <h4>Sorting</h4>
	 * As per the text file.  This should match a sorting of HYDSEQ then
	 * IDENTIFIER from the db.
	 * 
	 * For complete data definitions, please see:
	 * @see gov.usgswim.sparrow.PredictData#getTopo()
	 *
	 * @param modelId	The ID of the Sparrow model
	 * @return Fetched data - see Data Columns above.
	 */
	public DataTableWritable loadTopo(long modelId) {
		String topoFile = getModelResourceFilePath(modelId, TOPO_FILE);
		
		String[] headings = {
				"MODEL_REACH",	//actually the MRB_ID in the file
				"FNODE",
				"TNODE",
				"IFTRAN",
				"HYDSEQ",
				"frac"		//In file, but not used - remove
				};
		Class<?>[] types= {
				Integer.class,	//[i][0]MODEL_REACH
				Integer.class,	//[i][1]FNODE
				Integer.class,	//[i][2]TNODE
				Integer.class,	//[i][3]IFTRAN
				Integer.class,	//[i][4]HYDSEQ - seems to be empty for some
				String.class	//frac
				};
		DataTableWritable topo = new SimpleDataTableWritable(headings, null, types);
		
		topo.setName("topo");

		DataTableUtils.fill(topo, topoFile, false, "\t", true);
		
		//repopulate hydseq to be 1 and up, w/o gaps
		for (int r = 0; r < topo.getRowCount(); r++) {
			topo.setValue(new Integer(r + 1), r, 4);
		}
		
		//Normally the db version has the identifier as the row ID and the
		//model_reach (the db key) as column 0.  Here we are duplicating
		//the identifier (mrb_id) into both.
		copyColumnAsRowId(topo, 0);
		
		topo.removeColumn(5);
		
		return topo;
	}
	
	/**
	 * Loads a data table with delivery and individual source coefs for all
	 * iterations.  Note that this is a co-joined table, that is, its needs
	 * to be split up to be useful as the coef and delivery/decay tables.
	 * 
	 * Each row uses the reach identifier as a row id.
	 * 
	 * columns will be:
	 * Iteration
	 * Inc_Delivery
	 * Total_Delivery
	 * Boot_Error
	 * Coef for Source 1
	 * Coef for Source 2, etc...
	 * 
	 * @param modelId
	 * @param sourceMetaData
	 * @return
	 */
	public DataTable loadAllSourceReachCoef(long modelId, DataTable sourceMetaData) {
		
		final int SRC_META_NAME_COL = 1;
		
		String coefFile = getModelResourceFilePath(modelId, SOURCE_COEF_FILE);
		ArrayList<String> headings = new ArrayList<String>();
		ArrayList<Class<?>> types = new ArrayList<Class<?>>();
		
		headings.add("ITER");		//The iteration - is removed
		headings.add("INC_DELIVF");
		headings.add("TOT_DELIVF");
		headings.add("BOOT_ERROR");

		types.add(Integer.class);		//ITER
		types.add(Double.class);		//INC_DELIVF
		types.add(Double.class);		//TOT_DELIVF
		types.add(Double.class);		//BOOT_ERROR
		
		DataTableWritable result = new SimpleDataTableWritable(headings.toArray(new String[4]), null, types.toArray(new Class<?>[4]));

		for (int srcIndex = 0; srcIndex < sourceMetaData.getRowCount(); srcIndex++) {
			String name = sourceMetaData.getString(srcIndex, SRC_META_NAME_COL);
			StandardNumberColumnDataWritable<Double> newCol = new StandardNumberColumnDataWritable<Double>(name, null);
			newCol.setType(Double.class);
			result.addColumn(newCol);
			// may need to add names
		}
		
		result.setName("all_coef");
		DataTableUtils.fill(result, coefFile, true, "\t", true);
		result.removeColumn(0);	//Iteration - we weren't using it.
		
		//Must be immutable b/c it is used twice for delivery and coef as filtered
		//tables.  When toImmutable is called on these, it invalidates the underlying
		//non-immutable table.
		return result.toImmutable();
	}
	
	
	/**
	 * Filters the passed DataTable to be only delivery data.
	 *
	 * <h4>Data Columns</h4>
	 * The MRB_ID (same as the IDENTIFIER in the db) is used as a rowID, though
	 * in the db version of this loader there is no row ID.
	 * <p>One row per reach (i = reach index)</p>
	 * <ol>
	 * <li>[i][0] == the instream delivery at reach i
	 * <li>[i][1] == the upstream delivery at reach i.
	 * </ol>
	 * <h4>Sorting</h4>
	 * As per the text file.  This should match a sorting of HYDSEQ then
	 * IDENTIFIER from the db.
	 * 
	 * It is assumed that the table currently only has data for the 1st iteration.
	 * @param allCoefsTable
	 * @return
	 */
	public DataTable filterForDelivery(DataTable allCoefsTable) {
		
		ColumnRangeFilter columnFilter = new ColumnRangeFilter(0, 2);
		DataTable delivery = new FilteredDataTable(allCoefsTable, null, columnFilter);
		return delivery;
	}
	
	/**
	 * Filters the passed DataTable to contain only the source-reach coef's.
	 * 
	 * <h4>Data Columns</h4>
	 * <p>One row per reach (i = reach index).
	 * Row ID is IDENTIFIER (not the db model_reach_id)</p>
	 * <ol>
	 * <li>[i][Source 1] - The coef for the first source of reach i
	 * <li>[i][Source 2] - The coef's for the 2nd source of reach i
	 * <li>[i][Source 2] - The coef's for the 3rd...
	 * <li>...as many columns as there are sources.
	 * </ol>
	 * For complete data definitions, please see:
	 * @see gov.usgswim.sparrow.PredictData#getDeliverygetCoef()
	 * 
	 * <h4>Sorting</h4>
	 * As per the text file, which should match sorting by HYDSEQ then 
	 * IDENTIFIER from the db.
	 * 
	 * @param allCoefsTable
	 * @return
	 */
	public DataTable filterForSourceCoef(DataTable allCoefsTable) {
		
		//1st four cols are iteration, inc_del, tot_del, and boot_err
		//(MRB_ID has been stripped out as the ID)
		int srcCoefsCount = allCoefsTable.getColumnCount() - 3;
		
		ColumnRangeFilter columnFilter = new ColumnRangeFilter(3, srcCoefsCount);
		DataTable coefs = new FilteredDataTable(allCoefsTable, null, columnFilter);
		return coefs;
	}
	
	/**
	 * Returns a DataTable of all source values for a single model.
	 * <h4>Data Columns with one row per reach (sorted by HYDSEQ)</h4>
	 * <ol>
	 * <li>id: IDENTIFIER - The model specific ID for this reach (not a column)
	 * <li>[Source 1] - The values for the first source in one column
	 * <li>[Source 2...] - The values for the 2nd...
	 * <li>...
	 * </ol>
	 * @param modelId
	 * @param sourceMetaData
	 * @return
	 */
	public DataTableWritable loadSourceValues(long modelId, DataTable sourceMetaData) {
		int sourceCount = sourceMetaData.getRowCount();
		if (sourceCount == 0) {
			throw new IllegalArgumentException("There must be at least one source");
		}
		
		DataTableWritable sourceValues = new SimpleDataTableWritable();

		for (int srcIndex = 0; srcIndex < sourceCount; srcIndex++) {
			String name = sourceMetaData.getString(srcIndex, sourceMetaData.getColumnByName("NAME"));
			String constituent = sourceMetaData.getString(srcIndex, sourceMetaData.getColumnByName("CONSTITUENT"));
			String units = sourceMetaData.getString(srcIndex, sourceMetaData.getColumnByName("UNITS"));
			String precision = sourceMetaData.getString(srcIndex, sourceMetaData.getColumnByName("PRECISION"));

			StandardNumberColumnDataWritable<Double> column = new StandardNumberColumnDataWritable<Double>(name, units);
			column.setProperty(TableProperties.CONSTITUENT.getPublicName(), constituent);
			column.setProperty(TableProperties.PRECISION.getPublicName(), precision);
			column.setType(Double.class);
			sourceValues.addColumn(column);
		}

		sourceValues.setName("source");

		// Create columns
		String sourceValuesFile = getModelResourceFilePath(modelId, SOURCE_VALUES_FILE);
		DataTableUtils.fill(sourceValues, sourceValuesFile, true, "\t", true);
		return sourceValues;
	}
	
	protected static void loadIndexValues(Connection conn, DataTableWritable table,
			String baseQuery, String indexColumnName) throws SQLException {
		//Grab the query for the first source, but only taking the ID vals

		String query = "Select " + indexColumnName + " from ( " + baseQuery + " )";

		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		st.setFetchSize(2000);
		ResultSet rs = null;
		try {
			rs = st.executeQuery(query);
			DLUtils.loadIndex(rs, table, 0);
		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}
		}
	}
	
	public void copyColumnAsRowId(DataTableWritable table, int columnIndex) {
		for (int i=0; i<table.getRowCount(); i++) {
			long id = table.getLong(i, columnIndex);
			table.setRowId(id, i);
		}
	}

	public Long getModelId() {
		return this.modelId;
	}

	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}

}
