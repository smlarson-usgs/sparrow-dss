describe('PRECONDITIONS', {
	'Ext should exist': function() {
		value_of(Ext).should_not_be_undefined();
		value_of(Ext).should_not_be(null);
	},

	'USGS Utils should exist': function(){
		value_of(USGS).should_not_be_undefined();
		value_of(USGS).should_not_be(null);
	}

});

describe('Sparrow_ui', {
	'before': function() {

	},

});