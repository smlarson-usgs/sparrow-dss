package mcsadapter;

import java.awt.Dimension;
import java.net.URL;
import java.net.URLEncoder;

import java.util.Properties;
import oracle.lbs.mapclient.MapViewer;
import oracle.lbs.mapcommon.MapResponse;
import oracle.mapviewer.share.mapcache.*;

public class MVAdapter extends MapSourceAdapter
{
  /**
   * Gets the map tile request string that is to be sent to the map service
   * provider URL.
   * @param map tile definition
   * @return request string
   */
  public String getMapTileRequest(TileDefinition tile)
  {
    // Get map source specified parameters
    Properties props = this.getProperties() ;
    String dataSource = props.getProperty("data_source") ;
    String baseMap = props.getProperty("base_map") ;
    String transparency = props.getProperty("transparent") ;
    // Use oracle.lbs.mapclient.MapViewer to construct the request string
    MapViewer mv = new MapViewer(this.getMapServiceURL()) ;
    mv.setDataSourceName(dataSource);
    mv.setBaseMapName(baseMap);
    mv.setDeviceSize(new Dimension(tile.getImageWidth(), tile.getImageHeight()));
    mv.setCenterAndSize(tile.getBoundingBox().getCenterX(), 
                        tile.getBoundingBox().getCenterY(),
                        tile.getBoundingBox().getHeight());
    int format = MapResponse.FORMAT_PNG_STREAM ;
    String req = null ;
    switch(tile.getImageFormat())
    {
      case TileDefinition.FORMAT_GIF:
        mv.setImageFormat(MapResponse.FORMAT_GIF_URL);
        req = mv.getMapRequest().toXMLString().replaceFirst("format=\"GIF_URL\"", "format=\"GIF_STREAM\"") ;
        break ;
      case TileDefinition.FORMAT_PNG:
        mv.setImageFormat(MapResponse.FORMAT_PNG_URL);
        if("true".equalsIgnoreCase(transparency))
        {
          mv.setBackgroundTransparent(true);
          mv.setBackgroundColor(null);
        }
        mv.setIsTileRequest(true);
        req = mv.getMapRequest().toXMLString().replaceFirst("format=\"PNG_URL\"", "format=\"PNG_STREAM\"") ;
        break ;
      case TileDefinition.FORMAT_JPEG:
        mv.setImageFormat(MapResponse.FORMAT_JPEG_URL);
        req = mv.getMapRequest().toXMLString().replaceFirst("format=\"JPEG_URL\"", "format=\"JPEG_STREAM\"") ;
        break ;
    }
    
    byte[] reqBytes = null ;
    String reqStr = null ;
    try
    {
      reqBytes = req.getBytes("UTF8") ;
      reqStr = URLEncoder.encode((new String(reqBytes)).replaceAll("\n", ""), "UTF-8") ;
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    // Return the request string.
    return "xml_request="+ reqStr;
  }
}