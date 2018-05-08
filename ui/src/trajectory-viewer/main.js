
//=============================================================================
// global vars
//=============================================================================
var scene;
var camera;
var renderer;
var controls;
//=============================================================================

//=============================================================================
// initializes the viewer
//=============================================================================
function init() {
  // instantiate globals
  scene = new THREE.Scene();
  scene.background = new THREE.Color( 0xf0f0f0 );
  camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 100000);
  renderer = new THREE.WebGLRenderer();
  controls = new THREE.TrackballControls(camera);

  // attach renderer
  renderer.setSize(window.innerWidth, window.innerHeight);
  document.body.appendChild(renderer.domElement);

  // position the camera
  camera.position.z = 200;
  camera.position.y = 200;
  camera.position.x = 200;

  // configure controls
  controls.dynamicDampingFactor = 0.1

  // create axis
  var axesHelper = new THREE.AxesHelper(500);
  scene.add(axesHelper);

  // create well hole
  var wellGeometry = new THREE.RingBufferGeometry(10, 50, 32);
  var wellMaterial = new THREE.MeshBasicMaterial({ color: 0x000000, side: THREE.DoubleSide });
  var well = new THREE.Mesh(wellGeometry, wellMaterial);
  well.rotation.x = Math.PI/2
  scene.add(well);

  // listen for window resizes
  window.addEventListener('resize', onWindowResize, false);

  // begin animating
  animate();
}
//=============================================================================

//=============================================================================
// loop that updates the viewer every frame
//=============================================================================
function animate() {
  controls.update();
  requestAnimationFrame(animate);
  renderer.render(scene, camera);
}
//=============================================================================

//=============================================================================
// handle resizes
//=============================================================================
function onWindowResize() {
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
  controls.handleResize();
  animate();
}
//=============================================================================

//=============================================================================
// main
//=============================================================================
init();
//=============================================================================