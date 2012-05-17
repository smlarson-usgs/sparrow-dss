
var termReachesAtTimeOfCreation;

$(document).ready(function(){
   $("#tabs").tabs();
	 
	 var terms = getParentCurrentTermReaches();
	 
	 termReachesAtTimeOfCreation = terms;
	 //alert(getTermReachesAsString(termReachesAtTimeOfCreation));
	 $("p.downstream-reaches-list").append(getTermReachesAsString(termReachesAtTimeOfCreation));
	 $("p.downstream-reaches-list").find('a')
			.click( function() {
				idDeliveryReach($(this).attr('href'));
				return false;
      });
	 
	 
	var timer = $.timer(function() {
		 var ok = checkTermReachesInSync(termReachesAtTimeOfCreation, getParentCurrentTermReaches());
		 if (!ok) {
			 timer.stop();
			 termReachesOutOfSync();
		 } else {
			 //$("p.downstream-reaches-list").append(".");
		 }
	});

  timer.set({time : 5000, autostart : true});
	 
});


function termReachesOutOfSync() {
	var oos = $("p.downstream-reaches-out-of-sync");
	oos.show('normal');
}

/**
 * Returns false if the terminal reaches used to create this report have changed
 * on the map.
 */
function checkTermReachesInSync(initTerms, curTerms) {
	if (curTerms == null || initTerms.length != curTerms.length) {
		return false;
	} else {
		//check each item in the list
	 for (var i=0; i < initTerms.length; i++) {
		 if (initTerms[i]["@id"] != curTerms[i]["@id"]) return false;
	 }
	 return true;
	}
}
 
function openTerminalHelp() {
	
		var newWindow = window.open('terminal_report_help.jsp', '_blank', 
		'resizable=1,location=0,status=1,scrollbars=1,width=500,height=640');
		newWindow.focus();
}

function openAggHelp() {
	
		var newWindow = window.open('aggregate_report_help.jsp', '_blank', 
		'resizable=1,location=0,status=1,scrollbars=1,width=500,height=640');
		newWindow.focus();
}

function idDeliveryReach(reachId) {
	
	var id = 0;
	
	//Calls this function on the opener window (the page that opened this page - ie the main map application)
	if (typeof reachId == "string") {
		id = parseInt($.trim(reachId), 10);
	} else {
		id = reachId
	}
	window.opener.idDeliveryReach(id);
}


function getTermReachesAsString(terms) {
	 var termString = "";
	 for (var i=0; i < terms.length; i++) {
		 
		 termString = termString + 
		 "<a href=" + $.trim(terms[i]["@id"]) + ">" +
		 terms[i]["@name"] + " (" + $.trim(terms[i]["@id"]) + ")" +
		 "</a>";
	 
		 if (i < (terms.length - 1)) {
			 termString += ", ";
		 }
	 }
	 return termString;
}

function getParentCurrentTermReaches() {
	var parentTerms = window.opener.Sparrow.SESSION.getAllTargetedReaches();
	var copyTerms = new Array();
	
	for (var i=0; i < parentTerms.length; i++) {
		 copyTerms.push(parentTerms[i]);
	}
	 
	 
	return copyTerms;
}

