/* Copyright (c) 2005, 2010, Oracle and/or its affiliates. 
All rights reserved. */

package mcsadapter;

import java.awt.geom.Rectangle2D;

import java.util.Iterator;
import java.util.Properties;
import oracle.mapviewer.share.mapcache.MapSourceAdapter;
import oracle.mapviewer.share.mapcache.TileDefinition;

public class WMSAdapter  extends MapSourceAdapter
{
  public WMSAdapter()
  {
  }
  

  /**
   * Returns a CGI request string that can be sent to an external WMS service. 
   * This request string can be used to fetche a single map image tile from 
   * the WMS service.
   * <br>
   * Note that this method should only return the CGI parameters portion of
   * the URL; in other words, everything after the '?'. For instance, if the
   * complete URL to request a tile image will be: <br>
   * <pre> http://www.foo.net/servlet/com.foo.getmap?REQUEST=GetMap&VERSION=1.1.0&BBOX=-126.0,24.0,-66.0,54.0&width=900&height=450&SRS=EPSG%3A4326&amp;STYLES=&amp;ServiceName=USGS_WMS_GTOPO&amp;layers=GTOPO60+Color+Shaded+Relief&format=image/png&transparent=false </pre>
   * then this method should only return something like: <br>
   * <pre>REQUEST=GetMap&VERSION=1.1.0&BBOX=-126.0,24.0,-66.0,54.0&width=900&height=450&SRS=EPSG%3A4326&amp;STYLES=&amp;ServiceName=USGS_WMS_GTOPO&amp;layers=GTOPO60+Color+Shaded+Relief&format=image/png&transparent=false</pre>
   * <br>
   * The other portion (service URL) is specified when creating a new Map Cache instance.
   * <br>
   * User must also specify the following properties when defining a new 
   * map cache instance based on this adapter: <br>
   * <ul>
   *   <li>   srs,  e.g., "EPSG:4326"
   *   <li>   service_name, "USGS_WMS_GTOPO"
   *   <li>   layers, e.g., "GTOPO60 Color Shaded Relief"
   *   <li>   format, e.g., "image/png"
   *   <li>   transparent, e.g., "false"
   * </ul>
   * @param tile definitoin of the image tile to be fetched
   * @return a valid CGI request string 
   */
  public String getMapTileRequest(TileDefinition tile)
  {
    StringBuffer req = new StringBuffer("REQUEST=GetMap&VERSION=1.1.0" ) ;
    
    Rectangle2D bbox = tile.getBoundingBox();
    req.append("&BBOX="+bbox.getMinX()+","+bbox.getMinY()+","+bbox.getMaxX()+","+bbox.getMaxY());
    req.append("&width="+tile.getImageWidth()+"&height="+tile.getImageHeight());

    Properties ps = this.getProperties();
    Iterator iterator = ps.keySet().iterator();
    while(iterator.hasNext())
    {
      String key = (String) iterator.next();
      String val = ps.getProperty(key);
      if(val==null)
	  val = "" ;
      if("srs".equalsIgnoreCase(key))
        req.append("&SRS="+val);
      else if("service_name".equalsIgnoreCase(key))
        req.append("&ServiceName="+val);
      else if("format".equalsIgnoreCase(key))
        req.append("&format="+val);
      else if("layers".equalsIgnoreCase(key))
        req.append("&layers="+val);
      else if("transparent".equalsIgnoreCase(key))
        req.append("&transparent="+val);
      else
      {
        if(key.equalsIgnoreCase("version"))
          req = req.replace(15,28,key.toUpperCase()+"="+val);
        else
          req.append("&"+key+"="+val);
      }
    }
    return req.toString();
  }
}

/*
 This is a sample map cache instance definition. 
 
<cache_instance name="wms8307" image_format="PNG">
   <external_map_source url="http://www.foo.net/servlet/com.foo.getmap" 
      proxy_host="www-proxy.my_corp.com" 
      proxy_port="80" request_method="GET" 
      adapter_class="oracle.mcsadapter.WMSAdapter"
      adapter_class_path="/ul/mywork/mvwar/adapters.jar">
      <properties>
         <property name="srs" value="EPSG:4326"/>
         <property name="service_name" value="USGS_WMS_GTOPO"/>
         <property name="layers" value="GTOPO60 Color Shaded Relief"/>
         <property name="format" value="image/png"/>
         <property name="transparent" value="false"/>
      </properties>
   </external_map_source>
   <cache_storage root_path="/ul/mapcache"/>
   <coordinate_system srid="8307" minX="-180.0" maxX="180.0" minY="-90.0" maxY="90.0"/>
   <tile_image width="256" height="256"/>
   <zoom_levels levels="10" min_scale="1000" max_scale="25000000">
      <zoom_level tile_width="30.47400967061566" tile_height="30.47400967061566" level_name="level0" scale="2.5E7"/>
      <zoom_level tile_width="9.89166850541995" tile_height="9.89166850541995" level_name="level1" scale="8114840.0"/>
      <zoom_level tile_width="3.2107721329057366" tile_height="3.2107721329057366" level_name="level2" scale="2634025.0"/>
      <zoom_level tile_width="1.0421952842500268" tile_height="1.0421952842500268" level_name="level3" scale="854987.0"/>
      <zoom_level tile_width="0.33828954343273077" tile_height="0.33828954343273077" level_name="level4" scale="277523.0"/>
      <zoom_level tile_width="0.10980638956593597" tile_height="0.10980638956593597" level_name="level5" scale="90082.0"/>
      <zoom_level tile_width="0.03564240171075207" tile_height="0.03564240171075207" level_name="level6" scale="29240.0"/>
      <zoom_level tile_width="0.01156915303135253" tile_height="0.01156915303135253" level_name="level7" scale="9491.0"/>
      <zoom_level tile_width="0.003754397991419849" tile_height="0.003754397991419849" level_name="level8" scale="3080.0"/>
      <zoom_level tile_width="0.0012189603868246264" tile_height="0.0012189603868246264" level_name="level9" scale="1000.0"/>
   </zoom_levels>
</cache_instance>

*/
