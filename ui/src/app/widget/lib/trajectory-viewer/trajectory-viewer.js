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
import OrbitControls from 'three-orbitcontrols';
import * as MeshLine from 'three.meshline';
import * as trajConverter from './trajectoryXYZconverter';
import angular from 'angular';

//=============================================================================
// Globals
//=============================================================================
// injected vars
var widgetContainer; // element that holds the widget
var widgetContext; //eslint-disable-line no-unused-vars

// threejs 
var sceneContainer; // element that holds the canvas element
var scene; // threejs scene
var camera; // threejs camera
var renderer; // threejs renderer
var controls; // camera controls
var raycaster; // threejs raycaster
var mouse; // XY coordinates of the mouse
var resolution; // screen resolution

// trajectory data
var rawReadings = []; // array of trajectory readings describing plotted trajectory
var points = []; // array of 3D points describing plotted trajectory
var gridSize = 1000; // size of grid to bound the plot

// colors
var backgroundColor = 0xffffff;
var plotColor = 0x0000ff;
var plotPointColor = 0xff0000;
var gridColor = 0xaaaaaa;
var gridCenterColor = 0x000000;
//=============================================================================

// creates all dom elements for widget
export function constructWidget() {
  // style widget container
  widgetContainer.css({
    'overflow': 'hidden',
    'margin': '0px',
    'height': '100%',
    'width': '100%'
  });
  
  var raw_html = `
    <div id='trajectory-viewer-scene'></div>
    <button id='trajectory-viewer-reset' type='button'>reset camera</button>

    <div id='trajectory-viewer-labelX' class='trajectory-viewer-labels'>east</div>
    <div id='trajectory-viewer-labelY' class='trajectory-viewer-labels'>tvd</div>
    <div id='trajectory-viewer-labelZ' class='trajectory-viewer-labels'>north</div>`

  // build elements
  widgetContainer.append(angular.element(raw_html));

  // style elements
  widgetContainer.children().css('position', 'absolute');

  // add event listeners
  angular.element('#trajectory-viewer-reset').click(resetCameraPosition);
}

// destroys all widget components
export function destroyWidget() {
  // remove widget components
  widgetContainer.children().remove();
}


// compute new points array. If overwrite, delete rawReadings first
export function updatePoints(newReadings, overwrite) {
  if (overwrite) {
    rawReadings = newReadings;
  } else {
    rawReadings = rawReadings.concat(newReadings);
    rawReadings.sort(function(a,b) {
      if (a.md < b.md) {
        return -1;
      } else if (a.md > b.md) {
        return 1;
      } else {
        return 0;
      }
    });
  }

  points = trajConverter.convertRawToXYZ(rawReadings);
}

// handles new data event
export function onDataUpdated() {
  /* var newData = widgetContext.data[0].data;
  var newReadings = [{md: 0, azi: 0, incl: 0}];
  for (let position = 1; position < newData.length; position++) {
    let prev = newReadings[position - 1];
    newReadings.push({ 
      md: prev.md + 100 * Math.random(), 
      azi: prev.azi + Math.PI - 2 * Math.PI * Math.random(), 
      incl: prev.incl + Math.PI - 2 * Math.PI * Math.random()
    });
  }
  updatePoints(newReadings, true);
  plot(); */
}
// use orbit controls
export function setOrbitControls() {
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enablePan = false;
}

// keep track of the mouse's 2D positioning on the screen
export function onMouseMove(event) {
  event.preventDefault();
  mouse.x = event.clientX;
  mouse.y = event.clientY;
}

// removes all objects from a scene that have name === objName
export function removeAllFromScene(objName) {
  if (objName) {
    for (let old = scene.getObjectByName(objName); old; old = scene.getObjectByName(objName)) {
      scene.remove(old);
    }
  }
}

