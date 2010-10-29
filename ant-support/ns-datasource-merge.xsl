<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes" />
    <xsl:template match="/MapperConfig">
        <MapperConfig>
            <!-- <xsl:apply-templates select="logging"/>  -->
            <!-- Use FINEST to log SQL query statement forms -->
            <logging log_level="INFO" log_thread_name="true" log_time="true"> 
				<log_output name="System.err" /> 
				<log_output name="../logs/sparrow_mv/mapviewer2.log" /> 
				<!--  ../../../../logs/sparrow_mv/mapviewer.log -->
			</logging>
            <xsl:apply-templates select="save_images_at"/>
            <xsl:apply-templates select="ip_monitor"/>
            <xsl:apply-templates select="web_proxy"/>
            <xsl:apply-templates select="security_config"/>
            <xsl:apply-templates select="global_map_config"/>
            <spatial_data_cache max_cache_size="1800" report_stats="true" />
            <xsl:apply-templates select="custom_image_renderer"/>
            <xsl:apply-templates select="srs_mapping"/>
            <xsl:apply-templates select="wms_config"/>
            <xsl:apply-templates select="ns_data_provider[@id!='sparrowPredict']"/>
            	<ns_data_provider id="sparrowPredict"
            		class="gov.usgswim.sparrow.MapViewerSparrowDataProvider" />
            <xsl:apply-templates select="s_data_provider"/>
            <xsl:apply-templates select="map_cache_server"/>
            <xsl:apply-templates select="map_data_source[@name!='sparrow']"/>
            
                <!-- This points to the production db -->
            	<map_data_source name="sparrow"
            	    jdbc_host="130.11.165.152"
                	jdbc_sid="widw"
                	jdbc_port="1521"
                	jdbc_user="SPARROW_DSS"
                	jdbc_password="!***REMOVED***"
                	jdbc_mode="thin"
                	max_connections="2"
                	number_of_mappers="20"
            	/>
            	
                <!-- This points to a clone of the production db (use for testing) -->
                <!-- 
            	<map_data_source name="sparrow"
            	    jdbc_host="130.11.165.154"
                	jdbc_sid="widw"
                	jdbc_port="1521"
                	jdbc_user="SPARROW_DSS"
                	jdbc_password="!***REMOVED***"
                	jdbc_mode="thin"
                	max_connections="2"
                	number_of_mappers="20"
            	/>
            	-->

        </MapperConfig>
    </xsl:template>
    <xsl:template match="*">
        <xsl:copy-of select="."/>
    </xsl:template>
</xsl:stylesheet>
