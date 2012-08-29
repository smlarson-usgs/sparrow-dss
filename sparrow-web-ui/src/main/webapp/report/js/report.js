
var termReachesAtTimeOfCreation;
var _TERM_REPORT_CONTAINER_ID = "terminal-report-container";
var _AGG_REPORT_CONTAINER_ID = "aggregate-report-container";

$(document).ready(function(){
   $("#tabs").tabs();
	 
	 initTermReachList();
	 initTermReport();
	 initAggReport();
	 termReportYieldChangeHandler();
	 
	 
});

var aggReportRegionChangeHandler = function(event) {
	var reqUrl = getAggReportUrl("xhtml_table");
	
	$.ajax({
			url: reqUrl,

			beforeSend: function(jqXHR, settings) {
				$("#" + _AGG_REPORT_CONTAINER_ID + " .report-load-status").show('normal');
			},
			success: function(response, textStatus, jqXHR) {
					// update status element
//					alert('OK - got new table content: ' + jqXHR.responseText);
					
					var reportTable = $("#" + _AGG_REPORT_CONTAINER_ID + " .report-table-area");
					reportTable.empty();
					reportTable.append(jqXHR.responseText);
					$("#" + _AGG_REPORT_CONTAINER_ID + " .report-load-status").hide('normal');

			},
			error: function(xhr) {
					alert('Error Loading the Aggregate Report - Status = ' + xhr.status);
			}
	});

	return true;
};

var termReportYieldChangeHandler = function(event) {
	var reqUrl = getTermReportUrl("xhtml_table");
	
		$.ajax({
				url: reqUrl,
				
				beforeSend: function(jqXHR, settings) {
					$("#" + _TERM_REPORT_CONTAINER_ID + " .report-load-status").show('normal');
				},
				success: function(response, textStatus, jqXHR) {
						// update status element
						//alert('OK - got new table content: ' + jqXHR.responseText);
						
						var reportTable = $("#" + _TERM_REPORT_CONTAINER_ID + " .report-table-area");
						reportTable.empty();
						reportTable.append(jqXHR.responseText);
						$("#" + _TERM_REPORT_CONTAINER_ID + " .report-load-status").hide('normal');
						
				},
				error: function(xhr) {
						alert('Error loading Terminal Report - Status = ' + xhr.status);
				}
		});

	return true;
};

var getAggReportUrl = function(mimeType) {
	var urlParams = getUrlVars();

	var contextId = urlParams["context-id"];
	var regionType = $("#" + _AGG_REPORT_CONTAINER_ID + " .controls input[name='region-type']:checked").val();
	if (regionType == null) {
		regionType = "state";
	}
	var serviceName = "getDeliveryAggReport";
	var reqParams = "context-id=" + contextId +
		"&region-type=" + regionType + 	
		"&include-zero-rows=false" + 
		"&report-yield=false" +
		"&mime-type=" + mimeType;
	return "../" + serviceName + "?" + reqParams;
}

var getTermReportUrl = function(mimeType) {
		var urlParams = getUrlVars();
		
		var serviceName = "getDeliveryTerminalReport";
		var contextId = urlParams["context-id"];
		var reportYield = $("#" + _TERM_REPORT_CONTAINER_ID + " .controls input[name='report-yield']:checked").val();
		
		var reqParams =
			"context-id=" + contextId + 
			"&include-zero-rows=true" + 
			"&report-yield=" + reportYield +
			"&mime-type=xhtml_table";
		var reqUrl = "../" + serviceName + "?" + reqParams;
		
		return reqUrl;
}
	
function initAggReport() {
	$("#" + _AGG_REPORT_CONTAINER_ID + " .controls input[name='region-type']").change(aggReportRegionChangeHandler);
	this.aggReportRegionChangeHandler();
}

function initTermReport() {
	$("#" + _TERM_REPORT_CONTAINER_ID + " .controls input[name='report-yield']").change(termReportYieldChangeHandler);
	this.aggReportRegionChangeHandler();
}

function initTermReachList() {
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

function getUrlVars() {
	var vars = [], hash;
	var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
	for(var i = 0; i < hashes.length; i++) {
		hash = hashes[i].split('=');
		vars.push(hash[0]);
		vars[hash[0]] = hash[1];
	}
	return vars;
}
