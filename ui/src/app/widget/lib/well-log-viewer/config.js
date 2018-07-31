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
 /*@ngInject*/
var loadConfig = function() {
	var config = {
		"tracks": [{
			"trackId": 1,
			"components": [				
				{
					"type": {
						"type": "line",
						"color": "blue",
						"lineWeight": 2,
						"headerMin": 10,
						"headerMax": 100,
						"headerName": "Tag Name",
						"areaFill" : {
							"enable": false,
							"fill": "left",
							"color": "green",
							"opacity": 0.2
						}
					},
					"hasHeader": true,
					"datasource": ""
				},{
					"type": {
						"type": "line",
						"color": "gray",
						"lineWeight": 2,
						"headerMin": 10,
						"headerMax": 200,
						"headerName": "Tag Gray",
						"areaFill" : {
							"enable": false,
							"fill": "right",
							"color": "gray",
							"opacity": 0.5
						}
					},
					"hasHeader": true,
					"datasource": ""
				},{
					"type": {
						"type": "linearGrid",
						"minorLines": {
							"lines": 5,
							"lineWeight": 1,
							"style": "dashed"
						},
						"majorLines": {
							"lines": 5,
							"lineWeight": 1.5
						}
					},
					"hasHeader": false,
					"datasource": ""
				}

			]
		},{			
			"trackId": 2,
			"components": [				
				{
					"type": {
						"type": "timeYaxis",
						"showMessage": "true"
					},
					"hasHeader": false,
					"datasource": ""
				}

			]

		},{			
			"trackId": 3,
			"components": [				
				{
					"type": {
						"type": "line",
						"color": "blue",
						"lineWeight": 2,
						"headerMin": 10,
						"headerMax": 200,
						"headerName": "Tag Name",
						"areaFill" : {
							"enable": false,
							"fill": "left",
							"color": "red",
							"opacity": 0.5
						}
					},
					"hasHeader": true,
					"datasource": ""
				},{
					"type": {
						"type": "linearGrid",
						"minorLines": {
							"lines": 5,
							"lineWeight": 1,
							"style": "dashed"
						},
						"majorLines": {
							"lines": 5,
							"lineWeight": 2
						},
					},
					"hasHeader": false,
					"datasource": ""
				}

			]

		}]
	}
	return config;
}

export {loadConfig};
