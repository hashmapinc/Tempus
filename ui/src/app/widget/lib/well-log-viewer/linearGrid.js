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
  var gridParameter;

  gridParameter = {
    state:state,
    width: width,
    isLinearScale: lineConfig.gridType === 'Linear',
    isLogarithmicScale: lineConfig.gridType === 'Logarithmic'
  }; 

  if(angular.isUndefined(gridParameter.width)){
    gridParameter.width = 3;
  } 

  function lGrid(group) {
    // group-scope
    group.each(render);
  }
 
  function render() {
    var context,xScale,yScale;
    gridParameter.mirorStroke = "6,0";

    context = d3.select(this);

    var margin = {top: 0, right: 10, bottom: 20, left: 10},
      width = gridParameter.width*140 - margin.right - margin.left,
      height = 700 - margin.top - margin.bottom;

    if(gridParameter.isLinearScale){
        xScale = d3.scaleLinear()
            .domain([0, width])
            .range([0, width]);
        yScale = d3.scaleLinear()
            .domain([0, height])
            .range([0, height]);
    }else if(gridParameter.isLogarithmicScale) {
        xScale = d3.scaleLog()
               .domain([0.1, 1000])
              .range([0 , width]);
        yScale = d3.scaleLinear()
                    .domain([0, height])
                    .range([0, height]);
    }


    var xAxis;
    if(gridParameter.isLinearScale) {
      xAxis = d3.axisBottom()
        .ticks(25)
        .tickSize(-height)
        .scale(xScale);
    }else if(gridParameter.isLogarithmicScale) {
      xAxis = d3.axisBottom()
        .ticks(40)
        .tickSize(-height)
        .scale(xScale)
    }

    var yAxis = d3.axisLeft()
        .ticks(30)
        .tickSize(-width)
        .scale(yScale);


    if(gridParameter.state === 'init'){
      context.select('.linearGrid')
          .attr('width', width + margin.right + margin.left)
          .attr('height', height + margin.top + margin.bottom)

      context.select('.linearGrid').append("g")
          .attr("id", "x_axis")
          .attr("class", "x axis")
          .style("stroke-width", '0.5px')
          .attr("transform", "translate(0," +  height +")")
          .call(xAxis);


      context.select('.linearGrid')
           .append("g")
           .attr("class", "axis")
           .attr("id", "moving-axis")
           .call(yAxis);
    }
    if(gridParameter.state === 'update'){

        context.select('.linearGrid').select(".x axis")
          .attr("id", "x_axis")
          .style("stroke-width", '2px')
          .call(xAxis)

        context.select('.linearGrid')
         .select("#moving-axis")
         .call(yAxis);
         if(lineConfig.gridType === 'Linear'){
            var count = context.select('.linearGrid').select("#x_axis").selectAll('g.tick');
            var middle = count._groups[0][Math.round((count._groups[0].length - 1) / 2)];
            middle.classList.add("major");
         }else if(lineConfig.gridType === 'Logarithmic') {
            context.select('.linearGrid').select("#x_axis").selectAll('g.tick')
                        .filter(function(d,i){
                            return (i%9) === 0;
                        } )
                      .select('line') //grab the tick line
                      .attr('class', 'major') //style with a custom class and CSS
         }

    }
  }
  
  lGrid.width = function(){/*...*/}
  lGrid.order = 1;

  return lGrid;
};
export {linearGrid};