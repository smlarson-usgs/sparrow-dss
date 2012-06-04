
var termReachesAtTimeOfCreation;

$(document).ready(function(){
   $("#tabs").tabs();
	 
	 initTerminalReaches();
	 initAggReportForm();
	 
	 
});

function initAggReportForm() {
	$("#agg-upstream-form").submit(function(event) {
		
		event.preventDefault();
		
		var contextId = $('#agg-upstream-form input[name="context-id"]').val();
		var regionType = $('#agg-upstream-form input[name="region-type"]:checked').val();
		var tableName = "getDeliveryAggReport";
		var reqParams = "context-id=" + contextId +
			"&region-type=" + regionType + 	
			"&include-zero-rows=false" + 					
			"&mime-type=xhtml_table";
		var reqUrl = "../" + tableName + "?" + reqParams;


		$.ajax({
				url: reqUrl,
				success: function(response, textStatus, jqXHR) {
						// update status element
						//alert('OK - got new table content: ' + jqXHR.responseText);
						
						$("#agg-report-table-area").empty();
						$("#agg-report-table-area").append(jqXHR.responseText);;
						
				},
				error: function(xhr) {
						alert('Error!  Status = ' + xhr.status);
				}
		});
		
		return false;
	});
	
}

function initTerminalReaches() {
	 termReachesAtTimeOfCreation = getParentCurrentTermReaches();
	 
	 $("p.downstream-reaches-list").append(getTermReachesAsString(termReachesAtTimeOfCreation));
	 
	 
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
}


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
		 
		 var rId = $.trim(terms[i]["@id"]);
		 //var href = rId;
		 var href = "javascript:idDeliveryReach(" + rId + ");";
		 
		 termString = termString + 
		 "<a href='" + href + "'>" +
		 terms[i]["@name"] + " (" + rId + ")" +
		 "</a>";
	 
		 if (i < (terms.length - 1)) {
			 termString += ", ";
		 }
	 }
	 return termString;
}

function getParentCurrentTermReaches() {
	
	if (window.opener != null) {
		var parentTerms = window.opener.Sparrow.SESSION.getAllTargetedReaches();
		var copyTerms = new Array();

		for (var i=0; i < parentTerms.length; i++) {
			copyTerms.push(parentTerms[i]);
		}


		return copyTerms;
	
	} else {
		//Possibly just testing - or the user closed the opening window
		return new Array();
	}
}

