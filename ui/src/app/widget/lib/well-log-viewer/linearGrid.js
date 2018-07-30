//var d3 = require('./node_modules/d3/build/d3');
//require('./node_modules/d3-selection-multi/build/d3-selection-multi');
import * as d3 from 'well-log-viewer/node_modules/d3/build/d3';
import './logViewer.css';
/*@ngInject*/
var linearGrid = function(lineConfig) {
  'use strict';
  var o;
     // local;

  o = {
    wodth: 200,
    value: null,
    key: null,
    majorLines: lineConfig.majorLines.lines -1,
    minorLines: lineConfig.minorLines.lines +1,
    majorColor: lineConfig.majorLines.color,
    minorColor: lineConfig.minorLines.color,
    majorLineWeight: lineConfig.majorLines.lineWeight,
    minorLineWeight: lineConfig.minorLines.lineWeight
  };  
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
      width = 440 - margin.right - margin.left,
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

  context.select('.linearGrid')
        .attr('width', width + margin.right + margin.left)
        .attr('height', height + margin.top + margin.bottom)

    var xAxis = d3.axisBottom(xScale)
        .tickSizeInner(-height)
        .tickSizeOuter(0)
        .tickValues(tickValues)
        .tickFormat(function (d, i) {
          return isMajorTick(i) ? "" : "";
        });

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

    var y = d3.scaleLinear()
        .range([0, height]);

    var yAxis = d3.axisLeft()
        .scale(y)
        .tickFormat(function(d){ return d.x;})
        .ticks(10)
        .tickSize(-width);

    context.select('.linearGrid')
    .append("g")
          .attr("class", "axis")
          .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
          .call(yAxis);

  }
  
  lGrid.width = function(){/*...*/}
  lGrid.order = 1;
  // set other methods here
 
  return lGrid;
};
export {linearGrid};