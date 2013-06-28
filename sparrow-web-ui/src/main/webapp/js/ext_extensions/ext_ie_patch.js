/*
 * Patch for EXT 3.1.1, which does not know about IE 9+ and assumes tha these
 * browsers are IE6.
 * 
 */


if (Ext.isIE) {
	var ua = navigator.userAgent.toLowerCase();
	
	Ext.isIE6 = /msie 6/.test(ua);
	Ext.isIE7 = /msie 7/.test(ua);
  Ext.isIE8 = /msie 8/.test(ua);
	﻿Ext.isIE9 = /msie 9/.test(ua);
	Ext.isIE10 = /msie 10/.test(ua);
	
	//Existing EXT code defaults to IE6 - switch to default to IE10
	if (!Ext.isIE6 && ! Ext.isIE7 && ! Ext.isIE8 && ! Ext.isIE9) {
		Ext.isIE10 = true;
	}
	
}