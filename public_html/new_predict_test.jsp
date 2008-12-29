<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
<head>
	   <link type="text/css" rel="stylesheet" href="http://privusgs7.er.usgs.gov/sparrow_map/css/usgs_style_main.css"/>    
		<title>Request Test</title>
		<link rel="icon" href="favicon.ico" >
</head>
<body>
      <div id="header">
       <div id="banner-area">

          <h1>US Geological Survey</h1><!-- Not actually visible unless printed -->
          <div id="usgs-header-logo"><a href="http://www.usgs.gov" title="Link to the US Geological Survey main web page">
          <img alt="USGS Logo" src="http://infotrek.er.usgs.gov/warp/images/USGS_web_logo.gif" />
          </a></div>
  
          <div id="usgsPrintCommHeader" class="print-only">
            <h3 id="printCommType">Web Page Hardcopy</h3>
            <p class="hide">The section 'Web Page Hardcopy' is only visible when printed.  Ignore if viewing with style sheets turrned off</p>
  
            <p id="printCommDate">
              <script type="text/javascript">document.write(new Date().toLocaleString());</script>
            </p>
            <p id="printCommPrintFrom">Printed From: <script type="text/javascript">document.write(document.location.href);</script></p>
            <p>
              This print version of the page is optimized to print only the
              content portions of the web page your were viewing and is not
              intended to have the same formatting as the original page.
            </p>
          </div>
  
        
          <div id="ccsa-area">
            <h4 class="access-help">Top Level USGS Links</h4>
            <a href="http://www.usgs.gov/" title="Link to main USGS page">USGS Home</a>
            <br/>
            <a href="http://www.usgs.gov/ask/index.html" title="Link to main USGS contact page">Contact USGS</a>
            <br/>
  
            <a href="http://search.usgs.gov/" title="Link to main USGS search (not publications search)">Search USGS</a>
            <br/>
          </div>
        </div><!-- End content -->
  
        <div id="quick-links" class="access-help">
          <h4>Quick Page Navigation</h4>
            <ul title="links to portions of this page.  Details:  Not normally visible and intended for screen readers.  Page layout has the content near top. Links opening new windows are noted in titles.">
  
            <li><a href="#page-content" title="Main content of this page.  Starts with the pages name.">Page Main Content</a></li>
            <li><a href="#site-top-links" title="Short list of top pages within the site.  Before page content.">Top Pages Within This Site</a></li>
            <li><a href="#site-full-links" title="Complete list of page within the site.  After page content.">All Pages Within This Site</a></li>
            <li><a href="#full-navigation" title="Pages within the site and external links.  After page content.">All Site Pages Plus External Links</a></li>
            <li><a href="#validation-info" title="HTML and CSS validation links for this page.  After page content.">HTML and CSS Validation Info</a></li>
            <li><a href="#footer" title="Mainenance info, general USGS links.  Bottom of page, after content.">Misc. Page Info</a></li>
  
          </ul>
        </div>
  
      <h2 id="site-title">
        SPAtially Referenced Regressions On Watershed Attributes (SPARROW) Model Decision Support
      </h2>
      <!--<h3 id="internal-only">For Internal USGS Access Only</h3>-->
      </div><!-- /header -->
<div>

		<form action="sp_predict" method="get" enctype="application/x-www-form-urlencoded">
			<fieldset title="Exporting a prediction">
				<label for="xml_input_1">Getting a Prediction Export: National Model w/ gross and specific adjustments.</label>
				<p>
				
				</p>
				<table>
					<tr>
						<td>Context ID</td>
						<td><input type="text" name="context-id" size="26"/></td>
					</tr>
					<!-- 
					<tr>
						<td>Model ID</td>
						<td><input type="text" name="model-id" size="26"/></td>
					</tr>
					 -->
				</table>
				
				<input type="submit" name="submit" value="submit"/>
				<input type="radio" name="mime-type" value="xml" checked="checked">xml
				<input type="radio" name="mime-type" value="csv">csv
				<input type="radio" name="mime-type" value="tab">tab
				<input type="radio" name="mime-type" value="excel">excel
				<br/>
				<!-- 
				* Either context-id or model-id must be specified<br/> -->
				<!--  &#165; format is  -->
				
		<!-- 	<input type="checkbox" name="compress" value="zip">zip -->	
			</fieldset>
		</form>


</div>
        <div id="footer" style="width: 100%; margin-right: -1em;">
           <div id="usgs-policy-links">
              <h4 class="access-help">USGS Policy Information Links</h4>
              <ul class="hnav">
              <li><a href="http://www.usgs.gov/accessibility.html" title="USGS web accessibility policy">Accessibility</a></li>
              <li><a href="http://www.usgs.gov/foia/" title="USGS Freedom of Information Act information">FOIA</a></li>
      
        
              <li><a href="http://www.usgs.gov/privacy.html" title="USGS privacy policies">Privacy</a></li>
              <li><a href="http://www.usgs.gov/policies_notices.html" title="USGS web policies and notices">Policies and Notices</a></li>
          </ul>
          </div><!-- end usgs-policy-links -->

          <div class="content">
            <div id="page-info">
              <p id="footer-doi-links">
                <span class="vcard">
                
                    <a class="url fn org" href="http://www.doi.gov/" title="Link to the main DOI web site">U.S. Department of the Interior</a>
                    <span class="adr">
                        <span class="street-address">1849 C Street, N.W.</span><br>
                        <span class="locality">Washington</span>, 
                        <span class="region">DC</span>
                        <span class="postal-code">20240</span>
                
                    </span>
                
                    <span class="tel">202-208-3100</span>
                </span><!-- vcard -->
                |
                <span class="vcard">
                    <a class="url fn org" href="http://www.usgs.gov" title="Link to the main USGS web site">U.S. Geological Survey</a>
                    <span class="adr">
                        <span class="post-office-box">Box 25286</span><br>
                
                        <span class="locality">Denver</span>, 
                        <span class="region">CO</span>
                
                        <span class="postal-code">8022</span>
                    </span>
                </span><!-- vcard -->
              </p>

              <p id="footer-page-url">URL: </p>
              <p id="footer-contact-info">
                Page Contact Information:
                <a title="Contact Email" href="mailto:eeverman@usgs.gov?subject=Sparrow Map Comments">webmaster</a>

              </p>
              <p id="footer-page-modified-info">Page Last modified: <script type="text/javascript">document.write(document.lastModified);</script></p>
            </div><!-- /page-info -->

            <div id="gov-buttons">
              <a title="link to the official US Government web portal" href="http://firstgov.gov/">
                <img src="http://infotrek.er.usgs.gov/docs/nawqa_www/nawqa_public_template/assets/footer_graphic_firstGov.jpg" alt="FirstGov button">
              </a>
              <a title="Link to Take Pride in America, a volunteer organization that helps to keep America's public lands beautiful." href="http://www.takepride.gov/">

                <img src="http://infotrek.er.usgs.gov/docs/nawqa_www/nawqa_public_template/assets/footer_graphic_takePride.jpg" alt="Take Pride in America button">
              </a>
            </div><!-- /gov-buttons -->

          </div><!-- /content -->
        </div><!-- /footer -->
 </body>
 </html>
