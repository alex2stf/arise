var host = '{{host}}',
    cameraIndex = 0,
    rotations =  ['90', '180', '270', '0'],
    currentRotationIndex = 0,
    cameraEnabled = false,
    cameraMode = 'mjpeg'
    lightMode = 0;

var stopIcn = '<img src="https://img.icons8.com/flat_round/64/000000/stop.png"/>';
var playIcn = '<img src="https://img.icons8.com/flat_round/64/000000/youtube-play.png"/>';


function calculateDeviceParams() {
    return 'camIndex=' + cameraIndex + '&lightMode=' + lightMode+ '&camEnabled=' + cameraEnabled;
}



function cameraUrl() {
    return host + '/device/live/'+cameraMode+'?' + calculateDeviceParams();
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

function takeSnapshot(){
    stopStream();
    document.getElementById('cam-img').src = '/device/live/jpeg'
}

function fitPortrait() {
//    var img = document.getElementById('cam-preview');
//    var mSW = minScreenWidth();
//    img.style.width = mSW;
//    img.style.height = 'auto';

//    $('#cam-img').height("100%");
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
    document.getElementById('cam-img').style['transform-origin'] = 'center center';
    rotateElement('cam-img', rotation);
    // document.getElementById('cam-img').setAttribute('class', rotation);

    currentRotationIndex++;
}


function toggleStreamControls(){
    var controlsVisible = AppSettings.get('controlsVisible', true) == 'true';

    if(controlsVisible){
        $('#stream-controls').hide();
        AppSettings.set('controlsVisible', false);
        rotateElement('btn-toggle', 0);
    }
    else {
        $('#stream-controls').show();
        AppSettings.set('controlsVisible', true);
        rotateElement('btn-toggle', 180);
    }
}
toggleStreamControls();


function changeCameraMode() {
    cameraMode = $('#camMode').val();
    refreshCamera();
}


function match_width() {
    $('#cam-img').width($(window).width());
    $('#cam-img').height('auto');
    $('#cam-img').css('margin', '0');
}


function match_height() {
    $('#cam-img').height($(window).height() - 100);
    $('#cam-img').width('auto');
    $('#cam-img').css('margin', '0');
}

function hideControls() {
    document.getElementById('cam-img').src = cameraUrl();
    document.getElementById('audio-src').src = host + '/device/live/audio.wav?' + calculateDeviceParams();
    document.getElementById('audio-src').style.display = 'none';
    clearTimeout(rtimeout);
}

function showControls() {
    document.getElementById('cam-img').src = cameraUrl();
    document.getElementById('audio-src').src = host + '/device/live/audio.wav?' + calculateDeviceParams();
    document.getElementById('audio-src').style.display = 'inline-block';
    if(cameraMode == 'jpeg'){
        startRefreshTimer();
    } else {
        clearTimeout(rtimeout);
    }
}

var rtimeout = -1;
function startRefreshTimer() {
    var d = new Date();
    var x = d.getYear() + '' + d.getDate() + Math.round((Math.random() * 1000)) + '' + d.getHours() + d.getMilliseconds() + d.getMinutes() + d.getSeconds();
    var src = cameraUrl() + '&id=' + x;
    // console.log(src);
    $('#cam-img').attr('src', src );
    if(cameraMode === 'jpeg'){
        rtimeout = setTimeout(function(){
            startRefreshTimer();
        }, 200)
    }
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
