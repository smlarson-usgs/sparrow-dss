/* Taken from www.braemoor.co.uk/software  - by John Gardner - 2003-2005 */

/*
Write a session cookie w/o specifying a path, which will write relative to the
current url.

Distinct from writeSessionCookie, which specifies the '/' path.

    Parameters:
        cookieName        Cookie name
        cookieValue       Cookie Value
*/
function writeLocalSessionCookie (cookieName, cookieValue) {
    document.cookie = escape(cookieName) + "=" + escape(cookieValue);
}

/*
Routine to write a session cookie

    Parameters:
        cookieName        Cookie name
        cookieValue       Cookie Value
    
*/

function writeSessionCookie (cookieName, cookieValue) {
    document.cookie = escape(cookieName) + "=" + escape(cookieValue) + "; path=/";
}

/*
Routine to get the current value of a cookie

    Parameters:
        cookieName        Cookie name
    
    Return value:
        false             Failed - no such cookie
        value             Value of the retrieved cookie
*/
function getCookieValue (cookieName) {
  var exp = new RegExp (escape(cookieName) + "=([^;]+)");
  if (exp.test (document.cookie + ";")) {
    exp.exec (document.cookie + ";");
    return unescape(RegExp.$1);
  }
  else return false;
}

/*
Routine to see if session cookies are enabled

    Parameters:
        None
    
    Return value:
        true              Session cookies are enabled
        false             Session cookies are not enabled
*/
function testSessionCookie () {
  document.cookie ="testSessionCookie=Enabled";
  if (getCookieValue ("testSessionCookie")=="Enabled")
    return true;
  else
    return false;
}

/*
Routine to see of persistent cookies are allowed:

    Parameters:
        None
    
    Return value:
        true              Session cookies are enabled
        false             Session cookies are not enabled
*/
function testPersistentCookie () {
  writePersistentCookie ("testPersistentCookie", "Enabled", "minutes", 1);
  if (getCookieValue ("testPersistentCookie")=="Enabled")
    return true;
  else 
    return false;
}

/*
Routine to write a persistent cookie

    Parameters:
        CookieName        Cookie name
        CookieValue       Cookie Value
        periodType        "years","months","days","hours", "minutes"
        offset            Number of units specified in periodType
    
    Return value:
        true              Persistent cookie written successfullly
        false             Failed - persistent cookies are not enabled
    
    e.g. writePersistentCookie ("Session", id, "years", 1);
*/       
function writePersistentCookie (CookieName, CookieValue, periodType, offset) {

  var expireDate = new Date ();
  offset = offset / 1;
  
  var myPeriodType = periodType;
  switch (myPeriodType.toLowerCase()) {
    case "years": 
     var year = expireDate.getYear();     
     // Note some browsers give only the years since 1900, and some since 0.
     if (year < 1000) year = year + 1900;     
     expireDate.setYear(year + offset);
     break;
    case "months":
      expireDate.setMonth(expireDate.getMonth() + offset);
      break;
    case "days":
      expireDate.setDate(expireDate.getDate() + offset);
      break;
    case "hours":
      expireDate.setHours(expireDate.getHours() + offset);
      break;
    case "minutes":
      expireDate.setMinutes(expireDate.getMinutes() + offset);
      break;
    default:
      alert ("Invalid periodType parameter for writePersistentCookie()");
      break;
  } 
  
  document.cookie = escape(CookieName ) + "=" + escape(CookieValue) + "; expires=" + expireDate.toGMTString() + "; path=/";
}  

/*
Routine to delete a persistent cookie

    Parameters:
        CookieName        Cookie name
    
    Return value:
        true              Persistent cookie marked for deletion
    
    e.g. deleteCookie ("Session");
*/    
function deleteCookie (cookieName) {

  if (getCookieValue (cookieName)) writePersistentCookie (cookieName,"Pending delete","years", -1);  
  return true;     
}
