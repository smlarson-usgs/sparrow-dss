<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   
<xsl:output method="text"/>
<xsl:strip-space elements="*"/>
   
<xsl:template match="/">
	<xsl:text>
/*
To Run this script via any sort of SQL Editor, make sure that '/' is set as the statement delimiter, not ';'
since ';' characters must be inlcluded in the triggers themselves.
*/
	</xsl:text>
	<xsl:apply-templates select="//structure/shape[elemRef/@type='DatabaseTable']"/>
	<xsl:text>
COMMIT
/
	</xsl:text>
</xsl:template>

<xsl:template match="shape">
	<xsl:if test="../shape/@name = concat(@name, '_SEQ')">
CREATE OR REPLACE TRIGGER <xsl:value-of select="@name"/>_AUTO_ID_TRIG
BEFORE INSERT ON <xsl:value-of select="@name"/>
REFERENCING NEW AS newRow
FOR EACH ROW WHEN (newRow.<xsl:value-of select="@name"/>_ID IS NULL)
BEGIN
	SELECT <xsl:value-of select="@name"/>_SEQ.nextval INTO :newRow.<xsl:value-of select="@name"/>_ID FROM dual;
END;
/
<xsl:text>&#xa;</xsl:text>
	</xsl:if>
</xsl:template>
</xsl:stylesheet>

