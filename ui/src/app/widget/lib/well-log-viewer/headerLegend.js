import * as d3 from 'well-log-viewer/node_modules/d3/build/d3';
import './logViewer.css';

/*@ngInject*/

var headerLegend = function(lineConfig, headerCount) {
  'use strict';
  var o;
     // local;

  o = {
    wodth: 200,
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