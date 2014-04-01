/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoserver.sparrow.process;

import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author eeverman
 */
public class CreateDatastoreProcessTest {
	
	static final String TEST_HOME = "/test";
	
	private String originalUserHome;
	
	public CreateDatastoreProcessTest() {
	}
	
	@BeforeClass
	public static void setUpClass() {
	}
	
	@AfterClass
	public static void tearDownClass() {
	}
	
	@Before
	public void setUp() {
		originalUserHome = System.getProperty("user.home");
		System.setProperty("user.home", TEST_HOME);
	}
	
	@After
	public void tearDown() {
		System.setProperty("user.home", originalUserHome);
	}


	 @Test
	 public void testGetFileReference() {
		 
		 assertEquals(TEST_HOME + "/sample", CreateDatastoreProcess.getFileReference("~/sample").getAbsolutePath());
		 assertEquals(TEST_HOME + "/sample", CreateDatastoreProcess.getFileReference("sample").getAbsolutePath());
		 assertEquals("/sample", CreateDatastoreProcess.getFileReference("/sample").getAbsolutePath());
	 }
	
	
//	static class Wps extends CreateDatastoreProcess {
//		Wps() {
//			super(null, null);
//		}
//		
//		public File getFileReference(String path) {
//			return super.getFileReference(path);
//		}
//	}
}
