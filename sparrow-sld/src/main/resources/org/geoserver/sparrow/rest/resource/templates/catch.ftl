<StyledLayerDescriptor 
    xmlns="http://www.opengis.net/sld" 
    xmlns:ogc="http://www.opengis.net/ogc" 
    xmlns:xlink="http://www.w3.org/1999/xlink" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    version="1.0.0" 
    xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd">
  <NamedLayer>
    <Name>${workspace}:${layer}</Name>
    <UserStyle>
      <Title>Sparrow Catchment style</Title>
      <#list bins as bin>
      <FeatureTypeStyle>
        <Rule>
          <Name>Bin ${bin_index + 1} of ${bins?size}</Name>
          <ogc:Filter>
            <#if bin_index == 0 && !bounded>
                <ogc:PropertyIsLessThanOrEqualTo>
                    <ogc:PropertyName>VALUE</ogc:PropertyName>
                    <ogc:Literal>${bin.upper}</ogc:Literal>
                </ogc:PropertyIsLessThanOrEqualTo>
            <#elseif bin_index == bins?size - 1 && !bounded>
                <ogc:PropertyIsGreaterThanOrEqualTo>
                    <ogc:PropertyName>VALUE</ogc:PropertyName>
                    <ogc:Literal>${bin.lower}</ogc:Literal>
                </ogc:PropertyIsGreaterThanOrEqualTo>
            <#else>
                <ogc:And>
                    <ogc:PropertyIsGreaterThanOrEqualTo>
                      <ogc:PropertyName>VALUE</ogc:PropertyName>
                      <ogc:Literal>${bin.lower}</ogc:Literal>
                    </ogc:PropertyIsGreaterThanOrEqualTo>
                    <ogc:PropertyIsLessThan>
                      <ogc:PropertyName>VALUE</ogc:PropertyName>
                      <ogc:Literal>${bin.upper}</ogc:Literal>
                    </ogc:PropertyIsLessThan>
                </ogc:And>
            </#if>
          </ogc:Filter>
          <LineSymbolizer>
            <Stroke>
              <CssParameter name="stroke">#${bin.color}</CssParameter>
              <CssParameter name="stroke-width">2</CssParameter>
            </Stroke>
          </LineSymbolizer>
        </Rule>
      </FeatureTypeStyle>
      </#list>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>
