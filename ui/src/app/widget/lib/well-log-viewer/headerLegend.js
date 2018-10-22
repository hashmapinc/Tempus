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
import './logViewer.css';

/*@ngInject*/

var headerLegend = function(lineConfig, headerCount) {
  'use strict';
  var o;
     // local;

  o = {
    width: 200,
    value: null,
    key: null,
    min: lineConfig.headerMin,
    max: lineConfig.headerMax,
    lineWeight: lineConfig.lineWeight,
    color: lineConfig.color,
    label: lineConfig.headerName,
    headerCount: headerCount
  };  

  // local = {
  // //  label: d3.local(),
  // //  dimensions: d3.local()
  // };
 
  function header(group) {
    // group-scope
    group.each(render);
  }
 
  //function render(data) {
  function render() {
    var context;
       // dim;

    context = d3.select(this);

    var margin = {top: 30, right: 10, bottom: 30, left: 10},
      width = 440 - margin.right - margin.left,
      height = 60 - margin.top - margin.bottom;

    var x = d3.scaleLinear()
        .domain([o.min, o.max])
        .range([0, width])

    // var y = d3.scaleLinear()
    //     .range([0, height]);

    var xAxis = d3.axisTop()
        .scale(x)
        .tickValues([o.min, o.max])
        .tickSize(5)

    context.select(".header")
         .append("svg")
         .attr("class", "header"+ headerCount)
         // .attr("width", 440)
         // .attr("height", 0)
        .attr("width", width + margin.right + margin.left)
        .attr("height", height + margin.top + margin.bottom)
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

          context.select('.header'+ headerCount)
          .append("text")             
          .attr("transform",
                "translate(" + (width/2) + " ," + 
                               (20) + ")")
          .style("text-anchor", "middle")
          .text(o.label);

  }
  header.order = 0;
  return header;
}
export {headerLegend};