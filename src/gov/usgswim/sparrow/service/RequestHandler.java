package gov.usgswim.sparrow.service;

import java.io.IOException;
import java.io.OutputStream;

public interface RequestHandler<T> {
	public void dispatch(T request, OutputStream out) throws IOException;
}
