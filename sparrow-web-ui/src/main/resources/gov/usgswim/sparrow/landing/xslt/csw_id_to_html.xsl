<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:xs="http://www.w3.org/2001/XMLSchema" 
     xmlns:gco="http://www.isotc211.org/2005/gco"
     xmlns:gmd="http://www.isotc211.org/2005/gmd"
     xmlns:dc="http://purl.org/dc/elements/1.1/"
     xmlns:dct="http://purl.org/dc/terms/"
     xmlns:ows="http://www.opengis.net/ows"
     xmlns:cat="http://www.esri.com/metadata/csw/"
     xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
     xmlns:gml="http://www.opengis.net/gml"
     xmlns:gmd2="http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2"
     xmlns:gts="http://www.isotc211.org/2005/gts"
     xmlns:gmx="http://www.isotc211.org/2005/gmx"
     xmlns:gss="http://www.isotc211.org/2005/gss"
     xmlns:srv="http://www.isotc211.org/2005/srv"
     xmlns:gsr="http://www.isotc211.org/2005/gsr"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:gmi="http://www.isotc211.org/2005/gmi"
     xmlns:nc="http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:geonet="http://www.fao.org/geonetwork"
     version="2.0">

<!--    <xsl:output method="html" encoding="ISO-8859-1"/>-->

    <xsl:template match="/">
        <div class="preserve-format">
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <xsl:template match="/*[local-name()='GetRecordByIdResponse']">
        <xsl:apply-templates select="gmd:MD_Metadata"/>
    </xsl:template>

    <xsl:template match="cat:FullRecord">
        <xsl:apply-templates select="metadata"/>
    </xsl:template>

	
<!-- Start Metadata ISO19139 -->
    <xsl:template match="gmd:MD_Metadata">
        
        <xsl:variable name="keywordsVocabulary">
            <xsl:for-each select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:thesaurusName/gmd:CI_Citation/gmd:title/gco:CharacterString">
                <xsl:choose>
                    <xsl:when test="position()!=last()">
                        <xsl:value-of select='concat(.,", ")'/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select='.'/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:variable>        

		<!-- Inserts the title / description -->
		<xsl:apply-templates select="./gmd:identificationInfo/gmd:MD_DataIdentification"/>
    	
    	<!-- Adds the thumbnail and link to empty sessions -->
    	<xsl:call-template name="model-main-link-header"/>
		
		<div id="sparrow-keywords-section">
        	<xsl:call-template name="sparrowAttributes" />
		</div>
        <xsl:apply-templates select="./gmd:contentInfo/gmd:MD_CoverageDescription"/>
        <xsl:apply-templates select="./gmd:spatialRepresentationInfo/gmd:MD_GridSpatialRepresentation"/>
    	
