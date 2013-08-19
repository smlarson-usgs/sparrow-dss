<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:gco="http://www.isotc211.org/2005/gco"
     xmlns:gmd="http://www.isotc211.org/2005/gmd"
     version="2.0">

	<xsl:output method="xml" encoding="UTF-8"/>
    
    <xsl:template match="/">
    	<id>
    	<xsl:apply-templates select="//gmd:fileIdentifier"/>
<!--    	<xsl:apply-templates select="//gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue = 'sparrow_dss_model_id']"></xsl:apply-templates>-->
    	</id>
    </xsl:template>
    
    <!-- Creates the name/value keywords section -->
	<xsl:template match="gmd:MD_Keywords">
		<xsl:variable name="modelId" select="gmd:keyword[1]/gco:CharacterString"/>
		<model-id><xsl:value-of select="$modelId"/></model-id>
	</xsl:template>
	
	<xsl:template match="gmd:fileIdentifier">
		<xsl:variable name="id" select="gco:CharacterString"/>
		<uid><xsl:value-of select="$id"/></uid>
	</xsl:template>
	
	
	
</xsl:stylesheet>