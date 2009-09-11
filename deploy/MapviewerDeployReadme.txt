This application (currently) needs to be deployed inside of Mapviewer.

1.  From JDev, deploy 'CompleteMapviewerDeployment.deploy' to create the jar sparrowMV.jar

2.  Copy the following jars to the current [Mapviewer Deployment]/web/WEB-INF/lib directory:
*  sparrowMV.jar
*  wstx-lgpl-3.2.jar from [SparrowProject]/java_lib/codehaus/woodstox/usgs_core/wstx-lgpl-3.2.jar
*  stax-api-1.0.jar from [SparrowProject]/java_lib/codehaus/woodstox/usgs_dep/stax-api-1.0.jar

3.  Add the following to [Mapviewer Deployment]/web/WEB-INF/conf/mapViewerConfig.xml

It should be added to the 'Custom Non-Spatial Data Provider' section.  There is
usually a 'defaultNSDP' ns_data_provider already there, so just add it after
that one.

================================================================================
      <ns_data_provider
        id="sparrowPredict" class="gov.usgswim.sparrow.MapViewerSparrowDataProvider" >

        <parameters>
          <!-- <parameter name="datasource-name" value="jdbc/Sparrow" /> -->
        <parameter name="jdbc-driver" value="oracle.jdbc.OracleDriver" />
        <parameter name="jdbc-url" value="jdbc:oracle:thin:@130.11.165.152:1521:widw" />
        <parameter name="jdbc-user" value="sparrow_tester1" />
        <parameter name="jdbc-pwd" value="usgs-787" />
        </parameters>

      </ns_data_provider>
================================================================================

4.  Add the following xml chunks to [Mapviewer Deployment]/web/WEB-INF/web.xml.

Note that the 'servlet' elements must be placed in with the other servlet
elements and the same for 'servlet-mapping' elements.

================================================================================
== In the 'servlet' section
================================================================================
  <!-- Sparrow Servlets -->
  <servlet>
    <servlet-name>ModelService</servlet-name>
    <servlet-class>gov.usgswim.service.ServiceServlet</servlet-class>
    <init-param>
      <param-name>handler-class</param-name>
      <param-value>gov.usgswim.sparrow.service.ModelService</param-value>
    </init-param>
    <init-param>
      <param-name>parser-class</param-name>
      <param-value>gov.usgswim.sparrow.service.ModelParser</param-value>
    </init-param>
  </servlet>
  <servlet>
    <servlet-name>PredictService</servlet-name>
    <servlet-class>gov.usgswim.service.ServiceServlet</servlet-class>
    <init-param>
      <param-name>handler-class</param-name>
      <param-value>gov.usgswim.sparrow.service.PredictService</param-value>
    </init-param>
    <init-param>
      <param-name>parser-class</param-name>
      <param-value>gov.usgswim.sparrow.service.PredictParser</param-value>
    </init-param>
  </servlet>
  <servlet>
    <servlet-name>IDByPointService</servlet-name>
    <servlet-class>gov.usgswim.service.ServiceServlet</servlet-class>
    <init-param>
      <param-name>handler-class</param-name>
      <param-value>gov.usgswim.sparrow.service.IDByPointService</param-value>
    </init-param>
    <init-param>
      <param-name>parser-class</param-name>
      <param-value>gov.usgswim.sparrow.service.IDByPointParser</param-value>
    </init-param>
  </servlet>
================================================================================

================================================================================
== In the 'servlet-mapping' section
================================================================================
   <!-- Sparrow Servlet mappings -->
  <servlet-mapping>
    <servlet-name>ModelService</servlet-name>
    <url-pattern>/sp_model/*</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>PredictService</servlet-name>
    <url-pattern>/sp_predict/*</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>IDByPointService</servlet-name>
    <url-pattern>/sp_idpoint/*</url-pattern>
  </servlet-mapping>
================================================================================

5.  Copy the html and jsp files from [SparrowProject]/public_html
to [Mapviewer Deployment]/web.  These are test files to test requests directly
to the Sparrow service, bypassing Mapviewer.

6.  Restart the application from the OC4J admin console - restarting via the
MapViewer interface will not reload the web.xml file or any of the jar files.