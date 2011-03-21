package gov.usgswim.datatable.impl;

import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class BuilderHelperTest {

	@Test public void testBuildIndex() {
		long[] array = new long[] {23, 56, 93, 8344};

		Map<Long, Integer> map = BuilderHelper.buildIndex(array);

		assertEquals((Integer)0, map.get(23L));
		assertEquals((Integer)1, map.get(56L));
		assertEquals((Integer)2, map.get(93L));
		assertEquals((Integer)3, map.get(8344L));

	}

	@Test public void testFill() {
		SimpleDataTableWritable dt = new SimpleDataTableWritable();
		double[][] data = {
				{1.0,2.0,3.0,4.0},
				{1.1,2.1,3.2,4.1}
		};
		String[] headings = {"alpha", "beta", "gamma", "delta"};
		BuilderHelper.fill(dt, data, headings);
		assertEquals(2, dt.getRowCount());
		assertEquals(4, dt.getColumnCount());
		assertEquals(3.2, dt.getDouble(1, 2), .00001);
	}

	@Test public void testFillTranspose() {
		SimpleDataTableWritable dt = new SimpleDataTableWritable();
		double[][] data = {
				{1.0,2.0,3.0,4.0},
				{1.1,2.1,3.2,4.1}
		};
		String[] headings = {"alpha", "beta"};
		BuilderHelper.fillTranspose(dt, data, headings);
		assertEquals(4, dt.getRowCount());
		assertEquals(2, dt.getColumnCount());
		assertEquals(4.1, dt.getDouble(3,1), .00001);
	}

}
