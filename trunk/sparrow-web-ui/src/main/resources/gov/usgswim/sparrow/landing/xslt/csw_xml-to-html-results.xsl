<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
 xmlns:dc="http://purl.org/dc/elements/1.1/"
 xmlns:dct="http://purl.org/dc/terms/"
 xmlns:gco="http://www.isotc211.org/2005/gco"
 xmlns:gmd="http://www.isotc211.org/2005/gmd">
<!--xsl:output method="html" encoding="ISO-8859-1"/-->


	<xsl:template match="/results/*[local-name()='GetRecordsResponse']">
		<xsl:apply-templates select="./*[local-name()='SearchResults']"/>
	</xsl:template>


	<xsl:template match="*[local-name()='SearchResults']">


		<xsl:variable name="start">
			<xsl:value-of select="../../request/@start"/>
		</xsl:variable>


<!-- because GeoNetwork does not return nextRecord we have to do some calculation -->
		<xsl:variable name="next">
			<xsl:choose>
				<xsl:when test="@nextRecord">
					<xsl:value-of select="@nextRecord"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="number(@numberOfRecordsMatched) >= (number($start) + number(@numberOfRecordsReturned))">
							<xsl:value-of select="number($start) + number(@numberOfRecordsReturned)"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="0"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<div class="captioneddiv">
			<xsl:choose>
				<xsl:when test="count(./gmd:MD_Metadata) > 0">
					<ol>
						<xsl:attribute name="start">
							<xsl:value-of select="$start"/>
						</xsl:attribute>
		
						<xsl:apply-templates select="./*[local-name()='SummaryRecord']"/>
						<xsl:apply-templates select="./gmd:MD_Metadata"/>
					</ol>
				</xsl:when>
				<xsl:otherwise>
					<div class="model-select-default-instructions">
						<h4>No models match your criteria</h4>
						<p>Change the‌ region and/or constituent filters above‌ to find models matching your criteria.</p>
					</div>
				</xsl:otherwise>
			</xsl:choose>

		</div>
	</xsl:template>


	<xsl:template match="dc:title">
		<xsl:choose>
			<xsl:when test=".!=''">
				<xsl:value-of select="."/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text> ...</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="gmd:MD_Metadata">
		<xsl:for-each select=".">
			<li>
				<a>
					<xsl:attribute name="href">
						<xsl:text>javascript:(CONTROLLER.selectUUID</xsl:text>
						<xsl:text>('</xsl:text>
						<xsl:value-of select="./gmd:fileIdentifier/gco:CharacterString"/>
						<xsl:text>'))</xsl:text>
					</xsl:attribute>
					<xsl:choose>
						<xsl:when test="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString">
							<xsl:apply-templates select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text> ...</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</a>
<!--				<br/>
				<xsl:apply-templates select="./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString"/>-->
				<hr/>
			</li>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString">
		<xsl:choose>
			<xsl:when test=".!=''">
				<xsl:value-of select="."/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text> ...</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
