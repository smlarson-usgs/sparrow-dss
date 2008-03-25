package gov.usgswim.service.pipeline;

import java.util.Arrays;
import java.util.List;

public class PipelineRegistry {
	public static final List<String> flatMimeTypes = Arrays.asList(new String[] {"csv", "tab", "excel", "html"});

	public static Pipeline lookup(PipelineRequest o) {
		if (o.isEcho()) {
			return new EchoPipeline();
		} else {
			return new SimplePipeline();
		}
		
	}


}
