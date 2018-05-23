/*
 * Copyright © 2017-2018 Hashmap, Inc
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

import * as THREE from 'three';
import * as trajXYZ from './trajectoryXYZconverter';
import angular from 'angular';
import Plotly from 'plotly.js/lib/index-basic';

/**
 * This function takes a XYZ trajectory array and returns the ideal plane of 
 * projection for a 2D view of the coordinates.
 * 
 * The plane is calculated by unbounding y (the tvd dimension) and calculating the line
 * between the first and last xyz points in the XZ plane, creating a plane in 3 dimensions
 * 
 * Coordinates should be ordered by the measured depth of the source reading.
 *
 * @param xyzArray - array of XYZ coordinates to calculate plane from
 * @returns normal - 3D vector normal to the calculated plane
 */
export function calculatePlaneNormal(xyzArray) {
  // get planar vector endpoints 
  var p0 = xyzArray[0];
  var pn = xyzArray[xyzArray.length - 1];

  // create the 2 necessary vectors to describe a plane
  var planarVectorA = new THREE.Vector3(0,1,0); // first planar vector
  var planarVectorB = new THREE.Vector3(
    pn.x - p0.x, 
    pn.y - p0.y, 
    pn.z - p0.z
  );
  
  // compute the plane normal vector with cross product of the planar vectors
  var normal = new THREE.Vector3(0,0,0);
  normal.crossVectors(planarVectorA, planarVectorB);

  return normal;
}

/**
 * This function projects all points in xyzArray onto the plane normal to planeNormal.
 * 
 * Coordinates should be ordered by the measured depth of the source reading.
 *
 * @param xyzArray - array of XYZ coordinates to project
 * @param planeNormal - vector normal to the plane of projection
 * @returns projectedArray - array of THREE.Vector3 objects containing the projection.
 */
export function projectOnPlane(xyzArray, planeNormal) {
  // create empty array to hold results
  var projectedArray = [];

  // iterate through each point and push projection into projectedArray
  xyzArray.forEach(point => {
    var pointVector = new THREE.Vector3(point.x, point.y, point.z);
    projectedArray.push(pointVector.projectOnPlane(planeNormal));
  });

  return projectedArray;
}

/**
 * This function converts an array of THREE.Vector3 objects that have been projected
 * into the same plane to an array of objects with X and Y coordinates for 2D plotting
 * 
 * projections should be ordered by the measured depth of the source reading.
 * 
 * @param projections - array of XYZ coordinates to project
 * @returns xyArray - array of XY objects ready for plotting in 2 dimensions
 */
export function convertProjectionsToXY(projections) {
  // create empty array to hold results
  var xyArray = [];

  // get origin point. This will be used to calculate the X dimension
  var origin = projections[0];

  // iterate through projections and generate XY coordinates
  projections.forEach(projection => {
    // get y poisiton directly from projection
    var y = projection.y;

    // get x position as the distance between the origin and projection in the XZ plane
    var xDelta = projection.x - origin.x;
    var zDelta = projection.z - origin.z;
    var x = Math.sqrt(xDelta*xDelta + zDelta*zDelta);

    // push the new XY point to the results array
    xyArray.push({x: x, y: y});
  });

  return xyArray;
}

/** 
 * this function builds the chart
 * 
 * @param $container - jquery object to contain the widget
 */
export function createWidget($container) {
  // create chart
  var rawHTML = `
    <div id='trajectory-viewer2D-chart'></div>
  `
  var chart = angular.element(rawHTML);

  // style chart
  chart.css({
    height: '100%',
    width: '100%'
  });

  // append chart
  $container.append(chart);
}

/** 
 * this function builds the chart
 */
export function destroyWidget() {
  angular.element("#trajectory-viewer2D-chart")
}

/**
 * 
 * @param xArray - array containing data to plot in the x axis
 * @param yArray - array containing data to plot in the y axis
 */
export function plot(xArray, yArray) {
  var trace1 = {
    x: xArray,
    y: yArray,
    type: 'scatter'
  };

  var data = [trace1];

  Plotly.newPlot('trajectory-viewer2D-chart', data);
}

