<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; UTF-8"/>
    <title>Request Test</title>
    <link rel="icon" href="favicon.ico" />
  </head>
  <body>

		<form action="../sp_session" method="PUT" enctype="application/x-www-form-urlencoded">
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
					<input type="radio" name="type" value="UNLISTED" checked="checked"/>
					<input type="radio" name="type" value="FEATURED"/>
					<input type="radio" name="type" value="LISTED"/>
				</div>
				
				<div class="input-group">
					<label for="name">Name</label>
					<input type="text" name="name" />
				</div>
				
				<div class="input-group">
					<label for="description">Description</label>
					<textarea name="description" ></textarea>
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
					<input type="text" name="add_note" />
				</div>
				<div class="input-group">
					<label for="context_string">Context String (paste from a saved file)</label>
					<textarea name="context_string" ></textarea>
				</div>
				

				<input type="submit" name="submit" value="submit"/>

			</fieldset>
		</form>

	</body>
</html>