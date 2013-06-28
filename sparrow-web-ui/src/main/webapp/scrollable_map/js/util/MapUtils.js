JMap.util.sorter = function(t1, t2) {
  if (t1.zdepth < t2.zdepth) return -1;
  if (t1.zdepth > t2.zdepth) return 1;
  return 0;
}


JMap.util.unselect = function() {

  if (document.selection)
    document.selection.empty();
  
  if (window.getSelection)
    window.getSelection().removeAllRanges();
}


//utility function
//returns an array of nodes whose class name is the passed classname
JMap.util.getElementsByClassName = function(node, classname){
  var a = [];
  var re = new RegExp('\\b' + classname + '\\b');
  var els = node.getElementsByTagName("*");
  for(var i=0,j=els.length; i<j; i++)
    if(re.test(els[i].className))a.push(els[i]);
  return a;
}

JMap.util.getRelativeCoords = function(evt, elem) {

	var sx,sy;
	if (self.pageYOffset) {
		sx = self.pageXOffset;
		sy = self.pageYOffset;
	} else if (document.documentElement && document.documentElement.scrollTop) {
		// Explorer 6 Strict
		sx = document.documentElement.scrollLeft;
		sy = document.documentElement.scrollTop;
	} else if (document.body) {
		sx = document.body.scrollLeft;
		sy = document.body.scrollTop;
	}
	
	
	var x = evt.clientX - JMap.util.findPosX(elem) + sx;
	var y = evt.clientY - JMap.util.findPosY(elem) + sy;

	return {x:x, y:y};
}





JMap.util.findPosX = function(obj)
{
	var curleft = 0;
	if (obj.offsetParent) {
		while (obj.offsetParent) {
			curleft += obj.offsetLeft
			obj = obj.offsetParent;
		}
	}
	else if (obj.x) {
		curleft += obj.x;
	}
	return curleft;
}

JMap.util.findPosY = function(obj) {
	var curtop = 0;
	if (obj.offsetParent) {
		while (obj.offsetParent) {
			curtop += obj.offsetTop
			obj = obj.offsetParent;
		}
	}
	else if (obj.y) {
		curtop += obj.y;
	}
	return curtop;
}



JMap.util.getWindowDimensions = function() {
  
  var width = 0;
  var height = 0;
  
  //get browser window width
  if (window.innerWidth) {
    width = window.innerWidth;
  } else {
    width = document.documentElement.clientWidth;
  }
  
  //get browser window height
  if (window.innerHeight) {
    height = window.innerHeight;
  } else {
    height = document.documentElement.clientHeight;
  }          
  return {width:width - 2, height:height - 2};        
}
      

JMap.util.getNodeValue = function(node, tagname, default_val) {
  var elems = node.getElementsByTagName(tagname);
  if (elems && elems[0] && elems[0].firstChild && elems[0].firstChild.nodeValue) {
    return elems[0].firstChild.nodeValue;
  }
  return default_val;
}   

JMap.util.clone = function(obj) {
	var c = {};
	for (var x in obj) {
		c[x] = obj[x];
	}
	return c;
}



 
JMap.util.pad = function(str, len, pad, dir) {
	var STR_PAD_LEFT = 1;
	var STR_PAD_RIGHT = 2;
	var STR_PAD_BOTH = 3; 
	if (typeof(len) == "undefined") { var len = 0; }
	if (typeof(pad) == "undefined") { var pad = ' '; }
	if (typeof(dir) == "undefined") { var dir = STR_PAD_RIGHT; }
 
	if (len + 1 >= str.length) {
 
		switch (dir){
 
			case STR_PAD_LEFT:
				str = Array(len + 1 - str.length).join(pad) + str;
			break;
 
			case STR_PAD_BOTH:
				var right = Math.ceil((padlen = len - str.length) / 2);
				var left = padlen - right;
				str = Array(left+1).join(pad) + str + Array(right+1).join(pad);
			break;
 
			default:
				str = str + Array(len + 1 - str.length).join(pad);
			break;
 
		} // switch
 
	}
 
	return str;
 
}