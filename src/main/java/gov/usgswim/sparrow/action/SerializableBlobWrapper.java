package gov.usgswim.sparrow.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A wrapper for an object that indicates that it should be put in a
 * prepared statement as a Blob.
 * @author eeverman
 *
 */
public class SerializableBlobWrapper {
	public Object object;

	public SerializableBlobWrapper(Serializable object) {
		this.object = object;
	}
	
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream objOstream = new ObjectOutputStream(baos);
		objOstream.writeObject(object);
		byte[] bArray = baos.toByteArray();

		System.out.println("*** bArray = " + bArray);

		return bArray;
	}
}