<!doctype html>
<%@ page contentType="text/html; charset=utf-8"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>SPARROW Decision Support System</title>
    <link rel="icon" href="favicon.ico" type="image/x-icon">

	
    <!--Ext JS-->
    <link rel="stylesheet" type="text/css" href="ext_js/resources/css/ext-all.css" />
    <link rel="stylesheet" href="scrollable_map/css/scrollable_map.css"/>
    
    <!-- Sparrow/USGS -->
    <link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="css/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="css/custom.css" />
	
	<%-- the minify plugin doesn't recognize the 'type' attribute, so that will prevent this one from being minified twice --%>
    <script type="text/javascript" src="ext_js/adapter/ext/ext-base.js"></script>
	<script type="text/javascript" src="ext_js/ext-all.js"></script>
	
	<%-- John scrollable map framework --%>
	<script src="scrollable_map/JMap-header.js"></script>
	<script src="scrollable_map/js/web/Map.js"></script>
	<script src="scrollable_map/js/web/mapLayer/Layer.js"></script>
	<script src="scrollable_map/js/web/mapLayer/WMSLayer.js"></script>
	<script src="scrollable_map/js/web/mapLayer/ArcTMSLayer.js"></script>
	<script src="scrollable_map/js/web/mapLayer/TMSLayer.js"></script>
	<script src="scrollable_map/js/web/mapLayer/MVMapCacheLayer.js"></script>
	<script src="scrollable_map/js/web/mapLayer/MVWMSLayer.js"></script>
	<script src="scrollable_map/js/web/mapLayer/MVBaseMapLayer.js"></script>
	<script src="scrollable_map/js/web/mapLayer/GoogleMapsLayer.js"></script>
	<script src="scrollable_map/js/web/mapLayer/YahooMapsLayer.js"></script>
	<script src="scrollable_map/js/web/mapLayer/LiveMapsLayer.js"></script>
	<script src="scrollable_map/js/web/mapLayer/OpenStreetMapsLayer.js"></script>
	<script src="scrollable_map/js/web/LayerManager.js"></script>
	<script src="scrollable_map/js/web/MapServicesFile.js"></script>
	<script src="scrollable_map/js/web/MapTile.js"></script>
	<script src="scrollable_map/js/web/TileManager.js"></script>
	<script src="scrollable_map/js/web/tile/TileLoader.js"></script>
	<script src="scrollable_map/js/web/tile/TileCache.js"></script>
	<script src="scrollable_map/js/projection/PlateCarree.js"></script>
	<script src="scrollable_map/js/projection/Mercator.js"></script>
	<script src="scrollable_map/js/svg/SVGManager.js"></script>
	<script src="scrollable_map/js/svg/Path.js"></script>
	<script src="scrollable_map/js/svg/PolyLine.js"></script>
	<script src="scrollable_map/js/svg/Line.js"></script>
	<script src="scrollable_map/js/svg/Polygon.js"></script>
	<script src="scrollable_map/js/util/MapUtils.js"></script>
	<script src="scrollable_map/js/util/Tools.js"></script>
	<script src="scrollable_map/js/util/AjaxRequest.js"></script>
	<script src="scrollable_map/js/hud/HUDManager.js"></script>
	<script src="scrollable_map/js/hud/LatLonLabel.js"></script>
	<script src="scrollable_map/js/hud/ScaleRake.js"></script>
	<script src="scrollable_map/js/hud/OverviewMap.js"></script>
	<script src="scrollable_map/js/hud/ZoomSlider.js"></script>
	<script src="scrollable_map/js/foi/FOIManager.js"></script>
	<script src="scrollable_map/js/foi/FOI.js"></script>

	
	<%-- Project specific files --%>
	<script src="js/sparrow_ns.js"></script>
	<script src="js/sparrow_config.js"></script>
	<script src="js/ext_extensions/groupdataview.js"></script>
    <script src="js/ext_extensions/groupcombo.js"></script>
	<script src="js/ext_extensions/dockpanel.js"></script>
	<script src="js/USGSUtils.js"></script>
	<script src="js/SparrowUIContext.js"></script>
	<script src="js/sparrow_ui.js"></script>
	<script src="js/GroupMembershipWindow.js"></script>
	<script src="js/mapoptions/MapOptionsPanel.js"></script>
	<script src="js/mapoptions/AutoGenerateBucketsWindow.js"></script>
	<script src="js/mapoptions/ColorField.js"></script>
	<script src="js/mapoptions/CustomBinsWindow.js"></script>
	<script src="js/mapoptions/PredefinedSessionsWindow.js"></script>
	<script src="js/reachmanager/ReachManagerPanel.js"></script>
	<script src="js/identify/AddToGroupPanel.js"></script>
	<script src="js/identify/AdjustmentsGrid.js"></script>
	<script src="js/identify/AttributesGrid.js"></script>
	<script src="js/identify/PredictedValuesGrid.js"></script>
	<script src="js/identify/GraphPanel.js"></script>
	<script src="js/identify/ReachIdentifyWindow.js"></script>
	<script src="js/sparrow_rpc.js"></script>
	<script src="js/reachmanager/GoToReachWindow.js"></script>
	<script src="js/reachmanager/WatershedWindow.js"></script>
	<script src="js/ext_ui.js"></script>
	<script src="js/IdentifyMarker.js"></script>
	<script src="js/help.js"></script>
	<script src="js/mapoptions/PredefinedSessionsWindow.js"></script>
	<script src="js/maplayers/MapLayersWindow.js"></script>
	<script src="js/legend.js"></script>
	<script src="js/events.js"></script>
	<script src="js/svg_overlay.js"></script>
	<script src="js/DeliveryReport.js"></script>
	
	<jsp:include page="template_page_tracking.jsp" flush="true" />

  </head>
  <body>
    <div id="usgs-header-panel">
        <jsp:include page="header.jsp" flush="true" />
    </div>
    
    <%
    boolean modeler = false;
    try{ 
    	modeler = request.isUserInRole("sparrow_modeler");
    }catch(Exception e){}
    if(modeler){%>
		<div id="modeler-user-role" class="x-hidden">modeler</div>
	<%}%>
	
    <div id="ie6-warn-win" class="x-hidden">
      <b>It appears that you are using Internet Explorer Version 6.</b><br/>

        Internet Explorer Version 6 (IE6) is only partially supported by this
        application and some features may not work. If you discover an issue
        that completely prevents usage of this application in IE6, please report
        it to xxxxxx@usgs.gov. Please note, however, issues relating to
        formatting, ease of use, and minor features not related to the main
        purpose of the application will not be fixed for IE6.
        <br/><br/><b>Why are you only partially supporting IE6?</b><br/>

        The World Wide Web Consortium (W3C) creates standards so the Web and web
        pages can work consistently for everyone. New browsers generally support
        these standards while older browsers generally don't. In particular,
        IE6 does not support most W3C standards while most current versions of
        other browsers do. Your choice of browser may be out of your hands,
        however, if you are able to, you should consider upgrading your browser
        to IE7 or installing one of the browsers below. Doing so will improve
        your web experience, enabling you to use and view sites as their
        creators intended.

        <br/><br/>A partial list of browsers supporting current W3C standards
        includes:<br/>
          * Firefox<br/>
          * The many browsers based on Firefox<br/>
          * Internet Explorer 7<br/>
          * Opera<br/><br/>
        Click to continue and use the application in IE6, with possible reduced functionality.
    </div>

    <div id="navigation-bar">
      <div id="groups-tab" class="x-hide-display">
        <div id="gps-area-frame" style="overflow: auto; position: relative;">
          <div id="gps-area"></div>
          <div id="individual-group-area"></div>
          <div id="targeted-group-area"></div>
        </div>
      </div>
      <div id="map-options-tab" class="x-hide-display">
      </div>
    </div>

    <div id="map-area">
      <div class="controls-custom" id="map-controls"></div>
      <div class="map-sync-warning x-hidden" id="map-sync-warning"><span>Map out of sync </span><img src='images/warning.gif' alt='Map out of sync warning'/></div>
    </div>

    <div id="ui-save-window" class="x-hidden">
      <form id="save_state_form" method="post" action="saveMapState" target="_blank" onsubmit="return Sparrow.ui.save_map_state()">
        <br/>
        <div>
          <label for="savefileas">Save File As:</label><br />
          <input id="savefileas" type="text" name="savefileas" style="width: 17em"/>
          <input id="savefileas_extension" type="hidden" value="js" name="savefileas_extension" style="width: 17em"/>
          <input type="hidden" value="" id="ui_XML" name="ui_XML"/>
        </div>
        <br/>
      </form>
    </div>

    <div id="ui-load-window" class="x-hidden">
      <form id="file_upload_form" method="post" enctype="multipart/form-data" action="loadMapState">
        <input name="ui_file" id="ui_file" size="21" type="file" /><br/> <br/>
        <div align="center"><input type="submit" name="action" value="Load Session" onclick="return LOAD_STATE_WIN.close()"/></div><br/>
        <iframe onload="Sparrow.ui.load_ui(this)" id="upload_target" name="upload_target" src="" style="width:0;height:0;border:0px solid #fff;"></iframe>
      </form>
    </div>

    <div id="export-data-window" class="x-hidden">
    	<form class="export">
    	<p class="form-note">
    	Please note that no endorsement, expressed or implied, is made by the USGS or the U.S. Government of any interpretations or decisions that result from the use of these models, nor shall the fact of distribution constitute any such endorsement, and no responsibility is assumed by the USGS in connection therewith.
    	<br/><br/>
    	</p>
    	<fieldset>
    	<legend>Export Format</legend>
    	<p>
        <input class="visual-break" type="radio" checked="checked" name="export_radio" id="export_radio_csv" value="csv" /><label for="export_radio_csv">Comma Separated Values (CSV)&nbsp;&nbsp;</label>
        <input class="visual-break" type="radio" name="export_radio" id="export_radio_tab" value="tab" /><label for="export_radio_tab">Tab Delimited Values&nbsp;&nbsp;</label>
        <input class="visual-break" type="radio" name="export_radio" id="export_radio_excel" value="EXCEL" /><label for="export_radio_excel">Excel</label>
        </p>
        </fieldset>
        <fieldset>
        <legend>Included Rows</legend>
        <div>
        <input class="visual-break" type="radio" name="export_bound" id="export_checkbox_bounded" value="bounded" checked="checked"/>
        <label for="export_checkbox_bounded">Only include reaches in the region currently shown on the map</label><br/>
        <p class="form-note">Some reaches slighly outside the current region may be included in the export.</p>
        <input class="visual-break" type="radio" name="export_bound" id="export_checkbox_unbounded" value="unbounded" />
        <label for="export_checkbox_unbounded">Include all <b id="export-row-count-1"></b>&nbsp;&nbsp;reaches in the complete model</label><br/>
        <p class="form-note">This option will result in a very large download which may be sensitive to network interruptions.
        Please verify that all <b id="export-row-count-2"></b>&nbsp;&nbsp;data rows are received in the downloaded file.</p>
        </div>
        </fieldset>
        
        <fieldset>
        <legend>Included Content</legend>
        <p class="form-note">The export always includes the following columns:<br/>
		<a class="helpLink" href="javascript:getGeneralHelp('CommonTerms.Export Reach ID')">Reach id</a>,
		<a class="helpLink" href="javascript:getGeneralHelp('CommonTerms.Watershed Area')">Watershed Area</a> and the
		Mapped Value (see notes in the export file for definitions)<br/>
		Additional columns of data can be added below:
        </p>
        <fieldset class="distinct" id="export-data-window-adjusted-series"><legend>Data series that reflect source input changes on the Change Inputs tab</legend>
        <p class="form-note">These options are enabled if there are active changes on the Change Inputs tab and will include model data reflecting those changes.
        If you have created a scenario using the Change Inputs tab, you will likely want to select these options.</p>
        <input class="visual-break" type="checkbox" name="adjusted-predicted-values" id="export-adjusted-predicted-values" value="checked"/>
        	<label for="export-adjusted-predicted-values">Include predicted values for Total and Incremental loads</label><br />
        <p class="form-note">These are the predicted values generated by the model after changes to the input values have been applied from the Change Inputs tab.</p>
        <input class="visual-break" type="checkbox" name="adjusted-source-values" id="export-adjusted-source-values" value="checked"/>
        	<label for="export-adjusted-source-values">Include the source input values used to calculate the predicted values</label><br />
        <p class="form-note">These source values will reflect the changed input values from the Change Inputs tab.</p>
        </fieldset>
        
        <fieldset class="distinct"><legend>Data series that do NOT reflect source input changes on the Change Inputs tab</legend>
        <p class="form-note">These options will include original, as-calibrated model data, unaffected by changes made on the Change Inputs tab.
        This data is useful for comparison, or if there are no source input changes.</p>
        <input class="visual-break" type="checkbox" name="original-predicted-values" id="export-original-predicted-values" value="checked"/>
        	<label for="export-original-predicted-values">Include <i><b>original</b></i>&nbsp;&nbsp;predicted values for Total and Incremental loads</label><br />
        <p class="form-note">This option will also include the original mapped value, which is the currently mapped data series unaffected by source input changes.</p>
        <input class="visual-break" type="checkbox" name="original-source-values" id="export-original-source-values" value="checked"/>
        	<label for="export-original-source-values">Include the <i><b>original</b></i>&nbsp;&nbsp;source input values used to calculate the original predicted values</label><br />
        </fieldset>
        
        <fieldset class="distinct"><legend>Other non-prediction related data series</legend>
        <input class="visual-break" type="checkbox" name="id-attributes" id="export-include-id-attributes" value="checked"/>
        	<label for="export-include-id-attributes">Include additional identification information (
        	<a class="helpLink" href="javascript:getGeneralHelp('CommonTerms.HUC')">HUC</a>8,
        	reach name,
        	<a class="helpLink" href="javascript:getGeneralHelp('CommonTerms.EDA Code')">EDA Code</a> &amp;
        	<a class="helpLink" href="javascript:getGeneralHelp('CommonTerms.EDA Name')">EDA Name</a>)</label><br />
        <input class="visual-break" type="checkbox" name="stat-attributes" id="export-include-stat-attributes" value="checked"/>
        	<label for="export-include-stat-attributes">Include additional stream characteristics 
        	(<a class="helpLink" href="javascript:getGeneralHelp('CommonTerms.Streamflow')">Streamflow</a> &amp;
        	<a class="helpLink" href="javascript:getGeneralHelp('CommonTerms.Incremental Area')">Incremental Area</a>)</label>
        </fieldset>
        </fieldset>
        </form>
        
        <form id="export_form" method="POST" action="getPredict">
            <input type="hidden" id="report_xmlreq" name="xmlreq" value="" />
        </form>
    </div>

    <div id="custom-perc-bin" class="x-hidden">
    </div>

    <div id="custom-cp-win" class="x-hidden">
      <div id="custom-cp"></div>
    </div>
   

    <div id="name-group-area" class="x-hidden">
      <table width="100%" border="0">
        <tr>
          <td>Name:</td>
          <td>
            <input type="text" name="group_name" id="group_name" style="width: 14em"/>
          </td>
        </tr>
        <tr>
          <td>Desc:</td>
          <td>
            <textarea rows="2" cols="21" name="group_desc" id="group_desc"></textarea>
          </td>
        </tr>
      </table>
    </div>


    <div id="add-group-area" class="x-hidden">
      <div id="treatment-tab" class="x-hide-display"></div>
      <div id="export-tab" class="x-hide-display">export</div>
      <div id="graph-tab" class="x-hide-display">graph</div>
      <div id="notes-tab" class="x-hide-display" align="center">
        <textarea rows="11" cols="57" name="notes_text" id="group_notes"></textarea>
      </div>
    </div>

    <div id="please-wait-area" class="x-hidden" style="text-align: center; vertical-align: middle;">
        Please wait while we process your request.
    </div>
    
    <div id="sparrow-identify-mapped-value-a" class="sparrow-identify-mapped-value">
    </div>
    
    <div id="sparrow-identify-mapped-value-b" class="sparrow-identify-mapped-value">
    </div>

    <div id="wiki-help-panel" class="x-panel-reset x-hidden">
        <div id="help-content" class="x-panel-reset"></div>
    </div>
    
    <div id="delivery-report-panel" class="x-panel-reset x-hidden">
        <div id="delivery-report-content" class="x-panel-reset"></div>
    </div>
    
    <div id="display-results-text" class="x-window-mc x-hidden" style="padding: 3px">
    	Map the model results by reach or catchment.
    </div>
    
    <div id="adjustments-text" class="x-window-mc x-hidden" style="padding: 3px">
    	Map the effect of management scenarios on stream water quality, based on hypothetical changes in source inputs. For more information, <a href="javascript:getHelpFromService(<%= request.getParameter("model")  %>,'CommonTerms.Adjustments')">click here</a>.</div>
    
    <div id="targets-text" class="x-window-mc x-hidden" style="padding: 3px">
    	Map the amount of load in upstream catchments that is delivered to a downstream reach. For more information, <a href="javascript:getHelpFromService(<%= request.getParameter("model")  %>,'CommonTerms.Targets')">click here</a>.
    </div>

    <div id="usgs-footer-panel">
        <jsp:include page="footer.jsp" flush="true" />
    </div>

  </body>
</html>
