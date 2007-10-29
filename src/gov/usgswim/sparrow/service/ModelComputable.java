package gov.usgswim.sparrow.service;

import com.ctc.wstx.stax.WstxOutputFactory;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DView;
import gov.usgswim.task.Computable;
import gov.usgswim.sparrow.Int2DImm;
import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.domain.ModelImm;
import gov.usgswim.sparrow.util.JDBCUtil;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.List;

import javax.xml.stream.XMLEventWriter;

import org.apache.log4j.Logger;


public class ModelComputable implements Computable<ModelRequest, ModelImm> {
	protected static Logger log =
		Logger.getLogger(ModelComputable.class); //logging for this class
		
	public ModelComputable() {
	}

	public ModelImm compute(ModelRequest req) throws Exception {
		//find just one model and return it....
		
		return null;
	}
	
}
