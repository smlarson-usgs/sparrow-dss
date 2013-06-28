package gov.usgswim.sparrow.map;

import gov.usgswim.sparrow.SparrowProxyServlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.Part;

public class LoadMapStateServlet extends SparrowProxyServlet {

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/plain");
        OutputStream out = response.getOutputStream();
        try {
            MultipartParser parser = new MultipartParser(request, 1 * 1024 * 1024); // 10
                                                                                // MB
            Part part;
            while ((part = parser.readNextPart()) != null) {
                if (part.isFile()) {
                    // it's a file part
                    FilePart filePart = (FilePart) part;
                    String fileName = filePart.getFileName();
                    if (fileName != null) {
                        // the part actually contained a file
                        filePart.writeTo(out);
                    }
                    out.flush();
                }
            }
        } catch (IOException ioe) {
            this.log("Error reading or saving file", ioe);
        }
    }
}