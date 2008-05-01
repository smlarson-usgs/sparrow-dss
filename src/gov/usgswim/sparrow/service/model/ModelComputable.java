package gov.usgswim.sparrow.service.model;

import gov.usgswim.sparrow.domain.ModelImm;
import gov.usgswim.task.Computable;

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
