console.log("media");
var mindex = {};
var gData = {};

function fetchMedia(p) {
    startFetch(p);
}

function startFetch(playlist) {
    if(!mindex[playlist]){
        mindex[playlist] = 0;
    }

    if(!gData[playlist]){
        gData[playlist] = {};
    }
    $.get( host + "/media/list/" + playlist + "?index=" + mindex[playlist], function( data ) {
          //console.log("media type " + playlist + " fecthed index " + mindex[playlist] + " response " + decodeURIComponent(JSON.stringify(data)));

        for(var i = 0; i < data.d.length; i++){
            var ob = data.d[i];
            gData[playlist][ob.P] = ob;

            placeThumbnail(ob, playlist);
        }

        if (data.i > 0){
            mindex[playlist] = data.i;
            setTimeout(function () {
                startFetch(playlist);
            }, 1000);
        }
    });
}



function  getFileName(obj) {
    if(obj.F === 'S' && obj.T){
        return decodeString(obj.T);
    }
    var x = obj.P;
    var names = decodeURIComponent(x);
    names = names.split("/");
    var n = names[names.length -1 ];
    var x = n.lastIndexOf('.');
    n = n.substr(0, x);
    return decodeString(n);
}

function decodeString(n) {
    return decodeURIComponent(n.replace(/\+/g, ' ').replace(/\%27/g, ' '));
}

function placeThumbnail(obj, playlist) {
    var id = (obj.P + '').replace(/\./g, '_')
        .replace(/%/g, '-')
        .replace(/\+/g, 'cP');

    if (document.getElementById(id)){
        return;
    }
    var div = document.createElement('div');
    div.setAttribute('class', 'mb');
    div.setAttribute("onclick", "showOptions('" + obj.P + "')");
    div.id = id;
    var name = getFileName(obj);
    var innerHtml = '';
    //= '<span class="media-title">'+name+'</span><button class="media-btn" onclick="showOptions(\'' + obj.P + '\')">';
    if(obj.Q){
        innerHtml += '<img src="' + host +  '/thumbnail?id=' + obj.Q +'" class="thmb"/>'
    } else {
        //TODO sensitive icon
        innerHtml += '<img src="https://img.icons8.com/dusk/64/000000/musical.png" class="thmb"/>';
    }

    innerHtml += '<div class="mt">';
    if((obj.B && obj.T) && obj.B != '' && obj.T != ''){
        innerHtml += '<div id="p1">' + decodeString(obj.B)  +  '</div>';
        innerHtml += '<div style="font-style: italic">' + decodeString(obj.T) + '</div>';
    } else {
        innerHtml += '<div id="p3">' + name  + '</div>';
    }
    innerHtml += '</div>';
    div.innerHTML = innerHtml;


    $('#media-content-' + playlist).append(div);

}

function showOptions(path) {
    var name = getFileName(path);
    $('#media-title').text(name);
    $('#play-btn').attr('onclick', 'openFile(\''+path+'\')');
    $('#play-btn').show();
    $('#stop-btn').attr('onclick', 'closeFile(\''+path+'\')');
    $('#stop-btn').show();
    $('#pause-btn').attr('onclick', 'pauseFile(\''+path+'\')')
    $('#pause-btn').show();
    $('#download-btn').attr('href', host + '/download?file='+path);
    $('#download-btn').show();
    $('#modal-area').show();
}

function pauseFile(x) {
    $.get( host + "/media/pause?path=" + x, function( data ) {
        console.log("PAUSE PLAY " + data);
    });
}

function openFile(x) {
    $.get( host + "/files/open?path=" + x, function( data ) {
        console.log("START PLAY ", data);
        updateUi(data);
    });
}

function closeFile(x) {
    $.get( host + "/files/close?path=" + x, function( data ) {
        console.log("after close ", data);
        updateUi(data);
    });
}