// redraw the points
export function plot() {
  // remove old plot
  removeAllFromScene("well-plot");
  removeAllFromScene("well-plot-points");

  // get line geometry
  var geometry = new THREE.Geometry();
  points.forEach(point => {
    // add point to line
    geometry.vertices.push(new THREE.Vector3(point.x, point.y, point.z));

    // add point to scene
    var spriteMaterial = new THREE.SpriteMaterial({ color: plotPointColor});
    spriteMaterial.rotation = Math.PI / 4;
    spriteMaterial.transparent = true;
    spriteMaterial.opacity = 0.3;    
    var sprite = new THREE.Sprite(spriteMaterial);
    sprite.position.set(point.x, point.y, point.z);
    sprite.name = "well-plot-points";
    sprite.scale.set(30,30,30);
    scene.add(sprite);
  });

  // create line material
  var material = new MeshLine.MeshLineMaterial({
    color: new THREE.Color(plotColor), 
    sizeAttenuation: true,
    lineWidth: 18,
    resolution: resolution,
    transparent: true,
    opacity: .8
  });

  // create line
  var line = new MeshLine.MeshLine();
  line.setGeometry(geometry);

  // add the line to the scene
  var mesh = new THREE.Mesh(line.geometry, material);
  mesh.name="well-plot";
  scene.add(mesh);

  // update the grid now
  updateGrids();
}

// redraw the grids
export function updateGrids() {
  // remove old grids
  removeAllFromScene("grid");

  // get new grid sizes
  gridSize = 1000;
  if (rawReadings && rawReadings[0]) {
    gridSize = rawReadings[rawReadings.length-1].md;
    gridSize = 2*(Math.floor(gridSize / 100) + 1) * 100; // make an even multiple of 100

  }

  // get num divisions in grid
  var numDivisions = gridSize / 100;

  // make grids
  var gridXZ = new THREE.GridHelper(gridSize, numDivisions, gridCenterColor, gridColor);
  var gridXY = new THREE.GridHelper(gridSize, numDivisions, gridCenterColor, gridColor); // needs rotated and moved
  var gridZY = new THREE.GridHelper(gridSize, numDivisions, gridCenterColor, gridColor); // needs rotated and moved

  // position grids
  gridXZ.position.set(0,10,0); // move up just a tad to show the grid center coloring better
  gridXY.rotateX(Math.PI / 2);
  gridXY.position.set(0, -gridSize / 2, 0);
  gridZY.rotateZ(Math.PI / 2);
  gridZY.position.set(0, -gridSize / 2, 0);

  // name the grids
  gridXZ.name = "grid"
  gridXY.name = "grid"
  gridZY.name = "grid"

  // add grids to scene
  scene.add(gridXZ);
  scene.add(gridXY);
  scene.add(gridZY);

  // update axis labels
  angular.element('#trajectory-viewer-labelX').html(gridSize/2 + "east");
  angular.element('#trajectory-viewer-labelY').html(gridSize + "tvd");
  angular.element('#trajectory-viewer-labelZ').html(gridSize/2 + "north");
}

// moves the axis labels with the 3D world
export function updateLabelPositions() {
  // get window dimensions
  var widthHalf = widgetContainer.innerWidth() / 2; 
  var heightHalf = widgetContainer.innerHeight() / 2;

  // define 3D and 2D label positions
  var xLabelPosition = new THREE.Vector3(gridSize / 2, 0, 0);
  xLabelPosition.project(camera)
  xLabelPosition.x = (xLabelPosition.x * widthHalf) + widthHalf;
  xLabelPosition.y = -(xLabelPosition.y * heightHalf) + heightHalf;

  var yLabelPosition = new THREE.Vector3(0, -gridSize, 0);
  yLabelPosition.project(camera)
  yLabelPosition.x = (yLabelPosition.x * widthHalf) + widthHalf;
  yLabelPosition.y = -(yLabelPosition.y * heightHalf) + heightHalf;

  var zLabelPosition = new THREE.Vector3(0, 0, gridSize / 2);
  zLabelPosition.project(camera)
  zLabelPosition.x = (zLabelPosition.x * widthHalf) + widthHalf;
  zLabelPosition.y = -(zLabelPosition.y * heightHalf) + heightHalf;

  // update positions
  angular.element('#trajectory-viewer-labelX').css({
    'left': xLabelPosition.x + 'px',
    'top': xLabelPosition.y + 'px'
  });
  angular.element('#trajectory-viewer-labelY').css({
    'left': yLabelPosition.x + 'px',
    'top': yLabelPosition.y + 'px'
  });
  angular.element('#trajectory-viewer-labelZ').css({
    'left': zLabelPosition.x + 'px',
    'top': zLabelPosition.y + 'px'
  });
}

// resets camera position and target
export function resetCameraPosition(){
  camera.position.set(-1500, 3500, -1500);
  controls.target.set(0,0,0);
}

