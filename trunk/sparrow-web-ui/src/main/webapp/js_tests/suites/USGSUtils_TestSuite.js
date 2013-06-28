describe('PRECONDITIONS', {
	'Ext should exist': function() {
		value_of(Ext).should_not_be_undefined();
		value_of(Ext).should_not_be(null);
	},

	'USGS Utils should exist': function(){
		value_of(USGS).should_not_be_undefined();
		value_of(USGS).should_not_be(null);
	},

	'LOG should exist': function(){
		value_of(Sparrow.LOG).should_not_be_undefined();
		value_of(Sparrow.LOG).should_not_be(null);
	}

});

describe('USGS Utils', {
	'before': function() {

	},

	'reverse() reverses the order of an array': function() {
		var result = USGS.reverse(['A','B']);
		value_of(result).should_be(['B','A']);
	},

	'*Array.concat() concatenates two arrays': function() {
		var base = ['A','B'];
		value_of(base.concat([2,3,4])).should_be(['A', 'B', 2, 3,4]);
	},

	'*findFirst() finds the first item with the matching attribute value': function() {
		var paul ={name: 'Paul'};
		var beatles=[{name: 'John'}, {name: 'Paul'}, {name: 'George'}, {name: 'Ringo'}];
		value_of(USGS.findFirst(beatles, "name", "Paul")).should_be(paul);
	},

	'findFirst() returns null if not found': function() {
		var beatles=[{name: 'John'}, {name: 'Paul'}, {name: 'George'}, {name: 'Ringo'}];
		value_of(USGS.findFirst(beatles, "name", "Mick")).should_be(null);
	},

	'*findAll() finds all the items with the the matching attribute value': function() {
		var john ={name: 'John'};
		var beatles=[{name: 'John'}, {name: 'Paul'}, {name: 'George'}, {name: 'Ringo'}, {name: 'John', isClone: true}];
		value_of(USGS.findAll(beatles, "name", "John")).should_have(2, "items");
	},

	'findAll() returns empty array if not found': function() {
		var beatles=[{name: 'John'}, {name: 'Paul'}, {name: 'George'}, {name: 'Ringo'}, {name: 'John', isClone: true}];
		value_of(USGS.findAll(beatles, "name", "Mick")).should_have(0, "items");
	},

	'removeFirst() returns excised member': function() {
		var beatles=[{name: 'John'}, {name: 'Paul'}, {name: 'George'}, {name: 'Ringo'}, {name: 'John', isClone: true}];
		var result = USGS.removeFirst(beatles, "name", "Ringo");
		value_of(result).should_have(1, "items");
		value_of(result[0]).should_be({name: 'Ringo'});
	},

	'getURLParam() returns model=50': function() {
		var result = USGS.getURLParam("model", "http://localhost:8088/sparrow/map.jsp?model=50");
		value_of(result).should_be("50");
	},

	'insertArrayAt()': function() {
		// Test can insert in the middle
		var result = USGS.insertArrayAt([1,2,3,4,5],['A','B'],2);
		value_of(result).should_be([1, 2, "A", "B", 3, 4, 5]);

		// Test can insert at beginning
		result = USGS.insertArrayAt([1,2,3,4,5],['A','B'],0);
		value_of(result).should_be(["A", "B", 1, 2, 3, 4, 5]);
	},


});


describe('COLORS', {
	'before': function() {

	},

	'decomposeRGB(AABBCC) returns {}': function() {
		var result = COLORS.decomposeRGB("AABBCC");
		value_of(result.R).should_be("AA");
		value_of(result.G).should_be("BB");
		value_of(result.B).should_be("CC");
	},

	'interpolateRGB() returns midpoint and thirds': function() {
		var result = COLORS.interpolateRGB('FF6644','008899',2);
		value_of(result).should_be(["aa7261", "557e7e"]);

		result = COLORS.interpolateRGB('FF6644','008899',1);
		value_of(result).should_be(["80776f"]);
	},

	'interpolateRGB() pads when interpolate result is single digit': function() {
		var result = COLORS.interpolateRGB('FF0000','000812',2);
		value_of(result).should_be(["aa7261", "557e7e"]); // incorrect result. try later
		// TODO verify that interp
	},

	'interpolateColors() extends the base color array': function() {
		var base = COLORS.POSITIVE_HUES[4];
		var result = COLORS.interpolateColors(base, 5);
		value_of(result).should_be(["33CC00", "FFFF66", "FF6633", "ff331a", "FF0000"]);
		value_of(base.length).should_be(4); // verifies that the base array has not been altered

		result = COLORS.interpolateColors(base, 6);
		value_of(result).should_be(["33CC00", "FFFF66", "ffb34d", "FF6633", "ff331a", "FF0000"]);

		result = COLORS.interpolateColors(base, 7);
		value_of(result).should_be(["33CC00", "99e633", "FFFF66", "ffb34d", "FF6633", "ff331a", "FF0000"]);

		result = COLORS.interpolateColors(base, 8);
		value_of(result).should_be(["33CC00", "99e633", "FFFF66", "ffb34d", "FF6633", "ff4422", "ff2211", "FF0000"]);

		result = COLORS.interpolateColors(base, 9);
		value_of(result).should_be(["33CC00", "99e633", "FFFF66", "ffcc55", "ff9944", "FF6633", "ff4422", "ff2211", "FF0000"]);

		result = COLORS.interpolateColors(base, 10);
		value_of(result).should_be(["33CC00", "77dd22", "bbee44", "FFFF66", "ffcc55", "ff9944", "FF6633", "ff4422", "ff2211", "FF0000"]);

		result = COLORS.interpolateColors(base, 11);
		value_of(result).should_be(["33CC00", "77dd22", "bbee44", "FFFF66", "ffcc55", "ff9944", "FF6633", "ff4d27", "ff341b", "ff1bf", "FF0000"]);

		result = COLORS.interpolateColors(base, 12);
		value_of(result).should_be(["33CC00", "77dd22", "bbee44", "FFFF66", "ffd95a", "ffb34e", "ff8d42", "FF6633", "ff4d27", "ff341b", "ff1bf", "FF0000"]);

		result = COLORS.interpolateColors(base, 13);
		value_of(result).should_be(["33CC00", "66d91a", "99e634", "ccf34e", "FFFF66", "ffd95a", "ffb34e", "ff8d42", "FF6633", "ff4d27", "ff341b", "ff1bf", "FF0000"]);

		result = COLORS.interpolateColors(base, 14);
		value_of(result).should_be(["33CC00", "66d91a", "99e634", "ccf34e", "FFFF66", "ffd95a", "ffb34e", "ff8d42", "FF6633", "ff5229", "ff3e1f", "ff2a15", "ff16b", "FF0000"]);
	}

});