<!-- Metadata block -->
        <div class="captioneddiv">
            
            <div id="new-watershed-session" class="preserve-format">
                <h3>Watershed Based Sessions</h3>
            	<p>
            	To start the DSS with the outlet river reach of a major watershed selected for downstream tracking,
            	select a watershed and click <em>Go</em>.
            	</p>
            	<select id="select-watershed">
                <xsl:for-each select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine">
                    <xsl:variable name="distId" select="./gmd:CI_OnlineResource/gmd:applicationProfile/gco:CharacterString" />
					<xsl:variable name="distName" select="./gmd:CI_OnlineResource/gmd:name/gco:CharacterString" />
                    <xsl:variable name="distURL" select="./gmd:CI_OnlineResource/gmd:linkage/gmd:URL" />
					<xsl:variable name="distDesc" select="./gmd:CI_OnlineResource/gmd:description/gco:CharacterString"/>
                    <xsl:if test="$distId = 'SPARROW DSS (WATERSHED)' and $distURL != ''">
                    	<xsl:element name="option">
                    		<xsl:attribute  name="value"><xsl:value-of select="$distURL"/></xsl:attribute>
                    		<xsl:value-of select="$distName"/>
                    	</xsl:element>
                    </xsl:if>
                </xsl:for-each>
            	</select>
            	<button id="select-watershed-go-button" onclick="CONTROLLER.launchModelWatershed();">Go &gt;&gt;</button>
            </div> 
        	<div id="new-scenario-session" class="preserve-format">
                <h3>Scenario Based Sessions</h3>
        		<p>
            	To start the DSS with a predefined scenario,
            	click on the link for one of the scenarios below.
            	</p>
                <xsl:for-each select="./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine">
                    <xsl:variable name="distId" select="./gmd:CI_OnlineResource/gmd:applicationProfile/gco:CharacterString" />
					<xsl:variable name="distName" select="./gmd:CI_OnlineResource/gmd:name/gco:CharacterString" />
                    <xsl:variable name="distURL" select="./gmd:CI_OnlineResource/gmd:linkage/gmd:URL" />
					<xsl:variable name="distDesc" select="./gmd:CI_OnlineResource/gmd:description/gco:CharacterString"/>
                    <xsl:if test="$distId = 'SPARROW DSS (SCENARIO)' and $distURL != ''">
                    	<dl>
                    		<dt>
                    			<a>
                    				<xsl:attribute name="href"><xsl:value-of select="$distURL"/></xsl:attribute>
                    				<xsl:value-of select="$distName"/>
                    			</a>
                    		</dt>
                    		<dd>
                    			<xsl:value-of select="$distDesc"/>
                    		</dd>
                    	</dl>
                    </xsl:if>
                </xsl:for-each>
            </div> 
        </div>
    </xsl:template>
	
    <!-- Inserts the thumbnail image and new session launch button -->
	<xsl:template name="model-main-link-header">
		<xsl:variable name="empty-session-url" select="//csw:GetRecordByIdResponse/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:applicationProfile/gco:CharacterString = 'SPARROW DSS (MAIN)']/gmd:linkage/gmd:URL"/>
		<xsl:variable name="thumbnail-url" select="//csw:GetRecordByIdResponse/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:applicationProfile/gco:CharacterString = 'thumbnail']/gmd:linkage/gmd:URL"/>
		
		<div id="model-main-link-header" class="preserve-format">
			<div id="model-thumbnail">
				<a title="click to explore this model in the decision support system">
					<xsl:attribute name="href"><xsl:value-of select="$empty-session-url"/></xsl:attribute>
					<img width="170" height="85">
						<xsl:if test="$thumbnail-url != ''">
							<xsl:attribute name="src">
								<xsl:value-of select="$thumbnail-url"/>
							</xsl:attribute>
						</xsl:if>
					</img>
				</a>
			</div>
			<div id="new-empty-session">
				<button>
					<xsl:attribute name="onclick">location='<xsl:value-of select="$empty-session-url"/>'</xsl:attribute>
					Explore this model in the<br />Decision Support System &gt;&gt;
				</button>
            </div>	
		</div>
	</xsl:template>
    
    
	<xsl:template name="sparrowAttributes">
		
		<xsl:variable name="pubName" select="/csw:GetRecordByIdResponse/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:applicationProfile/gco:CharacterString = 'model page']/gmd:name/gco:CharacterString"/>
		<xsl:variable name="pubURL" select="/csw:GetRecordByIdResponse/gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource[gmd:applicationProfile/gco:CharacterString = 'model page']/gmd:linkage/gmd:URL"/>
        <xsl:variable name="model_id" select="gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue = 'sparrow_dss_model_id']/gmd:keyword[1]/gco:CharacterString"/>
		
		<dl class="sparrow-keywords">
			<xsl:apply-templates mode="sparrow-keywords" select="/csw:GetRecordByIdResponse/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification"></xsl:apply-templates>
			
			<xsl:if test="$pubURL != ''">
				<dt>Reference:</dt>
				<dd>
					<a>
						<xsl:attribute name="href"><xsl:value-of select="$pubURL"/></xsl:attribute>
						<xsl:attribute name="title">The model publication</xsl:attribute>
						<xsl:value-of select="$pubName"/>
					</a>
				</dd>
			</xsl:if>
                <dt>Model Updates:</dt>
                <dd>
                    <a>
                        <xsl:attribute name="href">modelUpdates.jsp#<xsl:value-of select="$model_id"/></xsl:attribute>
                        <xsl:attribute name="title">Model Updates</xsl:attribute>
                        View this model's updates
                    </a>
                </dd>
			
		</dl>
	</xsl:template>
    
    <!-- Creates the name/value keywords section -->
	<xsl:template mode="sparrow-keywords" match="gmd:MD_DataIdentification">
		<xsl:variable name="stream_network" select="gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue = 'sparrow_river_network']/gmd:keyword[1]/gco:CharacterString"/>
		<xsl:variable name="stream_network_url" select="gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue = 'sparrow_river_network_url']/gmd:keyword[1]/gco:CharacterString"/>
		<xsl:variable name="constituent" select="gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue = 'sparrow_constituent']/gmd:keyword[1]/gco:CharacterString"/>
		<xsl:variable name="base_year" select="gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue = 'sparrow_base_year']/gmd:keyword[1]/gco:CharacterString"/>
		<xsl:variable name="model_id" select="gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue = 'sparrow_dss_model_id']/gmd:keyword[1]/gco:CharacterString"/>
		
		
		<dt>Modeled Constituent:</dt>
		<dd><xsl:choose>
			<xsl:when test="$constituent != ''"><xsl:value-of select="$constituent"/></xsl:when>
			<xsl:otherwise>[Unspecified]</xsl:otherwise>
		</xsl:choose></dd>
		
		<dt>Base Year:</dt>
		<dd><xsl:choose>
			<xsl:when test="$base_year != ''"><xsl:value-of select="$base_year"/></xsl:when>
			<xsl:otherwise>[Unspecified]</xsl:otherwise>
		</xsl:choose></dd>
		
		<dt>Stream Network:</dt>
		<dd><xsl:choose>
			<xsl:when test="$stream_network_url != ''">
				<a>
					<xsl:attribute name="href"><xsl:value-of select="$stream_network_url"/></xsl:attribute>
					<xsl:attribute name="title">The network model and associated geometry the model is based on</xsl:attribute>
					<xsl:choose>
						<xsl:when test="$stream_network != ''"><xsl:value-of select="$stream_network"/></xsl:when>
						<xsl:otherwise><xsl:value-of select="$stream_network_url"/></xsl:otherwise>
					</xsl:choose>
				</a>
				<br />
				<i>Geometry and additional reach and network attribute data are available with the stream network data,
				which is available as a separate download.</i>
			</xsl:when>
			<xsl:otherwise>[Unknown]</xsl:otherwise>
		</xsl:choose></dd>
		
		<dt class="sparrow_model_id">Model ID:</dt>
		<dd id="sparrow_model_id_value" class="sparrow_model_id"><xsl:choose>
			<xsl:when test="$model_id != ''"><xsl:value-of select="$model_id"/></xsl:when>
			<xsl:otherwise>[Unspecified]</xsl:otherwise>
		</xsl:choose></dd>
	</xsl:template>
	
	<!-- 
    Creates a Title for the model
     -->
    <xsl:template match="gmd:MD_DataIdentification">
        
        <div class="captioned-div">
            <h3>
            	<xsl:apply-templates select="./gmd:abstract"/>
            </h3>
        </div>
    </xsl:template>

    <xsl:template match="gmd:MD_CoverageDescription">
        <div class="captioneddiv">
            <h3>Content Info</h3>
            <table class="meta">
                <tr>
                    <td class="meta" valign="top">
                        <table class="meta">
                            
                            <xsl:if test="gmd:attributeDescription/@gco:nilReason = ''">
                                <xsl:call-template name="tablerow">
                                    <xsl:with-param name="cname" select="'Attribute Description'"/>
                                    <xsl:with-param name="cvalue" select="./gmd:attributeDescription"/>
                                </xsl:call-template>
                            </xsl:if>
                            
                            <xsl:if test="gmd:contentType/@gco:nilReason = ''">
                                <xsl:call-template name="tablerow">
                                    <xsl:with-param name="cname" select="'Content Type'"/>
                                    <xsl:with-param name="cvalue" select="./gmd:contentType"/>
                                </xsl:call-template>
                            </xsl:if>
                            
                            <xsl:for-each select="./gmd:dimension">
                                <xsl:variable name="attributeId" select="./gmd:MD_Band/gmd:sequenceIdentifier/gco:MemberName/gco:aName/gco:CharacterString" />
                                <xsl:variable name="attributeType" select="./gmd:MD_Band/gmd:sequenceIdentifier/gco:MemberName/gco:attributeType/gco:TypeName/gco:aName/gco:CharacterString" />
                                <xsl:variable name="descriptor" select="./gmd:MD_Band/gmd:descriptor/gco:CharacterString" />
                                
                                <tr>
                                    <td class="meta-param">
                                        <xsl:value-of select="$attributeId"/> 
                                        (
                                        <xsl:value-of select="$attributeType"/>)
                                        <xsl:text>: </xsl:text>
                                    </td>
                                    <td class="meta-value">
                                        <xsl:value-of select="$descriptor"/>
                                    </td>
                                </tr>
                            </xsl:for-each>
                            
                        </table>
                    </td>
                </tr>
            </table>
        </div>
    </xsl:template>

