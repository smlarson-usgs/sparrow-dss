Ext.onReady(function() {

	//A requirement of using Ext.History
	Ext.History.init();
	
	CONTROLLER.readStateFromHistory();
	
	//Instrument the go button for the videos
	var videoGoBtn = document.getElementById('tutorial-video-go-button');
	
	videoGoBtn.onclick = function() {
		var videoUrl = document.getElementById('tutorial-video-select').value;
		
		if (videoUrl != '') {
	   		var newWindow = window.open(videoUrl, '_blank', 
				'resizable=1,location=0,status=0,scrollbars=0,width=640,height=384');
	   		newWindow.focus();
		}
	};
});