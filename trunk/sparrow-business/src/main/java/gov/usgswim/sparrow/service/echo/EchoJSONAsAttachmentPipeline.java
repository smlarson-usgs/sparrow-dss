package gov.usgswim.sparrow.service.echo;

import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.JSON;
public class EchoJSONAsAttachmentPipeline extends EchoPipeline {

	public EchoJSONAsAttachmentPipeline() {
		super(JSON);
		this.setEchoAsAttachment("sparrow-session");
		// TODO add date-time format to filename
	}
}
