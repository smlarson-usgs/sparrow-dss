// Make the standard JSSpec describe available while we override the functionality
var JSSDescribe = describe;
var module = module || false;

var TestSuite = new (function(){
	var suites = [];
	var currentSuite = null;

	this.newSuite = function(suiteName){
		currentSuite = [];
		currentSuite.name = suiteName;
		suites.push(currentSuite);
	};

	this.add = function(name, tests, moduleName){
		currentSuite.push({
			name: name,
			tests: tests,
			module: moduleName
		});
	};

	this.run = function(){
		// identify the repeated Names
		var nameSet = {}, repeatedNameSet = {};
		for (var i=0; i<suites.length; i++){
			var suite = suites[i];
			var name = suite.name;
			console.log("analyzing " + name);
			if (nameSet[name]){
				console.log("repeating " + name + "...");
				repeatedNameSet[name] = true;
			} else {
				console.log("first appearance of " + name + "...");
				nameSet[name] = true;
			}
		}


		console.log("suites.length =" + suites.length);
		var output=[];
		Ext.iterate(nameSet, function(key, value){
				output.push(key);
			}
		);
		console.log("nameSet: " + output.join(","));
		console.log("nameSet: " + nameSet);
		console.log("repeatedNameSet: " + repeatedNameSet);

		// output the tests
		for (var i=0; i<suites.length; i++){
			var suite = suites[i];
			var name = suite.name;
			var moduleName = suite.module;
			console.log("describing " + name);
			for (var j=0; j<suite.length; j++){
				var test = suite[j];
				if (repeatedNameSet[name] && moduleName){
					JSSDescribe(test.name + " (" + moduleName + ")", test.tests);
				} else {
					JSSDescribe(test.name, test.tests);
				}
			}
		}
	};

})();

describe = function(name, tests){
	TestSuite.newSuite();
	console.log("adding suite " + name + " : " + module);
	TestSuite.add(name, tests);
}


