/*
 * Copyright © 2016-2018 The Thingsboard Authors
 * Modifications © 2017-2018 Hashmap, Inc
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
  /* eslint-disable */

import * as d3 from 'd3';

import './logViewer.css';
/*@ngInject*/
var mudLog = function(stackBarConfig, data, state, index, width) {
  'use strict';
  var o;
     // local;

  o = {
    value: null,
    key: null,
    data : data,
    state:state,
    index: index,
    width: width
  }; 

  // local = {
  // //  label: d3.local(),
  // //  dimensions: d3.local()
  // };
 
  function sBar(group) {
    // group-scope
    group.each(render);
  }
 
  //function render(data) {
  function render() {
  	var context;

    context = d3.select(this);

  	var initStackedBarChart = {
		draw: function(config) {
		var	me = this,
			domEle = config.element,
			stackKey = config.key,
			data = config.data,
			margin = {top: 20, right: 20, bottom: 30, left: 50},
			parseDate = d3.timeParse("%m/%Y"),
			width = o.width*110 - margin.left - margin.right,
			height = 2700 - margin.top - margin.bottom,
			xScale = d3.scaleLinear().rangeRound([0, width*2]),
			yScale = d3.scaleBand().rangeRound([height, 0]).padding(0.1),
			color = d3.scaleOrdinal(d3.schemeCategory20),
			xAxis = d3.axisBottom(xScale),
			yAxis =  d3.axisLeft(yScale),
			svg = context.select('.linearGrid')
				//	.attr("width", width + margin.left + margin.right)
				//	.attr("height", height + margin.top + margin.bottom)
					.append("g")
					.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

			var stack = d3.stack()
				.keys(stackKey)
				/*.order(d3.stackOrder)*/
				.offset(d3.stackOffsetNone);
		
			var layers= stack(data);
				//data.sort(function(a, b) { return b.total - a.total; });
			//	yScale.domain(data.map(function(d) { return parseDate(d.date); }));
				yScale.domain(data.map(function(d) {
				//console.log(d["depth"]);
				return d["depth"]; }));
			//	xScale.domain([0, d3.max(layers[layers.length - 1], function(d) { return d[0] + d[1]; }) ]).nice();
				xScale.domain([0, d3.max(layers[layers.length - 1], function(d) { return d[0] + d[1]; }) ]).nice();

			var layer = svg.selectAll(".layer")
				.data(layers)
				.enter().append("g")
				.attr("class", "layer")
				.style("fill", function(d, i) { return color(i); });

			  layer.selectAll("rect")
				  .data(function(d) { return d; })
				.enter().append("rect")
				  .attr("y", function(d) { //console.log(d);
				   return yScale(d.data["depth"]); })
				  .attr("x", function(d) { return xScale(d[0]); })
				  .attr("height", yScale.bandwidth())
				  .attr("width", function(d) { return xScale(d[1]) - xScale(d[0]) });

				svg.append("g")
				.attr("class", "axis axis--x")
				.attr("transform", "translate(0," + (height+5) + ")")
				.call(xAxis);

				svg.append("g")
				.attr("class", "axis axis--y")
				.attr("transform", "translate(0,0)")
				.call(yAxis);							
		}
	}
	var data = [{"date":"4/1854","total":8571,"disease":1,"wounds":0,"other":5},{"date":"5/1854","total":23333,"disease":12,"wounds":0,"other":9},{"date":"6/1854","total":28333,"disease":11,"wounds":0,"other":6},{"date":"7/1854","total":28772,"disease":359,"wounds":0,"other":23},{"date":"8/1854","total":30246,"disease":828,"wounds":1,"other":30},{"date":"9/1854","total":30290,"disease":788,"wounds":81,"other":70},{"date":"10/1854","total":30643,"disease":503,"wounds":132,"other":128},{"date":"11/1854","total":29736,"disease":844,"wounds":287,"other":106},{"date":"12/1854","total":32779,"disease":1725,"wounds":114,"other":131},{"date":"1/1855","total":32393,"disease":2761,"wounds":83,"other":324},{"date":"2/1855","total":30919,"disease":2120,"wounds":42,"other":361},{"date":"3/1855","total":30107,"disease":1205,"wounds":32,"other":172},{"date":"4/1855","total":32252,"disease":477,"wounds":48,"other":57},{"date":"5/1855","total":35473,"disease":508,"wounds":49,"other":37},{"date":"6/1855","total":38863,"disease":802,"wounds":209,"other":31},{"date":"7/1855","total":42647,"disease":382,"wounds":134,"other":33},{"date":"8/1855","total":44614,"disease":483,"wounds":164,"other":25},{"date":"9/1855","total":47751,"disease":189,"wounds":276,"other":20},{"date":"10/1855","total":46852,"disease":128,"wounds":53,"other":18},{"date":"11/1855","total":37853,"disease":178,"wounds":33,"other":32},{"date":"12/1855","total":43217,"disease":91,"wounds":18,"other":28},{"date":"1/1856","total":44212,"disease":42,"wounds":2,"other":48},{"date":"2/1856","total":43485,"disease":24,"wounds":0,"other":19},{"date":"3/1856","total":46140,"disease":15,"wounds":0,"other":35}];

	var dataArr1 = [];
	var key = [];
	//data = data2;
	//data.mudLogs.mudLog.geologyInterval.forEach(function(depthObj){
	data.mudLog[0].geologyInterval.forEach(function(depthObj){
		if(Array.isArray(depthObj.lithology)){
			depthObj.lithology.forEach(function(lith){
				if(key.indexOf(lith.codeLith) === -1) {
					    key.push(lith.codeLith);
					}
			})
		}
		else if(depthObj.lithology !== undefined){
			if(key.indexOf(depthObj.lithology.codeLith) === -1) {
			    key.push(depthObj.lithology.codeLith);
			}
		}
		else{
			///console.log(depthObj);
		}
	})
	//data.mudLogs.mudLog.geologyInterval.forEach(function(depthObj){
	data.mudLog[0].geologyInterval.forEach(function(depthObj){
		var tmp = {};
		key.forEach(function(key){
		tmp[key] = 0;
		})
		var bottom = depthObj.mdBottom["#text"];
		if(Array.isArray(depthObj.lithology)){
			depthObj.lithology.forEach(function(lith){
				var codeLith = lith.codeLith;
				var percent = parseInt(lith.lithPc["#text"]);
				tmp[codeLith] = percent;
			})
		}
		else{
			if(depthObj.lithology !== undefined){
				var codeLith = depthObj.lithology.codeLith;
				var percent = parseInt(depthObj.lithology.lithPc["#text"]);
				tmp[codeLith] = percent;
			}
		}
		if(depthObj.lithology !== undefined){
			tmp.depth = parseInt(bottom);
			dataArr1.push(tmp);
		}
	})

	dataArr1 = dataArr1.reverse();
	var key1 = ["wounds", "other", "disease"];
	if(o.state === "init"){
		initStackedBarChart.draw({
		data: dataArr1,
		key: key,
		element: 'stacked-bar'
	});
	}

  }
  sBar.order = 2;
  // set other methods here
 
  return sBar;
}
export {mudLog};
