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
