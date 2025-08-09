const canvas = document.getElementById('drawingCanvas');
const ctx = canvas.getContext('2d');
const startBtn = document.getElementById('startBtn');
const stopBtn = document.getElementById('stopBtn');
const clearBtn = document.getElementById('clearBtn');

// Set canvas size to bigger 1000x1000
canvas.width = 1000;
canvas.height = 1000;

// Core variables
const startPoint = { x: canvas.width / 2, y: canvas.height / 2 };
let currentPosition = { x: startPoint.x, y: startPoint.y };
let path = [{ x: currentPosition.x, y: currentPosition.y }];

// State & sensors
let isTracking = false;
let listenersReady = false;
let isWalking = false;
let lastAcceleration = { x: 0, y: 0, z: 0 };
const stepThreshold = 2.5;
const stepDistance = 15;

// Direction calibration
let currentHeading = 0;
let initialHeading = 0;
let isCalibrated = false;

// New pen size smaller - radius 4
const penRadius = 4;

function draw() {
    ctx.fillStyle = 'white';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.strokeStyle = 'red';
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.moveTo(path[0].x, path[0].y);
    for (let i = 1; i < path.length; i++) {
        ctx.lineTo(path[i].x, path[i].y);
    }
    ctx.stroke();

    // Start point circle smaller
    ctx.fillStyle = 'black';
    ctx.beginPath();
    ctx.arc(startPoint.x, startPoint.y, penRadius, 0, 2 * Math.PI);
    ctx.fill();

    // Current position circle smaller
    ctx.fillStyle = 'red';
    ctx.beginPath();
    ctx.arc(currentPosition.x, currentPosition.y, penRadius, 0, 2 * Math.PI);
    ctx.fill();
}

function setupListeners() {
    const orient_promise = (typeof DeviceOrientationEvent.requestPermission === 'function') ?
        DeviceOrientationEvent.requestPermission() : Promise.resolve('granted');

    orient_promise.then(permissionState => {
        if (permissionState === 'granted') {
            window.addEventListener('deviceorientation', (event) => {
                if (!isTracking || !event.alpha) return;

                if (!isCalibrated) {
                    initialHeading = event.alpha;
                    isCalibrated = true;
                }
                currentHeading = event.alpha;
            });
        }
    }).catch(console.error);

    const motion_promise = (typeof DeviceMotionEvent.requestPermission === 'function') ?
        DeviceMotionEvent.requestPermission() : Promise.resolve('granted');

    motion_promise.then(permissionState => {
        if (permissionState === 'granted') {
            window.addEventListener('devicemotion', (event) => {
                if (!isTracking) return;

                const accel = event.acceleration;
                if (!accel || accel.x === null) return;

                let delta = Math.abs(accel.x - lastAcceleration.x) +
                            Math.abs(accel.y - lastAcceleration.y) +
                            Math.abs(accel.z - lastAcceleration.z);

                if (delta > stepThreshold && !isWalking) {
                    isWalking = true;
                    takeStep();
                    setTimeout(() => { isWalking = false; }, 500);
                }
                lastAcceleration = { x: accel.x, y: accel.y, z: accel.z };
            });
        }
    }).catch(console.error);

    listenersReady = true;
}

function takeStep() {
    let relativeHeading = currentHeading - initialHeading;
    const angle = relativeHeading * (Math.PI / 180);

    const newX = currentPosition.x - stepDistance * Math.sin(angle);
    const newY = currentPosition.y - stepDistance * Math.cos(angle);

    currentPosition = { x: newX, y: newY };
    path.push(currentPosition);

    draw();
}

function clearCanvas() {
    isTracking = false;
    isCalibrated = false;
    currentPosition = { x: startPoint.x, y: startPoint.y };
    path = [{ x: currentPosition.x, y: currentPosition.y }];
    draw();
}

startBtn.addEventListener('click', () => {
    if (!listenersReady) {
        setupListeners();
    }
    if (!isTracking) {
        isCalibrated = false;
    }
    isTracking = true;
});

stopBtn.addEventListener('click', () => {
    isTracking = false;
});

clearBtn.addEventListener('click', clearCanvas);

draw();
