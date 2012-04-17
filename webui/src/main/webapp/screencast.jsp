<!DOCTYPE html>
<%@ page contentType="text/html; charset=utf-8"%>
<html>
<head>
  <meta charset="utf-8" />
  <title>SPARROW DSS Screencast</title>
<%
	//The file name, which should not include a path or extension.
	String fileName = request.getParameter("file-name");
	if (fileName == null) fileName = "";

	//Do some simple filtering of possible incoming hack requests
	if (fileName.indexOf("/") > -1) fileName = "";
	if (fileName.indexOf("\\") > -1) fileName = "";
	
	//Build path portion of video
	String videoBasePath = "http://gallery.usgs.gov/video/screencasts/sparrow/";
	
	//Build the local path back to this server
	//This contains some specific logic for the water.usgs.gov server, which 
	//is forwarded and munged.
	String appBasePath = null;	// eg: http://host:8080/sparrow/

	
	String thisPageURLfromHeader = request.getHeader("x-request-url");
	
	
	//Note:  Pieces are generally built as '/chunk' - leading separator, no following.
	if (thisPageURLfromHeader == null) {
		//No header added, so we are likely running on a local machine - build the url
		
		String reqURL = request.getRequestURL().toString();
		//Drop the page name and trailing slash
		appBasePath = reqURL.substring(0, reqURL.lastIndexOf('/'));
		
	} else {
		//Running behind Apache
		String WATER_SERVER_NAME = "water.usgs.gov";
		String xForwardHost = request.getHeader("x-forwarded-host");
		if (xForwardHost == null) xForwardHost = "";
		boolean reqFromWaterServer = xForwardHost.contains(WATER_SERVER_NAME);
		
		String server = (reqFromWaterServer)?WATER_SERVER_NAME:request.getServerName();
		String port = (80 == request.getServerPort())? "" : ":" + request.getServerPort();
		String contextPath = null;
		if (reqFromWaterServer) {
			contextPath = "/nawqa/sparrow/dss";
		} else {
			contextPath = thisPageURLfromHeader;
			contextPath = contextPath.substring(8);	//drop http:// or https://
			
			//Drop the page name and trailing slash
			contextPath = contextPath.substring(0, contextPath.lastIndexOf('/'));
			
			//Drop everything before the first slash
			contextPath = contextPath.substring(contextPath.indexOf('/'));
		}
		
		appBasePath = request.getScheme() + "://" + server + port + contextPath;
	}
	
%>
  <!-- Include the VideoJS Library -->
  <script src="video-js/video.js" type="text/javascript" charset="utf-8"></script>

  <script type="text/javascript">

    VideoJS.DOMReady(function(){
      
		// Using the video's ID or element
		var myPlayer = VideoJS.setup("sparrow_video");
		
		
		myPlayer.enterFullWindow();
		//myPlayer.play(); // Starts playing the video for this player.
    });
  </script>
  
  <style type="text/css">
  	body {
  		background-color: black; padding: 0; margin: 0;
  	}
  	#fall-back-no-video {width: 100%; height: 100%; }
  
  </style>

  <!-- Include the VideoJS Stylesheet -->
  <link rel="stylesheet" href="video-js/video-js.css" type="text/css" media="screen" title="Video JS">
</head>
<body>

  <div class="video-js-box">
    <!-- Using the Video for Everybody Embed Code http://camendesign.com/code/video_for_everybody -->
    <video id="sparrow_video" class="video-js" width="1280" height="768" preload="none" controls="controls" autoplay="true" poster="images/usgs_logo.jpg">
      <source src="<%= videoBasePath + fileName %>.m4v" type="video/x-m4v"/>
      <source src="<%= videoBasePath + fileName %>.mp4" type="video/mp4"/>
		<!-- <track kind="subtitles" src="subtitles.srt" srclang="en" label="English"/> -->
      <!-- Flash Fallback. Use any flash video player here. Make sure to keep the vjs-flash-fallback class. -->
      <object id="flash_fallback_1" data="http://releases.flowplayer.org/swf/flowplayer-3.2.1.swf" class="vjs-flash-fallback" width="1280" height="768" type="application/x-shockwave-flash">
      	<param name="movie" value="http://releases.flowplayer.org/swf/flowplayer-3.2.1.swf" />
      	<param name="allowfullscreen" value="true" />
      	<param name="flashvars" value='config={"playlist":["<%= appBasePath %>/images/usgs_logo.jpg", {"url": "<%= videoBasePath + fileName %>.flv","autoPlay":true,"autoBuffering":true}]}' />
      	<img id="fall-back-no-video" src="<%= appBasePath %>/images/usgs_logo.jpg" alt="Poster Image" title="No video playback capabilities." />
      </object>
    </video>
  </div>

</body>
</html>