<!-- 'Distribution Info' block -->
    <xsl:template match="gmd:MD_Distribution">
        <div class="captioneddiv">
            <h3>Distribution info</h3>
            <table class="meta">
                <tr></tr>
                <xsl:for-each select="gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource">
                    <xsl:choose>
                        <xsl:when test="starts-with(./gmd:protocol/gco:CharacterString,'WWW:DOWNLOAD-') and contains(./gmd:protocol/gco:CharacterString,'http--download') and ./gmd:name/gco:CharacterString">
                            <tr>
                                <td class="meta-param">Download:</td>
                                <td class="meta-value">
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="gmd:linkage/gmd:URL"/>
                                        </xsl:attribute>
                                        <xsl:value-of select="gmd:name/gco:CharacterString"/>
                                    </a>
                                </td>
                            </tr>
                        </xsl:when>
                        <xsl:when test="starts-with(./gmd:protocol/gco:CharacterString,'ESRI:AIMS-') and contains(./gmd:protocol/gco:CharacterString,'-get-image') and ./gmd:name/gco:CharacterString">
                            <tr>
                                <td class="meta-param">Esri ArcIms:</td>
                                <td class="meta-value">
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="gmd:linkage/gmd:URL"/>
                                        </xsl:attribute>
                                        <xsl:value-of select="gmd:name/gco:CharacterString"/>
                                    </a>
                                </td>
                            </tr>
                        </xsl:when>
                        <xsl:when test="starts-with(./gmd:protocol/gco:CharacterString,'OGC:WMS-') and contains(./gmd:protocol/gco:CharacterString,'-get-map') and ./gmd:name/gco:CharacterString">
                            <tr>
                                <td class="meta-param">OGC-WMS:</td>
                                <td class="meta-value">
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:text>javascript:void(window.open('</xsl:text>
                                            <xsl:value-of select="gmd:linkage/gmd:URL"/>
                                            <xsl:text>'))</xsl:text>
                                        </xsl:attribute>
                                        <xsl:value-of select="gmd:name/gco:CharacterString"/>
                                    </a>
                                </td>
                            </tr>
                        </xsl:when>
                        <xsl:when test="starts-with(./gmd:protocol/gco:CharacterString,'OGC:WMS-') and contains(./gmd:protocol/gco:CharacterString,'-get-capabilities') and ./gmd:name/gco:CharacterString">
                            <tr>
                                <td class="meta-param">OGC-WMS Capabilities:</td>
                                <td class="meta-value">
                                    <a>
                                        <xsl:attribute name="href">
                                            <xsl:value-of select="gmd:linkage/gmd:URL"/>
                                        </xsl:attribute>
                                        <xsl:value-of select="gmd:name/gco:CharacterString"/>
                                    </a>
                                </td>
                            </tr>
                        </xsl:when>
                    </xsl:choose>
                </xsl:for-each>
            </table>
        </div>
    </xsl:template>

<!-- 'Identification->Abstract -->
    <xsl:template match="gmd:abstract">
        <xsl:apply-templates select="./gco:CharacterString/text()"/>
    </xsl:template>
<!-- End Metadata ISO19139 -->


<!-- StartMetadata Dublin Core -->

<!-- 'Identification' block -->
    <xsl:template match="*[local-name()='Record']|*[local-name()='SummaryRecord']|*[local-name()='BriefRecord']">
        <div class="captioneddiv">
            <h3>Identification info</h3>
            <table class="meta">
                <tr></tr>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Title'"/>
                    <xsl:with-param name="cvalue" select="./dc:title"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Date'"/>
                    <xsl:with-param name="cvalue" select="./dc:date"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Presentation form'"/>
                    <xsl:with-param name="cvalue" select="./dc:format"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Individual name'"/>
                    <xsl:with-param name="cvalue" select="./dc:publisher"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Identifier'"/>
                    <xsl:with-param name="cvalue" select="./dc:identifier"/>
                </xsl:call-template>
                <xsl:if test="./dct:abstract">
                    <tr><!-- this "tr" causes problems for new line replacement by "p" -->
                        <td class="meta-param">Abstract:</td>
                        <td class="meta-value">
                            <xsl:apply-templates select="./dct:abstract"/>
                        </td>
                    </tr>
                </xsl:if>
                <xsl:for-each select="./dc:subject">
                    <xsl:call-template name="tablerow">
                        <xsl:with-param name="cname" select="'Keyword'"/>
                        <xsl:with-param name="cvalue" select="."/>
                    </xsl:call-template>
                </xsl:for-each>
            </table>
            <xsl:apply-templates select="./ows:BoundingBox"/>
            <xsl:apply-templates select="./ows:WGS84BoundingBox"/>
            <xsl:for-each select="./dc:URI">
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'URI'"/>
                    <xsl:with-param name="cvalue" select="."/>
                </xsl:call-template>
            </xsl:for-each>
        </div>
    </xsl:template>


    <xsl:template match="dct:abstract">
<!--xsl:value-of select="."/-->
        <xsl:apply-templates select="text()"/>
    </xsl:template>

<!-- 'Identification->Geographic box' block -->
    <xsl:template match="ows:BoundingBox|ows:WGS84BoundingBox">
        <div class="captioneddiv">
            <h3>Geographic box</h3>
            <table class="meta">
                <tr></tr>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Lower corner'"/>
                    <xsl:with-param name="cvalue" select="./ows:LowerCorner"/>
                </xsl:call-template>
                <xsl:call-template name="tablerow">
                    <xsl:with-param name="cname" select="'Upper corner'"/>
                    <xsl:with-param name="cvalue" select="./ows:UpperCorner"/>
                </xsl:call-template>
            </table>
        </div>
    </xsl:template>
<!-- End Metadata Dublin Core -->

<!-- Start Utills -->
    <xsl:template  match="text()">
        <xsl:call-template name="to-para">
            <xsl:with-param name="from" select="'&#10;&#10;'"/>
            <xsl:with-param name="string" select="."/>
        </xsl:call-template>
    </xsl:template> 

<!-- replace all occurences of the character(s) `from'
                   by  <p/> in the string `string'.-->
    <xsl:template name="to-para" >
        <xsl:param name="string"/>
        <xsl:param name="from"/>
        <xsl:choose>
            <xsl:when test="contains($string,$from)">
                <xsl:value-of select="substring-before($string,$from)"/>
      <!-- output a <p/> tag instead of `from' -->
                <p/>
                <xsl:call-template name="to-para">
                    <xsl:with-param name="string" select="substring-after($string,$from)"/>
                    <xsl:with-param name="from" select="$from"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$string"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="tablerow" >
        <xsl:param name="cname"/>
        <xsl:param name="cvalue"/>
        <xsl:choose>
            <xsl:when test="string($cvalue)">
                <tr>
                    <td class="meta-param">
                        <xsl:value-of select="$cname"/>
                        <xsl:text>: </xsl:text>
                    </td>
                    <td class="meta-value">
                        <xsl:value-of select="$cvalue"/>
                    </td>
                </tr>
            </xsl:when>
            <xsl:otherwise>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="tableHtmlRow" >
        <xsl:param name="cname"/>
        <xsl:param name="cvalue"/>
		<xsl:param name="cdesc"/>
        <xsl:choose>
            <xsl:when test="string($cvalue)">
                <tr>
                    <td class="meta-param">
						<a>
                            <xsl:attribute name="href">
                                <xsl:value-of select="$cvalue"/>
                            </xsl:attribute>
							<xsl:value-of select="$cname"/>
                        </a>
                        <xsl:text>: </xsl:text>
                    </td>
                    <td class="meta-value">
                        <xsl:value-of select="$cdesc"/>
                    </td>
                </tr>
            </xsl:when>
            <xsl:otherwise>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
	

<!-- End Utills -->

</xsl:stylesheet>