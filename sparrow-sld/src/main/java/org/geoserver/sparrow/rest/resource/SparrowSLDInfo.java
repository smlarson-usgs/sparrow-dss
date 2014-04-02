package org.geoserver.sparrow.rest.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author isuftin
 */
public class SparrowSLDInfo implements Serializable {

    private static final long serialVersionUID = -1719366685408084145L;

    private String workspace;
    private String layer;
    private String sldName;
    private Boolean bounded;
    private List<Map<String, String>> bins = new ArrayList<>();

    private SparrowSLDInfo() {
        this.workspace = null;
        this.layer = null;
        this.sldName = null;
        this.bins = null;
    }

    public SparrowSLDInfo(String workspace, String layer, String sldName, String[] binLowList, String[] binHighList, String[] binColorList, Boolean bounded) {
        this.workspace = workspace;
        this.layer = layer;
        this.sldName = sldName;
        this.bounded = bounded;
        
        bins = new ArrayList<>();

        for (int ind = 0; ind < binLowList.length; ind++) {
            String lower = binLowList[ind];
            String upper = binHighList[ind];
            String color = binColorList[ind];
            Map<String, String> bin = new HashMap<>();
            bin.put("lower", lower);
            bin.put("upper", upper);
            bin.put("color", color);
            bins.add(bin);
        }
    }

    /**
     * @return the workspace
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * @param workspace the workspace to set
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    /**
     * @return the layer
     */
    public String getLayer() {
        return layer;
    }

    /**
     * @param layer the layer to set
     */
    public void setLayer(String layer) {
        this.layer = layer;
    }

    /**
     * @return the sldName
     */
    public String getSldName() {
        return sldName;
    }

    /**
     * @param sldName the sldName to set
     */
    public void setSldName(String sldName) {
        this.sldName = sldName;
    }

    /**
     * @return the bins
     */
    public List<Map<String, String>> getBins() {
        return Collections.unmodifiableList(bins);
    }

    /**
     * @param bins the bins to set
     */
    public void setBins(List<Map<String, String>> bins) {
        this.bins = new ArrayList<>(bins);
    }

    /**
     * @return the bounded
     */
    public Boolean getBounded() {
        return bounded;
    }

    /**
     * @param bounded the bounded to set
     */
    public void setBounded(Boolean bounded) {
        this.bounded = bounded;
    }

}
