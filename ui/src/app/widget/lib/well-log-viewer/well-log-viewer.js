/*
 * Copyright © 2017-2018 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import loadLogViewer from './logViewer';
//import loadLogViewer from 'WellLogViewer/logViewer';
//import welllogviwer from 'WellLogViewer'
//import * as welllogviwer from 'WellLogViewer'

/* eslint-disable angular/angularelement */
/* eslint-disable */
export default class WellLogViewer {
     constructor(ctx) {
     	this.ctx = ctx;
          //  var settings = ctx.settings;
            this.init();
     }
     init() {
		//loadLogViewer();
        //alert(test);
        //alert("test.test");
     }

     test(){
     	var datasources = this.ctx.datasources;
        console.log(settings);
        var dataKeys = [];
        datasources.forEach(function(dkeys){
        	dkeys.dataKeys.forEach(function(datakey){

        		dataKeys.push({"value": datakey.label, "label": datakey.label});
        	})
        })
     	return dataKeys;
     }

     update() {
     	loadLogViewer(this.ctx);
     	console.log(this.ctx);
     }

     static settingsSchema(ctx){
		return {
			"schema":{
			    "type": "object",
			    "properties": {
			      "Track": {
			        "type": "array",
			        "items": {
			              "title": "Settings",
			              "type": "object",
			              "properties":{
			                  "component":{
			                      "title":"Component",
			                      "type": "array",
			                      "items": {
			                          "title": "Settings",
			                          "type": "object",
			                          "properties":{
			                              "ComponentType":{
			                                  "title":"Type",
			                                  "type": "string",
			                                  "enum": ["yy","tt"]
			                              },
			                              "lineWidth":{
			                                  "title":"Line Width",
			                                  "type": "string"
			                              }
			                            }       
			                      }
			                  },
			                  "test":{
			                      "title": "tttt",
			                      "type": "string"
			                  },
			                  "test1":{
			                      "title": "yyyy",
			                      "type": "string"
			                  },
			                  "test2":{
			                  	"title": "Datasource",
								"type": "string"
								}
			                }       
			          
			        }
			      }
			    }
			  },
		"form":[{
	      "key": "Track",
	      "items": [
	           {
	              "key": "Track[].component",
	              "items": [
	                "Track[].component[].ComponentType",
	                {
	                  "key": "Track[].component[].lineWidth",
	                  "condition": "model.Track[form.key[1]].component[form.arrayIndex].ComponentType === 'one'"
	                }
	              ]
	            },
	           "Track[].test1",
	           {
                    "key": "Track[].test2",
                    "type": "rc-select",
                    "multiple": false,
                    "items": ["one", "two"]
                }
	      ]
	}]


		};
	}
}

/* eslint-enable angular/angularelement */