// init function for instantiating the widget
export function init(widgetContext) {
  // create the widget
  createWidget(widgetContext.$container);

  // grab some dummy data
  // get raw data
  var input = '[{"azi":0.0,"incl":0.0,"md":0.0},{"azi":0.0,"incl":0.0,"md":43.5},{"azi":339.6,"incl":0.0,"md":152.0},{"azi":339.6,"incl":2.53,"md":291.0},{"azi":328.08,"incl":2.12,"md":300.0},{"azi":330.06,"incl":2.07,"md":310.0},{"azi":325.98,"incl":2.27,"md":320.0},{"azi":331.23,"incl":2.37,"md":330.0},{"azi":330.29,"incl":2.56,"md":340.0},{"azi":330.71,"incl":2.77,"md":350.0},{"azi":330.65,"incl":3.04,"md":360.0},{"azi":333.94,"incl":3.44,"md":364.29},{"azi":348.46,"incl":3.73,"md":378.06},{"azi":347.09,"incl":3.79,"md":381.94},{"azi":352.58,"incl":2.6,"md":393.56},{"azi":348.38,"incl":1.89,"md":403.59},{"azi":310.4302062988,"incl":1.741545558,"md":429.7363891602},{"azi":324.5098266602,"incl":1.2525943518,"md":453.5132446289},{"azi":321.5378723145,"incl":1.4943230152,"md":463.38671875},{"azi":95.2311248779,"incl":0.5493834615,"md":473.21},{"azi":110.2006759644,"incl":2.59,"md":489.41},{"azi":103.7239456177,"incl":2.84,"md":504.88},{"azi":54.6622467041,"incl":2.34,"md":522.1030273437},{"azi":24.712146759,"incl":4.11,"md":549.2150268555},{"azi":15.32,"incl":5.4,"md":576.56},{"azi":7.1936445236,"incl":6.87,"md":603.7582397461},{"azi":1.1838481426,"incl":7.24,"md":617.9675292969},{"azi":356.5803833008,"incl":7.5,"md":623.0197143555},{"azi":352.0757751465,"incl":7.74,"md":630.7108154297},{"azi":346.164855957,"incl":8.78,"md":645.1820678711},{"azi":346.74,"incl":10.43,"md":658.32},{"azi":346.7636413574,"incl":12.09,"md":672.1657714844},{"azi":345.4946594238,"incl":14.04,"md":685.2329101563},{"azi":340.84,"incl":16.12,"md":712.25},{"azi":338.545501709,"incl":17.97,"md":739.6813964844},{"azi":342.2864990234,"incl":20.43,"md":766.8629150391},{"azi":344.9617919922,"incl":23.52,"md":794.18},{"azi":341.3251647949,"incl":25.24,"md":821.35},{"azi":336.8590087891,"incl":26.38,"md":848.28},{"azi":339.3255615234,"incl":28.33,"md":875.57},{"azi":341.2254943848,"incl":31.42,"md":902.7392578125},{"azi":342.9117736816,"incl":33.16,"md":919.3889160156},{"azi":343.0886535645,"incl":34.5,"md":946.6367797852},{"azi":341.6498413086,"incl":33.73,"md":973.9765014648},{"azi":343.2836303711,"incl":32.97,"md":1001.5057983398},{"azi":341.6416625977,"incl":32.52,"md":1028.3920898438},{"azi":339.7196960449,"incl":33.56,"md":1055.7971191406},{"azi":339.711517334,"incl":34.45,"md":1082.958984375},{"azi":337.9357299805,"incl":35.46,"md":1137.6472167969},{"azi":338.0027770996,"incl":34.82,"md":1164.7596435547},{"azi":338.7321472168,"incl":34.77,"md":1192.1204833984},{"azi":339.9628295898,"incl":34.64,"md":1253.5303955078},{"azi":340.6786193848,"incl":33.15,"md":1300.4992675781},{"azi":340.430847168,"incl":32.37,"md":1345.7178955078},{"azi":340.9657897949,"incl":32.31,"md":1373.0893554688},{"azi":341.2445678711,"incl":33.03,"md":1400.5101318359},{"azi":341.2211608887,"incl":33.7,"md":1427.8498535156},{"azi":341.0071105957,"incl":35.35,"md":1454.7435302734},{"azi":340.4712524414,"incl":35.37,"md":1481.9958496094},{"azi":340.755065918,"incl":35.42,"md":1509.1533203125},{"azi":341.3230285645,"incl":35.2,"md":1530.3831787109},{"azi":343.1461791992,"incl":35.38,"md":1563.9252929688},{"azi":343.7471008301,"incl":34.55,"md":1590.5174560547},{"azi":343.7061767578,"incl":34.53,"md":1617.9241943359},{"azi":343.7421875,"incl":34.34,"md":1645.0900878906},{"azi":343.3536376953,"incl":34.25,"md":1672.4910888672},{"azi":343.5046081543,"incl":34.44,"md":1696.8723144531},{"azi":342.720489502,"incl":33.32,"md":1727.1253662109},{"azi":343.2664489746,"incl":32.82,"md":1754.3231201172},{"azi":342.536315918,"incl":31.57,"md":1781.6500244141},{"azi":342.0537414551,"incl":29.94,"md":1808.9030761719},{"azi":342.0120544434,"incl":29.93,"md":1835.8624267578},{"azi":341.8739318848,"incl":29.94,"md":1850.4187011719},{"azi":341.1149902344,"incl":29.69,"md":1905.1544189453},{"azi":340.8925476074,"incl":30.17,"md":1932.9757080078},{"azi":341.3746032715,"incl":30.36,"md":1960.1788330078},{"azi":341.4132385254,"incl":30.32,"md":1987.4606933594},{"azi":341.3897705078,"incl":30.22,"md":2014.7091064453},{"azi":341.2257385254,"incl":29.97,"md":2041.4984130859},{"azi":342.2607727051,"incl":29.86,"md":2068.8159179688},{"azi":340.0125427246,"incl":30.02,"md":2083.328125},{"azi":340.01,"incl":30.02,"md":2100.7}]';
  var rawTrajectory = angular.fromJson(input);

  // convert to XYZ coordinates
  var xyz = trajXYZ.convertRawToXYZ(rawTrajectory);

  // get 2D coordinates
  var planeNormal = calculatePlaneNormal(xyz);
  var projections = projectOnPlane(xyz, planeNormal);
  var xy = convertProjectionsToXY(projections);

  // process 2D coordinates for plotly
  var xArray = [], yArray = [];
  xy.forEach(point => {
    xArray.push(point.x);
    yArray.push(point.y);
  });

  //plot the coordinates
  plot(xArray,yArray);
}