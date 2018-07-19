import {loadConfig} from './config';
import {lnGrid} from './linearGrid';
// var loadConfig = require('./config');
// var lnGrid = require('./linearGrid');
//var d3 = require('./node_modules/d3/build/d3');
//require('./node_modules/d3-selection-multi/build/d3-selection-multi');

if(typeof LogViewer === 'undefined') {LogViewer = {};}
/*@ngInject*/
loadLogViewer = function(){
    'use strict';
    var config,
        buildArray = [],
        events;
   
    function build() {
      config = loadConfig();
      config.tracks.forEach(function(track){
        var trackObj = [];
        var headerCount = 0
        track.components.forEach(function(component){
          if(component.hasHeader){
            headerCount +=1;
            // var headerLegend = LogViewer.headerLegend(component.type, headerCount);
            // trackObj.push(headerLegend);
          }
          if(component.type.type === 'linearGrid'){
            var linearGrid = lnGrid(component.type);
            trackObj.push(linearGrid);
          }
          if(component.type.type === 'timeYaxis'){
            // var timeYaxis = LogViewer.timeYaxis(component.type)
            // trackObj.push(timeYaxis);
          }
          if(component.type.type === 'line'){
            // var lineGraph = LogViewer.lineGraph(component.type);
            // trackObj.push(lineGraph);
          }

        })
        buildArray.push(trackObj);
      })
    }
   
    function addToDom() {
      var panelTracker = 1;
      buildArray.forEach(function(track){
        var trackId = '#track' + panelTracker;
        panelTracker += 1;
        d3.select(trackId)
           .append("div")
           .attr("class", "header")
           // .append("svg")
           // .attr("class", "header")
           // .attr("width", 440)
           // .attr("height", 0)

        d3.select(trackId)
           .append("svg")
           .attr("class", "linearGrid")

        for(var i = 0; i < 3; i++){
          track.forEach(function(component){
            if(component.order == i)
             d3.select(trackId)
               .call(component)
          })
        }

      })
      // d3.select('#track1')
      //  // .datum(LogViewer.generateData())
      //   .call(headerLegend)
      //   .call(linearGrid)
      //   .call(LogViewer.lineGraph());

    }
   
    function addListeners() {
    //  d3.select('button').on('click', events.dataButtonClick);
    }
   
    events = {
      dataButtonClick: function() {
      }
    };
   
    build();
    addToDom();
    addListeners();
}
module.exports = loadLogViewer;