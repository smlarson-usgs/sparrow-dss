<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
<head>
	<jsp:include page="template_meta_tags.jsp" flush="true" />

	<title>SPARROW DSS Screencast</title>

	<jsp:include page="template_page_tracking.jsp" flush="true" />

	<script src="jquery/jquery-1.7.2.js"></script>
	<script src="screencast/js/jquery.fitvids.js"></script>
	<script src="js/Screencasts.js"></script>

	<script type="text/javascript" language="javascript">
	$(document).ready(function(){
		var videoIdOfInterest = getQueryParam('videoId');

		var otherVideoIds = [];

		$.each(Screencasts, function(index, screencast){
			//filtered pluck
			var videoId = screencast.videoId;
			if(videoId !== videoIdOfInterest){
				otherVideoIds.push(videoId);
			}
		});
		var otherVideoIdsCsv = otherVideoIds.join(',');

		var	playerVars = {
			autoplay: 1,
			controls: 1,
			modestbranding: 1,
			playlist: otherVideoIdsCsv,
			origin: window.location.protocol + '////' + window.location.hostname,
			vq: 'hd720'
		};
		var url = '//www.youtube.com/embed/' + videoIdOfInterest + '?' + $.param(playerVars);
		console.log(url);
		$('#player').attr('src', url);
		//refresh page first time, otherwise leave it be

	});
	function conditionallyRefresh(){
		if(null === getQueryParam('secondLoad')){
			location.assign(location.href + '&secondLoad=true');
		}
	};


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

/*		#please-wait {
			position: absolute; top: 20%; width: 100%; text-align: center; z-index: 1;
			color:#FFFFFF; font-size: 150%;
		}*/

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
				<!--<h4 id="please-wait">Initial video loading may be slow - please be patient.</h4>-->
				<div>
					<iframe id="player" width="960" height="720" frameborder="0" onload = "conditionallyRefresh();" allowfullscreen></iframe>
				</div>
			</div>
		</div>
	</div>
	<div id="footer">
	</div>
</body>
</html>