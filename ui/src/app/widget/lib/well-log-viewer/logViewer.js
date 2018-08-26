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
import * as d3 from 'well-log-viewer/node_modules/d3/build/d3';
//import {loadConfig} from './config';
import {linearGrid} from './linearGrid';
import {headerLegend} from './headerLegend';
import {lineGraph} from './lineGraph';
import './logViewer.css';

// var loadConfig = require('./config');
// var lnGrid = require('./linearGrid');
//var d3 = require('./node_modules/d3/build/d3');
//require('./node_modules/d3-selection-multi/build/d3-selection-multi');
/*@ngInject*/
export default function loadLogViewer(ctx, sequence){
    'use strict';
    var config,
        buildArray = [];
    //    events;
   
    function build(ctx) {
    //  config = loadConfig();
      config = ctx.widgetConfig.settings;
      config.Track.forEach(function(track){
        var trackObj = [];
        var headerCount = 0
        track.component.forEach(function(componentObj){
          if(componentObj.hasHeader){
           headerCount +=1;
            var hLegend = headerLegend(componentObj, headerCount);
            trackObj.push(hLegend);
          }
          if(componentObj.cType === 'linearGrid'){
            var lnGrid = linearGrid(componentObj);
            trackObj.push(lnGrid);
          }
          if(componentObj.cType === 'timeYaxis'){
            // var timeYaxis = LogViewer.timeYaxis(component.type)
            // trackObj.push(timeYaxis);
          }
          if(componentObj.cType === 'line'){
            var lnGraph = lineGraph(componentObj);
            trackObj.push(lnGraph);
          }

        })
        buildArray.push(trackObj);
      })
    }
   
    function addToDom() {
      var panelTracker = 1;
      buildArray.forEach(function(track){

        var trackId = '#track' + panelTracker;

        d3.select(trackId).selectAll('div')
           .remove();
            d3.select(trackId).selectAll('svg')
           .remove();
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
   
    // events = {
    //   dataButtonClick: function() {
    //   }
    // };
   
   if(sequence === "update"){
      build(ctx);
      addToDom();
      addListeners();
   }
}
//export {loadLogViewer};