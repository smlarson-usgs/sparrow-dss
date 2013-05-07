<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
<head>
	<jsp:include page="template_meta_tags.jsp" flush="true" />
	
	<title>SPARROW DSS Screencast</title>
	
	<jsp:include page="template_page_tracking.jsp" flush="true" />
	<script type="text/javascript" language="javascript">

	var tag = document.createElement('script');

	tag.src = "https://www.youtube.com/iframe_api";
	var firstScriptTag = document.getElementsByTagName('script')[0];
	firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

	var ytp;
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
			events: {'onReady': onPlayerReady}
		});
	}

	function onPlayerReady(event) {
		event.target.setPlaybackQuality('hd720');
		event.target.playVideo();
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
  		background-color: black; padding: 0; margin: 0;
  	}
  </style>
</head>
<body>
		<div id="page-content" class="area-1 area"> 
			<div class="area-content">
				<div id="player"></div>
			</div>
		</div>
</body>
</html>