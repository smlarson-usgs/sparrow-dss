<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; UTF-8"/>
    <title>Create Predefined Session</title>
    <link rel="icon" href="favicon.ico" />
  </head>
  <body>
		<h1>Create a new Predefined Session</h1>
		<form action="../sp_session" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="New Predefined Context">
				<div class="input-group">
					<label for="code">Unique Code</label>
					<input type="text" name="code" />
				</div>
				
				<div class="input-group">
					<label for="modelId">Model ID</label>
					<input type="text" name="modelId" />
				</div>
				
				<div class="input-group">
					<label for="type">Type</label>
					<p><input type="radio" name="type" value="UNLISTED" checked="checked"/> Unlisted</p>
					<p><input type="radio" name="type" value="FEATURED"/> Featured</p>
					<p><input type="radio" name="type" value="LISTED"/> Listed</p>
				</div>
				
				<div class="input-group">
					<label for="name">Name</label>
					<input type="text" name="name" />
				</div>
				
				<div class="input-group">
					<label for="description">Description</label>
					<textarea rows="5" cols="60" name="description" ></textarea>
				</div>
				
				<div class="input-group">
					<label for="group_name">Grouping Name</label>
					<input type="text" name="group_name" />
				</div>
				
				<div class="input-group">
					<label for="sort_order">Sort Order</label>
					<input type="text" name="sort_order" />
				</div>
				
				<div class="input-group">
					<label for="add_by">Your Name</label>
					<input type="text" name="add_by" />
				</div>
				<div class="input-group">
					<label for="add_contact_info">Contact Info</label>
					<input type="text" name="add_contact_info" />
				</div>
				<div class="input-group">
					<label for="add_note">Notes - Why is this Predefined Session needed?</label>
					<textarea rows="5" cols="60" name="add_note" ></textarea>
				</div>
				<div class="input-group">
					<label for="context_string">Context String (paste from a saved file)</label>
					<textarea rows="20" cols="60" name="context_string" ></textarea>
				</div>
				

				<input type="submit" name="submit" value="submit"/>

			</fieldset>
		</form>

	</body>
</html>