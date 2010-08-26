package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.ColumnMappedTable;
import gov.usgswim.datatable.filter.ColumnRangeFilter;
import gov.usgswim.datatable.filter.FilteredDataTable;
import gov.usgswim.datatable.filter.RowRangeFilter;
import gov.usgswim.datatable.utils.DataTablePrinter;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.LoadModelPredictDataOfflineTest;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;
import java.sql.Connection;

public class LoadModelPredictDataTest extends LoadModelPredictDataOfflineTest {
	public static final Long TEST_MODEL = 32L;

	private Connection conn;


	public LoadModelPredictDataTest(String sTestName) {
		super(sTestName);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		conn = SharedApplication.getConnectionFromCommandLineParams();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		conn.close();
		conn = null;
	}


	/**
	 * @see DataLoader#loadTopo(Connection,int)
	 */
	public void testLoadTopo() throws Exception {
		DataTable jdbcData = LoadModelPredictData.loadTopo(conn, TEST_MODEL);
		System.out.println("  -- Printing Topo --");
		DataTablePrinter.printDataTableSample(jdbcData, 30, 30);

//		assertEquals(2339, jdbcData.getRowCount());
//		assertEquals(4 + 1, jdbcData.getColumnCount());

//		DataTableCompare comp = buildTopoComparison(jdbcData);
//		// 9717 is the maximum reach hydrological sequence
//		assertEquals(Integer.valueOf(2250), comp.getMaxInt(0));
//		assertEquals(Integer.valueOf(-8688), comp.getMinInt(0));
//		assertEquals(Integer.valueOf(2959), comp.getMaxInt(1));
//		assertEquals(Integer.valueOf(-3368), comp.getMinInt(1));
//		assertEquals(Integer.valueOf(9717), comp.getMaxInt(2));
//		assertEquals(Integer.valueOf(1), comp.getMinInt(2));
//		assertEquals(Integer.valueOf(0), comp.getMaxInt(3));
//		assertEquals(Integer.valueOf(-2338), comp.getMinInt(3));
//
//		assertEquals(Integer.valueOf(9717), comp.getMaxInt());
	}


	public void testLoadSourceReachCoef() throws Exception {
		DataTable sourceMetadata = LoadModelPredictData.loadSourceMetadata(conn, TEST_MODEL);
		DataTable jdbcData = LoadModelPredictData.loadSourceReachCoef(conn, TEST_MODEL, 0, sourceMetadata);
		System.out.println("  -- Printing Source Reach Coefficients --");
		DataTablePrinter.printDataTableSample(jdbcData, 30, 30);
	}

	public void testLoadDecay() throws Exception {
		DataTable jdbcData = LoadModelPredictData.loadDelivery(conn, TEST_MODEL, 0);
		System.out.println("  -- Printing Decay Coefficients --");
		DataTablePrinter.printDataTableSample(jdbcData, 30, 30);
	}

	public void testSourceValues() throws Exception {
		DataTable sourceMetadata = LoadModelPredictData.loadSourceMetadata(conn, TEST_MODEL);
		DataTable jdbcData = LoadModelPredictData.loadSourceValues(conn, TEST_MODEL, sourceMetadata);
		System.out.println("  -- Printing Decay Coefficients --");
		DataTablePrinter.printDataTableSample(jdbcData, 30, 30);
	}


	// ==============
	// HELPER METHODS
	// ==============

	protected DataTableCompare buildTopoComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/topo.txt");
		DataTable data = TabDelimFileUtil.readAsInteger(fileStream, true, -1);

		DataTableCompare comp= new DataTableCompare(data, toBeCompared, true);
		return comp;
	}

	protected DataTableCompare buildSourceReachCoefComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/coef.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int firstNonZeroRow = data.findFirst(0, Double.valueOf(1));
		DataTable view = new FilteredDataTable(data, new RowRangeFilter(0, firstNonZeroRow), new ColumnRangeFilter(4, 11)); //Crop to only iteration 0 and remove non-coef columns

		DataTableCompare comp= new DataTableCompare(view, toBeCompared, true);

		return comp;
	}

	protected DataTableCompare buildDecayComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/coef.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int firstNonZeroRow = data.findFirst(0, Double.valueOf(1));
		DataTable view = new FilteredDataTable(data, new RowRangeFilter(0, firstNonZeroRow), new ColumnRangeFilter(1, 2));	//Crop to only iteration 0 and only the two decay columns

		DataTableCompare comp= new DataTableCompare(view, toBeCompared, true);

		return comp;
	}

	protected DataTableCompare buildSourceValueComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/src.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);

		DataTableCompare comp= new DataTableCompare(data, toBeCompared, true);

		return comp;
	}

	protected DataTableCompare buildPredictionComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int[] DEFAULT_COMP_COLUMN_MAP =
			new int[] {40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 39, 15};
		ColumnMappedTable mappedTable = new ColumnMappedTable(data, DEFAULT_COMP_COLUMN_MAP);

		DataTableCompare comp = new DataTableCompare(toBeCompared, mappedTable, true);

		return comp;
	}


}
