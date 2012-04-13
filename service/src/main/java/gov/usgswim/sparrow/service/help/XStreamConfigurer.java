package gov.usgswim.sparrow.service.help;

import com.thoughtworks.xstream.XStream;

public interface XStreamConfigurer {
	public XStream configure(XStream xstream);
}
