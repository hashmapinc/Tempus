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
 import * as d3 from 'd3';

//import * as d3 from 'well-log-viewer/node_modules/d3/build/d3';
import './logViewer.css';

/*@ngInject*/

var headerLegend = function(lineConfig, state, index, width) {
  'use strict';
  var o = {
    value: null,
    key: null,
    min: lineConfig.headerMin,
    max: lineConfig.headerMax,
    lineWeight: lineConfig.lineWeight,
    color: lineConfig.color,
    label: lineConfig.headerName,
    state:state,
    index: index,
    width: width
  };  

  if(angular.isUndefined(o.width)){
    o.width = 3;
  } 

  function header(group) {
    // group-scope
    if(o.state === 'init'){
      group.each(render);
    }  
  }
 
  function render() {
    var context;

    context = d3.select(this);

    var margin = {top: 30, right: 10, bottom: 30, left: 10},
      width = o.width*140 - margin.right - margin.left;
      //height = 60 - margin.top - margin.bottom;

    var x = d3.scaleLinear()
        .domain([o.min, o.max])
        .range([0, width])

    var xAxis = d3.axisTop()
        .scale(x)
        .tickValues([o.min, o.max])
        .tickSize(5)

    context.select(".header")
         .append("svg")
         .attr("class", "header"+ index)
        .attr("width", width + margin.right + margin.left)
        .attr("height","80")
      .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
          .call(xAxis)
          .style("fill", "none")
          .style("stroke", o.color)
          .style("stroke-width", "1")
          .selectAll("path")
          .style("fill", "none")
          .style("stroke", o.color)
          .style("stroke-width", o.lineWeight)
          context.select('.header'+ index)
          .append("text")             
          .attr("transform",
                "translate(" + (width/2) + " ," + 
                               (20) + ")")
          .style("text-anchor", "middle")
          .text(o.label);

        context.select(".header"+ index)

        .append("text")
        .attr("class", "log-data"+ index)
                  .attr("transform",
                        "translate(" + (width/2) + " ," +
                                       (60) + ")")
                  .style("text-anchor", "middle")
                  .text("");

  }
  header.order = 0;
  return header;
}
export {headerLegend};