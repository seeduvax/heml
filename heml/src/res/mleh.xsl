<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

<xsl:template match="p"><xsl:apply-templates select="*|text()"
/></xsl:template>

<xsl:template match="@*"> %<xsl:value-of select="name()"/>=<xsl:value-of select="."/>
</xsl:template>

<xsl:template match="text()"><xsl:text>
</xsl:text><xsl:value-of select="normalize-space(.)"
/></xsl:template>

<xsl:template match="*">{<xsl:value-of select="name()"/>  <xsl:apply-templates select="@*"/>
  <xsl:apply-templates select="*|text()"/>
}</xsl:template>
</xsl:stylesheet>
