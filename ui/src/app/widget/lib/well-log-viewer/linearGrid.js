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
 //var d3 = require('./node_modules/d3/build/d3');
//require('./node_modules/d3-selection-multi/build/d3-selection-multi');
//import * as d3 from 'well-log-viewer/node_modules/d3/build/d3';
import * as d3 from 'd3';

import './logViewer.css';
/*@ngInject*/
var linearGrid = function(lineConfig, data, state, index, width) {
  'use strict';
  var o;
     // local;

  o = {
    wodth: 200,
    value: null,
    key: null,
    majorLines: parseInt(lineConfig.majorLines.lines) -1,
    minorLines: parseInt(lineConfig.minorLines.lines) +1,
    majorColor: lineConfig.majorLines.color,
    minorColor: lineConfig.minorLines.color,
    majorLineWeight: lineConfig.majorLines.lineWeight,
    minorLineWeight: lineConfig.minorLines.lineWeight,
    data : data,
    state:state,
    index: index,
    width: width
  }; 

  if(angular.isUndefined(o.width)){
    o.width = 3;
  } 
  if(lineConfig.minorLines.style === "dashed"){
    o.mirorStroke = "6,4";
  }
  else if(lineConfig.minorLines.style === "solid"){
    o.mirorStroke = "6,0";
  }

  // local = {
  // //  label: d3.local(),
  // //  dimensions: d3.local()
  // };
 
  function lGrid(group) {
    // group-scope
    group.each(render);
  }
 
  //function render(data) {
  function render() {
    var context,
      //  dim,
        ticks = [];

    context = d3.select(this);

    var margin = {top: 0, right: 10, bottom: 20, left: 10},
      width = o.width*110 - margin.right - margin.left,
      height = 700 - margin.top - margin.bottom;

     // var dx = width - margin.right - margin.left;

    var isMajorTick = function (index) {
      return ticks[index].isVisible;
    }
    
    for (var i = 0; i <= o.minorLines*o.majorLines; i++) {
      ticks.push( { value: i, isVisible: i % o.minorLines === 0 });
    }

    var tickValues = ticks.map( function(t) { return t.value; });

    var xScale = d3.scaleLinear()
        .domain([tickValues[0], tickValues[tickValues.length - 1]])
        .range([margin.left, width + margin.left]);

    var xAxis = d3.axisBottom(xScale)
        .tickSizeInner(-height)
        .tickSizeOuter(0)
        .tickValues(tickValues)
        .tickFormat(function (d, i) {
          return isMajorTick(i) ? "" : "";
        });

    var y = d3.scaleLinear()
        .domain(d3.extent(o.data.data, function(d) { return d[0]; }))
        .range([0, height]);

    var yAxis = d3.axisLeft()
        .scale(y)
        .tickFormat(function(d){ return d.x;})
        .ticks(10)
        .tickSize(-width);

    if(o.state === 'init'){
      context.select('.linearGrid')
          .attr('width', width + margin.right + margin.left)
          .attr('height', height + margin.top + margin.bottom)



        var x_axis =  context.select('.linearGrid').append("g")
          .attr("id", "x_axis")
          .attr("class", "x axis")
          .style("stroke-width", o.majorLineWeight)
          .attr("transform", "translate(0," +  height +")")
          .call(xAxis)
        // Add the class 'minor' to all minor ticks
        x_axis.selectAll("g")
          .filter(function (d, i) {
            return !isMajorTick(i);
          })
          .classed("minor", true)
          .style("stroke-width", o.minorLineWeight)
          .style("stroke-dasharray", o.mirorStroke)
        // console.log("all the visible points", xAxis.scale().ticks(xAxis.ticks()[0]));

      context.select('.linearGrid')
      .append("g")
            .attr("class", "axis")
            .attr("id", "moving-axis")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
            .call(yAxis);
    }
    if(o.state === 'update'){

        x_axis =  context.select('.linearGrid').select(".x axis")
          .attr("id", "x_axis")
          .style("stroke-width", o.majorLineWeight)
          .attr("transform", "translate(0," +  height +")")
          .call(xAxis)
        // Add the class 'minor' to all minor ticks
        x_axis.selectAll("g")
          .filter(function (d, i) {
            return !isMajorTick(i);
          })
          .classed("minor", true)
          .style("stroke-width", o.minorLineWeight)
          .style("stroke-dasharray", o.mirorStroke)
        // console.log("all the visible points", xAxis.scale().ticks(xAxis.ticks()[0]));

        context.select('.linearGrid')
         .select("#moving-axis")
         .call(yAxis);
    }

  }
  
  lGrid.width = function(){/*...*/}
  lGrid.order = 1;
  // set other methods here
 
  return lGrid;
};
export {linearGrid};