//=============================================================================
// initializes the application
//=============================================================================
export function init(ctx) {
  // get injected vars
  widgetContainer = ctx.$container;
  widgetContext = ctx;

  // construct widget
  constructWidget();

  // instantiate globals
  sceneContainer = angular.element('#trajectory-viewer-scene');
  scene = new THREE.Scene();
  scene.background = new THREE.Color( backgroundColor );
  camera = new THREE.PerspectiveCamera(75, widgetContainer.width() / widgetContainer.height(), 0.1, 100000);
  renderer = new THREE.WebGLRenderer();
  raycaster = new THREE.Raycaster();
  mouse = new THREE.Vector2();
  setOrbitControls();
  resolution = new THREE.Vector2(window.innerWidth, window.innerHeight); //eslint-disable-line

  // attach renderer
  renderer.setSize(widgetContainer.width(), widgetContainer.height());
  angular.element('#trajectory-viewer-scene').append(renderer.domElement);

  // position the camera
  resetCameraPosition();

  // setup object selection listeners
  sceneContainer.mousemove(onMouseMove); // monitor mouse positioning

  // process raw readings
  var input = '[{"azi":0.0,"incl":0.0,"md":0.0},{"azi":0.0,"incl":0.0,"md":43.5},{"azi":339.6,"incl":0.0,"md":152.0},{"azi":339.6,"incl":2.53,"md":291.0},{"azi":328.08,"incl":2.12,"md":300.0},{"azi":330.06,"incl":2.07,"md":310.0},{"azi":325.98,"incl":2.27,"md":320.0},{"azi":331.23,"incl":2.37,"md":330.0},{"azi":330.29,"incl":2.56,"md":340.0},{"azi":330.71,"incl":2.77,"md":350.0},{"azi":330.65,"incl":3.04,"md":360.0},{"azi":333.94,"incl":3.44,"md":364.29},{"azi":348.46,"incl":3.73,"md":378.06},{"azi":347.09,"incl":3.79,"md":381.94},{"azi":352.58,"incl":2.6,"md":393.56},{"azi":348.38,"incl":1.89,"md":403.59},{"azi":310.4302062988,"incl":1.741545558,"md":429.7363891602},{"azi":324.5098266602,"incl":1.2525943518,"md":453.5132446289},{"azi":321.5378723145,"incl":1.4943230152,"md":463.38671875},{"azi":95.2311248779,"incl":0.5493834615,"md":473.21},{"azi":110.2006759644,"incl":2.59,"md":489.41},{"azi":103.7239456177,"incl":2.84,"md":504.88},{"azi":54.6622467041,"incl":2.34,"md":522.1030273437},{"azi":24.712146759,"incl":4.11,"md":549.2150268555},{"azi":15.32,"incl":5.4,"md":576.56},{"azi":7.1936445236,"incl":6.87,"md":603.7582397461},{"azi":1.1838481426,"incl":7.24,"md":617.9675292969},{"azi":356.5803833008,"incl":7.5,"md":623.0197143555},{"azi":352.0757751465,"incl":7.74,"md":630.7108154297},{"azi":346.164855957,"incl":8.78,"md":645.1820678711},{"azi":346.74,"incl":10.43,"md":658.32},{"azi":346.7636413574,"incl":12.09,"md":672.1657714844},{"azi":345.4946594238,"incl":14.04,"md":685.2329101563},{"azi":340.84,"incl":16.12,"md":712.25},{"azi":338.545501709,"incl":17.97,"md":739.6813964844},{"azi":342.2864990234,"incl":20.43,"md":766.8629150391},{"azi":344.9617919922,"incl":23.52,"md":794.18},{"azi":341.3251647949,"incl":25.24,"md":821.35},{"azi":336.8590087891,"incl":26.38,"md":848.28},{"azi":339.3255615234,"incl":28.33,"md":875.57},{"azi":341.2254943848,"incl":31.42,"md":902.7392578125},{"azi":342.9117736816,"incl":33.16,"md":919.3889160156},{"azi":343.0886535645,"incl":34.5,"md":946.6367797852},{"azi":341.6498413086,"incl":33.73,"md":973.9765014648},{"azi":343.2836303711,"incl":32.97,"md":1001.5057983398},{"azi":341.6416625977,"incl":32.52,"md":1028.3920898438},{"azi":339.7196960449,"incl":33.56,"md":1055.7971191406},{"azi":339.711517334,"incl":34.45,"md":1082.958984375},{"azi":337.9357299805,"incl":35.46,"md":1137.6472167969},{"azi":338.0027770996,"incl":34.82,"md":1164.7596435547},{"azi":338.7321472168,"incl":34.77,"md":1192.1204833984},{"azi":339.9628295898,"incl":34.64,"md":1253.5303955078},{"azi":340.6786193848,"incl":33.15,"md":1300.4992675781},{"azi":340.430847168,"incl":32.37,"md":1345.7178955078},{"azi":340.9657897949,"incl":32.31,"md":1373.0893554688},{"azi":341.2445678711,"incl":33.03,"md":1400.5101318359},{"azi":341.2211608887,"incl":33.7,"md":1427.8498535156},{"azi":341.0071105957,"incl":35.35,"md":1454.7435302734},{"azi":340.4712524414,"incl":35.37,"md":1481.9958496094},{"azi":340.755065918,"incl":35.42,"md":1509.1533203125},{"azi":341.3230285645,"incl":35.2,"md":1530.3831787109},{"azi":343.1461791992,"incl":35.38,"md":1563.9252929688},{"azi":343.7471008301,"incl":34.55,"md":1590.5174560547},{"azi":343.7061767578,"incl":34.53,"md":1617.9241943359},{"azi":343.7421875,"incl":34.34,"md":1645.0900878906},{"azi":343.3536376953,"incl":34.25,"md":1672.4910888672},{"azi":343.5046081543,"incl":34.44,"md":1696.8723144531},{"azi":342.720489502,"incl":33.32,"md":1727.1253662109},{"azi":343.2664489746,"incl":32.82,"md":1754.3231201172},{"azi":342.536315918,"incl":31.57,"md":1781.6500244141},{"azi":342.0537414551,"incl":29.94,"md":1808.9030761719},{"azi":342.0120544434,"incl":29.93,"md":1835.8624267578},{"azi":341.8739318848,"incl":29.94,"md":1850.4187011719},{"azi":341.1149902344,"incl":29.69,"md":1905.1544189453},{"azi":340.8925476074,"incl":30.17,"md":1932.9757080078},{"azi":341.3746032715,"incl":30.36,"md":1960.1788330078},{"azi":341.4132385254,"incl":30.32,"md":1987.4606933594},{"azi":341.3897705078,"incl":30.22,"md":2014.7091064453},{"azi":341.2257385254,"incl":29.97,"md":2041.4984130859},{"azi":342.2607727051,"incl":29.86,"md":2068.8159179688},{"azi":340.0125427246,"incl":30.02,"md":2083.328125},{"azi":340.01,"incl":30.02,"md":2100.7}]';
  var newReading = angular.fromJson(input);
  updatePoints(newReading, true);

  // plot the well
  plot();
  updateGrids();

  // listen for window resizes and mouse clicks
  window.addEventListener('resize', onWindowResize, false); //eslint-disable-line
  widgetContainer.dblclick(onDoubleClick);

  // begin animating
  onWindowResize();
}
//=============================================================================

