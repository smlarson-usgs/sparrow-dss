<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
	<head>
    	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    	<title>ID By Point Test</title>
      <link rel="icon" href="../favicon.ico" />
	</head>
	<body>
		<p>
			<strong>Probably fails because prediction context needed </strong>
		</p>
		<h3>Requests the 7 closest reaches to lat/long 40/-100 in model 22</h3>
		<ul>
			<li>xml:
				<a href="../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7">../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7</a>
			</li>
			<li>excel:
				<a href="../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=excel">../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=excel</a>
			</li>
			<li>csv:
				<a href="../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=csv">../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=csv</a>
			</li>
			<li>tab:
				<a href="../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=tab">../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=tab</a>
			</li>
			<li>json:
				<a href="../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=json">../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=json</a>
			</li>

		</ul>

		<h3>Same as above, but there is an internal limit to 100 reaches, so the 200 will be ignored</h3>
		<ul>
			<li>xml:
				<a href="../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200">../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200</a>
			</li>
			<li>excel:
				<a href="../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&mimetype=excel">../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&amp;mimetype=excel</a>
			</li>
			<li>csv:
				<a href="../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&mimetype=csv">../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&amp;mimetype=csv</a>
			</li>
			<li>tab:
				<a href="../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&mimetype=tab">../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&amp;mimetype=tab</a>
			</li>
			<li>json:
				<a href="../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&mimetype=json">../sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&amp;mimetype=json</a>
			</li>

		</ul>

		<p>
			<strong>The Search for nearest reaches is only performed in a 4 deg. by 4 deg. box
				around the point, so a request like this will probably return zero points.</strong>
		</p>
		<ul>
			<li>xml:
				<a href="../sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7">../sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7</a>
			</li>
			<li>excel:
				<a href="../sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&mimetype=excel">../sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&amp;mimetype=excel</a>
			</li>
			<li>csv:
				<a href="../sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&mimetype=csv">../sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&amp;mimetype=csv</a>
			</li>
			<li>tab:
				<a href="../sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&mimetype=tab">../sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&amp;mimetype=tab</a>
			</li>
			<li>json:
				<a href="../sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&mimetype=json">../sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&amp;mimetype=json</a>
			</li>
		</ul>

	</body>
</html>