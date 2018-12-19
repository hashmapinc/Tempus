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
var timeYaxis = function(timeConfig, data, state, index, width) {
  'use strict';
  var o;

  o = {
    width: width,
    data: data,
    index: index,
    state: state
  };  
  function timeseriesYaxis(group) {
    // group-scope
    group.each(render);
  }
 
 // function render(data) {
  function render() {
    var context;

    context = d3.select(this);

    let margin = {top: 0, right: 10, bottom: 0, left: 30},
      w = o.width*110 - margin.right - margin.left,
      h = 700 - margin.top - margin.bottom;

    context.select('.linearGrid')
        .attr('width', w + margin.right + margin.left)
        .attr('height', h + margin.top + margin.bottom)

    var y = d3.scaleTime()
      .domain(d3.extent(o.data.data, function(d) { return d[0]; }))
      .range([0, h])

    if(o.state === "init"){
     context.select('.linearGrid')
      .append('g')
      .attr('class', 'y-axis')
      .attr('transform', 'translate(40,20)')
      .call(y.axis = d3.axisLeft().scale(y))
    }
    else {
      context.select('.linearGrid').select('.y-axis')
      .attr('transform', 'translate(40,20)')
      .call(y.axis = d3.axisLeft().scale(y))
    }


    // var text = context.select('.linearGrid').append('g').attr('transform', 'translate(40,30)')

    // var paths = context.select('.linearGrid').append('g')


    //let x = d3.scaleLinear().domain(o.min, o.max).range([0 + margin.left, w]);
    // let y = d3.scaleLinear().range([h, 0]);

   // function tick() {

        // // Shift domain
        // y.domain([now - (limit - 2) * duration, now - duration])
        // axis.transition()
        //    // .duration(duration)
        //     .call(y.axis)

        // // Slide paths left
        // paths.attr('transform', null)
        //     .transition()
        //   //   .duration(duration)
        //   //  .ease('linear')
        //   //  .attr('transform', 'translate(' + y(now - (limit - 1) * duration) + ')')
        //     .on('end', tick)

        // dataGen.tick();

        // let yScale = d3.scaleLinear().domain([now.valueOf() -600000, now.valueOf()]).range([0, h- margin.top -80]);


 // var textEle = text.selectAll("text")
 //    .data(dataGen.latestData);

 //    textEle.enter().append("text");


 //    textEle
 //    .merge(textEle)
 //    .attr("y", function(d){ return y(now.valueOf())})//magic number here
 //   // .attr("y", function(d){ return now.getSeconds() * 12})//magic number here
 //    .attr("x", w - 350)
 //    .text(function(d){ return now});
      // if(o.showMessage){
      //   if(text.selectAll('text').size() > 350){
      //      var  circleElements = text.nodes(); // <== Get all circle elements

      //   d3.select(circleElements[0].children[0]).remove();
      //   }

      //   text.selectAll('text').attr("y", function(d, i){ console.log(d3.select(this).attr("y")); return d3.select(this).attr("y")- 1.8;});

      //   text.append('text')
      //    .attr('transform', 'translate(40,30)')
      //    // .attr("y", function(d, i){ return (now.getSeconds()) * 3})//magic number here
      //     .attr("y", function(d, i){ return yScale(now) + 100})
      //     .attr("x", w - 350)
      //     .text(function(d){ if(now.getSeconds() % 4 == 0){return now}});
      // }
  //  }
  }
  timeseriesYaxis.order = 1;
  return timeseriesYaxis;

}
export {timeYaxis};