<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:param name="param">default</xsl:param>
<xsl:output method="text"/>
<xsl:template match="document">
- sections: <xsl:value-of select="count(//section)"/>
- paragraphs: <xsl:value-of select="count(//p)"/><xsl:text>
</xsl:text>
- parameter : <xsl:value-of select="$param"/><xsl:text>
</xsl:text>
</xsl:template>
</xsl:stylesheet>
