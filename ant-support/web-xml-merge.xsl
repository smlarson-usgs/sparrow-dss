<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:j2ee="http://java.sun.com/xml/ns/j2ee">
    <xsl:output method="xml" indent="yes" />
    <xsl:param name="includeFile" />
    <xsl:template match="/web-app">
        <web-app>
            <xsl:apply-templates select="icon"/>
            <xsl:apply-templates select="display-name"/>
            <xsl:apply-templates select="description"/>
            <xsl:apply-templates select="distributable"/>
            <xsl:apply-templates select="context-param"/>
            <xsl:apply-templates select="filter"/>
            <xsl:apply-templates select="filter-mapping"/>
            <xsl:apply-templates select="listener"/>
            <xsl:copy-of select="document($includeFile)/j2ee:web-app/j2ee:listener"/>
            <xsl:apply-templates select="servlet"/>
            <xsl:copy-of select="document($includeFile)/j2ee:web-app/j2ee:servlet"/>
            <xsl:apply-templates select="servlet-mapping"/>
            <xsl:copy-of select="document($includeFile)/j2ee:web-app/j2ee:servlet-mapping"/>
            <xsl:apply-templates select="session-config"/>
            <xsl:apply-templates select="mime-mapping"/>
            <xsl:apply-templates select="welcome-file-list"/>
            <xsl:apply-templates select="error-page"/>
            <xsl:apply-templates select="taglib"/>
            <xsl:apply-templates select="resource-env-ref"/>
            <xsl:apply-templates select="resource-ref"/>
            <xsl:apply-templates select="security-constraint"/>
            <xsl:apply-templates select="login-config"/>
            <xsl:apply-templates select="security-role"/>
            <xsl:apply-templates select="env-entry"/>
            <xsl:apply-templates select="ejb-ref"/>
            <xsl:apply-templates select="ejb-local-ref"/>
        </web-app>
    </xsl:template>
    <xsl:template match="*">
        <xsl:for-each select="@*">
            <xsl:attribute name="{.}">
                <xsl:value-of select="."/>
            </xsl:attribute>
        </xsl:for-each>
        <xsl:copy-of select="."/>
    </xsl:template>
</xsl:stylesheet>
