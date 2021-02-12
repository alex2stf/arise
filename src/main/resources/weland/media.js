console.log("media");
var mindex = {};

function fetchMedia(p) {
    startFetch(p);
}

function startFetch(playlist) {
    if(!mindex[playlist]){
        mindex[playlist] = 0;
    }
    $.get( host + "/media/list/" + playlist + "?index=" + mindex[playlist], function( data ) {
         console.log(data);

        for(var i = 0; i < data.d.length; i++){
            placeThumbnail(data.d[i], playlist);

        }

        if (data.i > 0){
            mindex[playlist] = data.i;
            setTimeout(function () {
                startFetch(playlist);
            }, 1000);
        }
    });
}



function  getFileName(x) {
    var names =   decodeURIComponent(x);
    names = names.split("/");
   var n = names[names.length -1 ];
   var x = n.lastIndexOf('.');
   n = n.substr(0, x);
   return n.replace(/\+/g, ' ');
}

function placeThumbnail(obj, playlist) {
    // console.log(obj);
    var id = (obj.P + '').replace(/\./g, '_')
        .replace(/%/g, '-')
        .replace(/\+/g, 'cP');

    if (document.getElementById(id)){
        return;
    }
    var div = document.createElement('div');
    div.setAttribute('class', 'media-icon');
    div.id = id;
    var name = getFileName(obj.P);
    var innerHtml = '<span class="media-title">'+name+'</span><button class="media-btn" onclick="showOptions(\'' + obj.P + '\')">';
    if(obj.Q){
        innerHtml += '<img src="' + host +  '/thumbnail?id=' + obj.Q +'" class="thumbnail"/>'
    } else {
        //TODO sensitive icon
        innerHtml += '<img src="https://img.icons8.com/dusk/64/000000/musical.png" class="thumbnail"/>';
    }
    innerHtml +='</button>';

    // innerHtml = btnHtml.replace(/_OBJP/g, obj.P)
    //     .replace('_NAME', name)
    //     .replace('_ID', id);

    div.innerHTML = innerHtml;
   // console.log(decodeURIComponent(obj.P));


    $('#media-list-' + playlist).append(div);


}

function showOptions(path) {
    var name = getFileName(path);

    // console.log(name);
    $('#media-title').text(name);
    $('#play-btn').attr('onclick', 'openFile(\''+path+'\')')
    $('#stop-btn').attr('onclick', 'closeFile(\''+path+'\')')
    $('#pause-btn').attr('onclick', 'pauseFile(\''+path+'\')')
    $('#download-btn').attr('href', host + '/download?file='+path);
    $('#modal-area').show();
}

function pauseFile(x) {
    $.get( host + "/media/pause?path=" + x, function( data ) {
        console.log("PAUSE PLAY " + data);
    });
}

function openFile(x) {
    $.get( host + "/files/open?path=" + x, function( data ) {
        console.log("START PLAY " + data);
    });
}

function closeFile(x) {
    $.get( host + "/files/close?path=" + x, function( data ) {
        console.log(data);
    });
}