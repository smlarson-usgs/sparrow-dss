/* 
 * Dumping ground for sparrow utils.
 */
Sparrow.utils.getFirstXmlElementValue = function(xmlText, tagName) {
	var value = xmlText.getElementsByTagName(tagName)[0].firstChild ? xmlText.getElementsByTagName(tagName)[0].firstChild.nodeValue : '';
	return value;
};


