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
const RAD_CONVERSION_FACTOR = Math.PI / 180.0 // multiply this with an angle in degrees to get radians

// converts an array of trajectory objects into an array of XYZ objects
function convertTrajectoryArray(trajectoryArray) {
  // catch empty input
  if(trajectoryArray.length === 0) {
    return [];
  }

  // holds ongoing results
  var results = [{x: 0, y: 0, z: 0}]; // all start at 0,0,0

  // catch only 1 point
  if (trajectoryArray.length === 1) {
    return results;
  }
 
  // process the trajectory array
  for (let position = 1; position < trajectoryArray.length; position++) {
    // get current and previous points
    let curr = trajectoryArray[position];
    let prev = trajectoryArray[position - 1];
    
    // extract values
    let mdDelta = curr.md - prev.md; // measured depth difference between readings
    let incl0 = prev.incl * RAD_CONVERSION_FACTOR; // previous inclination in radians
    let incl1 = curr.incl * RAD_CONVERSION_FACTOR; // current inclination in radians
    let azi0 = prev.azi * RAD_CONVERSION_FACTOR; // previous azimuth in radians
    let azi1 = curr.azi * RAD_CONVERSION_FACTOR; // current azimuth in radians
    
    // use minimum curvature method from http://www.drillingformulas.com/minimum-curvature-method/
    let beta = Math.acos(Math.cos(incl1 - incl0) - (Math.sin(incl0)*Math.sin(incl1) * (1 - Math.cos(azi1 - azi0))));
    let ratioFactor = 2.0 / beta * Math.tan(beta / 2.0);
    let northDelta = mdDelta / 2.0 * (Math.sin(incl0) * Math.cos(azi0) + Math.sin(incl1) * Math.cos(azi1)) * ratioFactor;
    let eastDelta = mdDelta / 2.0 * (Math.sin(incl0) * Math.sin(azi0) + Math.sin(incl1) * Math.sin(azi1)) * ratioFactor;
    let depthDelta = mdDelta/2.0 * (Math.cos(incl0) + Math.cos(incl1)) * ratioFactor;

    // since beta can be 0 when the incl and azi are zero, correct any NaN's here
    if (beta === 0) {
      northDelta = 0;
      eastDelta = 0;
      depthDelta = mdDelta;
    }

    // add new XYZ to results
    let lastPoint = results[results.length - 1];
    results.push({x: lastPoint.x + eastDelta, y: lastPoint.y + northDelta, z: lastPoint.z + depthDelta});
  }

  // return the results!!
  return results;
}