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
import {dataGenerator} from './dataGenerator';
import './logViewer.css';

/*@ngInject*/
var lineGraph = function(lineConfig) {
  'use strict';
  var o,
     // local,
      dataGen;

  o = {
    // width: 200,
    // value: null,
    // key: null,
    color: lineConfig.color,
    min: lineConfig.headerMin,
    max: lineConfig.headerMax,
    lineWeight: lineConfig.lineWeight,
    areaFill: lineConfig.areaFill  
  }

  // local = {
  // //  label: d3.local(),
  // //  dimensions: d3.local()
  // };

  dataGen = dataGenerator();
 
  function lineChart(group) {
    // group-scope
    group.each(render);
  }
 
 // function render(data) {
  function render() {
    var context;
     //   dim;

    context = d3.select(this);

    let margin = {top: 30, right: 10, bottom: 30, left: 10},
      w = 440 - margin.right - margin.left,
      h = 700 - margin.top;

    let x = d3.scaleLinear().domain(o.min, o.max).range([0 + margin.left, w]);
    let y = d3.scaleLinear().range([h, 0]);

    let xAxis = d3.axisTop()
      .scale(x)

    let yAxis = d3.axisLeft()
      .scale(y)
      .tickSize(0)
      .tickFormat("");

    let line = d3.line()
      .y((d, i) => y(i + dataGen.time))
      .x(d => x(d));

    if(angular.isDefined(o.areaFill)){
      if(o.areaFill.fill === "left"){
        var area = d3.area()
          .x0(-14)
          .x1(d => x(d))
          .y((d, i) => y(i + dataGen.time));
      }
      if(o.areaFill.fill === "right"){
         area = d3.area()
          .x0(d => x(d))
          .x1(w)
          .y((d, i) => y(i + dataGen.time));

      }
    }


    // context.select('.linearGrid')
    // .append('g')
    //   .attr('transform', 'translate(30, 20)');

    // let $xAxis = context.select('.linearGrid')
      //  context.select('.linearGrid')
      // .attr("width", w + margin.right + 1)
      // .append('g')
      // .attr('class', 'x axis')
      // .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
     // .call(xAxis);


    //grid
    // let $yAxis = context.select('.linearGrid').append('g')
      context.select('.linearGrid').append('g')
      .attr('class', 'y axis')
      .attr("transform", "translate(" + margin.left + ", 0)")
      .call(yAxis);

    let $lineGraph = context.select('.linearGrid')
      .attr("width", w + margin.right + 1)
      .append('g')
      .append('path')
      .attr('stroke', o.color)
      .attr('fill', 'none')
      .attr('stroke-width', o.lineWeight);

    let $areaGraph = context.select('.linearGrid').append('path')
      .attr('fill', o.areaFill.color)
      .style("opacity", o.areaFill.opacity);

    function update() {
      var leftPadding = margin.left + 15;
      y.domain([dataGen.time + dataGen.num, dataGen.time]);
      let xDom = d3.extent(dataGen.latestData);
      xDom[0] = o.min;
      xDom[1] = o.max;
      x.domain(xDom);
    //  x.domain([0,200]);

      // $xAxis
        // .call(xAxis);

       // $yAxis
       //   .call(yAxis);

      $lineGraph
        .datum(dataGen.latestData)
        .attr("transform", "translate(" + leftPadding + ", 0)")
        .attr('d', line);

      $areaGraph
        .datum(dataGen.latestData)
        .attr("transform", "translate(" + leftPadding + ", 0)")
        .attr('d', area);

      // $rects
      //   .attr('height', (_, i) => Math.abs(latestDeltas[i] * h / 10))
      //   .attr('fill', (_, i) => latestDeltas[i] < 0 ? 'red' : 'green')
      //   .attr('y', (_, i) => h - Math.abs(latestDeltas[i] * h / 10) - 42);
    }

    for (let i = 0; i < dataGen.num + 50; i++) {
      dataGen.tick();
    }

    update();

    setInterval(() => {
      dataGen.tick();
      update();
    }, 300);


  }
  lineChart.order = 2;
  return lineChart;
}
export {lineGraph};
