/*
 * Copyright 2008 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @description
 * <p>
 * Class that represents a java.util.HashMap
 * </p>
 * @namespace YAHOO.hippo
 * @module hashmap
 */

/**
 * add method removeAt(index) to Array prototype
 */
function _removeAt( index )
{
  var part1 = this.slice( 0, index);
  var part2 = this.slice( index+1 );

  return( part1.concat( part2 ) );
}
Array.prototype.removeAt = _removeAt;

( function() {

    YAHOO.namespace('hippo');

    YAHOO.hippo.HashMap = function() {
        this.keys = [];
        this.values = [];
    };

    YAHOO.hippo.HashMap.prototype = {
        
    	put : function(key, value) {
	    	var index = this._getIndex(key);
	        if(index == -1) {
	            this.keys.push(key);
	            this.values.push(value);
	            return null;
	        } else {
	        	var previous = this.values[index];
	            this.values[index] = value;
	            return previous;
	        }
        },
        
        putAll : function(hashMap) {
        	var entries = hashMap.entrySet();
        	for(var e in  entries) {
        		var entry =entries[e];
        		this.put(entry.getKey(), entry.getValue());
        	}
        },
        
        get : function(key) {
        	var index = this._getIndex(key);

        	if(index == -1) {
        		//TODO: throw error or return null?
        		var msg = "No value found for key[" + key + "]";
        		YAHOO.log(msg, "error", "HashMap");
        		throw new Error(msg)
        	}
             return this.values[index];
        },
        
        remove : function(key) {
        	var index = this._getIndex(key);
        	if(index == -1) {
        		//TODO: throw error or return null
        		var msg = "Can not remove entry, key[" + key + "] not found";
        		YAHOO.log(msg, "error", "HashMap");
        		throw new Error(msg)
        	}
        	var value = this.values[index];
         	this.keys = this.keys.removeAt(index);
        	this.values = this.values.removeAt(index);
        	
        	return value;
        },

        containsKey : function(key) {
        	return this._getIndex(key) > -1;
        },
        
        size : function() {
        	return this.keys.length;
        },
        
        isEmpty : function() {
        	return this.keys.length == 0;
        },
        
        clear : function() {
        	this.keys = [];
        	this.values = [];
        },
        
        keySet : function() {
        	return this.keys;
        },
        
        valueSet : function() {
        	return this.values;
        },
        
        entrySet : function() {
        	var o = {};
        	for(var i=0; i<this.keys.length; i++) {
        		o['o' + i] = new YAHOO.hippo.HashMapEntry(this.keys[i], this.values[i]);
        	}
        	return o;
        },

        entrySetAsArray : function() {
        	var ar = [];
        	for(var i=0; i<this.keys.length; i++) {
        		ar.push(new YAHOO.hippo.HashMapEntry(this.keys[i], this.values[i]));
        	}
        	return ar;
        },
        
        _getIndex : function(key) {
			for (var i=0; i < this.keys.length; i++) {
			    if( this.keys[ i ] == key ) {
			        return i;
			    }
			}
			return -1;
        },
        
        toString : function() {
    		var x = '';
    		for(var i=0; i<this.keys.length; i++) {
    			if (i>0) x += ', ';
    			var value = this.values[this.keys[i]];
    			x +='{' + this.keys[i] + '=' + value + '}'; 
    		}
            return 'HashMap[' + x + ']';
        }
    };
    
    YAHOO.hippo.HashMapEntry = function(key, value) {
        this._key = key;
        this._value = value;
    };
    
    YAHOO.hippo.HashMapEntry.prototype = {
    	getKey : function() {
    		return this._key;
    	},
    	
    	getValue : function() {
    		return this._value;
    	}
    };

})();

YAHOO.register("hashmap", YAHOO.hippo.HashMap, {
    version :"2.6.0",
    build :"1321"
});

