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

"use strict";

import * as d3 from 'd3';

import {dataGenerator} from './dataGenerator';
import './logViewer.css';

/*@ngInject*/
var lineGraph = function(lineConfig, areaFillConfig, state, currentComponentIndex, width) {

  function lineChart(group) {
    group.each(render);
  }

  function render() {
    var context;

    context = d3.select(this);

    let margin = {top: 30, right: 10, bottom: 30, left: 10},
      w = width*140 - margin.right - margin.left,
      h = 700 - margin.top;

    lineConfig.forEach(function(element, index) {


      let lineToBeRendered = element.line;
      let data = element.data;

      let xScale = d3.scaleLinear().domain([lineToBeRendered.headerMin, lineToBeRendered.headerMax]).range([-20 , w-20]);
      let yScale = d3.scaleLinear().domain(d3.extent(data.data.map(d => d[0]))).range([h, 0]);



      if(state === "init"){
        let $lineGraph = context.select('.linearGrid')
          .attr("width", w + margin.right + 1)
          .attr("height", h)
          .append('g')
          .attr("class", 'linepath'+index+currentComponentIndex)
          .append('path')
          .attr('stroke', lineToBeRendered.color)
          .attr('fill', 'none')
          .attr('stroke-width', lineToBeRendered.lineWeight);

          if(angular.isDefined(areaFillConfig) && areaFillConfig.enable){
            if(areaFillConfig.referenceLine === lineToBeRendered.headerName){
              context.select('.linearGrid')
                .append('g')
                .attr("class", 'areapath'+index+currentComponentIndex)
                .append('path')
                .attr('fill', areaFillConfig.color)
                .style("opacity", areaFillConfig.opacity);
          }
        }
      }

      function update() {

        //line painting

        let line = d3.line()
            .y(d => yScale(d[0]))
            .x(d => xScale(d[1]))
            .curve(d3.curveLinear);

        context.select('.linearGrid').select('.linepath'+index+currentComponentIndex).select('path')
            .data([data.data])
            .attr('class', 'grid')
            .attr('d', line)
            .attr("transform", "translate(" + margin.left + ", 0)")
            .attr('stroke', lineToBeRendered.color)
            .attr('fill', 'none')
            .attr('stroke-width', lineToBeRendered.lineWeight);

        //area painting

        let area;

        if(angular.isDefined(areaFillConfig) && areaFillConfig.enable){
          if(areaFillConfig.referenceLine === lineToBeRendered.headerName){
            if(areaFillConfig.fill === "left"){

              area = d3.area()
                  .x0(-14)
                  .x1((d) => xScale(d[1]))
                  .y((d) => yScale(d[0]))
                  .curve(d3.curveLinear);

              paintArea(data.data);
            }
            if(areaFillConfig.fill === "right"){
              area = d3.area()
                  .x0((d) => xScale(d[1]))
                  .x1(w)
                  .y((d) => yScale(d[0]))
                  .curve(d3.curveLinear);

              paintArea(data.data);
            }
            if(areaFillConfig.fill === "between"){
              let otherLineConfig = lineConfig[Math.abs(index-1)];
              let xScaleOfOtherLine = d3.scaleLinear().domain(d3.min(otherLineConfig.data.data, function(d) { return d[1]; }),d3.max(otherLineConfig.data.data, function(d) { return d[1]; })).range([0 , w]);
              let otherLineData = lineConfig[Math.abs(index-1)].data.data;
              let combinedData = [];
              data.data.forEach(dataElement =>
                  combinedData.push([dataElement[0], dataElement[1], findCorrespondingDataPoint(dataElement, otherLineData)]));
              combinedData = combinedData.filter(element => angular.isDefined(element[2]));

              area = d3.area()
                  .x0((d) => xScale(combinedData[d][1]))
                  .x1((d) => xScaleOfOtherLine(combinedData[d][2]))
                  .y((d) => yScale(combinedData[d][0]))
                  .curve(d3.curveLinear);
              paintArea(d3.range(combinedData.length));
            }
          }
        }
        function paintArea(dataToBeUsed) {
          context.select('.linearGrid')
              .select('.areapath' + index + currentComponentIndex)
              .select('path')
              .data([dataToBeUsed])
              .attr("transform", "translate(" + margin.left + ", 0)")
              .attr('d', area)
              .attr('fill', areaFillConfig.color)
              .style("opacity", areaFillConfig.opacity);
        }
      }
      update();
    });

  }
  lineChart.order = 2;
  return lineChart;
};
export {lineGraph};


function findCorrespondingDataPoint(dataElement, otherLineData) {
  let dataPoint = otherLineData.find(element => Math.abs(element[0] - dataElement[0]) < 0.03);
  if(dataPoint) {
    return dataPoint[1];
  }
}