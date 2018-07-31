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
 import SimplexNoise from 'simplex-noise/simplex-noise';
/*@ngInject*/
var dataGenerator = function() {
	let dataGen = {};
	dataGen.time = 0;
	// let d = new Date();
	// let time = d.getTime();

	dataGen.num = 300;

	let noise = new SimplexNoise();
	let seed = 10 + 100 * Math.random();
	dataGen.data = [seed];
	let deltas = [seed];

	dataGen.latestData = [seed];
	let latestDeltas = [seed];

	dataGen.tick = function() {
	// d.getTime();
		dataGen.time++;
		dataGen.data[dataGen.time] = dataGen.data[dataGen.time - 1] + noise.noise2D(seed, dataGen.time/60);
		dataGen.data[dataGen.time] = Math.max(dataGen.data[dataGen.time], 0);

		deltas[dataGen.time] = dataGen.data[dataGen.time] - dataGen.data[dataGen.time - 1];

		if (dataGen.time <= dataGen.num) {
			dataGen.latestData = dataGen.data.slice(-dataGen.num);
			dataGen.latestDeltas = deltas.slice(-dataGen.num);
		}
		else {
			dataGen.latestData.shift();
			latestDeltas.shift();
			dataGen.latestData.push(dataGen.data[dataGen.time]);
			latestDeltas.push(deltas[dataGen.time]);
		}
	}
	return dataGen;
}
export {dataGenerator};