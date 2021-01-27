var host = '{{host}}',
    cameraIndex = 0,
    rotations =  ['rotate90', 'rotate180', 'rotate270', 'rotate0'],
    currentRotationIndex = 0,
    cameraEnabled = false,
    lightMode = 0;

var stopIcn = '<img src="https://img.icons8.com/flat_round/64/000000/stop.png"/>';
var playIcn = '<img src="https://img.icons8.com/flat_round/64/000000/youtube-play.png"/>';


function calculateDeviceParams() {
    return 'camIndex=' + cameraIndex + '&lightMode=' + lightMode+ '&camEnabled=' + cameraEnabled;
}

function mjpegUrl() {
    return host + '/device/live/mjpeg?' + calculateDeviceParams();
}

function cameraUrl() {
    return mjpegUrl()
}

function minScreenHeight() {
    return Math.min(document.documentElement.clientHeight, window.innerHeight, screen.height);
}


function minScreenWidth() {
    return Math.min(document.documentElement.clientWidth, window.innerWidth || screen.width);
}

function maxScreenWidth() {
    return Math.max(document.documentElement.clientWidth, window.innerWidth || screen.width);
}

function startStream() {
    cameraEnabled = true;
    $('#cam_ctrl').html(stopIcn);
    refreshCamera();
}


function stopStream() {
    cameraEnabled = false;
    $('#cam_ctrl').html(playIcn);
    refreshCamera();
}

function toggleStream() {
    if (cameraEnabled){

        stopStream();
    }
    else {
        startStream();
    }
}

function fitPortrait() {
    var img = document.getElementById('cam-preview');
    var mSW = minScreenWidth();
    img.style.width = mSW;
    img.style.height = 'auto';

    $('#cam-img').height("100%");
    // img.style.top = ((img.offsetWidth - img.offsetHeight) / 2);
    // var diff = (img.offsetHeight - img.offsetWidth) / 2;
    // img.style.left = (diff + ((mSW - img.offsetHeight) / 2) );
}

var sidebarSize = 40;
var lastPosition = 'landscape';

function rotateCamPreview() {
    if (currentRotationIndex > rotations.length -1){
        currentRotationIndex = 0;
    }
    var rotation = rotations[currentRotationIndex];
    document.getElementById('cam-img').setAttribute('class', rotation);
    currentRotationIndex++;
}


function match_width() {
    var img = document.getElementById('cam-img');
    img.style.width = minScreenWidth() - sidebarSize - 4;
    img.style.height = 'auto';
}

function match_height() {
    var img = document.getElementById('cam-img');
    // img.style.width = minScreenWidth() - sidebarSize - 4;
    img.style.width = 'auto';
    img.style.height = minScreenHeight();
}

function hideControls() {
    document.getElementById('cam-img').src = "#";
    document.getElementById('cam-img').style.display = "none";
    document.getElementById('audio-src').src = "#";
    document.getElementById('audio-src').style.display = 'none';
}

function showControls() {
    document.getElementById('cam-img').src = cameraUrl();
    document.getElementById('cam-img').style.display = "block";
    document.getElementById('audio-src').src = host + '/device/live/audio.wav?' + calculateDeviceParams();
    document.getElementById('audio-src').style.display = 'inline-block';
}

function refreshCamera() {

    if (!cameraEnabled){
        hideControls();
        setTimeout(function () {
                if (!cameraEnabled){
                   hideControls();
                }
        }, 2000);
    } else  {
        showControls();
    }
}

function changeLightMode() {
    lightMode = +$('#lightMode').val() || 0;
    refreshCamera();
}

function changeCameraIndex(){
    cameraIndex = +$('#cameraIndex').val() || 0;
    refreshCamera();
}
