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
//import * as d3 from 'well-log-viewer/node_modules/d3/build/d3';
import * as d3 from 'd3';
//import {loadConfig} from './config';
import {linearGrid} from './linearGrid';
import {headerLegend} from './headerLegend';
import {lineGraph} from './lineGraph';
import {timeYaxis} from './timeYaxis';
import {mudLog} from './mudLog';
import './logViewer.css';

// var loadConfig = require('./config');
// var lnGrid = require('./linearGrid');
//var d3 = require('./node_modules/d3/build/d3');
//require('./node_modules/d3-selection-multi/build/d3-selection-multi');
/* eslint-disable angular/angularelement */
/* eslint-disable */
/*@ngInject*/
export default function loadLogViewer(ctx, sequence){
    'use strict';
    var config,
        buildArray = [],
        dataArray = [],
        dragCount,
        stopTick;
      //  events = {};

    if (angular.isDefined(Storage)) {
      if(sequence === 'init'){
            dataArray.push(ctx.data);
            localStorage.setItem("oldData", angular.toJson(dataArray));
            localStorage.setItem("stopTick", angular.toJson(false));
            localStorage.setItem("dragCount", angular.toJson(0));
          }
          else
          {
            stopTick = angular.fromJson(localStorage.getItem("stopTick"));
            dragCount = angular.fromJson(localStorage.getItem("dragCount"));
            dataArray = angular.fromJson(localStorage.getItem("oldData"));
            dataArray.push(ctx.data);
            localStorage.setItem("oldData", angular.toJson(dataArray));
          }      
        } else {
          alert("Sorry! No Web Storage support..");
        }
    
    function build(dArray, state) {
      function datasourceFilter(settings, datasources){
        var ds;
        if(settings.cType === 'Grid' || settings.cType === 'Time Y axis'){
          ds = datasources[0];
        }
        else {
          datasources.forEach(function(datasource){
            if(settings.datasource === datasource.dataKey.label){
              ds = datasource;
            }
         })
        }
        return ds;
      }
    //  config = loadConfig();
      config = ctx.widgetConfig.settings;
      if(angular.isDefined(config.Track)){
      config.Track.forEach(function(track){
        var trackObj = [];
        var graphElementsNumber = 0;
        track.component.forEach(function(componentObj){
          if(componentObj.cType === 'Grid'){
            var lnGrid = linearGrid(componentObj, datasourceFilter(componentObj, dArray), state, graphElementsNumber, parseInt(track.width));
            trackObj.push(lnGrid);
            graphElementsNumber+=1;
          }
          if(componentObj.cType === 'Time Y axis'){
             var tYaxis = timeYaxis(componentObj, datasourceFilter(componentObj, dArray), state, graphElementsNumber, parseInt(track.width));
            trackObj.push(tYaxis);
            graphElementsNumber+=1;
          }
          if(componentObj.cType === 'Line'){
            if(angular.isArray(componentObj.lines)){
              componentObj.lines.forEach(function(line) {
                var hLegend = headerLegend(line, datasourceFilter(line, dArray), state, graphElementsNumber, parseInt(track.width));
                trackObj.push(hLegend);
                graphElementsNumber+=1;
                var lnGraph = lineGraph(line, datasourceFilter(line, dArray), state, graphElementsNumber, parseInt(track.width));
                trackObj.push(lnGraph);
                graphElementsNumber+=1;
              })
            }
          }
          if(componentObj.cType === 'Mud Log Viewer'){
            var mdlog = mudLog(componentObj, datasourceFilter(componentObj, dArray), state, graphElementsNumber, parseInt(track.width));
            trackObj.push(mdlog);
            graphElementsNumber+=1;
          }

        })
        buildArray.push(trackObj);
      })
     }
    }
   
    function addToDom(state) {
      var panelTracker = 1;
      buildArray.forEach(function(track){
        var trackId = '#track' + panelTracker;
        panelTracker += 1;

        if(state === 'init'){
          d3.select(trackId).selectAll('div')
             .remove();
              d3.select(trackId).selectAll('svg')
             .remove();
          d3.select(trackId)
             .append("div")
             .attr("class", "header")
          d3.select(trackId)
                       .append("div")
          .attr("class", "track-div")
             .append("svg")
             .attr("class", "linearGrid")
        }

        track.sort(function(left, right) {
          left.order - right.order;
        });
        track.forEach(function(component){
            d3.select(trackId)
              .call(component);
        });

      })

    }

    function dragged(d) {
      localStorage.setItem("stopTick", angular.toJson(true));
      dataArray = angular.fromJson(localStorage.getItem("oldData"));
      if(d3.event.y > 0){
        dragCount +=1;
        build(dataArray[dataArray.length-1-dragCount], "update");
        addToDom("update");
      }
      if(d3.event.y < 0 && dragCount != 0){
        dragCount -=1;
        build(dataArray[dataArray.length-1-dragCount], "update");
        addToDom("update");
      }
      if(dragCount === 0){
        localStorage.setItem("stopTick", angular.toJson(false));
      }
      localStorage.setItem("dragCount", angular.toJson(dragCount));

    }
   
    function addListeners() {
      var drag = d3.drag()
          .subject( function() { 
            var t = d3.select(this);
            return {x: t.attr("x"), y: t.attr("y")};
          })
         .on("end", dragged);
     d3.select('#track1').call(drag);
     d3.select('#track1').on("dblclick", function(){
        dataArray = angular.fromJson(localStorage.getItem("oldData"));
        build(dataArray[dataArray.length-1], "update");
        addToDom("update");
        addListeners();
        localStorage.setItem("stopTick", angular.toJson(false));
        localStorage.setItem("dragCount", angular.toJson(0));
     });
       //  .on("click", dataScroll);
    }
   
     // var dataScroll = function(){
     //   // alert('scroll111');
     //   console.log('scroll');
     //  }
       
   if(sequence === "update"){
    if(stopTick === false){
      build(dataArray[dataArray.length-1], "update");
      addToDom("update");
      addListeners();
    }
   }
   else {
      build(dataArray[dataArray.length-1], "init");
      addToDom('init');
      addListeners();
   }
}
//export {loadLogViewer};