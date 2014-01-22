<!--[if lte IE 7]>
<style type="text/css">
#sizer { <%-- Allows the 'jello sizing to work with IE7 and older --%>
	width:expression(document.body.clientWidth > 1800 ? "800px" : "80%" );
}

button { <%-- Fix for ugly extra padding and button format: http://latrine.dgx.cz/the-stretched-buttons-problem-in-ie --%>
	padding: .1em .2em !important; margin: .2em; overflow: visible;
}
</style>
<![endif]-->
<!--[if gte IE 8]>
<style type="text/css">
body { <%-- IE8 and new has ungainly large text --%>
	font-size: 86%;
}
</style>
<![endif]-->