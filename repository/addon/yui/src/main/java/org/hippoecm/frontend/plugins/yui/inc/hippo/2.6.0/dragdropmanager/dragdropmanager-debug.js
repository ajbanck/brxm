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
 * Todo
 * </p>
 * @namespace YAHOO.hippo
 * @requires dragdropmodel, functionqueue, hashmap
 * @module drapdropmanager
 */

YAHOO.namespace('hippo');

if (!YAHOO.hippo.DragDropManager) {
    ( function() {
        var Dom = YAHOO.util.Dom, Lang = YAHOO.lang;
        
        YAHOO.hippo.DragDropManagerImpl = function() {
        };
        
        YAHOO.hippo.DragDropManagerImpl.prototype = {
        		
			loader :new YAHOO.hippo.FunctionQueue('DragDropQueue'),
			draggables : new YAHOO.hippo.HashMap(),
			droppables : new YAHOO.hippo.HashMap(),
			
			onLoad : function() {
			    this.cleanup();
				this.loader.handleQueue();
			},
			
			cleanup: function() {
				//remove old drag or drop components from dom and maps
			},
			
			/**
			 * TODO: Maybe the draggables and droppables can be merged?
			 */
			getModel : function(id) {
				if(this.draggables.containsKey(id)) {
					return this.draggables.get(id);
				} else if (this.droppables.containsKey(id)) {
					return this.droppables.get(id);
				}
				return null;
			},
			
			addDraggable : function(id, modelClass, config) {
				var clazz = this.getClass(modelClass, YAHOO.hippo.DDModel);
				YAHOO.log("Add draggable component[" + id + "] of type " + clazz, "info", "DragDropManager");
				this._add(id, clazz, config, this.draggables);
			},

			addDroppable : function(id, modelClass, config) {
				var clazz = this.getClass(modelClass, YAHOO.util.DDTarget);
				YAHOO.log("Add droppable component[" + id + "] of type " + clazz, "info", "DragDropManager");
				this._add(id, clazz, config, this.droppables);
			},
			
			_add: function(id, clazz, config, map) {
				var me = this;
				var func = function() {
					var c = null;
					
					if (Lang.isArray(config.groups)) {
						c = new clazz(id, config.groups.shift(), config);
					} else {
						c = new clazz(id, null, config);
					}
					map.put(id, c);
				};
				this.loader.registerFunction(func);
			},
			
			getClass: function(clazz, defaultClazz) {
				if (!Lang.isUndefined(clazz) && !Lang.isNull(clazz)) { //TODO: test for isObject?
					return clazz;
				}
				return defaultClazz;
			}
        };
    })();

    YAHOO.hippo.DragDropManager = new YAHOO.hippo.DragDropManagerImpl();
    YAHOO.register("dragdropmanager", YAHOO.hippo.DragDropManager, {
        version :"2.6.0",
        build :"1321"
    });
}