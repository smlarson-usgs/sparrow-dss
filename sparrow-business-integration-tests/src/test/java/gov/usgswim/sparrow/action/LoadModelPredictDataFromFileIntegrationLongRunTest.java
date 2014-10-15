package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.List;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowTestBaseWithDB;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.TopoData;
import gov.usgswim.sparrow.domain.Source;
import gov.usgswim.sparrow.domain.SparrowModel;

import org.junit.Test;

public class LoadModelPredictDataFromFileIntegrationLongRunTest extends SparrowTestBaseWithDB {

	static PredictData dbPredictData;
	static PredictData filePredictData;

	@Override
	public void doBeforeClassSingleInstanceSetup() throws Exception {

		//Normally we would load via the SharedApplication, which uses the cache.
		//However, since we want to ensure we are comparing db values to the text
		//file value, we call the Actions directly.
		LoadModelPredictData loadFromDb = new LoadModelPredictData(TEST_MODEL_ID);
		LoadModelPredictDataFromFile loadFromFile = new LoadModelPredictDataFromFile(TEST_MODEL_ID);
		dbPredictData = loadFromDb.run();
		filePredictData = loadFromFile.run();
	}

	@Test
	public void testModelMetadata() throws Exception {

		SimpleDateFormat dataFormat = new SimpleDateFormat("MM-d-yyyy");

		SparrowModel dbMod = dbPredictData.getModel();
		SparrowModel fileMod = filePredictData.getModel();


		assertEquals(dbMod.getConstituent(), fileMod.getConstituent());
		assertEquals(dbMod.getContactId(), fileMod.getContactId());

		assertEquals(dataFormat.format(dbMod.getDateAdded()), dataFormat.format(fileMod.getDateAdded()));
		assertEquals(dbMod.getDescription(), fileMod.getDescription());
		assertEquals(dbMod.getEastBound(), fileMod.getEastBound());
		assertEquals(dbMod.getEnhNetworkId(), fileMod.getEnhNetworkId());
		assertEquals(dbMod.getId(), fileMod.getId());
		assertEquals(dbMod.getName(), fileMod.getName());
		assertEquals(dbMod.getNorthBound(), fileMod.getNorthBound());
		//assertEquals(dbMod.getSessions(), fileMod.getSessions());
		assertEquals(dbMod.getSouthBound(), fileMod.getSouthBound());
		assertEquals(dbMod.getUnits(), fileMod.getUnits());
		assertEquals(dbMod.getUrl(), fileMod.getUrl());
		assertEquals(dbMod.getWestBound(), fileMod.getWestBound());

		List<Source> dbSrcs = dbMod.getSources();
		List<Source> fileSrcs = fileMod.getSources();
		for (int i = 0; i < dbSrcs.size(); i++) {
			Source dbSrc = dbSrcs.get(i);
			Source fileSrc = fileSrcs.get(i);

			assertEquals(dbSrc.getConstituent(), fileSrc.getConstituent());
			assertEquals(dbSrc.getDescription(), fileSrc.getDescription());
			assertEquals(dbSrc.getDisplayName(), fileSrc.getDisplayName());
			assertEquals(dbSrc.getId(), fileSrc.getId());
			assertEquals(dbSrc.getIdentifier(), fileSrc.getIdentifier());
			assertEquals(dbSrc.getModelId(), fileSrc.getModelId());
			assertEquals(dbSrc.getName(), fileSrc.getName());
			assertEquals(dbSrc.getSortOrder(), fileSrc.getSortOrder());

			SparrowUnits dbSrcUnit = dbSrc.getUnits();
			SparrowUnits fileSrcUnit = fileSrc.getUnits();
			assertEquals(dbSrcUnit, fileSrcUnit);

		}

	}

	@Test
	public void testTopo() throws Exception {
		DataTable db = dbPredictData.getTopo();
		DataTable file = filePredictData.getTopo();

		//compare, skipping column zero, which has db row ids.
		assertTrue(compareTables(db, file, new int[] {0}, true, 0d, false));
		
		assertTrue(file.isIndexed(PredictData.TOPO_TNODE_COL));
		assertTrue(file.isIndexed(PredictData.TOPO_FNODE_COL));
		assertTrue(file instanceof TopoData);
		
		assertTrue(db.isIndexed(PredictData.TOPO_TNODE_COL));
		assertTrue(db.isIndexed(PredictData.TOPO_FNODE_COL));
		assertTrue(db instanceof TopoData);
	}

	@Test
	public void testCoef() throws Exception {
		DataTable db = dbPredictData.getCoef();
		DataTable file = filePredictData.getCoef();
		assertTrue(compareTables(db, file));
	}

	@Test
	public void testDelivery() throws Exception {
		DataTable db = dbPredictData.getDelivery();
		DataTable file = filePredictData.getDelivery();

		//The text file version has row IDs, but we don't care.
		assertTrue(compareTables(db, file, null, false, 0d, false));
	}

	@Test
	public void testSrc() throws Exception {
		DataTable db = dbPredictData.getSrc();
		DataTable file = filePredictData.getSrc();
		assertTrue(compareTables(db, file));
	}

	@Test
	public void testSrcMetadata() throws Exception {
		DataTable db = dbPredictData.getSrcMetadata();
		DataTable file = filePredictData.getSrcMetadata();

		//Ignore column 0, which is the db id column
		assertTrue(compareTables(db, file, new int[]{0}, true, 0d, false));
	}

}
