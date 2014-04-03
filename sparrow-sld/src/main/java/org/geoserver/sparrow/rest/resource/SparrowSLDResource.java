package org.geoserver.sparrow.rest.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.geoserver.rest.AbstractResource;
import org.geoserver.rest.format.DataFormat;
import org.geoserver.rest.format.MediaTypes;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

/**
 *
 * @author isuftin
 */
public class SparrowSLDResource extends AbstractResource {

    static final Logger LOG = org.geotools.util.logging.Logging.getLogger("org.geoserver.sparrow.rest");
    public static final MediaType MEDIATYPE_SLD = new MediaType("application/vnd.ogc.sld+xml");

    static {
        MediaTypes.registerExtension("sld", MEDIATYPE_SLD);
    }

    public SparrowSLDResource() {
    }

    public SparrowSLDResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    protected List<DataFormat> createSupportedFormats(Request request, Response response) {
        List<DataFormat> handledFormats = new ArrayList<>();
        RefectiveSparrowSLDFormat format = new RefectiveSparrowSLDFormat();
        handledFormats.add(format);
        return handledFormats;
    }

    @Override
    public synchronized void handleGet() {
        //TODO- What to do if we are missing any of these? 
        String workspace = getAttribute("workspace");
        String layer = getAttribute("layer");
        String sldName = getAttribute("sldName");
        String binLowList = getQueryStringValue("binLowList", String.class, "");
        String binHighList = getQueryStringValue("binHighList", String.class, "");
        String binColorList = getQueryStringValue("binColorList", String.class, "");
        Boolean boundedFlag = Boolean.parseBoolean(getQueryStringValue("bounded", String.class, "false"));
        String[] binLowListArray = StringUtils.split(binLowList, ',');
        String[] binHighListArray = StringUtils.split(binHighList, ',');
        String[] binColorListArray = StringUtils.split(binColorList, ',');

        String logString = "SLD Request:\n";
        logString += String.format("Workspace: %s\n", workspace);
        logString += String.format("Layer: %s\n", layer);
        logString += String.format("SLD Name: %s\n", sldName);
        logString += String.format("Bin Low List: %s\n", binLowList);
        logString += String.format("Bin High List: %s\n", binHighList);
        logString += String.format("Bin Color List: %s\n", binColorList);
        logString += String.format("Bounded: %s\n", String.valueOf(boundedFlag));
        LOG.log(Level.FINE, logString);

        RefectiveSparrowSLDFormat format = (RefectiveSparrowSLDFormat) getFormatGet();
        format.setRequest(getRequest());
        SparrowSLDInfo sldInfo = new SparrowSLDInfo(workspace, layer, sldName, binLowListArray, binHighListArray, binColorListArray, boundedFlag);
        Representation sldRepresentation = format.toRepresentation(sldInfo);
        try {
            System.out.println(sldRepresentation.getText());
        } catch (IOException ex) {
            Logger.getLogger(SparrowSLDResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            LOG.log(Level.FINE, sldRepresentation.getText());
        } catch (IOException ex) {
            //
        }
        getResponse().setEntity(sldRepresentation);
    }

    @Override
    public boolean allowGet() {
        return true;
    }

    @Override
    public boolean allowDelete() {
        return false;
    }

    @Override
    public boolean allowPost() {
        return false;
    }

    @Override
    public boolean allowPut() {
        return false;
    }

}
