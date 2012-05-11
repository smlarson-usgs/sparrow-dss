package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.datatable.AggregateType;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test (offline) of the CalcAggregate Action.
 * 
 * TODO:  Nulls are not tested because some of the data2D classes choke on null
 * values, even though some of our values may actually be nulls.
 * 
 * @author eeverman
 *
 */
public class CalcAggregateTest {

	DataTable hucs;
	DataTable reachValues;
	SparrowColumnSpecifier reachValueCol;
	
	@Before
	public void setup() {
		ColumnDataWritable hucCol = new StandardNumberColumnDataWritable<Integer>().setType(Integer.class);

		DataTableWritable hucsW = new SimpleDataTableWritable();
		hucsW.addColumn(hucCol);
		
		/////////////////////////
		// Create the ReachID & Huc associations (normally ceated by the LoadReachHucs Action.
		/////////////////////////
		
		int[] reachHucs = new int[] {
			102, 2,	/* reach 102 is in huc 2, order shifted to test sort */	
			101, 1,	
			103, 3,
			104, 1, 
			105, 2,
			106, 3, 
			107, 1,
			108, 2,
			109, 3
		};
		
		for (int i=0; i < reachHucs.length; i+=2) {
			hucsW.setValue(reachHucs[i + 1], (i / 2), 0);	//Set the value, which is the HUC ID
			hucsW.setRowId(reachHucs[i], (i / 2));	//Set the row id, which is the reach ID
		}
		
		hucs = hucsW.toImmutable();
		
		
		/////////////////////////
		// Create values to aggregate (nominally the prediction values)
		/////////////////////////
		Integer[] reachValueArray = new Integer[] {
				101, 10,	/* reach 101 is in huc 1 */	
				102, -9,
				103, 0,
				104, 11, 
				105, 3,
				106, 0, 
				107, 12,
				108, -6,
				109, 0
			};
		
		//Summary by HUC:
		// 1: 10, 11, 12 (avg 11)
		// 2: -9, 3, -6 (avg -4)
		// 3: 0, 0, 0 (avg 0)
		
		ColumnDataWritable valueCol = new StandardNumberColumnDataWritable<Double>().setType(Double.class);

		DataTableWritable reachValuesW = new SimpleDataTableWritable();
		reachValuesW.addColumn(valueCol);
		
		for (int i=0; i < reachValueArray.length; i+=2) {
			reachValuesW.setValue(reachValueArray[i + 1], (i / 2), 0);	//Set the value, which is the HUC ID
			reachValuesW.setRowId(reachValueArray[i], (i / 2));	//Set the row id, which is the reach ID
		}
		
		reachValues = reachValuesW.toImmutable();
		reachValueCol = new SparrowColumnSpecifier(reachValues, 0, null);
	}
	
	
	@Test
	public void average() throws Exception {
		CalcAggregate action = new CalcAggregate();
		action.setAggType(AggregateType.avg);
		action.setData(reachValueCol);
		action.setReachHucs(hucs);
		SparrowColumnSpecifier result = action.run();
		
		//Row 0, HUC 1
		assertEquals(11d, result.getDouble(0).doubleValue(), .000000000000001d);
		assertEquals(1L, result.getIdForRow(0).longValue());
		
		//Row 1, HUC 2
		assertEquals(-4d, result.getDouble(1), .000000000000001d);
		assertEquals(2L, result.getIdForRow(1).longValue());
		
		//Row 2, HUC 3
		assertEquals(0d, result.getDouble(2).doubleValue(), .000000000000001d);
		assertEquals(3L, result.getIdForRow(2).longValue());
	}
	
	@Test
	public void min() throws Exception {
		CalcAggregate action = new CalcAggregate();
		action.setAggType(AggregateType.min);
		action.setData(reachValueCol);
		action.setReachHucs(hucs);
		SparrowColumnSpecifier result = action.run();
		
		//asserts
		assertEquals(10d, result.getDouble(0).doubleValue(), .000000000000001d);
		assertEquals(-9d, result.getDouble(1), .000000000000001d);
		assertEquals(0d, result.getDouble(2).doubleValue(), .000000000000001d);
	}
	
	@Test
	public void max() throws Exception {
		CalcAggregate action = new CalcAggregate();
		action.setAggType(AggregateType.max);
		action.setData(reachValueCol);
		action.setReachHucs(hucs);
		SparrowColumnSpecifier result = action.run();
		
		//asserts
		assertEquals(12d, result.getDouble(0).doubleValue(), .000000000000001d);
		assertEquals(3d, result.getDouble(1), .000000000000001d);
		assertEquals(0d, result.getDouble(2).doubleValue(), .000000000000001d);
	}
	
	@Test
	public void sum() throws Exception {
		CalcAggregate action = new CalcAggregate();
		action.setAggType(AggregateType.sum);
		action.setData(reachValueCol);
		action.setReachHucs(hucs);
		SparrowColumnSpecifier result = action.run();
		
		//asserts
		assertEquals(33d, result.getDouble(0).doubleValue(), .000000000000001d);
		assertEquals(-12d, result.getDouble(1), .000000000000001d);
		assertEquals(0d, result.getDouble(2).doubleValue(), .000000000000001d);
	}
	
	@Test
	public void none() throws Exception {
		CalcAggregate action = new CalcAggregate();
		action.setAggType(AggregateType.none);
		action.setData(reachValueCol);
		action.setReachHucs(hucs);
		SparrowColumnSpecifier result = action.run();
		
		//asserts - This should be the reachValues now, not agg data
		assertEquals(10d, result.getDouble(0).doubleValue(), .000000000000001d);
		assertEquals(-9d, result.getDouble(1), .000000000000001d);
		assertEquals(9, result.getRowCount());
	}
}