/*
        //test for hashmap
        var title = function(str) {
        	var len = 80;
        	var pre = '--------';
        	var suf = ' ';
        	for(var i=0; i<len - (pre.length + str.length); i++) {
        		suf += '-';
        	}
        	console.log(pre + ' ' + str + ' ' + suf);
        }
        var endline = '\n';
        var testFunction = function(func, numberOfRuns) {
            var testAr = [];
            for(var i=0; i<numberOfRuns; i++) {
    	        var start = new Date().getTime();
    	        var x = func();
    	        var stop = new Date().getTime();
    	        testAr.push(stop-start);
            }
            var average = 0;
            while(testAr.length > 0) {
            	average += testAr.pop();
            }
            return average/numberOfRuns;
        }

        title('Initial state test');
        var hm = new YAHOO.hippo.HashMap();
        console.log('Length = ' + hm.size() + ', Empty = ' + hm.isEmpty());
        console.log(endline);
        
        title('Add one value test')
        hm = new YAHOO.hippo.HashMap();
        var p = hm.put('key1', 'value1');
        console.log('Added key1=value1');
        console.log('Previous value of key1=' + p);
        console.log('ContainsKey[key1] = ' + hm.containsKey('key1'));
        console.log('Length = ' + hm.size() + ', Empty = ' + hm.isEmpty());
        console.log(endline);
        
        title('Clear after add values test');
        hm = new YAHOO.hippo.HashMap();
        var p = hm.put('key1', 'value1');
        var p = hm.put('key2', 'value2');
        console.log('Added key1=value1 and key2=value2');
        console.log('Length = ' + hm.size());
        console.log('Empty = ' + hm.isEmpty());
        hm.clear();
        console.log('Cleared')
        console.log('ContainsKey[key1] = ' + hm.containsKey('key1'));
        console.log('ContainsKey[key2] = ' + hm.containsKey('key2'));
        console.log('Length = ' + hm.size() + ', Empty = ' + hm.isEmpty());
        console.log(endline);

        title('Replace value test');
        hm = new YAHOO.hippo.HashMap();
        var p = hm.put('key1', 'value1');
        console.log('Added key1=value1');
        p = hm.put('key1', 'value1-1');
        console.log('Added key1=value1-1');
        console.log('Previous value at key1=' + p);
        console.log('Current value of key1=' + hm.get('key1'));
        console.log('ContainsKey[key1] = ' + hm.containsKey('key1'));
        console.log('Length = ' + hm.size() + ', Empty = ' + hm.isEmpty());
        console.log(endline);

		//TODO: write putAll test

        title('Remove keys test');
        hm = new YAHOO.hippo.HashMap();
        var p = hm.put('key1', 'value1');
        console.log('Added key1=value1');
        p = hm.remove('key1');
        console.log('Removed key1, value was ' + p);
        console.log('ContainsKey[key1] = ' + hm.containsKey('key1'));
        console.log('Length = ' + hm.size() + ', Empty = ' + hm.isEmpty());
        
        hm.put('key1', 'value1');
        hm.put('key2', 'value2');
        console.log('Added key1=value1 and key2=value2');
        console.log('ContainsKey[key1] = ' + hm.containsKey('key1'));
        console.log('ContainsKey[key2] = ' + hm.containsKey('key2'));
        console.log('Length = ' + hm.size() + ', Empty = ' + hm.isEmpty());
        var x1 = hm.remove('key1');
        var x2 = hm.remove('key2');
        console.log('removed key1 and key2, values were ' + x1 + ' and ' + x2);
        console.log('ContainsKey[key1] = ' + hm.containsKey('key1'));
        console.log('ContainsKey[key2] = ' + hm.containsKey('key2'));
        console.log('Length = ' + hm.size() + ', Empty = ' + hm.isEmpty());
        console.log(endline);
        
        title('Entryset test');
        var hm = new YAHOO.hippo.HashMap();
        hm.put('key1', 'value1');
        hm.put('key2', 'value2');
        hm.put('key3', 'value3');
        
        var testSet = hm.entrySet();
        var testCount = 0;
        for(var i in testSet) {
            ++testCount;
            var entry = testSet[i];
        	console.log('Found entry: key=' + entry.getKey() + ', value=' + entry.getValue());
        	if(entry.getKey() != ('key' + testCount) || entry.getValue() != ('value' + testCount)) {
        		console.log('Entry[' + entry.getKey() + ', '  + entry.getValue() + '] at index ' + testCount + ' should have [' + 'key'  + testCount + ',value' + testCount + ']');
        		break;
        	}
        }

        var numberOfMapEntries= 150;
        var numberOfTestRuns = 10;

        title('EntrySet performance test with ' + numberOfMapEntries + ' map entries');

        var hm = new YAHOO.hippo.HashMap();
        for(var i=0; i<numberOfMapEntries; i++) {
        	hm.put('key' + i, 'val' + i);
        }
        
        //first call seems very slow
        var testFunc = function() {
        	hm.entrySet();	
        }
        var average = testFunction(testFunc, 1);
        console.log('Initial call: ' + average + 'ms');
        
        average = testFunction(testFunc, numberOfTestRuns);
        console.log('EntrySet average on '  + numberOfTestRuns + ' runs: ' + average + 'ms');
        
        testFunc = function() {
        	hm.entrySetAsArray();	
        }
        average = testFunction(testFunc, numberOfTestRuns);
        console.log('EntrySetAsArray average on '  + numberOfTestRuns + ' runs: ' + average + 'ms');
        console.log(endline);
        
        var entrySet = hm.entrySet();
        for(var x in entrySet) {
        	var entry = entrySet[x];
        	console.log('Found entry: key=' + entry.getKey()+ ', value=' + entry.getValue());
        }

        title('Entryset as array test');
        entrySet = hm.entrySetAsArray();
        for(var i=0; i<entrySet.length; i++) {
        	var entry = entrySet[i];
        	console.log('Found entry: key=' + entry.getKey()+ ', value=' + entry.getValue());
        }
*/