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
//import * as d3 from 'well-log-viewer/node_modules/d3/build/d3';
import * as d3 from 'd3';

import {dataGenerator} from './dataGenerator';
import './logViewer.css';

/*@ngInject*/
var lineGraph = function(lineConfig, data, state, index, width) {

  'use strict';
  var lineParameter;

  lineParameter = {
      color: lineConfig.color,
      min: lineConfig.headerMin,
      max: lineConfig.headerMax,
      lineWeight: lineConfig.lineWeight,
      areaFill: lineConfig.areaFill,
      data: data,
      state:state,
      index: index,
      width: width
    }

  if(angular.isUndefined(lineParameter.width)){
    lineParameter.width = 3;
  } 

  function lineChart(group) {
    group.each(render);
  }
 
 // function render(data) {
  function render() {
    var context;

    context = d3.select(this);

    let margin = {top: 30, right: 10, bottom: 30, left: 10},
      w = lineParameter.width*110 - margin.right - margin.left,
      h = 700 - margin.top;

    let x = d3.scaleLinear().domain(d3.min(lineParameter.data.data, function(d) { return d[1]; }),d3.max(lineParameter.data.data, function(d) { return d[1]; })).range([0 , w]);
    let y = d3.scaleLinear().domain(d3.min(lineParameter.data.data, function(d) { return d[0]; }),d3.max(lineParameter.data.data, function(d) { return d[0]; })).range([h, 0]);



    let xAxis =  d3.axisTop()
                 .scale(x)
    let yAxis = d3.axisLeft()
        .scale(y)



    let line = d3.line()
      .y((d) => y(d[0]))
      .y(function(d) { return y(d[0]); })
      .x(d => x(d[1]))
      .curve(d3.curveLinear);



    if(angular.isDefined(lineParameter.areaFill)){
      if(lineParameter.areaFill.fill === "left"){

        var area = d3.area()
              .x0(-14)
              .x1((d) => x(d[1]))
              .y((d) => y(d[0]))
              .curve(d3.curveLinear);
      }
      if(lineParameter.areaFill.fill === "right"){
          area = d3.area()
                .x0((d) => x(d[1]))
                .x1(w)
                .y((d) => y(d[0]))
                .curve(d3.curveLinear);
      }
    }

if(lineParameter.state === "init"){
    let $lineGraph = context.select('.linearGrid')
      .attr("width", w + margin.right + 1)
      .attr("height", h)
      .append('g')
      .attr("class", 'linepath'+lineParameter.index)
      .append('path')
      .attr('stroke', lineParameter.color)
      .attr('fill', 'none')
      .attr('stroke-width', lineParameter.lineWeight)

    if(angular.isDefined(lineParameter.areaFill)){
         let $areaGraph = context.select('.linearGrid')
              .append('g')
              .attr("class", 'areapath'+lineParameter.index)
              .append('path')
              .attr('fill', lineParameter.areaFill.color)
              .style("opacity", lineParameter.areaFill.opacity);
    }

 }

    function update() {
      var leftPadding = margin.left + 15;
      y.domain(d3.extent(lineParameter.data.data, function(d) { return d[0]; }));
      x.domain(d3.extent(lineParameter.data.data, function(d) { return d[1]; }));

      let $line= context.select('.linearGrid').select('.linepath'+lineParameter.index).select('path');

      $line
        .data([lineParameter.data.data])
        .attr('class', 'grid')
        .attr('d', line)
        .attr("transform", "translate(" + leftPadding + ", 0)")
        .attr('stroke', lineParameter.color)
        .attr('fill', 'none')
        .attr('stroke-width', lineParameter.lineWeight)


       if(angular.isDefined(lineParameter.areaFill)){
       let $areaGraph = context.select('.linearGrid').select('.areapath'+lineParameter.index).select('path');
        $areaGraph
                .data([lineParameter.data.data])
                .attr("transform", "translate(" + leftPadding + ", 0)")
                .attr('d', area)
                .attr('fill', lineParameter.areaFill.color)
                .style("opacity", lineParameter.areaFill.opacity);

       }
       }
    update();
  }
  lineChart.order = 2;
  return lineChart;
}
export {lineGraph};
