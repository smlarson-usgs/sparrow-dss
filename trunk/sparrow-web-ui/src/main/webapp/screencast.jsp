<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
<head>
	<jsp:include page="template_meta_tags.jsp" flush="true" />
	
	<title>SPARROW DSS Screencast</title>
	
	<jsp:include page="template_page_tracking.jsp" flush="true" />
	
	<script src="jquery/jquery-1.7.2.js"></script>
	<script src="screencast/js/jquery.fitvids.js"></script>

	<script type="text/javascript" language="javascript">

	var tag = document.createElement('script');

	tag.src = "https://www.youtube.com/iframe_api";
	var firstScriptTag = document.getElementsByTagName('script')[0];
	firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

	var ytp;
	var waitMessageIsVisible = true;
	function onYouTubeIframeAPIReady() {
		var videoIdStr = getQueryParam('videoId');

		if (videoIdStr == null) {
			window.alert("No video is selected.");
			return;
		}

		ytp = new YT.Player('player', {
			videoId: videoIdStr,
			height: 720,
			width: 960,
			enablejsapi: 1,
			playerVars: {
				autoplay: 0,
				controls: 1,
				modestbranding: 1,
				playlist: '1tzeR4WkLv0,5K1Smu7Q4Fc,zrycRF7MeG8,UkC_76uq748,tHnxt2ORNQU'},
			events: {'onReady': onPlayerReady, 'onStateChange': onStateChange}
		});
		
		$("#page-content").fitVids();
	}

	function onPlayerReady(event) {
		event.target.setPlaybackQuality('hd720');
		event.target.playVideo();
	}
	
	function onStateChange(event) {
		if (waitMessageIsVisible && event.data == YT.PlayerState.PLAYING) {
			waitMessageIsVisible = false;
			$("#please-wait").hide();
		}
	}
		
	function getQueryParam(key) {
		var re=new RegExp('(?:\\?|&)'+key+'=(.*?)(?=&|$)','gi');
		var r=[], m;
		while ((m=re.exec(document.location.search)) != null) r.push(m[1]);
		
		if (r.length > 0) {
			return r[0]
		} else {
			return null;
		}
	}
		
	</script>
  <style type="text/css">
  	body {
  		background-color: black; padding: 0; margin: 0; font-family: Tahoma, Arial, sans-serif;
  	}
		
		#site-title {
			color:#FFFFFF; background-color:#345280; height: 17px; margin: 0; font-size:17px; padding: 5px; font-weight:bold;
		}
		
		#please-wait {
			position: absolute; top: 20%; width: 100%; text-align: center; z-index: 1;
			color:#FFFFFF; font-size: 150%;
		}
		
		#footer {display: block; height: 10px; background-color:#345280;}
  </style>
</head>
<body>
	<div id="header"> 
		<h2 id="site-title">SPARROW Decision Support System - Tutorial Screencast</h2>
	</div>
	<div id="page-area-container" class="area-container"> 
		<div id="page-content" class="area-1 area"> 
			<div class="area-content">
				<h4 id="please-wait">Initial video loading may be slow - please be patient.</h4>
				<div id="player"></div>
			</div>
		</div>
	</div>
	<div id="footer"> 
	</div>
</body>
</html>