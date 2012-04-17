var module="ModelSelector";

describe('PRECONDITIONS', {

	'Ext should exist': function(){
		value_of(Ext).should_not_be_undefined();
		value_of(Ext).should_not_be(null);
	}
});
