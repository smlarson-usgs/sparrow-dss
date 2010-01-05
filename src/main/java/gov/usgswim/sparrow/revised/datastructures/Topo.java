package gov.usgswim.sparrow.revised.datastructures;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.datatable.impl.StandardIntColumnData;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;

public class Topo extends SimpleDataTable{
	public StandardIntColumnData fNode;
	public StandardIntColumnData tNode;
	public StandardIntColumnData ifTran;
	public StandardIntColumnData hydseq;

	public Topo(int[][] source) {
		//SimpleDataTable(ColumnData[] columns, String name, String description, Map<String, String> properties, long[] rowIds)
		super(sourceToColumns(source), "Topo", "topographic reach data", null, extractRowIds(source));
	}

	public Topo(DataTable source) {
		super(sourceToColumns(source), "Topo", "topographic reach data", null, extractRowIds(source));
	}

	public static ColumnData[] sourceToColumns(int[][] source) {
		assert(source.length == 4);
		StandardNumberColumnDataWritable<Integer> fNodeColWr = new StandardNumberColumnDataWritable<Integer>();
		StandardNumberColumnDataWritable<Integer> tNodeColWr = new StandardNumberColumnDataWritable<Integer>();
		StandardNumberColumnDataWritable<Integer> ifTranColWr = new StandardNumberColumnDataWritable<Integer>();
		StandardNumberColumnDataWritable<Integer> hydseqColWr = new StandardNumberColumnDataWritable<Integer>();

		ColumnData[] result = new ColumnData[4];
		{
			result[0] = fNodeColWr;
			result[1] = tNodeColWr;
			result[2] = ifTranColWr;
			result[3] = hydseqColWr;

			fNodeColWr.setName("fnode");
			tNodeColWr.setName("tnode");
			ifTranColWr.setName("iftran");
			hydseqColWr.setName("hydseq");

			fNodeColWr.setDescription("the upstream node of a reach");
			tNodeColWr.setDescription("the downstream node of a reach");
			ifTranColWr.setDescription("1 if the reach transfers flux from upstream node and incremental contribution to downstream node");
			hydseqColWr.setDescription("hydrological sequence order. Upstream reaches must have a lower hydseq number than downstream reaches");

			// These are all dimensionless quantities
			fNodeColWr.setUnits("");
			tNodeColWr.setUnits("");
			ifTranColWr.setUnits("");
			hydseqColWr.setUnits("");
		}


		// TODO
		return null;
	}

	public static ColumnData[] sourceToColumns(DataTable source) {
		return null;
	}

	public static long[] extractRowIds(int[][] source) {
		// TODO
		return null;
	}

	public static long[] extractRowIds(DataTable source) {
		// TODO
		return null;
	}
}