//=============================================================================
// loop that updates the viewer every frame
//=============================================================================
export function animate() {
  controls.update();
  updateLabelPositions();

  // animate
  requestAnimationFrame(animate);
  renderer.render(scene, camera);
}
//=============================================================================

//=============================================================================
// handle events
//=============================================================================
export function onWindowResize() {
  var newWidth = widgetContainer.width();
  var newHeight = widgetContainer.height();

  camera.aspect = newWidth / newHeight;
  camera.updateProjectionMatrix();

  renderer.setSize(newWidth, newHeight);

  // update resolution
  resolution.set(newWidth, newHeight);

  animate();
}

export function onDoubleClick() {
  // get proper mouse coordinates
  let canvasBounds = renderer.context.canvas.getBoundingClientRect();
  mouse.x = ((mouse.x - canvasBounds.left) / (canvasBounds.right - canvasBounds.left)) * 2 - 1;
  mouse.y = - ((mouse.y - canvasBounds.top) / (canvasBounds.bottom - canvasBounds.top)) * 2 + 1;

  // raycast
  raycaster.setFromCamera(mouse, camera);
  var intersects = raycaster.intersectObjects(scene.children, true);

  // handle any objects clicked on
  intersects.forEach(intersect => {
    let obj = intersect.object;
    if(obj.name === "well-plot-points") {
      let pos = obj.position;
      controls.target.set(pos.x, pos.y, pos.z);
    }
  });
}
//=============================================================================